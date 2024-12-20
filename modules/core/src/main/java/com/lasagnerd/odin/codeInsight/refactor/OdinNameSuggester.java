package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.symbolTable.OdinSymbolTableHelper;
import com.lasagnerd.odin.lang.psi.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OdinNameSuggester {
    private static final List<String> GETTER_PREFIXES = Arrays.asList(
            "get", "get_", "retrieve", "retrieve_", "compute", "compute_",
            "fetch", "fetch_", "find", "find_", "calculate", "calculate_",
            "determine", "determine_", "generate", "generate_", "produce",
            "produce_", "load", "load_", "create", "create_", "new", "new_"
    );

    static List<String> getNameSuggestions(OdinExpression targetExpression) {
        return getNameSuggestions(targetExpression, Collections.emptyList());
    }

    static List<String> getNameSuggestions(OdinExpression targetExpression, List<String> blacklist) {
        List<String> names = new ArrayList<>();

        // Get the biggest scope this expression would be in. This way we can avoid clashes with other names
        OdinSymbolTable symbolTable = null;
        OdinScopeBlock scopeBlock = PsiTreeUtil.getParentOfType(targetExpression, OdinScopeBlock.class);
        if (scopeBlock != null) {
            OdinStatementList statementList;
            if (!(scopeBlock instanceof OdinStatementList odinStatementList)) {
                statementList = PsiTreeUtil.findChildOfType(scopeBlock, OdinStatementList.class);
            } else {
                statementList = odinStatementList;
            }
            if (statementList != null) {
                PsiElement lastChild = statementList.getLastChild();
                symbolTable = OdinSymbolTableHelper.buildFullSymbolTable(lastChild, new OdinContext());
            }
        }

        if (symbolTable == null) {
            symbolTable = OdinSymbolTableHelper.buildFullSymbolTable(targetExpression, new OdinContext());
        }

        if (targetExpression instanceof OdinRefExpression refExpression) {
            OdinIdentifier identifier = refExpression.getIdentifier();
            if (identifier != null) {
                String name = identifier.getText();
                addName(symbolTable, name, names, blacklist);
            }
        }

        if (targetExpression instanceof OdinCompoundLiteralExpression compoundLiteralExpression) {
            var  type = compoundLiteralExpression.getCompoundLiteral().getTypeContainer();
            if (type.getType() instanceof OdinQualifiedType qualifiedType) {
                String text = qualifiedType.getTypeIdentifier().getText();
                addName(symbolTable, text, names, blacklist);
            }

            if (type.getType() instanceof OdinSimpleRefType simpleRefType) {
                String text = simpleRefType.getIdentifier().getText();
                addName(symbolTable, text, names, blacklist);
            }
        }

        if (targetExpression instanceof OdinCallExpression odinCallExpression) {
            String text = odinCallExpression.getText();
            if (odinCallExpression.getExpression() instanceof OdinRefExpression odinRefExpression) {
                if (odinRefExpression.getIdentifier() != null) {
                    text = odinRefExpression.getIdentifier().getText();
                }

                if (odinRefExpression.getType() != null) {
                    text = odinRefExpression.getType().getText();
                }
            }
            String name = suggestVariableForProcedure(normalizeName(text));
            addName(symbolTable, name, names, blacklist);
        }

        if (names.isEmpty()) {
            addName(symbolTable, "value", names, blacklist);
        }

        if (names.isEmpty()) {
            names.add("value");
        }

        return names;
    }

    private static void addName(OdinSymbolTable symbolTable, String name, List<String> names, List<String> blacklist) {
        name = normalizeName(name);
        if (symbolTable.getSymbol(name) == null && !names.contains(name) && !blacklist.contains(name)) {
            names.add(name);
        } else {
            String originalName = name;

            for (int i = 1; i <= 9; i++) {
                name = originalName + i;
                if (symbolTable.getSymbol(name) == null && !names.contains(name) && !blacklist.contains(name)) {
                    names.add(name);
                    break;
                }
            }
        }
    }

    public static String normalizeName(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty");
        }

        // Step 1: Initial normalization (to lowercase, replacing invalid characters)
        StringBuilder normalized = new StringBuilder();
        boolean firstChar = true;

        for (char c : input.toCharArray()) {
            if (firstChar) {
                if (Character.isJavaIdentifierStart(c)) {
                    normalized.append(c);
                } else {
                    normalized.append('o'); // Replace invalid start character with 'o'
                }
                firstChar = false;
            } else {
                if (Character.isJavaIdentifierPart(c)) {
                    if (Character.isWhitespace(c)) {
                        normalized.append('_');
                    } else {
                        normalized.append(c);
                    }
                } else {
                    normalized.append('_');
                }
            }
        }

        // Ensure the result is valid by trimming leading or trailing underscores
        String result = normalized.toString().replaceAll("_+", "_").replaceAll("^_|_$", "");

        // Step 2: CamelCase to SnakeCase conversion
        result = convertCamelCaseToSnakeCase(result);

        // If the result is empty or invalid, provide a fallback name
        if (result.isEmpty() || !Character.isJavaIdentifierStart(result.charAt(0))) {
            return "odin_name";
        }

        return result.toLowerCase();
    }

    public static boolean isValidIdentifier(String input) {
        return input.equals(normalizeName(input));
    }

    private static String convertCamelCaseToSnakeCase(String input) {
        StringBuilder snakeCase = new StringBuilder();
        char[] chars = input.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c) && i > 0) {
                // Add underscore before uppercase letters except at the start
                snakeCase.append('_');
            }
            snakeCase.append(Character.toLowerCase(c));
        }

        return snakeCase.toString();
    }

    public static String suggestVariableForProcedure(String procedureName) {
        if (procedureName == null || procedureName.isEmpty()) {
            throw new IllegalArgumentException("Procedure name cannot be null or empty");
        }

        // Check for common prefixes and remove them if found
        String suggestedName = procedureName;
        for (String prefix : GETTER_PREFIXES) {
            if (procedureName.toLowerCase().startsWith(prefix.toLowerCase())) {
                suggestedName = procedureName.substring(prefix.length());
                break;
            }
        }

        // Normalize the remaining name
        return normalizeName(suggestedName);
    }
}
