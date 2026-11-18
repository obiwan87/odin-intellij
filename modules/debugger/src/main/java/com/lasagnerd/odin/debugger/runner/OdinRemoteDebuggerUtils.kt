package com.lasagnerd.odin.debugger.runner

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.remote.RemoteCredentials
import com.intellij.ssh.ConnectionBuilder
import com.intellij.ssh.process.CapturingSshProcessHandler
import java.lang.reflect.InvocationTargetException
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

object OdinRemoteDebuggerUtils {

    @JvmStatic
    fun createConnectionBuilder(credentials: RemoteCredentials): ConnectionBuilder {
        val loader = ConnectionBuilder::class.java.classLoader

        try {
            val factory = Class.forName(
                "com.intellij.ssh.RemoteCredentialsUtil", true, loader
            )
            val method = factory.getMethod(
                "connectionBuilder",
                RemoteCredentials::class.java,
                Class.forName("com.intellij.openapi.project.Project", false, loader),
                Class.forName(
                    "com.intellij.openapi.progress.ProgressIndicator", false, loader
                ),
                Boolean::class.javaPrimitiveType!!,
                Class.forName("com.intellij.ssh.ui.SshPasswordPrompt", false, loader),
            )

            return method.invoke(
                null, credentials, null, null, true, null
            ) as ConnectionBuilder
        } catch (e: InvocationTargetException) {
            throw e.targetException
        } catch (e: ReflectiveOperationException) {
            throw IllegalStateException(
                "Cannot create SSH connection builder with this IDE version", e
            )
        }
    }

    fun autoDetectLldbDap(): String? {
        var file = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS("lldb-dap")
            ?: PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS("lldb-dap-18")
        if (file != null) {
            return file.absolutePath
        }
        return null
    }

    fun autoDetectLldbServer(connectionBuilder: ConnectionBuilder): String {
        val command = "echo `which lldb-server-18 | which lldb-server`"
        val execBuilder = connectionBuilder.execBuilder(command)
        val process = execBuilder.execute();
        val capturing = CapturingSshProcessHandler(process, StandardCharsets.UTF_8, command)
        val processResult = capturing.runProcess()
        return processResult.stdout
    }

    fun extractPortGdbRemoteArgs(gdbRemoteArgs: String, defaultPort: Int): Int {
        val pattern = Pattern.compile(":([0-9]+)")
        val matcher = pattern.matcher(gdbRemoteArgs)
        if (matcher.find()) {
            return matcher.group(1).toIntOrNull() ?: defaultPort
        }
        return defaultPort
    }

}
