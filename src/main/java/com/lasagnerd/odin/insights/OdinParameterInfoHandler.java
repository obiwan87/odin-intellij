package com.lasagnerd.odin.insights;

import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.lasagnerd.odin.insights.OdinInsightUtils.getDeclarationsOfImportedPackage;

public class OdinParameterInfoHandler implements ParameterInfoHandler<OdinCallExpression, OdinProcedureDeclarationStatement> {

    private static final String DELIMITER = ", ";

    @Override
    public @Nullable OdinCallExpression findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        OdinCallExpression callExpression = findCallExpression(context.getFile(), context.getOffset());

        if (callExpression != null) {

            List<PsiElement> matchingDeclarations = findMatchingDeclarations(callExpression.getExpression().getText(), callExpression);
            if (!matchingDeclarations.isEmpty()) {
                context.setItemsToShow(matchingDeclarations.toArray(new PsiElement[0]));
                return callExpression;
            }
        }
        return null;
    }

    @Nullable
    private static OdinCallExpression findCallExpression(PsiFile file, int offset) {
        PsiElement element = file.findElementAt(offset);

        OdinCallExpression callExpression = null;
        if (element != null) {
            callExpression = PsiTreeUtil.getParentOfType(element, false, OdinCallExpression.class);
        }
        return callExpression;
    }

    public static List<PsiElement> findMatchingDeclarations(String name, OdinCallExpression callExpression) {
        OdinScope declarations = OdinScopeResolver.resolveScope(callExpression, psiElement -> {
            if (psiElement instanceof OdinDeclaredIdentifier identifier)
                if (identifier.getParent() instanceof OdinProcedureDeclarationStatement ||
                        identifier.getParent() instanceof OdinProcedureOverloadDeclarationStatement
                ) {
                    return identifier.getText().equals(name);
                }
            return false;
        });



        OdinExpression expression = callExpression.getExpression();

        if (expression instanceof OdinRefExpression) {
            String refName = expression.getText();
            String[] parts = refName.split("\\.");
            if (parts.length > 1) {
                String importName = parts[0];
                OdinFile containingFile = (OdinFile) callExpression.getContainingFile();
                OdinScope allImportedDeclarations = getDeclarationsOfImportedPackage(
                        OdinInsightUtils.getImportStatementsInfo(containingFile.getFileScope()).get(importName), containingFile.getVirtualFile().getPath(),
                        callExpression.getProject());
                declarations.addAll(allImportedDeclarations.getSymbolTable()
                        .values()
                        .stream()
                        .filter(decl -> decl.getDeclaredIdentifier().getText().equals(parts[1]))
                        .toList());
            }
        }

        List<PsiElement> procedures = new ArrayList<>();
        for (PsiElement declaration : declarations.getNamedElements()) {
            if (declaration instanceof OdinDeclaredIdentifier) {
                if (declaration.getParent() instanceof OdinProcedureDeclarationStatement proc) {
                    procedures.add(proc);
                }

                if (declaration.getParent() instanceof OdinProcedureOverloadDeclarationStatement overload) {
                    for (OdinIdentifier odinIdentifier : overload.getIdentifierList()) {
                        PsiReference identifierReference = odinIdentifier.getReference();
                        if (identifierReference != null) {
                            PsiElement resolve = identifierReference.resolve();
                            if (resolve != null && resolve.getParent() instanceof OdinProcedureDeclarationStatement proc) {
                                procedures.add(proc);
                            }
                        }
                    }
                }
            }
        }
        return procedures;
    }

    @Override
    public void showParameterInfo(@NotNull OdinCallExpression element, @NotNull CreateParameterInfoContext context) {
        context.showHint(element, element.getTextRange().getStartOffset() + 1, this);
    }

    @Override
    public OdinCallExpression findElementForUpdatingParameterInfo(@NotNull final UpdateParameterInfoContext context) {

        OdinCallExpression callExpression = findCallExpression(context.getFile(), context.getOffset());
        if (callExpression != null) {
            PsiElement currentParameterOwner = context.getParameterOwner();
            if (currentParameterOwner == callExpression || currentParameterOwner == null)
                return callExpression;
        }
        return null;
    }

    @Override
    public void updateParameterInfo(@NotNull OdinCallExpression odinCallExpression, @NotNull UpdateParameterInfoContext context) {
        int startOfList = odinCallExpression.getLparen().getTextOffset() + 1;
        int offset = context.getOffset();


        if (startOfList >= offset) {
            context.setCurrentParameter(0);
            return;
        }

        int start = startOfList - odinCallExpression.getTextOffset();
        int end = offset - odinCallExpression.getTextOffset();

        String text = odinCallExpression.getText().substring(start, end);
        int commas = text.length() - text.replace(",", "").length();

        context.setCurrentParameter(commas);
    }

    @Override
    public void updateUI(OdinProcedureDeclarationStatement p, @NotNull ParameterInfoUIContext context) {

        System.out.println("Parameter index in Update UI: " + context.getCurrentParameterIndex());
        var parameters = p.getProcedureType().getParamEntryList();
        // Each entry can declare several parameters. In order to make navigation easier we flatten the list.

        List<String> params = new ArrayList<>();
        List<Integer> lengths = new ArrayList<>();
        int length = 0;
        lengths.add(length);
        for (OdinParamEntry odinParamEntry : parameters) {
            OdinTypeDefinitionExpression typeDefinition = odinParamEntry.getParameterDeclaration().getTypeDefinition();
            for (OdinParameter odinParamDeclaration : odinParamEntry.getParameterDeclaration().getParameterList()) {
                String param = odinParamDeclaration.getDeclaredIdentifier().getText();
                if (typeDefinition != null) {
                    param += ": " + typeDefinition.getText();
                }
                params.add(param);
                length += param.length() + DELIMITER.length();
                lengths.add(length);
            }
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
