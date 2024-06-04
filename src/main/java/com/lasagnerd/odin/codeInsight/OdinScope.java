package com.lasagnerd.odin.codeInsight;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinImportDeclarationStatement;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@Getter
public class OdinScope {

    @Setter
    OdinScope parentScope;

    @Setter
    private String packagePath;
    public static final OdinScope EMPTY = new OdinScope();
    Map<String, OdinSymbol> symbolTable = new HashMap<>();

    /**
     * Used substituting polymorphic types. The key
     * is the name of the polymorphic type
     */
    Map<String, TsOdinType> typeTable = new HashMap<>();

    /**
     * Acts as a cache for already defined types.
     */
    Map<OdinDeclaredIdentifier, TsOdinType> knownTypes = new HashMap<>();

    public OdinScope() {

    }


    @Nullable
    public OdinSymbol getSymbol(String name) {

        OdinSymbol odinSymbol = symbolTable.get(name);
        if(odinSymbol != null)
            return odinSymbol;

        return parentScope != null? parentScope.getSymbol(name) : null;
    }

    @Nullable
    public PsiNamedElement getNamedElement(String name) {
        OdinSymbol odinSymbol = getSymbol(name);
        if(odinSymbol != null)
            return odinSymbol.getDeclaredIdentifier();
        return null;
    }
    public TsOdinType getType(String polymorphicParameter) {
        TsOdinType tsOdinType = typeTable.get(polymorphicParameter);
        if(tsOdinType == null) {
            return parentScope != null? parentScope.getType(polymorphicParameter) : null;
        }
        return tsOdinType;
    }

    public void addType(String typeName, TsOdinType type) {
        typeTable.put(typeName, type);
    }

    public void addTypes(OdinScope scope) {
        typeTable.putAll(scope.typeTable);
    }

    public Collection<PsiNamedElement> getNamedElements() {
        return symbolTable.values().stream().map(OdinSymbol::getDeclaredIdentifier).toList();
    }

    public Collection<OdinSymbol> getFilteredSymbols(Predicate<OdinSymbol> filter) {
        return symbolTable.values().stream().filter(filter).toList();
    }

    public Collection<OdinSymbol> getSymbols() {
        return symbolTable.values();
    }

    public Collection<OdinSymbol> getSymbols(OdinSymbol.OdinVisibility minVisibility) {
        return getFilteredSymbols(symbol -> symbol.getVisibility().compareTo(minVisibility) >= 0);
    }


    public void addAll(Collection<? extends OdinSymbol> symbols) {
        addAll(symbols, true);
    }

    public void addAll(Collection<? extends OdinSymbol> symbols, boolean override) {
        for (OdinSymbol symbol : symbols) {
            if(!symbolTable.containsKey(symbol.getName()) || !override) {
                symbolTable.put(symbol.getName(), symbol);
            }
        }
    }

    public void putAll(OdinScope scope) {
        symbolTable.putAll(scope.symbolTable);
        typeTable.putAll(scope.typeTable);
        knownTypes.putAll(scope.knownTypes);
        if(scope.getParentScope() != null) {
            if(parentScope == null) {
                parentScope = new OdinScope();
            }
            parentScope.putAll(scope.getParentScope());
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
        if(!override)
            symbolTable.put(symbol.getName(), symbol);
        else if(!symbolTable.containsKey(symbol.getName())) {
            symbolTable.put(symbol.getName(), symbol);
        }

    }

    static OdinScope from(Collection<OdinSymbol> symbols) {
        if (symbols.isEmpty())
            return OdinScope.EMPTY;

        OdinScope scope = new OdinScope();
        for (var declaredIdentifier : symbols) {
            scope.symbolTable.put(declaredIdentifier.getName(), declaredIdentifier);
        }

        return scope;
    }


    static OdinScope from(List<OdinSymbol> identifiers, String packagePath) {
        OdinScope scope = from(identifiers);
        scope.packagePath = packagePath;

        return scope;
    }

    public OdinScope with(List<OdinSymbol> identifiers) {
        OdinScope scope = from(identifiers);
        scope.packagePath = this.packagePath;

        return scope;
    }

    public OdinScope with(String packagePath) {
        OdinScope scope = new OdinScope();
        scope.symbolTable = this.symbolTable;
        scope.packagePath = packagePath;
        scope.parentScope = parentScope;
        return scope;
    }

    /**
     * Creates a new scope for a given package identifier defined within this scope.
     *
     * @param packageIdentifier The identifier that is used to reference the package
     * @return A new scope with all the declared symbols of the referenced package
     */

    public OdinScope getScopeOfImport(String packageIdentifier) {
        OdinSymbol odinSymbol = symbolTable.get(packageIdentifier);
        if (odinSymbol != null && odinSymbol.getDeclaredIdentifier() instanceof OdinImportDeclarationStatement importDeclarationStatement) {
            return OdinImportUtils.getSymbolsOfImportedPackage(this.getPackagePath(), importDeclarationStatement);
        }
        return OdinScope.EMPTY;
    }

}
