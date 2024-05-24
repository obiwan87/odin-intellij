package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.NotNull;

class ProcedureInsertHandler implements InsertHandler<LookupElement> {
    @Override
    public void handleInsert(@NotNull InsertionContext insertionContext, @NotNull LookupElement lookupElement) {
        insertionContext.getDocument().insertString(insertionContext.getTailOffset(), "(");
        insertionContext.getDocument().insertString(insertionContext.getTailOffset(), ")");
        insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getTailOffset() - 1);
        // commit document
        PsiDocumentManager.getInstance(insertionContext.getProject()).commitDocument(insertionContext.getDocument());
    }
}
