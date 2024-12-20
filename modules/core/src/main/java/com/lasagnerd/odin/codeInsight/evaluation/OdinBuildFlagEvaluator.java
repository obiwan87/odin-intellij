package com.lasagnerd.odin.codeInsight.evaluation;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.lasagnerd.odin.codeInsight.dataflow.OdinSymbolValueStore;
import com.lasagnerd.odin.codeInsight.sdk.OdinSdkService;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinEnumType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Evaluates build flags to actual constant values. E.g.
 * #+build linux, darwin is set, then it evaluates to ODIN_OS being
 * a set of (.Linux, .Darwin)
 */
public class OdinBuildFlagEvaluator {
    private static final Map<String, Function<Project, OdinSymbolValue>> VALUES = new HashMap<>();
    private static final Map<String, Function<Project, OdinSymbolValue>> OS_VALUES = new HashMap<>();
    private static final Map<String, Function<Project, OdinSymbolValue>> ARCH_VALUES = new HashMap<>();

    public static final String ODIN_ARCH = "ODIN_ARCH";
    public static final String ODIN_OS = "ODIN_OS";

    public static final String ODIN_OS_TYPE = "Odin_OS_Type";

    public static final String ODIN_ARCH_TYPE = "Odin_Arch_Type";

    static {

        OS_VALUES.put("windows", project -> getSdkEnumValue(project, ODIN_OS, ODIN_OS_TYPE, "Windows"));
        OS_VALUES.put("darwin", project -> getSdkEnumValue(project, ODIN_OS, ODIN_OS_TYPE, "Darwin"));
        OS_VALUES.put("linux", project -> getSdkEnumValue(project, ODIN_OS, ODIN_OS_TYPE, "Linux"));
        OS_VALUES.put("essence", project -> getSdkEnumValue(project, ODIN_OS, ODIN_OS_TYPE, "Essence"));
        OS_VALUES.put("freebsd", project -> getSdkEnumValue(project, ODIN_OS, ODIN_OS_TYPE, "FreeBSD"));
        OS_VALUES.put("openbsd", project -> getSdkEnumValue(project, ODIN_OS, ODIN_OS_TYPE, "OpenBSD"));
        OS_VALUES.put("netbsd", project -> getSdkEnumValue(project, ODIN_OS, ODIN_OS_TYPE, "NetBSD"));
        OS_VALUES.put("haiku", project -> getSdkEnumValue(project, ODIN_OS, ODIN_OS_TYPE, "Haiku"));
        OS_VALUES.put("wasi", project -> getSdkEnumValue(project, ODIN_OS, ODIN_OS_TYPE, "WASI"));
        OS_VALUES.put("js", project -> getSdkEnumValue(project, ODIN_OS, ODIN_OS_TYPE, "JS"));
        OS_VALUES.put("orca", project -> getSdkEnumValue(project, ODIN_OS, ODIN_OS_TYPE, "Orca"));
        OS_VALUES.put("freestanding", project -> getSdkEnumValue(project, ODIN_OS, ODIN_OS_TYPE, "Freestanding"));

        ARCH_VALUES.put("amd64", project -> getSdkEnumValue(project, ODIN_ARCH, ODIN_ARCH_TYPE, "amd64"));
        ARCH_VALUES.put("i386", project -> getSdkEnumValue(project, ODIN_ARCH, ODIN_ARCH_TYPE, "i386"));
        ARCH_VALUES.put("arm32", project -> getSdkEnumValue(project, ODIN_ARCH, ODIN_ARCH_TYPE, "arm32"));
        ARCH_VALUES.put("arm64", project -> getSdkEnumValue(project, ODIN_ARCH, ODIN_ARCH_TYPE, "arm64"));
        ARCH_VALUES.put("wasm32", project -> getSdkEnumValue(project, ODIN_ARCH, ODIN_ARCH_TYPE, "wasm32"));
        ARCH_VALUES.put("wasm64p32", project -> getSdkEnumValue(project, ODIN_ARCH, ODIN_ARCH_TYPE, "wasm64p32"));
        ARCH_VALUES.put("riscv64", project -> getSdkEnumValue(project, ODIN_ARCH, ODIN_ARCH_TYPE, "riscv64"));

        VALUES.putAll(OS_VALUES);
        VALUES.putAll(ARCH_VALUES);
    }

    record OdinSymbolValue(OdinSymbol symbol, EvOdinValue value) {

    }

    private static @NotNull OdinSymbolValue getSdkEnumValue(Project project, String constantName, String enumName, String value) {
        OdinSdkService sdkService = OdinSdkService.getInstance(project);
        TsOdinType type = sdkService.getType(enumName);
        TsOdinEnumType enumType = (TsOdinEnumType) type.dereference().baseType(true);
        EvEnumValue enumValue = OdinExpressionEvaluator.getEnumValue(enumType, value);
        OdinSymbol symbol = sdkService.getBuiltinSymbol(constantName);
        return new OdinSymbolValue(symbol, new EvOdinValue(enumValue, enumType));
    }

    public Map<OdinSymbol, EvOdinValueSet> evaluate(List<OdinBuildFlagClause> buildFlagClauses) {
        Map<OdinSymbol, EvOdinValueSet> conjunctions = new HashMap<>();
        for (OdinBuildFlagClause buildFlagClause : buildFlagClauses) {
            Map<OdinSymbol, EvOdinValueSet> buildFlagClauseValues = evaluateBuildFlagClause(buildFlagClause);
            processConjunctions(buildFlagClauseValues, conjunctions);
        }

        return conjunctions;
    }

