package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.lasagnerd.odin.codeInsight.OdinTypeType;
import org.jetbrains.annotations.NotNull;

class OdinInsertSymbolHandler implements InsertHandler<LookupElement> {
    private final OdinTypeType typeType;
    private final String prefix;

    OdinInsertSymbolHandler(String prefix) {
        this(null, prefix);
    }

    OdinInsertSymbolHandler(OdinTypeType typeType) {
        this(typeType, "");
    }

    OdinInsertSymbolHandler(OdinTypeType typeType, String prefix) {
        this.typeType = typeType;
        this.prefix = prefix != null ? prefix : "";
    }

    @Override
    public void handleInsert(@NotNull InsertionContext insertionContext, @NotNull LookupElement lookupElement) {
        Document document = insertionContext.getDocument();
        if(!prefix.isBlank()) {
            document.insertString(insertionContext.getStartOffset(), prefix);
        }
        if (typeType == OdinTypeType.PROCEDURE || typeType == OdinTypeType.PROCEDURE_OVERLOAD) {

            document.insertString(insertionContext.getTailOffset(), "(");

            document.insertString(insertionContext.getTailOffset(), ")");
            insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getTailOffset() - 1);
        }
        // commit document
        PsiDocumentManager.getInstance(insertionContext.getProject()).commitDocument(document);
    }
}
