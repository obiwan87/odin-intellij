package com.lasagnerd.odin.debugger.runner

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.ssh.ConnectionBuilder
import com.intellij.ssh.process.CapturingSshProcessHandler
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

object OdinRemoteDebuggerUtils {

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
