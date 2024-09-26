package com.lasagnerd.odin.lang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.psi.*;
import com.intellij.psi.impl.BlockSupportImpl;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.impl.DiffLog;
import com.intellij.psi.impl.source.tree.ForeignLeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.util.containers.ContainerUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTableResolver;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
class OdinPsiTestHelpers {
    static <T extends TsOdinType> void assertExpressionIsOfTypeWithName(OdinFile odinFile, String procedure, String variableName, Class<T> aClass, String name) {
        TsOdinType tsOdinType = inferFirstRightHandExpressionOfVariable(odinFile, procedure, variableName);
        T structType = UsefulTestCase.assertInstanceOf(tsOdinType, aClass);
        TestCase.assertEquals(name, structType.getName());
    }

    // Helpers
    static @NotNull List<OdinBlockStatement> getProcedureBlocks(OdinProcedureDefinition procedureDefinition) {
        OdinStatementList statementList = Objects
                .requireNonNull(procedureDefinition.getProcedureBody().getBlock())
                .getStatementList();
        List<OdinBlockStatement> blocks = new ArrayList<>();
        for (OdinStatement odinStatement : Objects.requireNonNull(statementList).getStatementList()) {
            if (odinStatement instanceof OdinBlockStatement blockStatement) {
                blocks.add(blockStatement);
            }
        }
        return blocks;
    }

    static TsOdinType inferTypeOfDeclaration(OdinDeclaration declaration) {
        return OdinInferenceEngine.resolveTypeOfDeclaration(null,
                OdinSymbolTableResolver.computeSymbolTable(declaration),
                declaration.getDeclaredIdentifiers().getFirst(),
                declaration);
    }

    static void assertTopMostRefExpressionTextEquals(PsiElement odinStatement, String expected, String identifierName) {
        OdinRefExpression topMostRefExpression = getTopMostRefExpression(odinStatement, identifierName);
        TestCase.assertNotNull(topMostRefExpression);
        TestCase.assertEquals(expected, topMostRefExpression.getText());
    }

    static @NotNull OdinRefExpression getTopMostRefExpression(PsiElement odinStatement, String identifierName) {
        Collection<OdinIdentifier> odinIdentifiers = PsiTreeUtil.findChildrenOfType(odinStatement, OdinIdentifier.class);
        OdinIdentifier identifier = odinIdentifiers.stream().filter(s -> s.getIdentifierToken().getText().equals(identifierName)).findFirst().orElseThrow();
        TestCase.assertNotNull(identifier);
        OdinRefExpression odinRefExpression = UsefulTestCase.assertInstanceOf(identifier.getParent(), OdinRefExpression.class);

        OdinRefExpression topMostRefExpression = OdinInsightUtils.findTopMostRefExpression(odinRefExpression);
        TestCase.assertNotNull(topMostRefExpression);
        return topMostRefExpression;
    }

    static TsOdinType inferFirstRightHandExpressionOfVariable(OdinFile odinFile, String procedureName, String variableName) {
        OdinExpression odinExpression = findFirstExpressionOfVariable(odinFile, procedureName, variableName);
        return OdinInferenceEngine.doInferType(odinExpression);
    }

    static OdinExpression findFirstExpressionOfVariable(OdinFile odinFile, String procedureName, String variableName) {
        var shapeVariable = findFirstVariableDeclarationStatement(odinFile, procedureName,
                variableName);
        return Objects.requireNonNull(shapeVariable.getRhsExpressions()).getExpressionList().getFirst();
    }

    static @NotNull OdinProcedureDefinition findFirstProcedure(@NotNull OdinFile odinFile, String procedureName) {
        Collection<OdinProcedureLiteralType> procedureDeclarationStatements = PsiTreeUtil.findChildrenOfType(odinFile.getFileScope(),
                OdinProcedureLiteralType.class);

        return procedureDeclarationStatements.stream()
                .filter(procedureDefinition -> PsiTreeUtil.getParentOfType(procedureDefinition, OdinConstantInitializationStatement.class) != null)
                .filter(p -> {
                    OdinConstantInitializationStatement constantInitializationStatement = PsiTreeUtil.getParentOfType(p, OdinConstantInitializationStatement.class);
                    assert constantInitializationStatement != null;
                    return Objects.equals(constantInitializationStatement.getDeclaredIdentifiers().getFirst().getName(), procedureName);
                })
                .map(OdinProcedureLiteralType::getProcedureDefinition)
                .findFirst().orElseThrow();
    }

