package com.lasagnerd.odin.structureView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.OdinIcons;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.completion.OdinCompletionContributor;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public class OdinStructureViewElement implements StructureViewTreeElement, SortableTreeElement {

    private final NavigatablePsiElement element;

    public OdinStructureViewElement(NavigatablePsiElement element) {
        this.element = element;
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        if (element instanceof OdinFile file) {
            return new PresentationData(file.getVirtualFile().getPresentableName(),
                    "",
                    OdinIcons.OdinFileType,
                    null);
        } else if (element instanceof OdinDeclaredIdentifier declaredIdentifier) {
            return declaredIdentifier.getPresentation();
        } else if (element instanceof PsiNamedElement namedElement) {
            OdinSymbolType symbolType = OdinInsightUtils.classify(namedElement);
            Icon icon = OdinCompletionContributor.getIcon(symbolType);
            return new PresentationData(namedElement.getName(), "", icon, null);
        }

        return new PresentationData();
    }

    @Override
    public void navigate(boolean requestFocus) {
        element.navigate(true);
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public TreeElement @NotNull [] getChildren() {
        if (element instanceof OdinFile file) {
            return getFileChildren(file);
        }

        if (element instanceof OdinDeclaredIdentifier declaredIdentifier) {
            OdinType declaredType = OdinInsightUtils.getDeclaredType(declaredIdentifier);
            if (declaredType instanceof OdinStructType structType) {
                List<OdinStructureViewElement> structureViewElements = new ArrayList<>();
                List<OdinFieldDeclarationStatement> fieldDeclarations = OdinInsightUtils.getStructFieldsDeclarationStatements(structType);
                for (OdinFieldDeclarationStatement fieldDeclaration : fieldDeclarations) {
                    for (OdinDeclaredIdentifier identifier : fieldDeclaration.getDeclaredIdentifiers()) {
                        structureViewElements.add(new OdinStructureViewElement((NavigatablePsiElement) identifier));
                    }
                }

                return structureViewElements.toArray(new TreeElement[0]);
            }
        }
        return new TreeElement[0];
    }

    private static TreeElement @NotNull [] getFileChildren(OdinFile file) {
        OdinFileScope fileScope = file.getFileScope();
        if (fileScope != null) {
            List<OdinStructureViewElement> treeElements = new ArrayList<>();
            List<OdinSymbol> symbols = fileScope.getSymbolTable()
                    .getSymbols()
                    .stream()
                    .filter(s -> s.getSymbolType() != OdinSymbolType.PACKAGE_REFERENCE)
                    .toList();
            for (OdinSymbol symbol : symbols) {
                PsiNamedElement declaredIdentifier = symbol.getDeclaredIdentifier();
                if (declaredIdentifier instanceof NavigatablePsiElement navigatablePsiElement) {
                    OdinStructureViewElement treeElement = new OdinStructureViewElement(navigatablePsiElement);
                    treeElements.add(treeElement);
                }
            }
            return treeElements.toArray(new TreeElement[0]);
        }
        return new TreeElement[0];
    }

    @Override
    public @NotNull String getAlphaSortKey() {
        String name = element.getName();
        if (name != null)
            return name;
        return "";
    }
}
