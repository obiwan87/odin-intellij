package com.lasagnerd.odin.lang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("TokenSetInParserDefinition")
public class OdinParserDefinition implements ParserDefinition {
    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE, OdinTypes.NEW_LINE);

    public static final @NotNull TokenSet COMMENT_TOKENS = TokenSet.create(OdinTypes.LINE_COMMENT, OdinTypes.BLOCK_COMMENT, OdinTypes.MULTILINE_BLOCK_COMMENT);
    public static final @NotNull TokenSet STRING_LITERAL_ELEMENTS = TokenSet.create(OdinTypes.DQ_STRING_LITERAL, OdinTypes.RAW_STRING_LITERAL, OdinTypes.SQ_STRING_LITERAL);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new OdinLexerAdapter();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new OdinParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return OdinFileElementType.INSTANCE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return COMMENT_TOKENS;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return STRING_LITERAL_ELEMENTS;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return OdinTypes.Factory.createElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new OdinFile(viewProvider);
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return WHITE_SPACES;
    }
}
