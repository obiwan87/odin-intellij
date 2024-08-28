package com.lasagnerd.odin.debugger

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
import com.jetbrains.cidr.execution.CidrRunner
import com.lasagnerd.odin.runConfiguration.OdinRunCommandLineState
import com.lasagnerd.odin.runConfiguration.OdinRunConfiguration
import com.lasagnerd.odin.utils.ApplicationUtil
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.asCompletableFuture
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

            val failedToBuild = BuildProcessListener(console)
            processHandler.addProcessListener(failedToBuild)
            processHandler.startNotify()
            processHandler.waitFor() // This causes EDT-Error -> maybe use AsyncRunner with Kotlin which is made for return a Promise to a content descriptor

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


        return promise
    }


    private class BuildProcessListener(private val console: ConsoleView) : ProcessListener {
        override fun processTerminated(event: ProcessEvent) {
            if (event.exitCode != 0) {
                console.print(
                    "Build failed. Starting debug session with previously built executable if any exists. \n",
                    ConsoleViewContentType.ERROR_OUTPUT
                )
            } else {
                console.print("Build Successful. Starting debug session. \n", ConsoleViewContentType.ERROR_OUTPUT)
            }
        }
    }
}