    public Map<OdinSymbol, EvOdinValueSet> evaluateBuildFlagClause(OdinBuildFlagClause buildFlagClause) {
        Map<OdinSymbol, EvOdinValueSet> disjunctions = new HashMap<>();

        // OR's
        for (OdinBuildFlagArgument buildFlagArgument : buildFlagClause.getBuildFlagArgumentList()) {
            // AND's
            Map<OdinSymbol, EvOdinValueSet> conjunctions = new HashMap<>();
            for (OdinBuildFlag buildFlag : buildFlagArgument.getBuildFlagList()) {
                Map<OdinSymbol, EvOdinValueSet> buildFlagValueSet = evaluateBuildFlag(buildFlag);
                processConjunctions(buildFlagValueSet, conjunctions);
            }

            for (var entry : conjunctions.entrySet()) {
                if (!disjunctions.containsKey(entry.getKey())) {
                    disjunctions.put(entry.getKey(), entry.getValue());
                    continue;
                }
                disjunctions.computeIfPresent(
                        entry.getKey(),
                        (__, presentValue) -> presentValue.combine(entry.getValue())
                );
            }

        }
        return disjunctions;
    }

    public Map<OdinSymbol, EvOdinValueSet> evaluateFileSuffix(Project project, String fileName) {
        fileName = FileUtil.getNameWithoutExtension(fileName);
        String[] splits = fileName.split("_");

        Map<OdinSymbol, EvOdinValueSet> values = new HashMap<>();
        String os;
        String arch;

        if (splits.length > 1) {
            for (int i = 1; i < splits.length; i++) {
                String split = splits[i];
                {
                    Function<Project, OdinSymbolValue> osFunc = OS_VALUES.get(split);
                    if (osFunc != null) {
                        OdinSymbolValue symbolValue = osFunc.apply(project);
                        if (symbolValue != null && symbolValue.symbol() != null) {
                            values.put(symbolValue.symbol(), symbolValue.value().asSet());
                        }
                    }
                }

                {
                    Function<Project, OdinSymbolValue> osFunc = ARCH_VALUES.get(split);
                    if (osFunc != null) {
                        OdinSymbolValue symbolValue = osFunc.apply(project);
                        if (symbolValue != null && symbolValue.symbol() != null) {
                            values.put(symbolValue.symbol(), symbolValue.value().asSet());
                        }
                    }
                }
            }
        }

        return values;
    }

    public OdinSymbolValueStore evaluateBuildFlags(OdinFile file) {
        OdinFileScope fileScope = file.getFileScope();
        Map<OdinSymbol, EvOdinValueSet> valuesBuildFlagClauses = evaluate(fileScope.getBuildFlagClauseList());
        VirtualFile virtualFile = file.getContainingFile().getVirtualFile();

        Map<OdinSymbol, EvOdinValueSet> valuesFileSuffix;
        if (virtualFile != null) {
            valuesFileSuffix = evaluateFileSuffix(fileScope.getProject(),
                    virtualFile.getName());
        } else {
            valuesFileSuffix = Collections.emptyMap();
        }

        OdinSymbolValueStore clauseStore = new OdinSymbolValueStore();
        clauseStore.putAll(valuesBuildFlagClauses);
        OdinSymbolValueStore suffixStore = new OdinSymbolValueStore();
        suffixStore.putAll(valuesFileSuffix);

        suffixStore.intersect(clauseStore);
        return suffixStore;
    }

    private static void processConjunctions(Map<OdinSymbol, EvOdinValueSet> newValues,
                                            Map<OdinSymbol, EvOdinValueSet> conjunctions) {
        for (var entry : newValues.entrySet()) {
            if (!conjunctions.containsKey(entry.getKey())) {
                conjunctions.put(entry.getKey(), entry.getValue());
                continue;
            }
            conjunctions.computeIfPresent(
                    entry.getKey(),
                    (__, presentValue) -> presentValue.intersect(entry.getValue())
            );

        }
    }

    private Map<OdinSymbol, EvOdinValueSet> evaluateBuildFlag(OdinBuildFlag buildFlag) {
        if (buildFlag instanceof OdinBuildFlagIdentifier identifier) {
            return evaluateBuildFlagIdentifier(identifier);
        }

        if (buildFlag instanceof OdinBuildFlagNegation negation) {
            return evaluateBuildFlagNegation(negation);
        }
        return Collections.emptyMap();
    }

    private Map<OdinSymbol, EvOdinValueSet> evaluateBuildFlagNegation(OdinBuildFlagNegation negation) {
        Map<OdinSymbol, EvOdinValueSet> complementaryValueMap = new HashMap<>();
        Map<OdinSymbol, EvOdinValueSet> symbolValues = evaluateBuildFlag(negation.getBuildFlag());
        for (Map.Entry<OdinSymbol, EvOdinValueSet> entry : symbolValues.entrySet()) {
            OdinSymbol symbol = entry.getKey();
            EvOdinValueSet valueSet = entry.getValue();
            complementaryValueMap.put(symbol, valueSet.complement());
        }
        return complementaryValueMap;
    }

    private Map<OdinSymbol, EvOdinValueSet> evaluateBuildFlagIdentifier(OdinBuildFlagIdentifier identifier) {
        Function<Project, OdinSymbolValue> value = VALUES.get(identifier.getText());
        if (value != null) {
            OdinSymbolValue symbolValue = value.apply(identifier.getProject());
            return Map.of(symbolValue.symbol, symbolValue.value().asSet());
        }

        return Collections.emptyMap();
    }
}
