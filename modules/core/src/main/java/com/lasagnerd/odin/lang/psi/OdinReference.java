package com.lasagnerd.odin.lang.psi;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.LineColumn;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTableResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinReference extends PsiReferenceBase<OdinIdentifier> {
    public static Logger LOG = Logger.getInstance(OdinReference.class);

    public OdinReference(@NotNull OdinIdentifier element) {
        super(element);
    }

    @Override
    public @NotNull TextRange getAbsoluteRange() {
        return super.getAbsoluteRange();
    }

    @Override
    public @Nullable PsiElement resolve() {
        try {
            OdinSymbol firstDeclaration = OdinSymbolTableResolver.findSymbol(getElement());
            if (firstDeclaration != null) {
                return firstDeclaration.getDeclaredIdentifier();
            }
            return null;
        } catch (StackOverflowError e) {
            logStackOverFlowError(getElement(), LOG);
            return null;
        }
    }

    public static void logStackOverFlowError(@NotNull OdinIdentifier element, Logger log) {
        String text = element.getText();
        int textOffset = element.getTextOffset();
        PsiFile containingFile = element.getContainingFile();
        String fileName = "UNKNOWN";
        if (containingFile != null) {
            VirtualFile virtualFile = containingFile.getVirtualFile();
            if (virtualFile != null) {
                fileName = virtualFile.getCanonicalPath();
            }
            LineColumn lineColumn = StringUtil.offsetToLineColumn(containingFile.getText(), textOffset);
            log.error("Stack overflow caused by element with text '%s' in %s:%d:%d".formatted(text,
                    fileName,
                    lineColumn.line + 1,
                    lineColumn.column + 1));
        } else {
            log.error("Stack overflow caused by element with text '%s'".formatted(text));
        }
    }

    @Override
    public Object @NotNull [] getVariants() {
        return new Object[0];
    }
}
