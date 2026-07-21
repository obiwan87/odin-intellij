package com.lasagnerd.odin.settings.projectSettings;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public record OdinRelease(long id, @SerializedName("tag_name") String tagName, String name,
                          @SerializedName("published_at") String publishedAt, @SerializedName("html_url") String htmlUrl,
                          boolean draft, boolean prerelease, List<Asset> assets) {
    public record Asset(long id, String name, long size, @SerializedName("browser_download_url") String downloadUrl,
                        String digest) {}
    @Override public String toString() { return tagName; }
}
