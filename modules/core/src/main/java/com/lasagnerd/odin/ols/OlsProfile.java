package com.lasagnerd.odin.ols;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class OlsProfile {
    private String name;
    private String os;
    @SerializedName("checker_path")
    private List<String> checkerPath;
}
