package com.lasagnerd.odin.sdkConfig
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel

object OdinSdkSettingsComponents {
    fun createCheckBoxWithComment(label: String, comment: String) {
        panel {
            row {
                val checkBox = checkBox("").comment("")
            }
        }
    }
}