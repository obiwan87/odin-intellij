package com.lasagnerd.odin.projectStructure.module.rootTypes.collection

import org.jetbrains.jps.model.ex.JpsElementBase

class OdinCollectionRootProperties : JpsElementBase<OdinCollectionRootProperties?> {
    var collectionName: String? = null

    constructor(collectionName: String?) {
        this.collectionName = collectionName
    }


    constructor()
}
