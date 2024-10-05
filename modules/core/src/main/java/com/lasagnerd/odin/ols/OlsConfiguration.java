package com.lasagnerd.odin.ols;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OlsConfiguration {

    public static final ObjectMapper MAPPER = new ObjectMapper();
    @JsonProperty("$schema")
    private String schema;

    private List<OlsCollection> collections;

    @JsonProperty("thread_pool_count")
    private Integer threadPoolCount;

    @JsonProperty("enable_checker_only_saved")
    private Boolean enableCheckerOnlySaved;

    @JsonProperty("enable_semantic_tokens")
    private Boolean enableSemanticTokens;

    @JsonProperty("enable_document_symbols")
    private Boolean enableDocumentSymbols;

    @JsonProperty("enable_format")
    private Boolean enableFormat = true;

    @JsonProperty("enable_hover")
    private Boolean enableHover;

    @JsonProperty("enable_procedure_context")
    private Boolean enableProcedureContext;

    @JsonProperty("enable_snippets")
    private Boolean enableSnippets;

    @JsonProperty("enable_inlay_hints")
    private Boolean enableInlayHints;

    @JsonProperty("enable_inlay_hints_params")
    private Boolean enableInlayHintsParams = true;

    @JsonProperty("enable_inlay_hints_default_params")
    private Boolean enableInlayHintsDefaultParams = true;

    @JsonProperty("enable_procedure_snippet")
    private Boolean enableProcedureSnippet = true;

    @JsonProperty("enable_references")
    private Boolean enableReferences;

    @JsonProperty("enable_fake_methods")
    private Boolean enableFakeMethods;

    @JsonProperty("disable_parser_errors")
    private Boolean disableParserErrors;

    private Boolean verbose;
    private Boolean fileLog;

    @JsonProperty("odin_command")
    private String odinCommand;

    @JsonProperty("checker_args")
    private String checkerArgs;

    private String profile;
    private List<OlsProfile> profiles;

    public static OlsConfiguration read(String json) {
        try {
            return MAPPER.readValue(json, OlsConfiguration.class);
        } catch (JsonProcessingException e) {
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
