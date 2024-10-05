package com.lasagnerd.odin.projectStructure.module.rootTypes.collection;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.jps.model.ex.JpsElementBase;


@Setter
@Getter
public class OdinCollectionRootProperties extends JpsElementBase<OdinCollectionRootProperties> {
    private String collectionName;

    public OdinCollectionRootProperties(String collectionName) {
        this.collectionName = collectionName;
    }


    public OdinCollectionRootProperties() {
    }
}
