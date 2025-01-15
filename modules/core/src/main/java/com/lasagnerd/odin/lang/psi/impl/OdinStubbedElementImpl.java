package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinPsiElement;
import org.jetbrains.annotations.NotNull;

public class OdinStubbedElementImpl<T extends StubBase<?>> extends StubBasedPsiElementBase<T> implements OdinPsiElement {
    private OdinSymbolTable fullSymbolTable;

    public OdinStubbedElementImpl(@NotNull T stub, @NotNull IStubElementType<?, ?> nodeType) {
        super(stub, nodeType);
    }

    public OdinStubbedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    public OdinStubbedElementImpl(T stub, IElementType nodeType, ASTNode node) {
        super(stub, nodeType, node);
    }

    @Override
    public OdinExpression parenthesesUnwrap() {
        return OdinInsightUtils.parenthesesUnwrap(this);
    }

    @Override
    public String getLocation() {
        return OdinInsightUtils.getLocation(this);
    }

    @Override
    public OdinFile getContainingOdinFile() {
        if (this.getContainingFile() instanceof OdinFile odinFile)
            return odinFile;
        return null;
    }
}
