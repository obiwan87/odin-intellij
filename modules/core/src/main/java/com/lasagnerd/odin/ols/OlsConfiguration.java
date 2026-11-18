package com.lasagnerd.odin.ols;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Data;

import java.util.List;

@Data
public class OlsConfiguration {

    private static final Gson GSON = new Gson();
    @SerializedName("$schema")
    private String schema;

    private List<OlsCollection> collections;

    @SerializedName("thread_pool_count")
    private Integer threadPoolCount;

    @SerializedName("enable_checker_only_saved")
    private Boolean enableCheckerOnlySaved;

    @SerializedName("enable_semantic_tokens")
    private Boolean enableSemanticTokens;

    @SerializedName("enable_document_symbols")
    private Boolean enableDocumentSymbols;

    @SerializedName("enable_format")
    private Boolean enableFormat = true;

    @SerializedName("enable_hover")
    private Boolean enableHover;

    @SerializedName("enable_procedure_context")
    private Boolean enableProcedureContext;

    @SerializedName("enable_snippets")
    private Boolean enableSnippets;

    @SerializedName("enable_inlay_hints")
    private Boolean enableInlayHints;

    @SerializedName("enable_inlay_hints_params")
    private Boolean enableInlayHintsParams = true;

    @SerializedName("enable_inlay_hints_default_params")
    private Boolean enableInlayHintsDefaultParams = true;

    @SerializedName("enable_procedure_snippet")
    private Boolean enableProcedureSnippet = true;

    @SerializedName("enable_references")
    private Boolean enableReferences;

    @SerializedName("enable_fake_methods")
    private Boolean enableFakeMethods;

    @SerializedName("disable_parser_errors")
    private Boolean disableParserErrors;

    private Boolean verbose;
    private Boolean fileLog;

    @SerializedName("odin_command")
    private String odinCommand;

    @SerializedName("checker_args")
    private String checkerArgs;

    private String profile;
    private List<OlsProfile> profiles;

    public static OlsConfiguration read(String json) {
        try {
            return GSON.fromJson(json, OlsConfiguration.class);
        } catch (JsonParseException e) {
            return null;
        }
    }

    public static OlsConfiguration read(VirtualFile file) {
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document != null) {
            String olsContent = document.getText();
            return read(olsContent);
        }
        return null;
    }
}
