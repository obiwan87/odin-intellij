package com.lasagnerd.odin.debugger.runner

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.AsyncProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ssh.ConnectionBuilder
import com.intellij.ssh.config.unified.SshConfigManager
import com.intellij.ssh.connectionBuilder
import com.intellij.ssh.process.CapturingSshProcessHandler
import com.intellij.ssh.process.SshExecProcess
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.jetbrains.cidr.execution.debugger.remote.CidrRemoteDebugParameters
import com.jetbrains.cidr.execution.debugger.remote.CidrRemotePathMapping
import com.lasagnerd.odin.debugger.driverConfigurations.LLDBDAPDriverConfiguration
import com.lasagnerd.odin.debugger.runConfiguration.OdinRemoteDebugRunConfiguration
import com.lasagnerd.odin.runConfiguration.OdinRunConfigurationUtils
import org.jetbrains.annotations.NonNls
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.resolvedPromise
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Path

class OdinRemoteDebugProgramRunner : AsyncProgramRunner<RunnerSettings>() {
    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return DefaultDebugExecutor.EXECUTOR_ID == executorId
                && profile is OdinRemoteDebugRunConfiguration
    }

    override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
        val runProfile = environment.runProfile
        if (runProfile !is OdinRemoteDebugRunConfiguration) {
            return resolvedPromise()
        }
        val debuggerPath = runProfile.options.debuggerPath ?: return resolvedPromise()
        val sshConfig =
            SshConfigManager.getInstance(runProfile.project).configs.find { it.id == runProfile.options.sshConfigId } ?: return resolvedPromise()

        val driverConfiguration =
            LLDBDAPDriverConfiguration(Path.of(debuggerPath))

        val cidrRemoteDebugParameters = CidrRemoteDebugParameters()
        if (!runProfile.options.isBuildOnTarget) {
            cidrRemoteDebugParameters.symbolFile = runProfile.options.localExecutablePath
        } else {
            cidrRemoteDebugParameters.symbolFile = runProfile.options.targetExecutableDownloadPath!!
        }
        cidrRemoteDebugParameters.sysroot = runProfile.options.localPackageDirectoryPath
        cidrRemoteDebugParameters.pathMappings = listOf(
            CidrRemotePathMapping(runProfile.options.localPackageDirectoryPath, runProfile.options.remotePackageDirectoryPath)
        )
        cidrRemoteDebugParameters.remoteCommand = """gdb-remote ${sshConfig.host}:1234"""


        val copyToCredentials = sshConfig.copyToCredentials()

        val runContentDescriptorPromise = AsyncPromise<RunContentDescriptor?>()
        runBackgroundableTask("Preparing remote debug session") {
            val textConsoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(runProfile.project)
            val console = textConsoleBuilder.console

            val debugRunnerParameters = OdinRemoteDebugParameters(cidrRemoteDebugParameters, driverConfiguration)

            @Suppress("UnstableApiUsage") val connectionBuilder = copyToCredentials.connectionBuilder()
            val remoteExecutablePathString: @NonNls String = if (!runProfile.options.isBuildOnTarget) {
                prepareLocalBuild(connectionBuilder, runProfile)
            } else {
                it.text = "Building target ..."
                prepareTargetBuild(connectionBuilder, console, runProfile)
            }

            val command =
                """chmod +x $remoteExecutablePathString && ${runProfile.options.lldbServerPath} ${runProfile.options.lldbServerArgs}"""
            val execBuilder = connectionBuilder.execBuilder(command)
            val sshExecProcess: SshExecProcess = execBuilder.execute()
            val sshProcessHandler = CapturingSshProcessHandler(sshExecProcess, StandardCharsets.UTF_8, command)
            console.attachToProcess(sshProcessHandler)
            runBackgroundableTask("'lldb-server' listening ...") {
                sshProcessHandler.runProcess()
            }

            it.text = "Running ..."
            ApplicationManager.getApplication().invokeLater {
                val debuggerManager = XDebuggerManager.getInstance(environment.project)
                val xDebugSession = debuggerManager.startSession(environment, object : XDebugProcessStarter() {
                    @Throws(ExecutionException::class)
                    override fun start(session: XDebugSession): XDebugProcess {
                        val project = session.project
                        // Since the debug process only accepts a console builder, give it a console builder that will return the
                        // console that we used for the build process.

                        val debugProcess =
                            OdinRemoteDebugProcess(debugRunnerParameters, session, OdinNativeDebugProgramRunner.SharedConsoleBuilder(console))
                        ProcessTerminatedListener.attach(debugProcess.processHandler, project)
                        debugProcess.start()
                        return debugProcess
                    }
                })
                runContentDescriptorPromise.setResult(xDebugSession.runContentDescriptor)
            }
        }

        return runContentDescriptorPromise
    }

    private fun prepareLocalBuild(
        connectionBuilder: ConnectionBuilder,
        runProfile: OdinRemoteDebugRunConfiguration
    ): @NonNls String {
        val openSftpChannel = connectionBuilder.openSftpChannel()
        val file = File(runProfile.options.localExecutablePath)
        openSftpChannel.uploadFileOrDir(
            file,
            runProfile.options.targetExecutableUploadDirPath,
            "/", null, null, true
        )

        val remoteExecutablePath = Path.of(runProfile.options.targetExecutableUploadDirPath).resolve(file.name);
        val remoteExecutablePathString = FileUtil.toSystemIndependentName(remoteExecutablePath.toString())
        return remoteExecutablePathString
    }

    private fun prepareTargetBuild(
        connectionBuilder: ConnectionBuilder,
        console: ConsoleView,
        runProfile: OdinRemoteDebugRunConfiguration,
    ): String {
        val options = runProfile.options
        val commandLine = OdinRunConfigurationUtils.createCommandLine(
            runProfile.project,
            options.remoteOdinSdkPath,
            true,
            OdinRunConfigurationUtils.OdinToolMode.BUILD,
            options.remoteCompilerOptions,
            options.targetExecutableOutputPath,
            options.remotePackageDirectoryPath,
            null,
            options.remoteWorkingDirectory
        )

        val buildCommand = commandLine.commandLineString
        val buildExecBuilder = connectionBuilder.execBuilder(buildCommand);
        val buildSshExecProcess = buildExecBuilder.execute()
        val buildSshProcessHandler = CapturingSshProcessHandler(buildSshExecProcess, StandardCharsets.UTF_8, buildCommand)
        console.attachToProcess(buildSshProcessHandler)
        buildSshProcessHandler.runProcess()

        val sftpChannel = connectionBuilder.openSftpChannel()
        if (options.targetExecutableOutputPath != null && options.targetExecutableDownloadPath != null) {

            try {
                sftpChannel.downloadFileOrDir(options.targetExecutableOutputPath!!, options.targetExecutableDownloadPath!!)
            } catch (e: Exception) {

            }

        }

        return options.targetExecutableOutputPath!!
    }

    override fun getRunnerId(): String {
        return "OdinRemoteDebugRunner"
    }
}