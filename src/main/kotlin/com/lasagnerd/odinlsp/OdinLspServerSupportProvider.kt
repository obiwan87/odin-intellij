@file:Suppress("UnstableApiUsage")

package com.lasagnerd.odinlsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider

class OdinLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerSupportProvider.LspServerStarter) {
        serverStarter.ensureServerStarted(OdinLspServerDescriptor(project))
    }
}