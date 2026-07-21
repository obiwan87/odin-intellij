package com.lasagnerd.odin.settings.projectSettings;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

public final class OdinReleaseService {
    public static final String RELEASES_URL="https://api.github.com/repos/odin-lang/Odin/releases?per_page=20";
    private static final Logger LOG=Logger.getInstance(OdinReleaseService.class); private static final Gson GSON=new Gson();
    private OdinReleaseService() {}
    public static @NotNull List<OdinRelease> fetchReleases(boolean force) throws Exception {
        OdinSdkRegistryState state=OdinSdkRegistryService.getInstance().getState();
        HttpRequest.Builder request=HttpRequest.newBuilder(URI.create(RELEASES_URL)).timeout(Duration.ofSeconds(20))
                .header("Accept","application/vnd.github+json").header("X-GitHub-Api-Version","2022-11-28")
                .header("User-Agent","odin-intellij").GET();
        if(!force&&!state.releasesEtag.isBlank())request.header("If-None-Match",state.releasesEtag);
        HttpResponse<String> response=HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build().send(request.build(),HttpResponse.BodyHandlers.ofString());
        String json;
        if(response.statusCode()==304)json=state.cachedReleasesJson;
        else if(response.statusCode()==200){json=response.body();state.cachedReleasesJson=json;state.releasesEtag=response.headers().firstValue("ETag").orElse("");state.lastReleaseCheck=System.currentTimeMillis();}
        else throw new java.io.IOException("GitHub returned HTTP "+response.statusCode());
        if(json==null||json.isBlank())return List.of();
        List<OdinRelease> releases=GSON.fromJson(json,new TypeToken<List<OdinRelease>>(){}.getType());
        return releases==null?List.of():releases.stream().filter(it->!it.draft()).toList();
    }
    public static @Nullable OdinRelease.Asset compatibleAsset(OdinRelease release) {
        if(release==null||release.assets()==null)return null; String os=SystemInfo.isWindows?"windows":SystemInfo.isMac?"macos":SystemInfo.isLinux?"linux":"";
        String arch=System.getProperty("os.arch","").toLowerCase(Locale.ROOT); String cpu=(arch.contains("aarch64")||arch.contains("arm64"))?"arm64":"amd64";
        return compatibleAsset(release,os,cpu);
    }
    static @Nullable OdinRelease.Asset compatibleAsset(OdinRelease release,String os,String arch) {
        if(release.assets()==null)return null;String marker="odin-"+os+"-"+arch+"-";
        return release.assets().stream().filter(a->a.name()!=null&&a.name().toLowerCase(Locale.ROOT).startsWith(marker)
                &&(a.name().endsWith(".zip")||a.name().endsWith(".tar.gz"))).findFirst().orElse(null);
    }
    static boolean isSdkArchive(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.startsWith("odin-") && (lower.endsWith(".zip") || lower.endsWith(".tar.gz"));
    }
    public static void logCheckFailure(Throwable error){LOG.info("Unable to check for Odin releases",error);}
}
