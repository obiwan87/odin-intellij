package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.util.CommonProcessors;

import java.util.Collection;

public class CancellableCollectProcessor<T> extends CommonProcessors.CollectProcessor<T> {
    public CancellableCollectProcessor(Collection<T> collection) {
        super(collection);
    }

    @Override
    public boolean process(T s) {
        ProgressManager.checkCanceled();
        return super.process(s);
    }
}