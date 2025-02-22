package com.lasagnerd.odin.formatting.block;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OdinFormatterBlock extends AbstractBlock {
    public static final Spacing NO_SPACING = Spacing.createSpacing(0, 0, 0, false, 0);
    private final Indent indent;
    private final SpacingBuilder spacingBuilder;
    static List<IElementType> typesToIndent = List.of(
            OdinTypes.STATEMENT_LIST,
            OdinTypes.FOREIGN_STATEMENT_LIST,
            OdinTypes.STRUCT_BODY,
            OdinTypes.ENUM_BODY,
            OdinTypes.UNION_BODY,
            OdinTypes.BIT_FIELD_BODY,
            OdinTypes.COMPOUND_VALUE_BODY,
            OdinTypes.PROCEDURE_GROUP_BODY
    );

    static List<IElementType> typesToSmartIndent = List.of(
            OdinTypes.BLOCK,
            OdinTypes.STRUCT_BLOCK,
            OdinTypes.SWITCH_CASE,
            OdinTypes.UNION_BLOCK,
            OdinTypes.ENUM_BLOCK,
            OdinTypes.BIT_FIELD_BLOCK,
            OdinTypes.PROCEDURE_GROUP_BLOCK,
            OdinTypes.COMPOUND_VALUE
    );

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
            if ((elementType == TokenType.WHITE_SPACE
                    || elementType == OdinTypes.EOS
                    || elementType == OdinTypes.SOS
                    || elementType == OdinTypes.EOS_TOKEN
            )
                    && subNode.getText().trim().isEmpty()) {
                continue;
            }

            if (typesToIndent.contains(elementType)) {
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
        return new OdinFormatterLeaf(subNode, null, null, Indent.getNoneIndent());
    }

    @Override
    protected List<Block> buildChildren() {
        return subBlocks();
    }

    @Override
    public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        if (endOfBlockIsNewLine(child1, child2) || lastSwitchCaseIsEmpty(child1, child2))
            return NO_SPACING;

        return spacingBuilder.getSpacing(this, child1, child2);
    }

    private boolean lastSwitchCaseIsEmpty(@Nullable Block child1, @NotNull Block child2) {
        OdinSwitchCases switchCases = getPsiElement(child1, OdinSwitchCases.class);
        OdinBlockEnd blockEnd = getPsiElement(child2, OdinBlockEnd.class);

        if(switchCases == null || blockEnd == null)
            return false;

        List<OdinSwitchCase> switchCaseList = switchCases.getSwitchCaseList();
        if(switchCaseList.isEmpty())
            return false;

        OdinSwitchCase lastSwitchCase = switchCaseList.getLast();
        OdinCaseBlock caseBlock = lastSwitchCase.getCaseBlock();
        return caseBlock == null || caseBlock.getText().isBlank();
    }

    private <T extends PsiElement> T getPsiElement(Block block, Class<T> clazz) {
        if(block instanceof ASTBlock astBlock) {
            PsiElement psiElement = ASTBlock.getPsiElement(astBlock);
            if(clazz.isInstance(psiElement)) {
                return (T) psiElement;
            }
        }
        return null;
    }

    private static boolean endOfBlockIsNewLine(@Nullable Block child1, @NotNull Block child2) {
        if (child1 instanceof ASTBlock && child2 instanceof ASTBlock) {
            IElementType elementType2 = ASTBlock.getElementType(child2);
            if (elementType2 == OdinTypes.BLOCK_END) {
                int index = child1.getSubBlocks().size() - 1;
                if (index < 0) return false;
                Block lastBlockOfEnumBody = child1.getSubBlocks().get(index);
                PsiElement psiElement = ASTBlock.getPsiElement(lastBlockOfEnumBody);
                if (psiElement == null) return false;
                return psiElement.getText() == null || psiElement.getText().isBlank();
            }
        }
        return false;
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
        return myNode.getElementType().toString();
    }

    @Override
    protected @Nullable Indent getChildIndent() {
        return getChildAttributes(-1).getChildIndent();
    }

    @Override
    public @NotNull ChildAttributes getChildAttributes(int newChildIndex) {
        IElementType elementType = myNode.getElementType();
        if (typesToSmartIndent.contains(elementType)) {
            return new ChildAttributes(Indent.getNormalIndent(), null);
        }
        return new ChildAttributes(Indent.getNoneIndent(), null);
    }
}
