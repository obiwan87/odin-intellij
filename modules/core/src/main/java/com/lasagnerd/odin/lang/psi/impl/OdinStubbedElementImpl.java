package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinPsiElement;
import org.jetbrains.annotations.NotNull;

public class OdinStubbedElementImpl<T extends StubBase<?>> extends StubBasedPsiElementBase<T> implements OdinPsiElement {
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
    public OdinSymbolTable getFullSymbolTable() {
        return null;
    }

    @Override
    public void setFullSymbolTable(OdinSymbolTable symbolTable) {

    }

    @Override
    public OdinExpression parenthesesUnwrap() {
        return null;
    }
}
