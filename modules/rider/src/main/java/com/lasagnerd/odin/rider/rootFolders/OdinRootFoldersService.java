package com.lasagnerd.odin.rider.rootFolders;


import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@State(
        name = "com.lasagnerd.odin.settings.OdinRootFoldersState",
        storages = @Storage("OdinRootFolders.xml")
)
public class OdinRootFoldersService implements PersistentStateComponent<OdinRootFoldersState> {

    private final Project project;
    @Getter(AccessLevel.NONE)
    private OdinRootFoldersState state = new OdinRootFoldersState();

    public OdinRootFoldersService(Project project) {
        this.project = project;
    }

    public static OdinRootFoldersService getInstance(Project project) {
        return project.getService(OdinRootFoldersService.class);
    }

    @Override
    public @NotNull OdinRootFoldersState getState() {
        return state;
    }

    public boolean isRoot(VirtualFile file) {
        if (file == null)
            return false;

        String path = file.getPath();
        return state.getSourceRoots().contains(path) || state.getCollectionRoots().containsKey(path);
    }

    public boolean isCollectionRoot(VirtualFile file) {
        return state.getCollectionRoots().containsKey(file.getPath());
    }

    public boolean isSourceRoot(VirtualFile file) {
        return state.getSourceRoots().contains(file.getPath());
    }

    @Override
    public void loadState(@NotNull OdinRootFoldersState state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

    public Map<String, Collection> getCollcetionMap() {
        OdinRootFoldersState state = getState();
        Map<String, String> collectionRoots = state.getCollectionRoots();
        if (collectionRoots != null) {
            return collectionRoots.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> Collection.parse(e.getValue())
            ));
        }
        return Collections.emptyMap();
    }

    @Override
    public void initializeComponent() {
        PersistentStateComponent.super.initializeComponent();
    }

    public record Collection(String path, String name) {
        public static Collection parse(@Nullable String value) {
            if (value == null)
                return null;
            String[] split = value.split(":");
            if (split.length == 2) {
                return new Collection(split[0], split[1]);
            }
            return null;
        }
    }
}
