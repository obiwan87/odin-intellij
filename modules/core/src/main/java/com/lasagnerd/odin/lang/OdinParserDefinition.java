package com.lasagnerd.odin.lang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;

public class OdinParserDefinition implements ParserDefinition {

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
        return OdinSyntaxHighlighter.COMMENT_TOKENS;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return OdinSyntaxHighlighter.STRING_LITERAL_ELEMENTS;
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
        return OdinSyntaxHighlighter.WHITE_SPACES;
    }
}
