package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinContext;
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
    public static @Nullable OdinSymbol resolve(@NotNull OdinContext context, @NotNull OdinIdentifier element) {
        try {
            if (element.getParent() instanceof OdinImplicitSelectorExpression implicitSelectorExpression) {
                TsOdinType tsOdinType = implicitSelectorExpression.getInferredType(context);
                OdinContext typeElements = OdinInsightUtils.getTypeElements(element.getProject(), tsOdinType);
                return typeElements.getSymbol(element.getText());
            } else if (element.getParent() instanceof OdinNamedArgument namedArgument) {
                OdinInsightUtils.OdinCallInfo callInfo = OdinInsightUtils.getCallInfo(context, namedArgument);
                if (callInfo.callingType() instanceof TsOdinParameterOwner parameterOwner) {
                    List<TsOdinParameter> parameters = parameterOwner.getParameters();
                    TsOdinParameter tsOdinParameter = parameters.stream()
                            .filter(p -> p.getName().equals(namedArgument.getIdentifier().getText()))
                            .findFirst().orElse(null);

                    if (tsOdinParameter != null) {
                        return tsOdinParameter.toSymbol();
                    }
                }
            } else {
                OdinContext symbolContext = findIdentifierContext(context, element);
                OdinSymbol symbol = symbolContext.getSymbol(element.getIdentifierToken().getText());
                if (symbol != null) {
                    if (!OdinInsightUtils.isVisible(element, symbol) && symbol.getSymbolType() == OdinSymbolType.PACKAGE_REFERENCE) {
                        return null;
                    }
                    return symbol;
                }
            }
            return null;
        } catch (StackOverflowError e) {
            OdinInsightUtils.logStackOverFlowError(element, OdinReference.LOG);
            return null;
        }
    }

    // Computes the context under which the identifier is expected to be defined
    static OdinContext findIdentifierContext(OdinContext context, @NotNull OdinIdentifier element) {
        @NotNull OdinIdentifier identifier = element;
        PsiElement parent = identifier.getParent();
        if (parent instanceof OdinRefExpression refExpression) {
            if (refExpression.getExpression() != null) {
                return OdinInsightUtils.getReferenceableSymbols(refExpression.getExpression());
            } else {
                return OdinContextBuilder.buildIdentifierContext(context, element);
            }
        } else {
            OdinQualifiedType qualifiedType = PsiTreeUtil.getParentOfType(identifier, OdinQualifiedType.class);
            if (qualifiedType != null) {
                if (qualifiedType.getPackageIdentifier() == identifier) {
                    return OdinContextBuilder.buildIdentifierContext(context, element);
                } else {
                    return OdinInsightUtils.getReferenceableSymbols(context, qualifiedType);
                }
            } else if (parent instanceof OdinSimpleRefType) {
                return OdinContextBuilder.buildIdentifierContext(context, element);
            } else {
                return OdinContext.EMPTY;
            }
        }
    }

}


