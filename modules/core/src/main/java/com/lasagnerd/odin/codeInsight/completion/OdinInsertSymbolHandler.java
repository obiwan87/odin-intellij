package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.hint.ShowParameterInfoHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.lang.psi.OdinCallExpression;
import org.jetbrains.annotations.NotNull;

class OdinInsertSymbolHandler implements InsertHandler<LookupElement> {
    private final OdinSymbolType symbolType;
    private final String prefix;

    OdinInsertSymbolHandler(OdinSymbolType symbolType) {
        this(symbolType, "");
    }

    OdinInsertSymbolHandler(OdinSymbolType symbolType, String prefix) {
        this.symbolType = symbolType;
        this.prefix = prefix != null ? prefix : "";
    }

    @Override
    public void handleInsert(@NotNull InsertionContext insertionContext, @NotNull LookupElement lookupElement) {
        Document document = insertionContext.getDocument();
        if (!prefix.isBlank()) {
            document.insertString(insertionContext.getStartOffset(), prefix);
        }
        if (isProcedure()) {

            document.insertString(insertionContext.getTailOffset(), "(");

            document.insertString(insertionContext.getTailOffset(), ")");
            insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getTailOffset() - 1);
        }
        // commit document
        PsiDocumentManager.getInstance(insertionContext.getProject())
                .commitDocument(document);
        if (isProcedure()) {
            showParameterHints(insertionContext, document);
        }
    }

    private static void showParameterHints(@NotNull InsertionContext insertionContext, Document document) {
        int lbraceOffset = insertionContext.getEditor().getCaretModel().getOffset();
        PsiFile psiFile = PsiDocumentManager.getInstance(insertionContext.getProject())
                .getPsiFile(document);
        if (psiFile != null) {
            PsiElement element = psiFile.findElementAt(lbraceOffset);
            if(element != null) {
                OdinCallExpression callExpression = PsiTreeUtil.getParentOfType(element, false, OdinCallExpression.class);
                if(callExpression == null)
                    return;
                ShowParameterInfoHandler.invoke(
                        insertionContext.getProject(),
                        insertionContext.getEditor(),
                        insertionContext.getFile(),
                        lbraceOffset, callExpression, false
                        );
            }
        }
    }

    private boolean isProcedure() {
        return symbolType == OdinSymbolType.PROCEDURE || symbolType == OdinSymbolType.PROCEDURE_OVERLOAD;
    }
}
