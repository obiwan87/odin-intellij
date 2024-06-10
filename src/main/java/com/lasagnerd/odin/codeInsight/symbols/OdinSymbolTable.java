package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import com.lasagnerd.odin.lang.psi.OdinImportDeclarationStatement;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@Getter
public class OdinSymbolTable {

    @Setter
    OdinSymbolTable parentSymbolTable;

    public OdinSymbolTable(String packagePath) {
        this.packagePath = packagePath;
    }

    @Setter
    private String packagePath;
    public static final OdinSymbolTable EMPTY = new OdinSymbolTable();
    Map<String, OdinSymbol> symbolNameMap = new HashMap<>();

    /**
     * Used substituting polymorphic types. The key
     * is the name of the polymorphic type
     */
    Map<String, TsOdinType> typeTable = new HashMap<>();

    /**
     * Acts as a cache for already defined types.
     */
    Map<OdinDeclaredIdentifier, TsOdinType> knownTypes = new HashMap<>();

    public OdinSymbolTable() {

    }


    @Nullable
    public OdinSymbol getSymbol(String name) {

        OdinSymbol odinSymbol = symbolNameMap.get(name);
        if(odinSymbol != null)
            return odinSymbol;

        return parentSymbolTable != null? parentSymbolTable.getSymbol(name) : null;
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
            return parentSymbolTable != null? parentSymbolTable.getType(polymorphicParameter) : null;
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

    public Collection<OdinSymbol> getSymbols(OdinSymbol.OdinVisibility minVisibility) {
        return getFilteredSymbols(symbol -> symbol.getVisibility().compareTo(minVisibility) >= 0);
    }


    public void addAll(Collection<? extends OdinSymbol> symbols) {
        addAll(symbols, true);
    }

    public void addAll(Collection<? extends OdinSymbol> symbols, boolean override) {
        for (OdinSymbol symbol : symbols) {
            if(!symbolNameMap.containsKey(symbol.getName()) || !override) {
                symbolNameMap.put(symbol.getName(), symbol);
            }
        }
    }

    public void putAll(OdinSymbolTable symbolTable) {
        this.symbolNameMap.putAll(symbolTable.symbolNameMap);
        typeTable.putAll(symbolTable.typeTable);
        knownTypes.putAll(symbolTable.knownTypes);
        if(symbolTable.getParentSymbolTable() != null) {
            if(parentSymbolTable == null) {
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
        if(!override)
            symbolNameMap.put(symbol.getName(), symbol);
        else if(!symbolNameMap.containsKey(symbol.getName())) {
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

    /**
     * Creates a new scope for a given package identifier defined within this scope.
     *
     * @param packageIdentifier The identifier that is used to reference the package
     * @return A new scope with all the declared symbols of the referenced package
     */

    public OdinSymbolTable getScopeOfImport(String packageIdentifier) {
        OdinSymbol odinSymbol = symbolNameMap.get(packageIdentifier);
        if (odinSymbol != null) {
            PsiNamedElement declaredIdentifier = odinSymbol.getDeclaredIdentifier();
            OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(declaredIdentifier, OdinDeclaration.class);
            if (odinDeclaration instanceof OdinImportDeclarationStatement importDeclarationStatement) {
                return OdinImportUtils.getSymbolsOfImportedPackage(this.getPackagePath(), importDeclarationStatement);
            }
        }
        return OdinSymbolTable.EMPTY;
    }

    public OdinSymbolTable flatten() {
        OdinSymbolTable odinSymbolTable = new OdinSymbolTable();
        odinSymbolTable.setPackagePath(packagePath);

        OdinSymbolTable curr = this;
        do {
            odinSymbolTable.addAll(curr.getSymbols(), false);
            curr = curr.getParentSymbolTable();
        } while(curr != null);

        return odinSymbolTable;
    }
}
