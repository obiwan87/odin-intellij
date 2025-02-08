package com.lasagnerd.odin.languageInjection;

import com.intellij.lang.Language;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.lang.psi.OdinConstantInitializationStatement;
import com.lasagnerd.odin.lang.psi.OdinInitVariableStatement;
import com.lasagnerd.odin.lang.psi.OdinStatementList;
import com.lasagnerd.odin.lang.psi.OdinStringLiteral;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdinLanguageInjector implements LanguageInjector {

    @Override
    public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost host,
                                     @NotNull InjectedLanguagePlaces injectionPlacesRegistrar) {
        if (!host.isValidHost())
            return;

        if (!(host instanceof OdinStringLiteral stringLiteral)) {
            return;
        }

        PsiElement context = OdinInsightUtils.findParentOfType(
                stringLiteral,
                true,
                new Class<?>[]{
                        OdinInitVariableStatement.class,
                        OdinConstantInitializationStatement.class
                },
                new Class<?>[]{
                        OdinStatementList.class
                }
        );

        if (context != null) {
            PsiComment comment = PsiTreeUtil.getPrevSiblingOfType(context, PsiComment.class);
            if (comment != null) {
                String text = comment.getText();
                if (text.startsWith("//")) {
                    String trimmedComment = text.replaceAll("^//\\s+", "").trim();
                    Pattern languageAssignmentPattern = Pattern.compile("language=(.*)");
                    Matcher matcher = languageAssignmentPattern.matcher(trimmedComment);
                    if (matcher.find()) {
                        String languageId = matcher.group(1);
                        Language language = Language.findLanguageByID(languageId);
                        if (language != null) {
                            injectionPlacesRegistrar.addPlace(language, TextRange.create(1, stringLiteral.getTextLength() - 1), null, null);
                        }
                    }
                }
            }
        }
    }
}
