package com.lasagnerd.odin.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombinedInsertHandler implements InsertHandler<LookupElement> {
    private final List<InsertHandler<LookupElement>> handlers = new ArrayList<>();

    @SafeVarargs
    public CombinedInsertHandler(InsertHandler<LookupElement>... handlers) {
        this.handlers.addAll(Arrays.asList(handlers));
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
        handlers.forEach(handler -> handler.handleInsert(context, item));
    }
}
