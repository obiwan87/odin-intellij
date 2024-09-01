package com.lasagnerd.odin.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerEditorsExtensionBase;
import com.lasagnerd.odin.lang.psi.OdinStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinDebuggerEditorsExtension extends CidrDebuggerEditorsExtensionBase {
	@Override
	protected @Nullable PsiElement getContext(@NotNull Project project, @NotNull XSourcePosition sourcePosition) {
		PsiElement context = super.getContext(project, sourcePosition);
		return ancestorOrSelf(context, OdinStatement.class);
	}

	@Override
	protected @NotNull PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull String text, @NotNull PsiElement context, @NotNull EvaluationMode mode) {
		return super.createExpressionCodeFragment(project, text, context, mode);
	}

	/**
	 * Finds the nearest ancestor of a specified type, including the element itself.
	 */
	private @Nullable <T extends PsiElement> T ancestorOrSelf(@Nullable PsiElement element, @NotNull Class<T> cls) {
		return PsiTreeUtil.getParentOfType(element, cls, false);
	}
}