    static @NotNull OdinVariableInitializationStatement findFirstVariableDeclarationStatement(OdinFile odinFile, String procedureName, String variableName) {
        OdinProcedureDefinition procedure = findFirstProcedure(odinFile, procedureName);
        TestCase.assertNotNull(procedure);
        return findFirstVariable(procedure, variableName);
    }

    static @NotNull OdinVariableInitializationStatement findFirstVariable(PsiElement parent, String variableName) {
        Collection<OdinVariableInitializationStatement> vars = PsiTreeUtil.findChildrenOfType(parent, OdinVariableInitializationStatement.class);

        OdinVariableInitializationStatement variable = vars.stream()
                .filter(v -> v.getDeclaredIdentifiers().stream().anyMatch(d -> Objects.equals(d.getName(), variableName)))
                .findFirst().orElse(null);
        TestCase.assertNotNull(variable);

        return variable;
    }

    static @NotNull OdinVariableDeclarationStatement findFirstVariableDeclaration(PsiElement parent, String variableName) {
        Collection<OdinVariableDeclarationStatement> vars = PsiTreeUtil.findChildrenOfType(parent, OdinVariableDeclarationStatement.class);

        OdinVariableDeclarationStatement variable = vars.stream()
                .filter(v -> v.getDeclaredIdentifiers().stream().anyMatch(d -> Objects.equals(d.getName(), variableName)))
                .findFirst().orElse(null);
        TestCase.assertNotNull(variable);

        return variable;
    }

    static @NotNull TsOdinType inferTypeOfFirstExpressionInProcedure(OdinFile odinFile, String procedureName) {
        OdinProcedureDefinition procedure = findFirstProcedure(odinFile, procedureName);
        OdinExpressionStatement odinExpressionStatement =
                (OdinExpressionStatement) Objects.requireNonNull(procedure
                                .getProcedureBody()
                                .getBlock())
                        .getStatements()
                        .stream()
                        .filter(s -> s instanceof OdinExpressionStatement)
                        .findFirst()
                        .orElseThrow();

        OdinExpression expression = odinExpressionStatement.getExpression();
        OdinSymbolTable symbolTable = OdinSymbolTableResolver.computeSymbolTable(Objects.requireNonNull(expression));
        return OdinInferenceEngine.inferType(symbolTable, expression);
    }

    static void doSanityChecks(PsiFile root) {
        TestCase.assertEquals("psi text mismatch", root.getViewProvider().getContents().toString(), root.getText());
        ensureParsed(root);
        ensureCorrectReparse(root);
        checkRangeConsistency(root);
    }

    static void checkRangeConsistency(PsiFile file) {
        file.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof ForeignLeafPsiElement) return;

