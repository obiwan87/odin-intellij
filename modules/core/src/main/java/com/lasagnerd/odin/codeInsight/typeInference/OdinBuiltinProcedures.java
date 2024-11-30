package com.lasagnerd.odin.codeInsight.typeInference;

import com.lasagnerd.odin.codeInsight.symbols.OdinSdkService;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;

import java.util.ArrayList;
import java.util.List;

import static com.lasagnerd.odin.codeInsight.typeSystem.TsOdinBuiltInTypes.getBackingTypeOfComplexOrQuaternion;
import static com.lasagnerd.odin.codeInsight.typeSystem.TsOdinBuiltInTypes.getSimpleFloatingPointTypes;

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

    private TsOdinType len(OdinCallExpression ignoredCallExpression) {
        return TsOdinBuiltInTypes.INT;
    }

    private TsOdinType cap(OdinCallExpression ignoredCallExpression) {
        return TsOdinBuiltInTypes.INT;
    }

    private TsOdinType sizeOf(OdinCallExpression ignoredCallExpression) {
        return TsOdinBuiltInTypes.INT;
    }

    private TsOdinType alignOf(OdinCallExpression ignoredCallExpression) {
        return TsOdinBuiltInTypes.INT;
    }

    private TsOdinType offsetOfSelector(OdinCallExpression ignoredCallExpression) {
        return TsOdinBuiltInTypes.UINTPTR;
    }

    private TsOdinType offsetOfMember(OdinCallExpression ignoredCallExpression) {
        return TsOdinBuiltInTypes.UINTPTR;
    }

    private TsOdinType offsetOf(OdinCallExpression ignoredCallExpression) {
        return TsOdinBuiltInTypes.UINTPTR;
    }

    private TsOdinType offsetOfByString(OdinCallExpression ignoredCallExpression) {
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

    private TsOdinType typeidOf(OdinCallExpression ignored__) {
        return TsOdinBuiltInTypes.TYPEID;
    }

    private TsOdinNumericType getCompatibleFloatingType(OdinCallExpression callExpression) {
        TsOdinNumericType expectedType = TsOdinBuiltInTypes.F64;
        List<TsOdinType> types = inferTypeOfArguments(callExpression);
        // Gather all arguments
        if (callExpression.getArgumentList().size() != 2)
            return null;

        // Get all distinct typed numeric arguments
        List<TsOdinType> typedTypes = types.stream()
                .map(TsOdinType::baseType)
                .filter(t -> t.isNumeric() && !t.isUntyped())
                .distinct()
                .toList();

        // If there's more than one typed type we can abort, because all arguments must be equally typed
        if (typedTypes.size() > 1)
            return null;

        // If there is one, then it must be a floating type

        if (!typedTypes.isEmpty()) {
            if (!getSimpleFloatingPointTypes().contains(typedTypes.getFirst())) {
                return null;
            }
            expectedType = (TsOdinNumericType) typedTypes.getFirst();
        }

        return expectedType;
    }

    private TsOdinType complex(OdinCallExpression callExpression) {
        TsOdinNumericType numericType = getCompatibleFloatingType(callExpression);

        if (numericType != null) {
            return switch (numericType.getLength()) {
                case 16 -> TsOdinBuiltInTypes.COMPLEX32;
                case 32 -> TsOdinBuiltInTypes.COMPLEX64;
                case 64 -> TsOdinBuiltInTypes.COMPLEX128;
                default -> TsOdinBuiltInTypes.UNKNOWN;
            };
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType quaternion(OdinCallExpression callExpression) {
        TsOdinNumericType numericType = getCompatibleFloatingType(callExpression);
        if (numericType != null) {
            return switch (numericType.getLength()) {
                case 16 -> TsOdinBuiltInTypes.QUATERNION64;
                case 32 -> TsOdinBuiltInTypes.QUATERNION128;
                case 64 -> TsOdinBuiltInTypes.QUATERNION256;
                default -> TsOdinBuiltInTypes.UNKNOWN;
            };
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType complexOrQuaternionComponent(OdinCallExpression callExpression) {
        TsOdinType tsOdinType = inferTypeOfFirstArgument(callExpression).baseType();
        if (tsOdinType instanceof TsOdinNumericType numericType && (numericType.isQuaternion() || numericType.isComplex())) {
            return getBackingTypeOfComplexOrQuaternion(tsOdinType);
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType real(OdinCallExpression callExpression) {
        return complexOrQuaternionComponent(callExpression);
    }

    private TsOdinType imag(OdinCallExpression callExpression) {
        return complexOrQuaternionComponent(callExpression);
    }

    private TsOdinType quaternionComponent(OdinCallExpression callExpression) {
        TsOdinType tsOdinType = inferTypeOfFirstArgument(callExpression).baseType();
        if (tsOdinType instanceof TsOdinNumericType numericType && numericType.isQuaternion()) {
            return getBackingTypeOfComplexOrQuaternion(tsOdinType);
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }
    private TsOdinType jmag(OdinCallExpression callExpression) {
        return quaternionComponent(callExpression);
    }

    private TsOdinType kmag(OdinCallExpression callExpression) {
        return quaternionComponent(callExpression);
    }

    private TsOdinType conj(OdinCallExpression callExpression) {
        return complexOrQuaternionComponent(callExpression);
    }

    private TsOdinType expandValues(OdinCallExpression ignoredCallExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType min(OdinCallExpression callExpression) {
        // TODO very annoying implementation
        return inferTypeOfFirstArgument(callExpression).typed().baseType();
    }

    private TsOdinType max(OdinCallExpression callExpression) {
        // TODO very annoying implementation
        return inferTypeOfFirstArgument(callExpression).typed().baseType();
    }

    private TsOdinType abs(OdinCallExpression callExpression) {
        return inferTypeOfFirstArgument(callExpression).typed().baseType();
    }

    private TsOdinType clamp(OdinCallExpression callExpression) {
        return inferTypeOfFirstArgument(callExpression);
    }

    private TsOdinType unreachable(OdinCallExpression ignoredCallExpression) {
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private TsOdinType inferTypeOfFirstArgument(OdinCallExpression callExpression) {
        List<OdinArgument> argumentList = callExpression.getArgumentList();
        if (!argumentList.isEmpty()) {
            OdinArgument first = argumentList.getFirst();
            if (first instanceof OdinUnnamedArgument argument) {
                return argument.getExpression().getInferredType();
            }
        }
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    private List<TsOdinType> inferTypeOfArguments(OdinCallExpression callExpression) {
        List<TsOdinType> types = new ArrayList<>();
        List<OdinArgument> argumentList = callExpression.getArgumentList();
        if (!argumentList.isEmpty()) {
            OdinArgument first = argumentList.getFirst();
            if (first instanceof OdinUnnamedArgument argument) {
                types.add(argument.getExpression().getInferredType());
            }
        }
        return types;
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
