package com.lasagnerd.odin.lang;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.lasagnerd.odin.lang.psi.OdinForStatement;
import com.lasagnerd.odin.lang.psi.OdinIfStatement;
import com.lasagnerd.odin.lang.psi.OdinProcedureDeclarationStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdinBlock extends AbstractBlock {
    private final SpacingBuilder spacingBuilder;

    protected OdinBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, SpacingBuilder spacingBuilder) {
        super(node, wrap, alignment);
        this.spacingBuilder = spacingBuilder;
    }

    @Override
    protected List<Block> buildChildren() {
        var child = myNode.getFirstChildNode();
        var blocks = new java.util.ArrayList<Block>();
        while (child != null) {
            if (child instanceof OdinIfStatement ifStatement) {
                blocks.add(new OdinBlock(ifStatement.getBlock().getNode(), Wrap.createWrap(WrapType.NONE, false),
                        Alignment.createAlignment(), spacingBuilder));
            }

            if (child instanceof OdinProcedureDeclarationStatement psi) {
                var block = psi.getBlock();
                if(block != null) {
                    blocks.add(new OdinBlock(block.getNode(), Wrap.createWrap(WrapType.NONE, false),
                            Alignment.createAlignment(), spacingBuilder));
                }
            }

            if (child instanceof OdinForStatement psi) {
                blocks.add(new OdinBlock(psi.getForBody().getNode(), Wrap.createWrap(WrapType.NONE, false),
                        Alignment.createAlignment(), spacingBuilder));
            }

            child = child.getTreeNext();
        }

        return blocks;
    }

    @Override
    public @Nullable Spacing getSpacing(@Nullable Block block, @NotNull Block block1) {
        return spacingBuilder.getSpacing(this, block, block1);
    }

    @Override
    public boolean isLeaf() {
        return myNode.getFirstChildNode() == null;
    }

    @Override
    public @Nullable Indent getIndent() {
        return Indent.getIndent(Indent.Type.NORMAL, true, true);
    }
}