                try {
                    ensureNodeRangeConsistency(element, file);
                } catch (Throwable e) {
                    throw new AssertionError("In " + element + " of " + element.getClass(), e);
                }
                super.visitElement(element);
            }

            private void ensureNodeRangeConsistency(PsiElement parent, PsiFile file) {
                int parentOffset = parent.getTextRange().getStartOffset();
                int childOffset = 0;
                ASTNode child = parent.getNode().getFirstChildNode();
                if (child != null) {
                    while (child != null) {
                        int childLength = checkChildRangeConsistency(file, parentOffset, childOffset, child);
                        childOffset += childLength;
                        child = child.getTreeNext();
                    }
                    TestCase.assertEquals(childOffset, parent.getTextLength());
                }
            }

            private int checkChildRangeConsistency(PsiFile file, int parentOffset, int childOffset, ASTNode child) {
                TestCase.assertEquals(child.getStartOffsetInParent(), childOffset);
                TestCase.assertEquals(child.getStartOffset(), childOffset + parentOffset);
                int childLength = child.getTextLength();
                TestCase.assertEquals(TextRange.from(childOffset + parentOffset, childLength), child.getTextRange());
                if (!(child.getPsi() instanceof ForeignLeafPsiElement)) {
                    TestCase.assertEquals(child.getTextRange().substring(file.getText()), child.getText());
                }
                return childLength;
            }
        });
    }

    static void printAstTypeNamesTree(ASTNode node, StringBuffer buffer, int indent) {
        buffer.append(" ".repeat(indent));
        buffer.append(node.getElementType()).append("\n");
        indent += 2;
        ASTNode childNode = node.getFirstChildNode();

        while (childNode != null) {
            printAstTypeNamesTree(childNode, buffer, indent);
            childNode = childNode.getTreeNext();
        }
    }

    public static void doCheckResult(@NotNull String testDataDir,
                                     @NotNull PsiFile file,
                                     boolean checkAllPsiRoots,
                                     @NotNull String targetDataName,
                                     boolean skipSpaces,
                                     boolean printRanges) {
        doCheckResult(testDataDir, file, checkAllPsiRoots, targetDataName, skipSpaces, printRanges, false);
    }

    public static void doCheckResult(@NotNull String testDataDir,
                                     @NotNull PsiFile file,
                                     boolean checkAllPsiRoots,
                                     @NotNull String targetDataName,
                                     boolean skipSpaces,
                                     boolean printRanges,
                                     boolean allTreesInSingleFile) {
        FileViewProvider provider = file.getViewProvider();
        Set<Language> languages = provider.getLanguages();

        if (!checkAllPsiRoots || languages.size() == 1) {
            doCheckResult(testDataDir, targetDataName + ".txt", toParseTreeText(file, skipSpaces, printRanges).trim());
            return;
        }

        if (allTreesInSingleFile) {
            String expectedName = targetDataName + ".txt";
            StringBuilder sb = new StringBuilder();
            List<Language> languagesList = new ArrayList<>(languages);
            ContainerUtil.sort(languagesList, Comparator.comparing(Language::getID));
            for (Language language : languagesList) {
                sb.append("Subtree: ").append(language.getDisplayName()).append(" (").append(language.getID()).append(")").append("\n")
                        .append(toParseTreeText(provider.getPsi(language), skipSpaces, printRanges).trim())
                        .append("\n").append(StringUtil.repeat("-", 80)).append("\n");
            }
            doCheckResult(testDataDir, expectedName, sb.toString());
        } else {
            for (Language language : languages) {
                PsiFile root = provider.getPsi(language);
                TestCase.assertNotNull("FileViewProvider " + provider + " didn't return PSI root for language " + language.getID(), root);
                String expectedName = targetDataName + "." + language.getID() + ".txt";
                doCheckResult(testDataDir, expectedName, toParseTreeText(root, skipSpaces, printRanges).trim());
            }
        }
    }

    public static void doCheckResult(@NotNull String fullPath, @NotNull String targetDataName, @NotNull String actual) {
        String expectedFileName = fullPath + File.separatorChar + targetDataName;
        UsefulTestCase.assertSameLinesWithFile(expectedFileName, actual);
    }

    protected static String toParseTreeText(@NotNull PsiElement file, boolean skipSpaces, boolean printRanges) {
        return DebugUtil.psiToString(file, !skipSpaces, printRanges);
    }

    public static String loadFileDefault(@NotNull String dir, @NotNull String name) throws IOException {
        return FileUtil.loadFile(new File(dir, name), CharsetToolkit.UTF8, true).trim();
    }

    public static void ensureParsed(@NotNull PsiFile file) {
        file.accept(new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                element.acceptChildren(this);
            }
        });
    }

    public static void ensureCorrectReparse(@NotNull final PsiFile file) {
        final String psiToStringDefault = DebugUtil.psiToString(file, true, false);

        DebugUtil.performPsiModification("ensureCorrectReparse", () -> {
            final String fileText = file.getText();
            final DiffLog diffLog = new BlockSupportImpl().reparseRange(
                    file, file.getNode(), TextRange.allOf(fileText), fileText, new EmptyProgressIndicator(), fileText);
            diffLog.performActualPsiChange(file);
        });

        TestCase.assertEquals(psiToStringDefault, DebugUtil.psiToString(file, true, false));
    }
}
