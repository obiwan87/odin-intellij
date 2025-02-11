package com.lasagnerd.odin.debugger.renderers;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerCommandException;
import com.jetbrains.cidr.execution.debugger.backend.EvaluationContext;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrPhysicalValue;
import com.jetbrains.cidr.execution.debugger.evaluation.ValueRendererFactory;
import com.jetbrains.cidr.execution.debugger.evaluation.renderers.ValueRenderer;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.symbolTable.OdinSymbolTableHelper;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinSliceType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.debugger.dapDrivers.OdinEvaluationContext;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class OdinRendererFactory implements ValueRendererFactory {
    @Override
    public @Nullable ValueRenderer createRenderer(@NotNull FactoryContext factoryContext) {
        CidrPhysicalValue physicalValue = factoryContext.getPhysicalValue();
        XSourcePosition sourcePosition = physicalValue.getSourcePosition();

        if (sourcePosition == null) {
            return null;
        }
        Project project = physicalValue.getProcess().getProject();
        Objects.requireNonNull(project);

        VirtualFile file = sourcePosition.getFile();
        TsOdinType identifierType = ReadAction.compute(() -> {
            PsiManager psiManager = PsiManager.getInstance(project);
            PsiFile psiFile = psiManager.findFile(file);
            if (!(psiFile instanceof OdinFile odinFile)) {
                return null;
            }

            PsiElement elementAtPosition = odinFile.findElementAt(sourcePosition.getOffset());
            if (elementAtPosition == null) {
                return null;
            }

            PsiElement visibleLeaf = PsiTreeUtil.nextVisibleLeaf(elementAtPosition);
            if (visibleLeaf == null) {
                return null;
            }

            OdinSymbolTable symbolTable = OdinSymbolTableHelper.buildFullSymbolTable(visibleLeaf, new OdinContext());
            var symbol = symbolTable.getSymbol(physicalValue.getName());
            if (symbol == null) {
                return null;
            }

            PsiNamedElement psiNamedElement = symbol.getDeclaredIdentifier();
            if (!(psiNamedElement instanceof OdinDeclaredIdentifier declaredIdentifier)) {
                return null;
            }

            return declaredIdentifier.getType(new OdinContext());
        });

        if (identifierType != null && identifierType.baseType() instanceof TsOdinSliceType sliceType) {
            return new OdinSliceRenderer(physicalValue, sliceType);
        }

        return null;
    }

    private static class OdinSliceRenderer extends ValueRenderer {

        private final TsOdinSliceType sliceType;

        public OdinSliceRenderer(@NotNull CidrPhysicalValue value, TsOdinSliceType sliceType) {
            super(value);
            this.sliceType = sliceType;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        protected void doComputeChildren(@NotNull EvaluationContext context,
                                         @NotNull XCompositeNode container,
                                         int fromIndex,
                                         @Nullable Integer totalCount) throws ExecutionException, DebuggerCommandException {
            if (!(context instanceof OdinEvaluationContext odinEvaluationContext)) {
                return;
            }

            String sliceOperator = ",";
            LLValue lengthValue = context.evaluate(myValue.getName() + ".len");
            @Nullable Long len = lengthValue.getAddress();
            LLValue results = context.evaluate("%s.data%s %d".formatted(myValue.getName(), sliceOperator, len));
            LLValue llValue = new LLValue(
                    "elements",
                    results.getType(),
                    results.getDisplayType(),
                    results.getAddress(),
                    results.getTypeClass(),
                    results.getReferenceExpression(),
                    results.getFullExpression());

            for (@NotNull Key key : results.get().getKeys()) {
                Object userData = results.getUserData(key);
                llValue.putUserData(key, userData);
            }

            this.addChildrenTo(List.of(llValue),
                    context,
                    container,
                    0,
                    false);

            super.doComputeChildren(context, container, fromIndex, totalCount);
        }

    }
}

