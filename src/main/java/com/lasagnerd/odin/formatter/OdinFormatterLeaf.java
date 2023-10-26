package com.lasagnerd.odin.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.formatter.common.AbstractBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class OdinFormatterLeaf extends AbstractBlock {
    private final Indent indent;

    protected OdinFormatterLeaf(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, Indent indent) {
        super(node, wrap, alignment);
        this.indent = indent;
    }

    @Override
    protected List<Block> buildChildren() {
        return Collections.emptyList();
    }

    @Override
    public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Nullable
    @Override
    public Indent getIndent() {
        return indent;
    }
}
