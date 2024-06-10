package com.lasagnerd.odin.codeInsight;

import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class OdinAttributeUtils {
    public static boolean containsBuiltin(List<OdinAttributeStatement> attributeStatements) {
        if(attributeStatements == null)
            return false;

        for (OdinAttributeStatement attributeStatement : attributeStatements) {
            if(attributeStatement.getIdentifierToken() != null) {
                if(attributeStatement.getIdentifierToken().getText().equals("builtin")) {
                    return true;
                }
            } else {
                for (OdinArgument odinArgument : attributeStatement.getArgumentList()) {
                    if(odinArgument instanceof OdinUnnamedArgument) {
                        if(odinArgument.getText().equals("builtin")) {
                            return true;
                        }
                    }

                    if(odinArgument instanceof OdinNamedArgument) {
                        if(((OdinNamedArgument) odinArgument).getIdentifier().getText().equals("builtin")) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static @NotNull OdinSymbol.OdinVisibility computeVisibility(@NotNull Collection<OdinAttributeStatement> attributeStatements) {
        for (OdinAttributeStatement attributeStatement : attributeStatements) {
            for (OdinArgument odinArgument : attributeStatement.getArgumentList()) {
                if (odinArgument instanceof OdinNamedArgument odinNamedArgument) {
                    String text = odinNamedArgument.getIdentifier().getText();
                    if (text.equals("private")) {
                        String attributeValue = OdinInsightUtils.getStringLiteralValue(odinNamedArgument.getExpression());
                        OdinExpression valueExpression = odinNamedArgument.getExpression();
                        if (Objects.equals(attributeValue, "file")) {
                            return OdinSymbol.OdinVisibility.FILE_PRIVATE;
                        }

                        if(Objects.equals(attributeValue, "package")) {
                            return OdinSymbol.OdinVisibility.PACKAGE_PRIVATE;
                        }
                    }
                }

                if (odinArgument instanceof OdinUnnamedArgument unnamedArgument) {
                    OdinExpression expression = unnamedArgument.getExpression();
                    if (expression.getText().equals("private")) {
                        return OdinSymbol.OdinVisibility.PACKAGE_PRIVATE;
                    }
                }
            }
        }

        return OdinSymbol.OdinVisibility.PUBLIC;
    }
}
