package com.lasagnerd.odin.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OdinFormatterBlock extends AbstractBlock {


    private final Indent indent;
    private final SpacingBuilder spacingBuilder;


    public OdinFormatterBlock(@NotNull ASTNode node,
                              @Nullable Wrap wrap,
                              @Nullable Alignment alignment,
                              Indent indent,
                              SpacingBuilder spacingBuilder) {
        super(node, wrap, alignment);
        this.indent = indent;
        this.spacingBuilder = spacingBuilder;
    }


    public List<Block> subBlocks() {
        ASTNode node = getNode();
        List<Block> blocks = new ArrayList<>();
        for (ASTNode subNode = node.getFirstChildNode(); subNode != null; subNode = subNode.getTreeNext()) {
            IElementType elementType = subNode.getElementType();
            if (elementType == TokenType.WHITE_SPACE
                    || elementType == OdinTypes.EOS
                    || elementType == OdinTypes.EOS_TOKEN
            ) {
                continue;
            }

            if (elementType == OdinTypes.STATEMENT_LIST || elementType == OdinTypes.SWITCH_CASES) {
                Indent normalIndent = Indent.getNormalIndent();
                OdinFormatterBlock block = createFormatterBlock(subNode, normalIndent);
                blocks.add(block);
            }
            // Create leaf blocks for tokens
            else if (subNode.getPsi().getChildren().length == 0) {
                blocks.add(createLeaf(subNode));
            } else {
                blocks.add(createFormatterBlock(subNode, Indent.getNoneIndent()));
            }

        }

        return blocks;
    }

    @NotNull
    private OdinFormatterBlock createFormatterBlock(ASTNode node, Indent indent) {
        return new OdinFormatterBlock(node, null,
                null,
                indent,
                spacingBuilder);
    }

    @NotNull
    private static OdinFormatterLeaf createLeaf(ASTNode subNode) {
        return new OdinFormatterLeaf(subNode, null,
                null, Indent.getNoneIndent());
    }

    @Override
    protected List<Block> buildChildren() {
        return subBlocks();
    }

    @Override
    public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return spacingBuilder.getSpacing(this, child1, child2);
    }

    @Nullable
    @Override
    public Indent getIndent() {
        return indent;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public @Nullable String getDebugName() {
        return myNode.getElementType().getDebugName();
    }

    @Override
    protected @Nullable Indent getChildIndent() {
        return getIndent();
    }

    @Override
    public @NotNull ChildAttributes getChildAttributes(int newChildIndex) {
        if (myNode.getElementType() == OdinTypes.BLOCK || myNode.getElementType() == OdinTypes.SWITCH_CASE_BLOCK) {

            return new ChildAttributes(Indent.getNormalIndent(), null);
        }
        return new ChildAttributes(Indent.getNoneIndent(), null);
    }
}
