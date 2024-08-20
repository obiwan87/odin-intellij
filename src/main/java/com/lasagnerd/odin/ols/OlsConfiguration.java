package com.lasagnerd.odin.ols;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OlsConfiguration {
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
}
