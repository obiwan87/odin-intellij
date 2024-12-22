package com.lasagnerd.odin.lang.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinProcedureDefinition;
import com.lasagnerd.odin.lang.stubs.OdinDeclarationStub;
import com.lasagnerd.odin.lang.stubs.OdinFileStub;
import com.lasagnerd.odin.lang.stubs.indexes.OdinAllPublicNamesIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public abstract class OdinDeclarationStubElementType<S extends OdinDeclarationStub<T>, T extends OdinDeclaration> extends OdinStubElementType<S, T> {
    public OdinDeclarationStubElementType(@NotNull String debugName) {
        super(debugName);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        if (!super.shouldCreateStub(node)) return false;
        PsiElement psi = node.getPsi();
        if (psi instanceof OdinDeclaration declaration) {
            OdinProcedureDefinition proc = PsiTreeUtil.getParentOfType(declaration, OdinProcedureDefinition.class);
            if (proc != null)
                return false;
            return declaration.getDeclaredIdentifiers().stream()
                    .map(PsiNamedElement::getName)
                    .filter(Objects::nonNull)
                    .anyMatch(s -> !s.isBlank());
        }
        return false;
    }

    @Override
    public void serialize(@NotNull S stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(String.join(",", stub.getNames()));
    }

    @Override
    public void indexStub(@NotNull S stub, @NotNull IndexSink sink) {
        for (String name : stub.getNames()) {
            if (StringUtil.isNotEmpty(name)) {
                String packageName = null;
                StubElement<?> parent = stub.getParentStub();
                while (parent != null) {
                    if (parent instanceof OdinFileStub odinFileStub) {
                        packageName = odinFileStub.getPackageName();
                        break;
                    }
                    parent = parent.getParentStub();
                }

                String indexingName = StringUtil.isNotEmpty(packageName) ? packageName + "." + name : name;
                if (stub.isPublic()) {
                    sink.occurrence(OdinAllPublicNamesIndex.ALL_PUBLIC_NAMES, indexingName);
                }
            }
        }
    }

    protected static @NotNull List<String> getNames(@NotNull OdinDeclaration psi) {
        return psi.getDeclaredIdentifiers().stream().map(PsiNamedElement::getName).toList();
    }
}
