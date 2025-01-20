package com.lasagnerd.odin.debugger.runner

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.TextConsoleBuilder
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.AsyncProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.RunContentBuilder
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.util.ProgramParametersUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.lasagnerd.odin.OdinBundle
import com.lasagnerd.odin.debugger.OdinDebuggerToolchainService
import com.lasagnerd.odin.runConfiguration.OdinBaseCommandLineState
import com.lasagnerd.odin.runConfiguration.OdinBaseRunConfiguration
import com.lasagnerd.odin.runConfiguration.build.OdinBuildRunConfiguration
import com.lasagnerd.odin.runConfiguration.test.OdinTestRunCommandLineState
import com.lasagnerd.odin.runConfiguration.test.OdinTestRunConfiguration
import com.lasagnerd.odin.settings.projectSettings.OdinProjectConfigurable
import com.lasagnerd.odin.utils.CLIUtils
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.resolvedPromise
import java.nio.file.Path


class OdinNativeDebugProgramRunner : AsyncProgramRunner<RunnerSettings>() {
    override fun getRunnerId(): String {
        return "OdinNativeDebugProgramRunner"
    }


    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        if (profile is OdinBuildRunConfiguration) {
            if (!profile.options.isRunAfterBuild) {
                return false;
            }
        }
        return DefaultDebugExecutor.EXECUTOR_ID == executorId
                && profile is OdinBaseRunConfiguration<*>
    }

    override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
        // The state is passed through OdinRunCommandLineState which is provided OdinRunConfiguration
        val runProfile = environment.runProfile
        if (state !is OdinBaseCommandLineState || runProfile !is OdinBaseRunConfiguration<*>) {
            return resolvedPromise()
        }

        val expandedWorkingDirectory = expandPath(environment.project, runProfile.options.workingDirectory) ?: return resolvedPromise()

        val debuggerDriverConfiguration = OdinDebuggerToolchainService.getInstance(environment.project).debuggerDriverConfiguration
        if (debuggerDriverConfiguration == null) {
            showDebuggerNotSetNotification(environment)
            return resolvedPromise()
        }

        FileDocumentManager.getInstance().saveAllDocuments()

        // This will be set from the EDT thread
        val runContentDescriptorPromise = AsyncPromise<RunContentDescriptor?>()
        var outputPath = runProfile.options.outputPath
        val nioPath = Path.of(outputPath)

        if (!nioPath.isAbsolute) {
            outputPath = Path.of(expandedWorkingDirectory).resolve(nioPath).toString()
        }

        val expandedOutputPath = expandPath(environment.project, outputPath)

        var runExecutable = GeneralCommandLine(expandedOutputPath)
        val programArguments = runProfile.options.programArguments
        val argsList = CLIUtils.translateCommandline(programArguments)
        runExecutable.addParameters(argsList.toList())
        runExecutable.setWorkDirectory(expandedWorkingDirectory)
        val debugCompiledExeRunParameters = OdinDebugRunParameters(runExecutable, debuggerDriverConfiguration)

        // This is the console to be shared with the debug process

        val console = if (state is OdinTestRunCommandLineState && runProfile is OdinTestRunConfiguration) {
            state.createConsole(runProfile)
        } else {
            state.consoleBuilder.console
        }
        // Switch to BGT for long-running and blocking calls
        AppExecutorUtil.getAppExecutorService().execute {
            // Create the build command line with debug parameters: "odin build <path> -debug ..."
            val buildProcessHandler = ProcessHandlerFactory.getInstance().createProcessHandler(
                state.createCommandLine(true)
            )

            // Attach the process to the console
            console.attachToProcess(buildProcessHandler)

            // Listen to the build process and check if the build was successful
            val buildProcessListener = BuildProcessListener(console)
            buildProcessHandler.addProcessListener(buildProcessListener)
            buildProcessHandler.startNotify()
            buildProcessHandler.waitFor()

            // If build fails do not start debugging, but create a standard run tab instead
            if (buildProcessListener.buildFailed) {
                val executionResult = DefaultExecutionResult(console, buildProcessHandler)

                // Switch back to EDT to create standard run tab
                ApplicationManager.getApplication().invokeLater {
                    val runContentBuilder = RunContentBuilder(executionResult, environment)
                    val runContentDescriptor = runContentBuilder.showRunContent(null)
                    runContentDescriptorPromise.setResult(runContentDescriptor)
                }
            } else {
                // Switch back to EDT to start debug process
                ApplicationManager.getApplication().invokeLater {
                    val debuggerManager = XDebuggerManager.getInstance(environment.project)
                    val xDebugSession = debuggerManager.startSession(environment, object : XDebugProcessStarter() {
                        @Throws(ExecutionException::class)
                        override fun start(session: XDebugSession): XDebugProcess {
                            val project = session.project
                            // Since the debug process only accepts a console builder, give it a console builder that will return the
                            // console that we used for the build process.
                            val textConsoleBuilder: TextConsoleBuilder = SharedConsoleBuilder(console)
                            val debugProcess = OdinLocalDebugProcess(debugCompiledExeRunParameters, session, textConsoleBuilder)
                            ProcessTerminatedListener.attach(debugProcess.processHandler, project)
                            debugProcess.start()
                            return debugProcess
                        }
                    })
                    runContentDescriptorPromise.setResult(xDebugSession.runContentDescriptor)
                }
            }
        }

        return runContentDescriptorPromise
    }

    private fun showDebuggerNotSetNotification(environment: ExecutionEnvironment) {
        val notification = Notification("Odin Notifications", OdinBundle.message("odin.no-debugger-error"), NotificationType.ERROR)
            .addAction(object : NotificationAction("Open Odin settings") {
                override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(
                        environment.project,
                        OdinProjectConfigurable::class.java,
                        null
                    )
                }

            })

        Notifications.Bus.notify(notification, environment.project)
    }

    private fun expandPath(project: Project, outputPath: String?): String? =
        ProgramParametersUtil.expandPathAndMacros(
            outputPath,
            null,
            project
        )


    private class BuildProcessListener(private val console: ConsoleView) : ProcessListener {
        var buildFailed = false
        override fun processTerminated(event: ProcessEvent) {
            if (event.exitCode != 0) {
                console.print(
                    "Process finished with exit code " + event.exitCode,
                    ConsoleViewContentType.NORMAL_OUTPUT
                )
                buildFailed = true
            } else {
                buildFailed = false
                console.print("Build Successful. Starting debug session. \n", ConsoleViewContentType.NORMAL_OUTPUT)
            }
        }
    }

    public class SharedConsoleBuilder(private val console: ConsoleView) : TextConsoleBuilder() {
        override fun getConsole(): ConsoleView {
            return this.console
        }

        override fun addFilter(filter: Filter) {
        }

        override fun setViewer(isViewer: Boolean) {
        }
    }
}

