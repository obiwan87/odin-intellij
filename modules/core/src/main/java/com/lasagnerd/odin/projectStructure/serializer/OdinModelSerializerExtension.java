package com.lasagnerd.odin.projectStructure.serializer;

import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootProperties;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType;
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootProperties;
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.serialization.JpsModelSerializerExtension;
import org.jetbrains.jps.model.serialization.module.JpsModuleSourceRootPropertiesSerializer;

import java.util.List;

public class OdinModelSerializerExtension extends JpsModelSerializerExtension {
    @Override
    public @NotNull List<? extends JpsModuleSourceRootPropertiesSerializer<?>> getModuleSourceRootPropertiesSerializers() {
        return List.of(
                new OdinSourceRootPropertiesSerializer(),
                new OdinCollectionRootPropertiesSerializer()
        );
    }

    public static class OdinSourceRootPropertiesSerializer extends JpsModuleSourceRootPropertiesSerializer<OdinSourceRootProperties> {

        public OdinSourceRootPropertiesSerializer() {
            super(OdinSourceRootType.INSTANCE, OdinSourceRootType.ID);
        }

        @Override
        public OdinSourceRootProperties loadProperties(@NotNull Element sourceRootTag) {
            return new OdinSourceRootProperties();
        }

        @Override
        public void saveProperties(@NotNull OdinSourceRootProperties properties, @NotNull Element sourceRootTag) {

        }
    }

    public static class OdinCollectionRootPropertiesSerializer extends JpsModuleSourceRootPropertiesSerializer<OdinCollectionRootProperties> {
        public OdinCollectionRootPropertiesSerializer() {
            super(OdinCollectionRootType.INSTANCE, OdinCollectionRootType.ID);
        }

        @Override
        public OdinCollectionRootProperties loadProperties(@NotNull Element sourceRootTag) {
            OdinCollectionRootProperties odinCollectionRootProperties = new OdinCollectionRootProperties();
            Attribute collectionNameAttribute = sourceRootTag.getAttribute("collectionName");
            if(collectionNameAttribute != null) {
                String name = collectionNameAttribute.getValue();
                odinCollectionRootProperties.setCollectionName(name);
            }
            return odinCollectionRootProperties;
        }

        @Override
        public void saveProperties(@NotNull OdinCollectionRootProperties properties, @NotNull Element sourceRootTag) {
            String collectionName = properties.getCollectionName();
            sourceRootTag.setAttribute("collectionName", collectionName);
        }
    }
}
