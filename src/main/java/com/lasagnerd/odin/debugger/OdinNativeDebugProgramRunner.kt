package com.lasagnerd.odin.debugger

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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.lasagnerd.odin.runConfiguration.OdinRunCommandLineState
import com.lasagnerd.odin.runConfiguration.OdinRunConfiguration
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.resolvedPromise


class OdinNativeDebugProgramRunner : AsyncProgramRunner<RunnerSettings>() {
    override fun getRunnerId(): String {
        return "OdinNativeDebugProgramRunner"
    }


    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return DefaultDebugExecutor.EXECUTOR_ID == executorId && profile is OdinRunConfiguration
    }

    override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
        // The state is passed through OdinRunCommandLineState which is provided OdinRunConfiguration
        val runProfile = environment.runProfile
        if (state !is OdinRunCommandLineState || runProfile !is OdinRunConfiguration) {
            return resolvedPromise()
        }
        FileDocumentManager.getInstance().saveAllDocuments()


        val promise = AsyncPromise<RunContentDescriptor?>()

        // Switch to BGT for long-running and blocking calls
        AppExecutorUtil.getAppExecutorService().execute {
            val runExecutable = GeneralCommandLine(runProfile.outputPath)
            runExecutable.setWorkDirectory(runProfile.options.workingDirectory)
            val runParameters = OdinDebugRunParameters(runProfile, runExecutable)
            val debuggerManager = XDebuggerManager.getInstance(environment.project)
            val console = state.consoleBuilder.console
            val textConsoleBuilder: TextConsoleBuilder = object : TextConsoleBuilder() {
                override fun getConsole(): ConsoleView {
                    return console
                }

                override fun addFilter(filter: Filter) {
                }

                override fun setViewer(isViewer: Boolean) {
                }
            }
            val processHandler = ProcessHandlerFactory.getInstance().createProcessHandler(
                state.createCommandLine(true)
            )
            console.attachToProcess(processHandler)

            val buildProcessListener = BuildProcessListener(console)
            processHandler.addProcessListener(buildProcessListener)
            processHandler.startNotify()
            processHandler.waitFor()
            // If build fails return RunContentDescriptor here
            if (buildProcessListener.buildFailed) {
                val executionResult = DefaultExecutionResult(console, processHandler)
                ApplicationManager.getApplication().invokeLater {
                    val runContentBuilder = RunContentBuilder(executionResult, environment)
                    val runContentDescriptor = runContentBuilder.showRunContent(null)
                    promise.setResult(runContentDescriptor);
                }
            } else {
                // Switch back to EDT to start debug process
                ApplicationManager.getApplication().invokeLater {
                    val xDebugSession = debuggerManager.startSession(environment, object : XDebugProcessStarter() {
                        @Throws(ExecutionException::class)
                        override fun start(session: XDebugSession): XDebugProcess {
                            val project = session.project
                            val debugProcess = OdinLocalDebugProcess(runParameters, session, textConsoleBuilder)
                            ProcessTerminatedListener.attach(debugProcess.processHandler, project)
                            debugProcess.start()
                            return debugProcess
                        }
                    })
                    promise.setResult(xDebugSession.runContentDescriptor)
                }
            }
        }


        return promise
    }


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
}