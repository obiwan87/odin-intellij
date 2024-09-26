package com.lasagnerd.odin.structureView;

import com.intellij.ide.structureView.*;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Grouper;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
    public OdinStructureViewModel(@Nullable Editor editor, OdinFile psiFile) {
        super(psiFile, editor, new OdinStructureViewElement(psiFile));
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        if (element instanceof OdinStructureViewElement odinStructureViewElement) {
            if (odinStructureViewElement.getElement() instanceof OdinFile)
                return false;
            NavigatablePsiElement navigatablePsiElement = odinStructureViewElement.getElement();
            OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(navigatablePsiElement, OdinDeclaration.class, false);
            OdinType declaredType = OdinInsightUtils.getDeclaredType(odinDeclaration);
            return !(declaredType instanceof OdinStructType)
                    && !(declaredType instanceof OdinEnumType)
                    && !(declaredType instanceof OdinBitFieldType);
        }
        return true;
    }

    @Override
    public Sorter @NotNull [] getSorters() {
        return new Sorter[]{Sorter.ALPHA_SORTER};
    }

    @Override
    protected Class<?> @NotNull [] getSuitableClasses() {
        return new Class[]{
                OdinDeclaredIdentifier.class
        };
    }
}
