package com.lasagnerd.odin.documentation;

import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.lang.Language;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.ColorUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.sdk.OdinSdkService;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.OdinLanguage;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OdinDocumentationProvider extends AbstractDocumentationProvider {
    @Override
    public @Nullable @Nls String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (!(element instanceof OdinDeclaredIdentifier declaredIdentifier))
            return null;


        OdinDeclaration declaration = PsiTreeUtil.getParentOfType(declaredIdentifier, OdinDeclaration.class);
        if (declaration == null) {
            return null;
        }

        String declarationText = "";
        if (declaration instanceof OdinFieldDeclaration) {
            declarationText += "field ";
        }

        declarationText += declaredIdentifier.getName();

        if (declaration instanceof OdinFieldDeclaration fieldDeclarationStatement) {
            declarationText += ": " + fieldDeclarationStatement.getType().getText();
        }

        if (declaration instanceof OdinConstantInitializationStatement) {
            declarationText += " :: ";
        }

        if (declaration instanceof OdinInitVariableDeclaration initVariableDeclaration) {
            int index = initVariableDeclaration.getDeclaredIdentifiers().indexOf(declaredIdentifier);
            OdinExpression odinExpression = initVariableDeclaration.getRhsExpressions().getExpressionList().get(index);
            String type;
            if (initVariableDeclaration.getType() == null) {
                TsOdinType tsOdinType = odinExpression.getInferredType();
                type = tsOdinType.getLabel();
            } else {
                type = initVariableDeclaration.getType().getText();
            }
            declarationText += " : " + type + " = " + odinExpression.getText();
        }


        OdinType declaredType = OdinInsightUtils.getDeclaredType(declaredIdentifier);
        if (declaredType instanceof OdinProcedureLiteralType procedureLiteralType) {
            declarationText += procedureLiteralType.getProcedureDefinition().getProcedureSignature().getText();
        } else if (declaredType != null) {
            declarationText += declaredType.getText();
        } else if (declaration instanceof OdinConstantInitializationStatement constantInitializationStatement) {
            int index = constantInitializationStatement.getDeclaredIdentifiers().indexOf(declaredIdentifier);
            OdinExpression odinExpression = constantInitializationStatement.getExpressionList().get(index);
            String name = declaredIdentifier.getName();

            if (name != null && name.startsWith("ODIN_")) {
                EvOdinValue value = OdinSdkService.getInstance(declaration.getProject()).getValue(name);
                if (value != null && !value.isNull() && value.getValue() != null) {
                    declarationText += value.getValue() + " (evaluated)";
                } else {
                    declarationText += "NULL (evaluated)";
                }
            } else {
                declarationText += odinExpression.getText();
            }
        }

        StringBuilder builder = new StringBuilder();
        Language language = OdinLanguage.INSTANCE; // Your language instance
        SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(language, element.getProject(), element.getContainingFile().getVirtualFile());

        builder.append(highlightCode(syntaxHighlighter, declarationText));

        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(declaration);

        builder.append(DocumentationMarkup.CONTENT_START);

        List<String> comments = new ArrayList<>();
        while (prevVisibleLeaf instanceof PsiComment && PsiUtilCore.getElementType(prevVisibleLeaf) == OdinTypes.LINE_COMMENT) {

            String text = prevVisibleLeaf.getText().replaceAll("^//", "").trim();
            comments.add(text);
            comments.add("<br>");
            prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
        }

        builder.append(String.join("\n ", comments.reversed()));
        builder.append(DocumentationMarkup.CONTENT_END);
        return builder.toString();
    }

    private String highlightCode(SyntaxHighlighter syntaxHighlighter, String code) {

        // Prepare the scheme for highlighting
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        @NotNull EditorHighlighter editorHighlighter = HighlighterFactory.createHighlighter(syntaxHighlighter, scheme);
        editorHighlighter.setText(code);

        HighlighterIterator iterator = editorHighlighter.createIterator(0);

        HtmlBuilder htmlBuilder = new HtmlBuilder().append(HtmlChunk.raw("<pre>"));

        while (!iterator.atEnd()) {
            TextAttributes textAttributes = iterator.getTextAttributes();
            String fragment = code.substring(iterator.getStart(), iterator.getEnd());

            String colorStyle = getColorStyle(textAttributes);
            htmlBuilder.append(HtmlChunk.raw("<span style=\"" + colorStyle + "\">" + StringUtil.escapeXmlEntities(fragment) + "</span>"));

            iterator.advance();
        }

        htmlBuilder.append(HtmlChunk.raw("</pre>"));
        return htmlBuilder.toString();
    }

    private String getColorStyle(TextAttributes textAttributes) {
        // Convert the TextAttributes (color, font style, etc.) into inline CSS styles
        StringBuilder style = new java.lang.StringBuilder();

        if (textAttributes.getForegroundColor() != null) {
            style.append("color: #")
                    .append(ColorUtil.toHex(textAttributes.getForegroundColor())).append(";");
        }
        if (textAttributes.getFontType() == Font.PLAIN) {
            style.append("font-style: normal;");
        } else if (textAttributes.getFontType() == Font.ITALIC) {
            style.append("font-style: italic;");
        }
        if ((textAttributes.getFontType() & Font.BOLD) != 0) {
            style.append("font-weight: bold;");
        }

        return style.toString();
    }

}
