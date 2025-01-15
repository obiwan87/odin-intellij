package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinRunLineMarkerContributor extends RunLineMarkerContributor {
    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        if (element.getNode().getElementType() != OdinTypes.IDENTIFIER_TOKEN) {
            return null;
        }


        if (!(element.getParent() instanceof OdinDeclaredIdentifier)) {
            return null;
        }

        if (element.getParent().getParent() instanceof OdinPackageClause) {
            AnAction[] actions = ExecutorAction.getActions(0);
            VirtualFile containingVirtualFile = OdinInsightUtils.getContainingVirtualFile(element);
            if (containingVirtualFile != null && containingVirtualFile.getNameWithoutExtension().endsWith("_test")) {
                return new Info(
                        AllIcons.Actions.RunAll,
                        new AnAction[]{actions[0], actions[1], actions[actions.length - 1]},
                        (ignored) -> "Run Test"
                );
            }

            OdinFileScope fileScope = PsiTreeUtil.getParentOfType(element, OdinFileScope.class);
            if (fileScope != null) {
                OdinSymbolTable symbolTable = fileScope.getFullSymbolTable();
                boolean hasAnyTestProcedures = symbolTable.getSymbols().stream()
                        .filter(s -> s.getDeclaredIdentifier() != null)
                        .anyMatch(s -> OdinRunConfigurationUtils.getTestProcedure(s.getDeclaredIdentifier()) != null);
                if (hasAnyTestProcedures) {
                    return new Info(
                            AllIcons.Actions.RunAll,
                            new AnAction[]{actions[0], actions[1], actions[actions.length - 1]},
                            (ignored) -> "Run Test"
                    );
                }
            }
        }

        if (OdinInsightUtils.isLocal(element))
            return null;

        OdinDeclaration declaration = PsiTreeUtil.getParentOfType(element, OdinDeclaration.class);
        OdinProcedureType procedureType = OdinInsightUtils.getProcedureType(declaration);
        if (procedureType == null) {
            return null;
        }

        if (!(declaration instanceof OdinConstantInitDeclaration constantInitDeclaration)) {
            return null;
        }

        boolean isTest = OdinInsightUtils.containsAttribute(constantInitDeclaration.getAttributesDefinitionList(), "test");
        if (isTest) {
            AnAction[] actions = ExecutorAction.getActions(0);
            return new Info(
                    AllIcons.RunConfigurations.TestState.Run,
                    new AnAction[]{actions[0], actions[1], actions[actions.length - 1]},
                    (ignored) -> "Run Test"
            );
        } else if (element.getText().equals("main")) {
            VirtualFile containingVirtualFile = OdinInsightUtils.getContainingVirtualFile(element);
            if (containingVirtualFile != null && !containingVirtualFile.getNameWithoutExtension().endsWith("_test")) {
                AnAction[] actions = ExecutorAction.getActions(0);
                return new Info(
                        AllIcons.RunConfigurations.TestState.Run,
                        new AnAction[]{actions[0], actions[1], actions[actions.length - 1]},
                        (ignored) -> "Run " + element.getContainingFile().getName()
                );
            }
        }
        return null;
    }
}
