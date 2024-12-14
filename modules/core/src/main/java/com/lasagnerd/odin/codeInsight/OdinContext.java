package com.lasagnerd.odin.codeInsight;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.dataflow.OdinSymbolValueStore;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinVisibility;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinIdentifier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@AllArgsConstructor
@Getter
public class OdinContext {
    public static final OdinContext EMPTY = new OdinContext();

    private Set<OdinIdentifier> visitedIdentifiers = new HashSet<>();

    OdinSymbolTable symbolTable = new OdinSymbolTable();

    // Used for specialization
    Map<String, EvOdinValue> polymorphicValues = new HashMap<>();

    /**
     * Used substituting polymorphic types. The key
     * is the name of the polymorphic type
     */
    Map<String, TsOdinType> polymorphicTypes = new HashMap<>();

    @With
    OdinSymbolValueStore symbolValueStore = new OdinSymbolValueStore();

    public OdinContext(String packagePath) {
        this.symbolTable = new OdinSymbolTable(packagePath);
    }

    public String getPackagePath() {
        return getSymbolTable().getPackagePath();
    }

    /**
     * Acts as a cache for already defined types. Prevents stackoverflow in circular references
     */
    Map<OdinDeclaredIdentifier, TsOdinType> knownTypes = new HashMap<>();

    Map<TsOdinType, Map<List<? extends PsiElement>, TsOdinType>> specializedTypes = new HashMap<>();

    public OdinContext() {

    }

    public TsOdinType getSpecializedType(TsOdinType genericType, List<PsiElement> arguments) {
        Map<List<? extends PsiElement>, TsOdinType> argumentsMap = this.specializedTypes.get(genericType);
        if (argumentsMap != null) {
            return argumentsMap.get(arguments);
        }
        return null;
    }

    public void addSpecializedType(TsOdinType genericType, TsOdinType specializedType, List<PsiElement> arguments) {
        specializedTypes.computeIfAbsent(genericType, t -> new HashMap<>()).put(arguments, specializedType);
    }

    @Nullable
    public OdinSymbol getSymbol(String name) {
        return symbolTable.getSymbol(name);
    }

    @NotNull
    public List<OdinSymbol> getSymbols(String name) {
        return symbolTable.getSymbols(name);
    }

    public EvOdinValue getPolymorphicValue(String name) {
        return polymorphicValues.get(name);

    }

    public TsOdinType getPolymorphicType(String polymorphicParameter) {
        return polymorphicTypes.get(polymorphicParameter);
    }

    public void addPolymorphicType(String typeName, TsOdinType type) {
        polymorphicTypes.put(typeName, type);
    }

    public void addTypes(OdinContext context) {
        polymorphicTypes.putAll(context.polymorphicTypes);
    }

    public Collection<OdinSymbol> getSymbols() {
        return symbolTable.getSymbols();
    }

    public Collection<OdinSymbol> getSymbols(OdinVisibility minVisibility) {
        return symbolTable.getSymbols(minVisibility);
    }

    public void merge(OdinContext context) {
        this.symbolTable.merge(context.symbolTable);
        polymorphicTypes.putAll(context.polymorphicTypes);
        knownTypes.putAll(context.knownTypes);
        specializedTypes.putAll(context.specializedTypes);

    }

    public void addKnownType(OdinDeclaredIdentifier declaredIdentifier, TsOdinType type) {
        knownTypes.put(declaredIdentifier, type);
    }

    public void setPackagePath(String packagePath) {
        symbolTable.setPackagePath(packagePath);
    }
}
