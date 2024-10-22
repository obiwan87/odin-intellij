package com.lasagnerd.odin.codeInsight.annotators;

import com.intellij.lang.annotation.AnnotationSession;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class OdinAnnotationUtils {

    public static @NotNull <T> T getUserData(AnnotationSession session, Key<T> key, Supplier<T> initValue) {
        T value = session.getUserData(key);
        if (value == null) {
            value = initValue.get();
            session.putUserData(key, value);
        }
        return value;
    }
}
