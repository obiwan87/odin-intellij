package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinParameter;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinParameterOwner;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdinReferenceResolver {

    // TODO Reference resolver also, like type resolver and inference engine, needs context
    //  Example 1:
    //  In a when-block where the value of ODIN_OS/ODIN_ARCH is statically computable
    //  we need that information to import the correct files.
    //  Example 2:
    //  Same as above, but with build flags (?)

    // see https://odin-lang.org/docs/overview/#file-suffixes
    public static @Nullable OdinSymbol resolve(@NotNull OdinIdentifier element) {
        if (element.getParent() instanceof OdinImplicitSelectorExpression implicitSelectorExpression) {
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
            OdinContext typeElements = OdinInsightUtils.getTypeElements(element.getProject(), tsOdinType);
            return typeElements.getSymbol(element.getText());
        }

        if (element.getParent() instanceof OdinNamedArgument namedArgument) {
            OdinInsightUtils.OdinCallInfo callInfo = OdinInsightUtils.getCallInfo(namedArgument);
            if (callInfo.callingType() instanceof TsOdinParameterOwner parameterOwner) {
                List<TsOdinParameter> parameters = parameterOwner.getParameters();
                TsOdinParameter tsOdinParameter = parameters.stream()
                        .filter(p -> p.getName().equals(namedArgument.getIdentifier().getText()))
                        .findFirst().orElse(null);

                if (tsOdinParameter != null) {
                    return tsOdinParameter.toSymbol();
                }
            }
        }

        try {
            OdinSymbol symbol = findSymbol(element);
            if (symbol != null) {
                if (!OdinInsightUtils.isVisible(element, symbol) && symbol.getSymbolType() == OdinSymbolType.PACKAGE_REFERENCE) {
                    return null;
                }
                return symbol;
            }
            return null;
        } catch (StackOverflowError e) {
            OdinInsightUtils.logStackOverFlowError(element, OdinReference.LOG);
            return null;
        }
    }

    static OdinSymbol findSymbol(@NotNull OdinIdentifier element) {
        OdinContext context = OdinContextBuilder.buildIdentifierContext(element);
        return findSymbol(context, element);
    }

    static OdinSymbol findSymbol(OdinContext parentContext, @NotNull OdinIdentifier element) {
        @NotNull OdinIdentifier identifier = element;
        PsiElement parent = identifier.getParent();
        OdinContext context;
        if (parent instanceof OdinRefExpression refExpression) {
            if (refExpression.getExpression() != null) {
                context = OdinInsightUtils.getReferenceableSymbols(refExpression.getExpression());
            } else {
                context = parentContext;
            }
        } else if (parent instanceof OdinImplicitSelectorExpression implicitSelectorExpression) {
            TsOdinType tsOdinType = implicitSelectorExpression.getInferredType();
            if (!tsOdinType.isUnknown()) {
                context = OdinInsightUtils.getTypeElements(identifier.getProject(), tsOdinType);
            } else {
                // TODO This might lead to resolving to a wrong symbol that has the same name
                context = parentContext;
            }
        } else {
            OdinQualifiedType qualifiedType = PsiTreeUtil.getParentOfType(identifier, OdinQualifiedType.class);
            if (qualifiedType != null) {
                if (qualifiedType.getPackageIdentifier() == identifier) {
                    context = parentContext;
                } else {
                    context = OdinInsightUtils.getReferenceableSymbols(qualifiedType);
                }
            } else {
                context = parentContext;
            }
        }

        if (context == OdinContext.EMPTY || context == null) {
            context = parentContext;
        }

        if (context != null) {
            return context.getSymbol(identifier.getIdentifierToken().getText());
        }

        return null;
    }

}


