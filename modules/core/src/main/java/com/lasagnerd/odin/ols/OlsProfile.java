package com.lasagnerd.odin.ols;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OlsProfile {
    private String name;
    private String os;
    @JsonProperty("checker_path")
    private List<String> checkerPath;
}
