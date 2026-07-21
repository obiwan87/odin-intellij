package com.lasagnerd.odin.settings.projectSettings;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class OdinSdkRegistryState {
    public int version = 1;
    public boolean automaticallyCheckForReleases;
    public long lastReleaseCheck;
    public String lastNotifiedRelease = "";
    public String releasesEtag = "";
    public String cachedReleasesJson = "";
    public List<OdinSdkState> sdks = new ArrayList<>();
}
