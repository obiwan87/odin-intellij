package com.lasagnerd.odin.codeInsight;

import com.lasagnerd.odin.codeInsight.symbols.OdinVisibility;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class OdinAttributeUtils {
    public static boolean containsAttribute(List<OdinAttributesDefinition> attributesDefinitions, String attributeName) {
        if (attributesDefinitions == null)
            return false;

        for (OdinAttributesDefinition attributesDefinition : attributesDefinitions) {

            for (OdinAttributeArgument odinArgument : attributesDefinition.getAttributeArgumentList()) {
                if (odinArgument instanceof OdinUnassignedAttribute unassignedAttribute) {
                    if (unassignedAttribute.getText().equals(attributeName)) {
                        return true;
                    }
                }

                if (odinArgument instanceof OdinAssignedAttribute assignedAttribute) {
                    if (assignedAttribute.getAttributeIdentifier().getText().equals(attributeName)) {
                        return true;
                    }
                }
            }

        }

        return false;
    }

    public static @NotNull OdinVisibility computeVisibility(@NotNull Collection<OdinAttributesDefinition> odinAttributesDefinitions) {
        for (OdinAttributesDefinition odinAttributesDefinition : odinAttributesDefinitions) {
            for (OdinAttributeArgument attributeArgument : odinAttributesDefinition.getAttributeArgumentList()) {
                if (attributeArgument instanceof OdinAssignedAttribute assignedAttribute) {
                    String text = assignedAttribute.getAttributeIdentifier().getText();
                    if (text.equals("private")) {
                        String attributeValue = OdinInsightUtils.getStringLiteralValue(assignedAttribute.getExpression());
                        if (Objects.equals(attributeValue, "file")) {
                            return OdinVisibility.FILE_PRIVATE;
                        }

                        if (Objects.equals(attributeValue, "package")) {
                            return OdinVisibility.PACKAGE_PRIVATE;
                        }
                    }
                }

                if (attributeArgument instanceof OdinUnassignedAttribute unnamedArgument) {
                    if (unnamedArgument.getAttributeIdentifier().getText().equals("private")) {
                        return OdinVisibility.PACKAGE_PRIVATE;
                    }
                }
            }
        }

        return OdinVisibility.PACKAGE_EXPORTED;
    }
}
