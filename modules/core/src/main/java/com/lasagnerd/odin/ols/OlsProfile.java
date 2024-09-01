package com.lasagnerd.odin.ols;

import lombok.Data;

import java.util.List;

@Data
public class OlsProfile {
    private String name;
    private String os;
    private List<String> checkerPath;
}
