package com.lasagnerd.odin.codeInsight;

import com.intellij.lang.ASTNode;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.TokenSet;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OdinParameterInfoHandler implements ParameterInfoHandler<OdinPsiElement, TsOdinParameterOwner> {

    private static final String DELIMITER = ", ";

    private static OdinPsiElement findCallExpression(PsiFile file, int offset) {
        PsiElement element = file.findElementAt(offset);

        return OdinInsightUtils.getCallInfo(new OdinContext(), element).callingElement();
    }

    private static @Nullable TextRange getArgumentListTextRange(@NotNull OdinPsiElement callingElement) {

        if (callingElement instanceof OdinCallExpression callExpression) {
            int startOfList = callExpression.getLparen().getTextOffset() + 1;
            int endOfList = callExpression.getRparen().getTextOffset();
            return TextRange.create(startOfList, endOfList);
        }

        if (callingElement instanceof OdinCallType callType) {
            int startOfList = callType.getLparen().getTextOffset() + 1;
            int endOfList = callType.getRparen().getTextOffset();
            return TextRange.create(startOfList, endOfList);
        }

        return null;
    }

    @Override
    public @Nullable OdinPsiElement findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        PsiElement elementAtOffset = context.getFile().findElementAt(context.getOffset());
        OdinInsightUtils.OdinCallInfo callInfo = OdinInsightUtils.getCallInfo(new OdinContext(), elementAtOffset);
        if (callInfo.callingElement() != null && callInfo.callingType() != null) {
            List<TsOdinParameterOwner> parameterOwners = new ArrayList<>();
            TsOdinType tsOdinType = callInfo.callingType();

            if (tsOdinType.dereference() instanceof TsOdinProcedureType procedureType) {
                parameterOwners.add(procedureType);
            } else if (tsOdinType.dereference() instanceof TsOdinProcedureGroup procedureGroup) {
                parameterOwners.addAll(procedureGroup.getProcedures());
            } else if (tsOdinType.dereference() instanceof TsOdinObjcMember objcMember) {
                parameterOwners.add(objcMember);
            } else if (tsOdinType instanceof TsOdinPseudoMethodType tsOdinPseudoMethodType) {
                parameterOwners.add(tsOdinPseudoMethodType);
            } else if (tsOdinType.dereference() instanceof TsOdinStructType tsOdinStructType) {
                parameterOwners.add(tsOdinStructType);
            } else if (tsOdinType.dereference() instanceof TsOdinUnionType tsOdinUnionType) {
                parameterOwners.add(tsOdinUnionType);
            }

            if (!parameterOwners.isEmpty()) {
                context.setItemsToShow(parameterOwners.toArray(new TsOdinType[0]));
                return callInfo.callingElement();
            }
        }
        return null;
    }

    @Override
    public void showParameterInfo(@NotNull OdinPsiElement element, @NotNull CreateParameterInfoContext context) {
        context.showHint(element, element.getTextRange().getStartOffset() + 1, this);
    }

    @Override
    public OdinPsiElement findElementForUpdatingParameterInfo(@NotNull final UpdateParameterInfoContext context) {

        OdinPsiElement callingElement = findCallExpression(context.getFile(), context.getOffset());
        if (callingElement != null) {
            PsiElement currentParameterOwner = context.getParameterOwner();
            if (currentParameterOwner == callingElement || currentParameterOwner == null)
                return callingElement;
        }
        return null;
    }

    @Override
    public void updateParameterInfo(@NotNull OdinPsiElement callingElement, @NotNull UpdateParameterInfoContext context) {
        TextRange textRange = getArgumentListTextRange(callingElement);
        if (textRange == null)
            return;

        ASTNode[] commas = callingElement.getNode().getChildren(TokenSet.create(OdinTypes.COMMA));
        int paramNum = 0;
        for (ASTNode comma : commas) {
            int commaStartOffset = comma.getStartOffset();
            if (textRange.contains(commaStartOffset) && context.getOffset() > commaStartOffset) {
                paramNum++;
            }
        }
        context.setCurrentParameter(paramNum);
    }

    @Override
    public void updateUI(TsOdinParameterOwner p, @NotNull ParameterInfoUIContext context) {
        List<TsOdinParameter> parameters = p.getParameters();
        // Each entry can declare several parameters. In order to make navigation easier we flatten the list.

        List<String> params = new ArrayList<>();
        List<Integer> lengths = new ArrayList<>();
        int length = 0;
        lengths.add(length);
        int skip = 0;
        if (!parameters.isEmpty()) {
            if (p instanceof TsOdinObjcMember objcMember && !objcMember.isClassMethod()) {
                skip = 1;
            } else if (p instanceof TsOdinPseudoMethodType) {
                skip = 1;
            }
        }

        parameters = parameters.subList(skip, parameters.size());
        for (var parameter : parameters) {
            OdinType typeDefinition = parameter.getParameterDeclaration().getTypeDefinition();
            String paramLabel = (parameter.isExplicitPolymorphicParameter() ? "$" : "") + parameter.getName();

            if (typeDefinition != null) {
                paramLabel += ": " + typeDefinition.getText();
            }
            params.add(paramLabel);
            length += paramLabel.length() + DELIMITER.length();
            lengths.add(length);
        }
        if (params.isEmpty()) {
            context.setupUIComponentPresentation("<no parameters>",
                    0,
                    0,
                    false,
                    false,
                    false,
                    context.getDefaultParameterColor());
            return;
        }
        String parameterListString = String.join(DELIMITER, params);
        int currentIndexOffset = Math.min(Math.max(context.getCurrentParameterIndex(), 0), params.size() - 1);

        int startOffset = lengths.get(currentIndexOffset);
        int endOffset = startOffset + params.get(currentIndexOffset).length();

        context.setupUIComponentPresentation(parameterListString,
                startOffset,
                endOffset,
                false,
                false,
                false,
                context.getDefaultParameterColor());
    }
}
