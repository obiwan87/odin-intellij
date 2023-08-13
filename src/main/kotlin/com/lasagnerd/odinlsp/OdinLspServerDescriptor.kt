@file:Suppress("UnstableApiUsage")

package com.lasagnerd.odinlsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor

class OdinLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "Odin") {
    override fun isSupportedFile(file: VirtualFile) = file.extension == "odin"


    override fun createCommandLine(): GeneralCommandLine {
        val lspPath = "D:\\dev\\code\\ols\\ols.exe"

        return GeneralCommandLine().apply {
            exePath = lspPath
            withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            withCharset(Charsets.UTF_8)
            addParameter("--stdio")

        }
    }

    // references resolution is implemented without using the LSP server
    override val lspGoToDefinitionSupport = false
}