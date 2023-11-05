package com.lasagnerd.odin.insights;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.impl.CompletionSorterImpl;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.icons.ExpUiIcons;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.lasagnerd.odin.OdinIcons;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class OdinCompletionContributor extends CompletionContributor {
    public static final PsiElementPattern.@NotNull Capture<PsiElement> AT_IDENTIFIER = PlatformPatterns.psiElement()
            .withElementType(OdinTypes.IDENTIFIER);

    public static final PsiElementPattern.@NotNull Capture<PsiElement> REFERENCE = PlatformPatterns.psiElement(
    ).withAncestor(3, PlatformPatterns.psiElement(OdinDotReference.class));

    public OdinCompletionContributor() {
        extend(CompletionType.BASIC,
                REFERENCE,
                new CompletionProvider<>() {

                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                        System.out.println("hello from reference completion");

                        PsiElement position = parameters.getPosition();

                        // This constitutes our scope
                        OdinReferencePrimary reference = OdinInsightUtils.findFirstParentOfType(position, true, OdinReferencePrimary.class);
                        if (reference != null) {
                            if (reference.getPrimary() instanceof OdinOperandPrimary operandPrimary) {
                                if (operandPrimary.getExpression() instanceof OdinIdentifierExpression identifierExpression) {
                                    OdinDeclaredIdentifier identifierReference = (OdinDeclaredIdentifier) Objects.requireNonNull(identifierExpression.getReference())
                                            .resolve();
                                    OdinVariableInitializationStatement initialization = OdinInsightUtils.findFirstParentOfType(identifierReference,
                                            true,
                                            OdinVariableInitializationStatement.class);

                                    OdinExpression odinExpression = initialization.getExpressionsList().getExpressionList().get(0);
                                    OdinCompoundLiteral compoundLiteral = PsiTreeUtil.findChildOfType(odinExpression, OdinCompoundLiteral.class);

                                    findCompletionsForStruct(result, compoundLiteral);
                                }
                            }
                        }
                    }
                }
        );

        extend(CompletionType.BASIC,
                AT_IDENTIFIER.andNot(REFERENCE),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement position = parameters.getPosition();
                        PsiElement parent = OdinInsightUtils.findFirstParentOfType(
                                position,
                                true,
                                OdinPrimaryExpression.class);

                        if (parent != null) {
                            // Struct construction
                            OdinCompoundLiteral compoundLiteral
                                    = OdinInsightUtils.findFirstParentOfType(parent, true, OdinCompoundLiteral.class);

                            findCompletionsForStruct(result, compoundLiteral);

                            // Declarations in scope
                            List<PsiElement> declarations = OdinInsightUtils
                                    .findDeclarations(position, e -> true);

                            for (PsiElement declaration : declarations) {
                                if (declaration instanceof PsiNameIdentifierOwner declaredIdentifier) {
                                    Icon icon = switch (OdinInsightUtils.classify(declaredIdentifier)) {
                                        case STRUCT -> OdinIcons.Types.Struct;
                                        case ENUM -> ExpUiIcons.Nodes.Enum;
                                        case UNION -> OdinIcons.Types.Union;
                                        case PROCEDURE -> ExpUiIcons.Nodes.Function;
                                        case VARIABLE -> ExpUiIcons.Nodes.Variable;
                                        case CONSTANT -> ExpUiIcons.Nodes.Constant;
                                        case UNKNOWN -> ExpUiIcons.FileTypes.Unknown;
                                    };

                                    LookupElementBuilder element = LookupElementBuilder.create(declaredIdentifier).withIcon(icon);
                                    result.addElement(PrioritizedLookupElement.withPriority(element, 0));
                                }
                            }
                        }

                    }


                }


        );

    }

    private static void findCompletionsForStruct(@NotNull CompletionResultSet result, OdinCompoundLiteral compoundLiteral) {
        if (compoundLiteral != null && compoundLiteral.getType() instanceof OdinQualifiedNameType type) {
            List<OdinIdentifierExpression> identifierExpressionList = type.getIdentifierExpressionList();
            if (identifierExpressionList.size() == 1) {
                OdinIdentifierExpression identifier = identifierExpressionList.get(0);
                PsiElement reference = Objects.requireNonNull(identifier.getReference()).resolve();

                if (reference != null && reference.getParent() instanceof OdinStructDeclarationStatement structDeclarationStatement) {
                    String structName = structDeclarationStatement.getDeclaredIdentifier().getText();
                    OdinStructBody structBody = structDeclarationStatement.getStructSpec().getStructBlock().getStructBody();
                    if (structBody != null) {
                        List<OdinFieldDeclarationStatement> fieldDeclarationStatementList = structBody.getFieldDeclarationStatementList();
                        for (OdinFieldDeclarationStatement fieldDeclaration : fieldDeclarationStatementList) {
                            String typeOfField = fieldDeclaration.getTypeDefinitionExpression().getText();
                            for (OdinDeclaredIdentifier declaredIdentifier : fieldDeclaration.getDeclaredIdentifierList()) {
                                LookupElementBuilder element = LookupElementBuilder.create((PsiNameIdentifierOwner) declaredIdentifier)
                                        .withIcon(ExpUiIcons.Nodes.Property)
                                        .withBoldness(true)
                                        .withTypeText(typeOfField)
                                        .withTailText(" -> " + structName);

                                result.addElement(PrioritizedLookupElement.withPriority(element, 100));
                            }
                        }
                    }
                }
            }
        }
    }
}
