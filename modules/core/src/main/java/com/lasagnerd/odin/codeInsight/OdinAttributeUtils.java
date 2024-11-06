package com.lasagnerd.odin.codeInsight;

import com.lasagnerd.odin.codeInsight.symbols.OdinVisibility;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class OdinAttributeUtils {
    public static boolean containsAttribute(List<OdinAttribute> attributeStatements, String attributeName) {
        if(attributeStatements == null)
            return false;

        for (OdinAttribute attributeStatement : attributeStatements) {
            if(attributeStatement.getIdentifierToken() != null) {
                if(attributeStatement.getIdentifierToken().getText().equals(attributeName)) {
                    return true;
                }
            } else {
                for (OdinArgument odinArgument : attributeStatement.getArgumentList()) {
                    if(odinArgument instanceof OdinUnnamedArgument) {
                        if(odinArgument.getText().equals(attributeName)) {
                            return true;
                        }
                    }

                    if(odinArgument instanceof OdinNamedArgument) {
                        if(((OdinNamedArgument) odinArgument).getIdentifier().getText().equals(attributeName)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static @NotNull OdinVisibility computeVisibility(@NotNull Collection<OdinAttribute> attributeStatements) {
        for (OdinAttribute attributeStatement : attributeStatements) {
            if(attributeStatement.getIdentifierToken() != null) {
                if(attributeStatement.getIdentifierToken().getText().equals("private")) {
                    return OdinVisibility.PACKAGE_PRIVATE;
                }
            }
            for (OdinArgument odinArgument : attributeStatement.getArgumentList()) {
                if (odinArgument instanceof OdinNamedArgument odinNamedArgument) {
                    String text = odinNamedArgument.getIdentifier().getText();
                    if (text.equals("private")) {
                        String attributeValue = OdinInsightUtils.getStringLiteralValue(odinNamedArgument.getExpression());
                        if (Objects.equals(attributeValue, "file")) {
                            return OdinVisibility.FILE_PRIVATE;
                        }

                        if(Objects.equals(attributeValue, "package")) {
                            return OdinVisibility.PACKAGE_PRIVATE;
                        }
                    }
                }

                if (odinArgument instanceof OdinUnnamedArgument unnamedArgument) {
                    OdinExpression expression = unnamedArgument.getExpression();
                    if (expression.getText().equals("private")) {
                        return OdinVisibility.PACKAGE_PRIVATE;
                    }
                }
            }
        }

        return OdinVisibility.PACKAGE_EXPORTED;
    }
}
