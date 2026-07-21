package com.lasagnerd.odin.settings.projectSettings;

import lombok.Data;

@Data
public class OdinSdkState {
    public String id = "";
    public String name = "";
    public String compilerPath = "";
    public String libraryPath = "";
    /** Legacy value from the first registry version. */
    public String homePath = "";
    public String version = "";
    public String origin = "LOCAL";
    public String releaseTag = "";
    public long releaseId;
    public long releaseAssetId;
    public String releaseAssetUrl = "";
}
