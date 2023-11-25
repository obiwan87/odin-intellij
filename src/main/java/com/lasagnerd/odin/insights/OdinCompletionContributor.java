package com.lasagnerd.odin.insights;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.ExpUiIcons;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.lasagnerd.odin.OdinIcons;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdinCompletionContributor extends CompletionContributor {
    public static final PsiElementPattern.@NotNull Capture<PsiElement> REFERENCE = psiElement().withElementType(OdinTypes.IDENTIFIER_TOKEN).afterLeaf(".");

    public static final @NotNull ElementPattern<PsiElement> AT_IDENTIFIER = psiElement().withElementType(OdinTypes.IDENTIFIER_TOKEN).andNot(REFERENCE);

    public OdinCompletionContributor() {

        // REFERENCE completion
        extend(CompletionType.BASIC,
                REFERENCE,
                new CompletionProvider<>() {

                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {

                        // Walk up tree until no more ref expressions are found
                        PsiElement position = parameters.getPosition();
                        PsiElement parent = PsiTreeUtil.findFirstParent(position, e -> e instanceof OdinRefExpression);

                        // This constitutes our scope

                        if (parent instanceof OdinRefExpression reference) {
                            Scope scope = OdinInsightUtils.findScope(reference, e -> true).with(parameters
                                    .getOriginalFile()
                                    .getContainingDirectory()
                                    .getVirtualFile()
                                    .getPath());

                            if (reference.getExpression() != null) {
                                Scope completionScope = OdinReferenceResolver.resolve(scope, reference.getExpression());
                                addLookUpElements(result, completionScope.getNamedElements());
                            }
                        }
                    }
                }
        );

        // Basic Completion
        extend(CompletionType.BASIC, AT_IDENTIFIER,
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement position = parameters.getPosition();


                        PsiElement parent = OdinInsightUtils.findFirstParentOfType(
                                position,
                                true,
                                OdinRefExpression.class);

                        if (parent != null) {
                            // Struct construction
                            OdinCompoundLiteral compoundLiteral
                                    = OdinInsightUtils.findFirstParentOfType(parent, true, OdinCompoundLiteral.class);

                            findCompletionsForStruct(result, compoundLiteral);
                        }

                        Scope declarations = OdinInsightUtils
                                .findScope(position, e -> true);
                        addLookUpElements(result, declarations.getNamedElements());
                    }
                }
        );

    }

    private static void addLookUpElements(@NotNull CompletionResultSet result, Collection<PsiNamedElement> namedElements) {

        for (var namedElement : namedElements) {
            if (namedElement instanceof PsiNameIdentifierOwner declaredIdentifier) {
                OdinTypeType typeType = OdinInsightUtils.classify(declaredIdentifier);
                Icon icon = switch (typeType) {
                    case STRUCT -> OdinIcons.Types.Struct;
                    case ENUM -> ExpUiIcons.Nodes.Enum;
                    case UNION -> OdinIcons.Types.Union;
                    case PROCEDURE, PROCEDURE_OVERLOAD -> ExpUiIcons.Nodes.Function;
                    case VARIABLE -> ExpUiIcons.Nodes.Variable;
                    case CONSTANT -> ExpUiIcons.Nodes.Constant;
                    case PACKAGE -> ExpUiIcons.Nodes.Package;
                    case FIELD -> ExpUiIcons.Nodes.Property;
                    case PARAMETER -> ExpUiIcons.Nodes.Parameter;
                    case UNKNOWN -> ExpUiIcons.FileTypes.Unknown;
                };


                if (typeType == OdinTypeType.PROCEDURE) {
                    LookupElementBuilder element = LookupElementBuilder
                            .create(declaredIdentifier.getText())
                            .withIcon(icon);

                    OdinProcedureDeclarationStatement firstParentOfType = OdinInsightUtils.findFirstParentOfType(declaredIdentifier, true, OdinProcedureDeclarationStatement.class);
                    element = procedureLookupElement(element, firstParentOfType).withInsertHandler(procedureInsertHandler());
                    result.addElement(PrioritizedLookupElement.withPriority(element, 0));
                } else if (typeType == OdinTypeType.PROCEDURE_OVERLOAD) {
                    OdinProcedureOverloadStatement procedureOverloadStatement = OdinInsightUtils.findFirstParentOfType(declaredIdentifier, true, OdinProcedureOverloadStatement.class);
                    for (OdinIdentifier odinIdentifier : procedureOverloadStatement.getIdentifierList()) {
                        var resolvedReference = odinIdentifier.getReference();

                        if (resolvedReference != null) {
                            PsiElement resolved = resolvedReference.resolve();
                            if (resolved instanceof OdinDeclaredIdentifier) {
                                OdinProcedureDeclarationStatement declaringProcedure = OdinInsightUtils.getDeclaringProcedure((OdinDeclaredIdentifier) resolved);
                                if (declaringProcedure != null) {
                                    LookupElementBuilder element = LookupElementBuilder.create(resolved, declaredIdentifier.getText())
                                            .withItemTextItalic(true)
                                            .withIcon(icon)
                                            .withInsertHandler(procedureInsertHandler());
                                    element = procedureLookupElement(element, declaringProcedure);
                                    result.addElement(PrioritizedLookupElement.withPriority(element, 0));
                                }
                            }
                        }
                    }
                } else if (typeType == OdinTypeType.PACKAGE) {
                    OdinImportDeclarationStatement odinDeclaration = OdinInsightUtils.findFirstParentOfType(declaredIdentifier, false, OdinImportDeclarationStatement.class);

                    ImportInfo info = odinDeclaration.getImportInfo();

                    LookupElementBuilder element = LookupElementBuilder.create(info.packageName())
                            .withIcon(ExpUiIcons.Nodes.Package)
                            .withTypeText(info.path());

                    if (info.library() != null) {
                        element = element.withTailText(" -> " + info.library());
                    }

                    result.addElement(PrioritizedLookupElement.withPriority(element, 100));
                } else {
                    LookupElementBuilder element = LookupElementBuilder.create(declaredIdentifier).withIcon(icon);
                    result.addElement(PrioritizedLookupElement.withPriority(element, 0));

                }
            }
        }
    }

    @NotNull
    private static InsertHandler<LookupElement> procedureInsertHandler() {
        return (insertionContext, lookupElement) -> {
            insertionContext.getDocument().insertString(insertionContext.getTailOffset(), "(");
            insertionContext.getDocument().insertString(insertionContext.getTailOffset(), ")");
            insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getTailOffset() - 1);
        };
    }

    @NotNull
    private static LookupElementBuilder procedureLookupElement(LookupElementBuilder element, OdinProcedureDeclarationStatement declaringProcedure) {
        var params = declaringProcedure.getProcedureType().getParamEntries();
        String tailText = "(";
        if (params != null) {
            tailText += params.getText();
        }
        tailText += ")";
        element = element.withTailText(tailText);

        OdinReturnParameters returnType = declaringProcedure.getProcedureType().getReturnParameters();
        if (returnType != null) {
            element = element.withTypeText(returnType
                    .getText());
        }
        return element;
    }

    private static void findCompletionsForStruct(@NotNull CompletionResultSet result, OdinCompoundLiteral compoundLiteral) {
        if (compoundLiteral == null || !(compoundLiteral.getTypeExpression() instanceof OdinTypeRef typeRef)) {
            return;
        }

        var identifierExpressionList = typeRef.getIdentifierList();
        var identifier = identifierExpressionList.get(0);
        PsiElement reference = Objects.requireNonNull(identifier.getReference()).resolve();

        if (reference == null || !(reference.getParent() instanceof OdinStructDeclarationStatement structDeclarationStatement)) {
            return;
        }

        String structName = structDeclarationStatement.getDeclaredIdentifier().getText();
        OdinStructBody structBody = structDeclarationStatement.getStructType().getStructBlock().getStructBody();
        if (structBody == null) {
            return;
        }

        List<OdinFieldDeclarationStatement> fieldDeclarationStatementList = structBody.getFieldDeclarationStatementList();

        for (OdinFieldDeclarationStatement fieldDeclaration : fieldDeclarationStatementList) {
            String typeOfField = fieldDeclaration.getTypeDefinition().getText();
            for (OdinDeclaredIdentifier declaredIdentifier : fieldDeclaration.getDeclaredIdentifierList()) {
                LookupElementBuilder element = LookupElementBuilder.create(declaredIdentifier)
                        .withIcon(ExpUiIcons.Nodes.Property)
                        .withBoldness(true)
                        .withTypeText(typeOfField)
                        .withTailText(" -> " + structName);

                result.addElement(PrioritizedLookupElement.withPriority(element, 100));
            }
        }
    }
}
