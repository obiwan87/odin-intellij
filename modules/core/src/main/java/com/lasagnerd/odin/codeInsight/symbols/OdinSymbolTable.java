package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinScopeBlock;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@Getter
public class OdinSymbolTable {

    @Setter
    OdinScopeBlock containingElement;

    @Setter
    Map<OdinDeclaration, List<OdinSymbol>> declarationSymbols = new HashMap<>();

    @Setter
    OdinSymbolTable parentSymbolTable;


    public List<OdinSymbol> getDeclarationSymbols(@NotNull OdinDeclaration declaration) {
        return declarationSymbols.getOrDefault(declaration, Collections.emptyList());
    }

    @Setter
    private String packagePath;
    public static final OdinSymbolTable EMPTY = new OdinSymbolTable();

    Map<String, OdinSymbol> symbolNameMap = new HashMap<>();
    Map<String, EvOdinValue> valueStorage = new HashMap<>();


    public OdinSymbolTable(String packagePath) {
        this.packagePath = packagePath;
    }


    /**
     * Used substituting polymorphic types. The key
     * is the name of the polymorphic type
     */
    Map<String, TsOdinType> typeTable = new HashMap<>();

    /**
     * Acts as a cache for already defined types.
     */
    Map<OdinDeclaredIdentifier, TsOdinType> knownTypes = new HashMap<>();

    Map<TsOdinType, Map<List<? extends PsiElement>, TsOdinType>> specializedTypes = new HashMap<>();

    public OdinSymbolTable() {

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

        OdinSymbol odinSymbol = symbolNameMap.get(name);
        if (odinSymbol != null)
            return odinSymbol;

        return parentSymbolTable != null ? parentSymbolTable.getSymbol(name) : null;
    }

    public EvOdinValue getValue(String name) {
        EvOdinValue value = valueStorage.get(name);
        if (value != null)
            return value;

        return parentSymbolTable != null ? parentSymbolTable.getValue(name) : null;
    }

    @Nullable
    public PsiNamedElement getNamedElement(String name) {
        OdinSymbol odinSymbol = getSymbol(name);
        if (odinSymbol != null)
            return odinSymbol.getDeclaredIdentifier();
        return null;
    }

    public TsOdinType getType(String polymorphicParameter) {
        TsOdinType tsOdinType = typeTable.get(polymorphicParameter);
        if (tsOdinType == null) {
            return parentSymbolTable != null ? parentSymbolTable.getType(polymorphicParameter) : null;
        }
        return tsOdinType;
    }

    public void addType(String typeName, TsOdinType type) {
        typeTable.put(typeName, type);
    }

    public void addTypes(OdinSymbolTable symbolTable) {
        typeTable.putAll(symbolTable.typeTable);
    }

    public Collection<PsiNamedElement> getNamedElements() {
        return symbolNameMap.values().stream().map(OdinSymbol::getDeclaredIdentifier).toList();
    }

    public Collection<OdinSymbol> getFilteredSymbols(Predicate<OdinSymbol> filter) {
        return symbolNameMap.values().stream().filter(filter).toList();
    }

    public Collection<OdinSymbol> getSymbols() {
        return symbolNameMap.values();
    }

    public Collection<OdinSymbol> getSymbols(OdinVisibility minVisibility) {
        return getFilteredSymbols(symbol -> symbol.getVisibility().compareTo(minVisibility) >= 0);
    }


    public void addAll(Collection<? extends OdinSymbol> symbols) {
        addAll(symbols, true);
    }

    public void addAll(Collection<? extends OdinSymbol> symbols, boolean override) {
        for (OdinSymbol symbol : symbols) {
            if (!symbolNameMap.containsKey(symbol.getName()) || override) {
                symbolNameMap.put(symbol.getName(), symbol);
            }
        }
    }

    public void putAll(OdinSymbolTable symbolTable) {
        this.symbolNameMap.putAll(symbolTable.symbolNameMap);
        typeTable.putAll(symbolTable.typeTable);
        knownTypes.putAll(symbolTable.knownTypes);
        specializedTypes.putAll(symbolTable.specializedTypes);
        if (symbolTable.getParentSymbolTable() != null) {
            if (parentSymbolTable == null) {
                parentSymbolTable = new OdinSymbolTable();
            }
            parentSymbolTable.putAll(symbolTable.getParentSymbolTable());
        }
    }

    public void addKnownType(OdinDeclaredIdentifier declaredIdentifier, TsOdinType type) {
        knownTypes.put(declaredIdentifier, type);
    }

    public void add(OdinSymbol odinSymbol) {
        add(odinSymbol, true);
    }

    public void add(PsiNamedElement namedElement) {
        add(new OdinSymbol(namedElement));
    }

    public void add(OdinSymbol symbol, boolean override) {
        if (!override)
            symbolNameMap.put(symbol.getName(), symbol);
        else if (!symbolNameMap.containsKey(symbol.getName())) {
            symbolNameMap.put(symbol.getName(), symbol);
        }

    }

    public static OdinSymbolTable from(Collection<OdinSymbol> symbols) {
        if (symbols.isEmpty())
            return OdinSymbolTable.EMPTY;

        OdinSymbolTable newSymbolTable = new OdinSymbolTable();
        for (var declaredIdentifier : symbols) {
            newSymbolTable.symbolNameMap.put(declaredIdentifier.getName(), declaredIdentifier);
        }

        return newSymbolTable;
    }


    public static OdinSymbolTable from(List<OdinSymbol> identifiers, String packagePath) {
        OdinSymbolTable newSymbolTable = from(identifiers);
        newSymbolTable.packagePath = packagePath;

        return newSymbolTable;
    }

    public OdinSymbolTable with(List<OdinSymbol> identifiers) {
        OdinSymbolTable newSymbolTable = from(identifiers);
        newSymbolTable.packagePath = this.packagePath;

        return newSymbolTable;
    }

    public OdinSymbolTable with(String packagePath) {
        OdinSymbolTable newSymbolTable = new OdinSymbolTable();
        newSymbolTable.symbolNameMap = this.symbolNameMap;
        newSymbolTable.packagePath = packagePath;
        newSymbolTable.parentSymbolTable = parentSymbolTable;
        return newSymbolTable;
    }

    public OdinSymbolTable flatten() {
        OdinSymbolTable odinSymbolTable = new OdinSymbolTable();
        odinSymbolTable.setPackagePath(packagePath);

        OdinSymbolTable curr = this;
        do {
            odinSymbolTable.addAll(curr.getSymbols(), false);
            curr = curr.getParentSymbolTable();
        } while (curr != null);

        return odinSymbolTable;
    }

    public void setRoot(OdinSymbolTable symbolTable) {
        if (parentSymbolTable != null) {
            parentSymbolTable.setRoot(symbolTable);
        } else {
            if (symbolTable != this) {
                parentSymbolTable = symbolTable;
            }
        }
    }

    public void addAll(Map<String, EvOdinValue> builtInValues) {
        valueStorage.putAll(builtInValues);
    }

    public boolean isShadowing(String name) {
        return this.symbolNameMap.get(name) != null &&
                (parentSymbolTable != null && parentSymbolTable.getSymbol(name) != null);
    }


}
