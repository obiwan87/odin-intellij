package com.lasagnerd.odin.lang;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.ElementManipulator;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinIdentifier;
import com.lasagnerd.odin.lang.psi.OdinPsiElementFactory;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinElementManipulators extends AbstractElementManipulator<OdinIdentifier> {


    @Override
    public @Nullable OdinIdentifier handleContentChange(@NotNull OdinIdentifier element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        final StringBuilder replacement = new StringBuilder(element.getIdentifierToken().getText());
        final int valueOffset = element.getIdentifierToken().getTextRange().getStartOffset() - element.getTextOffset();

        replacement.replace(
                range.getStartOffset() - valueOffset,
                range.getEndOffset() - valueOffset,
                newContent
        );

        OdinPsiElementFactory odinPsiElementFactory = OdinPsiElementFactory.getInstance(element.getProject());
        OdinIdentifier newIdentifier = odinPsiElementFactory.createIdentifier(replacement.toString());

        ASTNode currentIdentifierToken = element.getNode().findChildByType(OdinTypes.IDENTIFIER_TOKEN);
        ASTNode newIdentifierToken = newIdentifier.getNode().findChildByType(OdinTypes.IDENTIFIER_TOKEN);
        if (currentIdentifierToken != null && newIdentifierToken != null) {
            element.getNode().replaceChild(currentIdentifierToken, newIdentifierToken);
        }

        return newIdentifier;
    }
}
