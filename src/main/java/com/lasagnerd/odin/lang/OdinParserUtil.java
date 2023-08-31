package com.lasagnerd.odin.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.lang.psi.OdinTypes;

public class OdinParserUtil {
    public static boolean closingBracket(PsiBuilder builder, int level) {
        IElementType iElementType = builder.lookAhead(1);
        return iElementType == OdinTypes.RPAREN || iElementType == OdinTypes.RBRACE;
    }

    public static boolean _eof(PsiBuilder builder, int level) {
        return builder.eof();
    }
}
