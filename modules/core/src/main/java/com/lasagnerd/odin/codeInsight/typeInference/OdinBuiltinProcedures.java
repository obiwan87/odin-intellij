package com.lasagnerd.odin.codeInsight.typeInference;

import com.lasagnerd.odin.codeInsight.symbols.OdinSdkService;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;

import java.util.List;

public class OdinBuiltinProcedures {
    private final OdinInferenceEngine engine;

    public OdinBuiltinProcedures(OdinInferenceEngine engine) {
        this.engine = engine;
    }

    public static TsOdinType inferType(OdinInferenceEngine engine, TsOdinBuiltinProc proc, OdinCallExpression callExpression) {
        return new OdinBuiltinProcedures(engine).inferTypeOfBuiltinProcedure(proc, callExpression);
    }

    public TsOdinType inferTypeOfBuiltinProcedure(TsOdinBuiltinProc builtinProcedure, OdinCallExpression callExpression) {
        return switch (builtinProcedure.getName()) {
            case "soa_zip" -> soaZip(callExpression);
            case "soa_unzip" -> soaUnzip(callExpression);
            case "swizzle" -> swizzle(callExpression);
            case "type_of" -> typeOf(callExpression);
            case "len" -> len(callExpression);
            case "cap" -> cap(callExpression);
            case "size_of" -> sizeOf(callExpression);
            case "align_of" -> alignOf(callExpression);
            case "offset_of_selector" -> offsetOfSelector(callExpression);
            case "offset_of_member" -> offsetOfMember(callExpression);
            case "offset_of" -> offsetOf(callExpression);
            case "offset_of_by_string" -> offsetOfByString(callExpression);
            case "type_info_of" -> typeInfoOf(callExpression);
            case "typeid_of" -> typeidOf(callExpression);
            case "complex" -> complex(callExpression);
            case "quaternion" -> quaternion(callExpression);
            case "real" -> real(callExpression);
            case "imag" -> imag(callExpression);
            case "jmag" -> jmag(callExpression);
            case "kmag" -> kmag(callExpression);
            case "conj" -> conj(callExpression);
            case "expand_values" -> expandValues(callExpression);
            case "min" -> min(callExpression);
            case "max" -> max(callExpression);
            case "abs" -> abs(callExpression);
            case "clamp" -> clamp(callExpression);
            case "unreachable" -> unreachable(callExpression);
            default -> TsOdinBuiltInTypes.UNKNOWN;
        };
    }

    private TsOdinType len(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.INT;
    }

    private TsOdinType cap(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.INT;
    }

    private TsOdinType sizeOf(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.INT;
    }

    private TsOdinType alignOf(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.INT;
    }

    private TsOdinType offsetOfSelector(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UINTPTR;
    }

    private TsOdinType offsetOfMember(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UINTPTR;
    }

    private TsOdinType offsetOf(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UINTPTR;
    }

    private TsOdinType offsetOfByString(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UINTPTR;
    }

    private TsOdinType typeInfoOf(OdinCallExpression callExpression) {
        OdinSymbolTable symbolTable = OdinSymbolTable.from(OdinSdkService.getInstance(callExpression.getProject()).getRuntimeCoreSymbols());
        OdinSymbol symbol = symbolTable.getSymbol("Type_Info");
        if (symbol != null) {
            TsOdinMetaType metaType = (TsOdinMetaType) ((OdinDeclaredIdentifier) symbol.getDeclaredIdentifier()).getType();
            TsOdinStructType structType = (TsOdinStructType) metaType.representedType();
            TsOdinPointerType tsOdinPointerType = new TsOdinPointerType();
            tsOdinPointerType.setDereferencedType(structType);

            return tsOdinPointerType;
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType typeidOf(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.TYPEID;
    }

    private TsOdinType complex(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType quaternion(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType real(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType imag(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType jmag(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType kmag(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType conj(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType expandValues(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType min(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType max(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType abs(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType clamp(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType unreachable(OdinCallExpression callExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType typeOf(OdinCallExpression callExpression) {
        List<OdinArgument> argumentList = callExpression.getArgumentList();
        if (!argumentList.isEmpty()) {
            OdinArgument first = argumentList.getFirst();
            if (first instanceof OdinUnnamedArgument argument) {
                TsOdinType tsOdinType = argument.getExpression().getInferredType();
                return OdinTypeResolver.createMetaType(tsOdinType, null);
            }
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType swizzle(OdinCallExpression callExpression) {
        if (callExpression.getArgumentList().size() > 1) {
            OdinArgument first = callExpression.getArgumentList().getFirst();
            if (first instanceof OdinUnnamedArgument arrayArgument) {
                TsOdinType tsOdinType = engine.doInferType(arrayArgument.getExpression());
                if (tsOdinType.baseType(true) instanceof TsOdinArrayType tsOdinArrayType) {
                    tsOdinArrayType.setSize(callExpression.getArgumentList().size() - 1);
                    return tsOdinArrayType;
                }
            }
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType soaUnzip(OdinCallExpression callExpression) {
        TsOdinTuple tuple = new TsOdinTuple();
        if (callExpression.getArgumentList().size() == 1) {
            if (callExpression.getArgumentList().getFirst() instanceof OdinUnnamedArgument unnamedArgument) {
                TsOdinType tsOdinType = engine.doInferType(unnamedArgument.getExpression());
                if (tsOdinType instanceof TsOdinSoaSliceType tsOdinSoaSliceType) {
                    tuple.getTypes().addAll(tsOdinSoaSliceType.getSlices().values());
                }
            }
        }
        return tuple;
    }

    private TsOdinType soaZip(OdinCallExpression callExpression) {
        TsOdinSoaSliceType soaSlice = new TsOdinSoaSliceType();
        for (OdinArgument odinArgument : callExpression.getArgumentList()) {
            if (odinArgument instanceof OdinNamedArgument namedArgument) {
                TsOdinType sliceType = engine.doInferType(namedArgument.getExpression());
                soaSlice.getSlices().put(namedArgument.getIdentifier().getText(), sliceType);
            }
        }
        return soaSlice;
    }
}
