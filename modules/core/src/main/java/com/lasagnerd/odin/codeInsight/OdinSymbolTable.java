package com.lasagnerd.odin.codeInsight;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.codeInsight.dataflow.OdinSymbolValueStore;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinVisibility;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinScopeBlock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@AllArgsConstructor
@Getter
public class OdinSymbolTable {

    @Setter
    OdinScopeBlock scopeBlock;

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

    Map<String, List<OdinSymbol>> symbolTable = new HashMap<>();

    // Used for specialization
    Map<String, EvOdinValue> polyParaValueStorage = new HashMap<>();

    @With
    OdinSymbolValueStore symbolValueStore = new OdinSymbolValueStore();

    public OdinSymbolTable(String packagePath) {
        this.packagePath = packagePath;
    }


    public OdinSymbolTable() {

    }

    @Nullable
    public OdinSymbol getSymbol(String name) {
        List<OdinSymbol> symbols = symbolTable.get(name);
        if (symbols != null && !symbols.isEmpty())
            return symbols.getFirst();

        return parentSymbolTable != null ? parentSymbolTable.getSymbol(name) : null;
    }

    @NotNull
    public List<OdinSymbol> getSymbols(String name) {
        List<OdinSymbol> symbols = symbolTable.get(name);
        if (symbols == null) {
            return parentSymbolTable != null ? parentSymbolTable.getSymbols(name) : Collections.emptyList();
        }
        return symbols;
    }

    public EvOdinValue getValue(String name) {
        EvOdinValue value = polyParaValueStorage.get(name);
        if (value != null)
            return value;

        return parentSymbolTable != null ? parentSymbolTable.getValue(name) : null;
    }


    public Collection<PsiNamedElement> getNamedElements() {
        return symbolTable.values().stream()
                .filter(c -> !c.isEmpty())
                .map(List::getFirst)
                .map(OdinSymbol::getDeclaredIdentifier)
                .toList();
    }

    public Collection<OdinSymbol> getFilteredSymbols(Predicate<OdinSymbol> filter) {
        return symbolTable.values()
                .stream()
                .flatMap(List::stream)
                .filter(filter)
                .toList();
    }

    public Collection<OdinSymbol> getSymbols() {
        return symbolTable.values().stream().flatMap(Collection::stream).toList();
    }

    public Collection<OdinSymbol> getSymbols(OdinVisibility minVisibility) {
        return getFilteredSymbols(symbol -> symbol.getVisibility().compareTo(minVisibility) >= 0);
    }


    public void addAll(Collection<? extends OdinSymbol> symbols) {
        addAll(symbols, true);
    }

    public void addAll(Collection<? extends OdinSymbol> symbols, boolean override) {
        for (OdinSymbol symbol : symbols) {
            if (!symbolTable.containsKey(symbol.getName()) || override) {
                symbolTable.computeIfAbsent(symbol.getName(), s -> new ArrayList<>()).add(symbol);
            }
        }
    }

    public void merge(OdinSymbolTable context) {
        this.symbolTable.putAll(context.symbolTable);
        if (context.getParentSymbolTable() != null) {
            if (parentSymbolTable == null) {
                parentSymbolTable = new OdinSymbolTable();
            }
            parentSymbolTable.merge(context.getParentSymbolTable());
        }
    }

    public void add(OdinSymbol odinSymbol) {
        add(odinSymbol, true);
    }

    public void add(PsiNamedElement namedElement) {
        add(new OdinSymbol(namedElement));
    }

    public void add(OdinSymbol symbol, boolean override) {
        symbolTable.computeIfAbsent(symbol.getName(), s -> new ArrayList<>()).add(symbol);
    }

    public static OdinSymbolTable from(Collection<OdinSymbol> symbols) {
        if (symbols.isEmpty())
            return OdinSymbolTable.EMPTY;

        OdinSymbolTable newContext = new OdinSymbolTable();
        for (var symbol : symbols) {
            newContext.symbolTable
                    .computeIfAbsent(symbol.getName(), s -> new ArrayList<>())
                    .add(symbol);
        }

        return newContext;
    }


    public static OdinSymbolTable from(List<OdinSymbol> identifiers, String packagePath) {
        OdinSymbolTable newContext = from(identifiers);
        newContext.packagePath = packagePath;

        return newContext;
    }

    public OdinSymbolTable with(List<OdinSymbol> identifiers) {
        OdinSymbolTable newContext = from(identifiers);
        newContext.packagePath = this.packagePath;

        return newContext;
    }

    public OdinSymbolTable with(String packagePath) {
        OdinSymbolTable newContext = new OdinSymbolTable();
        newContext.symbolTable = this.symbolTable;
        newContext.packagePath = packagePath;
        newContext.parentSymbolTable = parentSymbolTable;
        return newContext;
    }

    public OdinSymbolTable flatten() {
        OdinSymbolTable odinContext = new OdinSymbolTable();
        odinContext.setPackagePath(packagePath);

        OdinSymbolTable curr = this;
        do {
            odinContext.addAll(curr.getSymbols(), false);
            curr = curr.getParentSymbolTable();
        } while (curr != null);

        return odinContext;
    }

    public void setRoot(OdinSymbolTable context) {
        if (parentSymbolTable != null) {
            parentSymbolTable.setRoot(context);
        } else {
            if (context != this) {
                parentSymbolTable = context;
            }
        }
    }

    public void addAll(Map<String, EvOdinValue> builtInValues) {
        polyParaValueStorage.putAll(builtInValues);
    }

    public boolean isShadowing(String name) {
        return this.symbolTable.get(name) != null &&
                (parentSymbolTable != null && parentSymbolTable.getSymbol(name) != null);
    }


    public Collection<List<OdinSymbol>> values() {
        return symbolTable.values();
    }

    public OdinContext asContext() {
        OdinContext odinContext = new OdinContext();
        odinContext.symbolTable = this;
        return odinContext;
    }
}
