package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine.inferType;

public class OdinTypeSpecializer {

    public static boolean specializeStruct(OdinSymbolTable outerScope,
                                           @NotNull List<OdinArgument> arguments,
                                           TsOdinStructType genericType, TsOdinStructType specializedType) {
        List<TsOdinParameter> parameters = genericType.getParameters();
        if (parameters.isEmpty())
            return false;

        specializedType.setGenericType(genericType);
        specializedType.getSymbolTable().setPackagePath(genericType.getSymbolTable().getPackagePath());
        specializedType.getSymbolTable().putAll(genericType.getSymbolTable());
        OdinSymbolTable newScope = specializedType.getSymbolTable();
        resolveArguments(outerScope,
                genericType,
                genericType.getParameters(),
                specializedType,
                arguments);
        specializedType.setType(genericType.getType());
        specializedType.setName(genericType.getName());
        specializedType.setDeclaration(genericType.getDeclaration());
        specializedType.setDeclaredIdentifier(genericType.getDeclaredIdentifier());
        specializedType.getFields().putAll(genericType.getFields());

        OdinStructType type = genericType.type();
        List<OdinFieldDeclarationStatement> fieldDeclarations = OdinInsightUtils.getStructFieldsDeclarationStatements(type);
        for (OdinFieldDeclarationStatement fieldDeclaration : fieldDeclarations) {
            var fieldType = fieldDeclaration.getType();
            TsOdinType tsOdinType = OdinTypeResolver.resolveType(newScope, fieldType);
            for (OdinDeclaredIdentifier declaredIdentifier : fieldDeclaration.getDeclaredIdentifiers()) {
                specializedType.getFields().put(declaredIdentifier.getName(), tsOdinType);
            }
        }

        return true;
    }

    public static @NotNull TsOdinProcedureType specializeProcedure(@NotNull OdinSymbolTable outerScope,
                                                                   List<OdinArgument> arguments,
                                                                   TsOdinProcedureType genericType) {
        List<TsOdinParameter> parameters = genericType.getParameters();
        if (parameters.isEmpty())
            return genericType;

        TsOdinProcedureType specializedType = new TsOdinProcedureType();
        specializedType.getSymbolTable().setPackagePath(genericType.getSymbolTable().getPackagePath());
        specializedType.getSymbolTable().putAll(genericType.getSymbolTable());
        specializedType.setType(genericType.getType());
        specializedType.setName(genericType.getName());
        specializedType.setDeclaration(genericType.getDeclaration());
        specializedType.setDeclaredIdentifier(genericType.getDeclaredIdentifier());
        specializedType.setReturnParameters(genericType.getReturnParameters());
        resolveArguments(outerScope,
                genericType,
                genericType.getParameters(),
                specializedType,
                arguments);

        for (TsOdinParameter tsOdinReturnType : genericType.getReturnParameters()) {
            TsOdinType tsOdinType = OdinTypeResolver.resolveType(specializedType.getSymbolTable(), tsOdinReturnType.getPsiType());
            specializedType.getReturnTypes().add(tsOdinType);
        }

        return specializedType;
    }

    public static boolean specializeUnion(OdinSymbolTable outerSymbolTable, List<OdinArgument> arguments, TsOdinUnionType genericType, TsOdinUnionType specializedType) {
        List<TsOdinParameter> parameters = genericType.getParameters();
        if (parameters.isEmpty())
            return false;

        specializedType.getSymbolTable().setPackagePath(genericType.getSymbolTable().getPackagePath());
        specializedType.setGenericType(genericType);
        specializedType.getSymbolTable().putAll(genericType.getSymbolTable());
        specializedType.setType(genericType.getType());
        specializedType.setName(genericType.getName());
        specializedType.setDeclaration(genericType.getDeclaration());
        specializedType.setDeclaredIdentifier(genericType.getDeclaredIdentifier());
        resolveArguments(outerSymbolTable,
                genericType,
                genericType.getParameters(),
                specializedType,
                arguments);


        for (TsOdinUnionVariant baseField : genericType.getVariants()) {
            TsOdinType specializedFieldType = OdinTypeResolver.resolveType(specializedType.getSymbolTable(), baseField.getPsiType());
            TsOdinUnionVariant specializedField = new TsOdinUnionVariant();
            specializedField.setPsiType(baseField.getPsiType());
            specializedField.setType(specializedFieldType);
            specializedType.getVariants().add(specializedField);
        }

        return true;
    }

    private static void resolveArguments(
            OdinSymbolTable outerScope,
            TsOdinType genericType,
            List<TsOdinParameter> parameters,
            TsOdinType specializedType,
            List<OdinArgument> arguments
    ) {
        OdinSymbolTable instantiationScope = specializedType.getSymbolTable();

        if (!arguments.isEmpty()) {
            for (int i = 0; i < arguments.size(); i++) {
                final int currentIndex = i;
                OdinArgument odinArgument = arguments.get(i);

                OdinExpression argumentExpression = null;
                TsOdinParameter tsOdinParameter = null;

                if (odinArgument instanceof OdinUnnamedArgument argument) {
                    argumentExpression = argument.getExpression();
                    tsOdinParameter = parameters.stream()
                            .filter(p -> p.getIndex() == currentIndex)
                            .findFirst().orElse(null);
                }

                if (odinArgument instanceof OdinNamedArgument argument) {
                    tsOdinParameter = parameters.stream()
                            .filter(p -> argument.getIdentifier().getText().equals(p.getValueName()))
                            .findFirst().orElse(null);
                    argumentExpression = argument.getExpression();
                }

                if (argumentExpression == null || tsOdinParameter == null)
                    continue;

                TsOdinType argumentType = resolveArgumentType(argumentExpression, tsOdinParameter, outerScope);
                if (argumentType.isUnknown()) {
                    System.out.printf("Could not resolve argument [%s] type for base type %s with name %s%n", tsOdinParameter.getValueName(), genericType.getClass().getSimpleName(), genericType.getName());
                    continue;
                }

                TsOdinType parameterType = tsOdinParameter.getType();
                if (parameterType == null) {
                    System.out.println("Could not resolve parameter type");
                    continue;
                }

                if (tsOdinParameter.isExplicitPolymorphicParameter()) {
                    if (specializedType instanceof TsOdinGenericType generalizableType) {
                        generalizableType.getResolvedPolymorphicParameters().put(tsOdinParameter.getValueName(), argumentType);
                    }
                    instantiationScope.addType(tsOdinParameter.getValueName(), argumentType);
                }

                Map<String, TsOdinType> resolvedTypes = substituteTypes(parameterType, argumentType);
                if (specializedType instanceof TsOdinGenericType generalizableType) {
                    generalizableType.getResolvedPolymorphicParameters().putAll(resolvedTypes);
                }
                for (Map.Entry<String, TsOdinType> entry : resolvedTypes.entrySet()) {
                    instantiationScope.addType(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * The method substituteTypes maps $T -> Point as shown in the example below
     * declared type:         List($Item) { items: []$Item }
     * polymorphic parameter: List($T):       $Item -> $T
     * |                   |
     * v                   v
     * argument:              List(Point)   : $Item  -> Point
     *
     * @param parameterType The parameter type
     * @param argumentType  The argument type
     * @return a substitution map
     */
    private static @NotNull Map<String, TsOdinType> substituteTypes(TsOdinType parameterType, TsOdinType argumentType) {
        Map<String, TsOdinType> resolvedTypes = new HashMap<>();
        doSubstituteTypes(parameterType, argumentType, resolvedTypes);
        return resolvedTypes;
    }

    private static void doSubstituteTypes(@NotNull TsOdinType parameterType, @NotNull TsOdinType argumentType, @NotNull Map<String, TsOdinType> resolvedTypes) {
        if (parameterType.isPolymorphic() && !argumentType.isPolymorphic()) {
            resolvedTypes.put(parameterType.getName(), argumentType);
        } else {
            if (parameterType instanceof TsOdinConstrainedType constrainedType) {
                doSubstituteTypes(constrainedType.getMainType(), argumentType, resolvedTypes);
                TsOdinType resolvedMainType = resolvedTypes.get(constrainedType.getMainType().getName());
                if (resolvedMainType != null) {
                    doSubstituteTypes(constrainedType.getSpecializedType(), resolvedMainType, resolvedTypes);
                }
            }
            if (parameterType instanceof TsOdinArrayType parameterArrayType
                    && argumentType instanceof TsOdinArrayType argumentArrayType) {
                doSubstituteTypes(parameterArrayType.getElementType(), argumentArrayType.getElementType(), resolvedTypes);
            } else if (parameterType instanceof TsOdinSliceType sliceType
                    && argumentType instanceof TsOdinSliceType sliceType1) {
                doSubstituteTypes(sliceType.getElementType(), sliceType1.getElementType(), resolvedTypes);
            } else if (parameterType instanceof TsOdinPointerType pointerType
                    && argumentType instanceof TsOdinPointerType pointerType1) {
                doSubstituteTypes(pointerType.getDereferencedType(), pointerType1.getDereferencedType(), resolvedTypes);
            } else if (parameterType instanceof TsOdinMultiPointerType pointerType
                    && argumentType instanceof TsOdinMultiPointerType pointerType1) {
                doSubstituteTypes(pointerType.getDereferencedType(), pointerType1.getDereferencedType(), resolvedTypes);
            } else if (parameterType instanceof TsOdinMapType mapType
                    && argumentType instanceof TsOdinMapType mapType1) {
                doSubstituteTypes(mapType.getKeyType(), mapType1.getKeyType(), resolvedTypes);
                doSubstituteTypes(mapType.getValueType(), mapType1.getValueType(), resolvedTypes);
            } else if (parameterType instanceof TsOdinMatrixType matrixType &&
                    argumentType instanceof TsOdinMatrixType matrixType1) {
                doSubstituteTypes(matrixType.getElementType(), matrixType1.getElementType(), resolvedTypes);
            } else if (parameterType instanceof TsOdinBitSetType bitSetType &&
                    argumentType instanceof TsOdinBitSetType bitSetType1) {
                doSubstituteTypes(bitSetType.getElementType(), bitSetType1.getElementType(), resolvedTypes);
            } else if (parameterType instanceof TsOdinProcedureType procedureType &&
                    argumentType instanceof TsOdinProcedureType procedureType1) {
                if (procedureType.getParameters().size() == procedureType1.getParameters().size()) {

                    // Note: explicit polymorphic parameters are not allowed in this scenario, as they don't make
                    //  sense in a type definition
                    for (int i = 0; i < procedureType.getParameters().size(); i++) {
                        TsOdinParameter parameterParameter = procedureType.getParameters().get(i);
                        TsOdinParameter argumentParameter = procedureType1.getParameters().get(i);
                        doSubstituteTypes(parameterParameter.getType(), argumentParameter.getType(), resolvedTypes);
                    }
                }
                if (procedureType.getReturnParameters().size() == procedureType1.getReturnParameters().size()) {
                    for (int i = 0; i < procedureType.getReturnParameters().size(); i++) {
                        TsOdinParameter parameterParameter = procedureType.getReturnParameters().get(i);
                        TsOdinParameter argumentParameter = procedureType1.getReturnParameters().get(i);
                        doSubstituteTypes(parameterParameter.getType(), argumentParameter.getType(), resolvedTypes);
                    }
                }
            } // This should be working only structs, unions and procedures
            else if (parameterType instanceof TsOdinGenericType generalizableType && argumentType instanceof TsOdinGenericType generalizableType1) {
                if (generalizableType1.getClass().equals(generalizableType.getClass())) {
                    for (Map.Entry<String, TsOdinType> entry : generalizableType.getResolvedPolymorphicParameters().entrySet()) {
                        TsOdinType nextArgumentType = generalizableType1.getResolvedPolymorphicParameters().getOrDefault(entry.getKey(), TsOdinType.UNKNOWN);
                        TsOdinType nextParameterType = entry.getValue();
                        doSubstituteTypes(nextParameterType, nextArgumentType, resolvedTypes);
                    }
                }
            }
        }
    }

    @NotNull
    private static TsOdinType resolveArgumentType(OdinExpression argumentExpression, TsOdinParameter parameter, OdinSymbolTable symbolTable) {
        TsOdinType parameterType = parameter.getType();
        TsOdinType argumentType = inferType(symbolTable, argumentExpression);
        if (argumentType instanceof TsOdinMetaType metaType && (parameterType.isTypeId()
                || parameterType.getMetaType() == metaType.getRepresentedMetaType())) {
            return OdinTypeResolver.resolveMetaType(symbolTable, metaType);
        }

        // TODO if argumentExpression is a reference to a polymorphic type we need to treat this as a polymorphic type
        if(argumentExpression instanceof OdinRefExpression refExpression && argumentType == TsOdinBuiltInTypes.TYPEID) {
            OdinIdentifier identifier = refExpression.getIdentifier();
            if(identifier != null) {
                TsOdinType tsOdinType = symbolTable.getType(identifier.getText());
                return Objects.requireNonNullElse(tsOdinType, TsOdinType.UNKNOWN);
            }
        }

        // Case 2: The argumentExpression is a polymorphic type. In that case we know its type already and
        // introduce a new unresolved polymorphic type and map the parameter to that
        else if (argumentExpression instanceof OdinTypeDefinitionExpression typeDefinitionExpression) {
            if (typeDefinitionExpression.getType() instanceof OdinPolymorphicType polymorphicType) {
                return createPolymorphicType(symbolTable, polymorphicType);
            }
            return TsOdinType.UNKNOWN;
        }
        // Case 3: The argument has been resolved to a proper type. Just add the mapping
        return argumentType;
    }

    private static @NotNull TsOdinType createPolymorphicType(OdinSymbolTable newScope, OdinPolymorphicType polymorphicType) {
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(newScope, polymorphicType);
        OdinDeclaredIdentifier declaredIdentifier = polymorphicType.getDeclaredIdentifier();
        tsOdinType.setDeclaration(polymorphicType);
        tsOdinType.setDeclaredIdentifier(declaredIdentifier);
        return tsOdinType;
    }


    public static @NotNull TsOdinStructType specializeAndCacheStruct(OdinSymbolTable symbolTable, TsOdinStructType structType, @NotNull List<OdinArgument> argumentList) {
        TsOdinStructType specializedType = new TsOdinStructType();
        ArrayList<PsiElement> arguments = new ArrayList<>(argumentList);
        structType.getSymbolTable().addSpecializedType(structType, specializedType, arguments);

        boolean specializationCreated = specializeStruct(symbolTable,
                argumentList,
                structType,
                specializedType);
        if (!specializationCreated) {
            structType.getSymbolTable().getSpecializedTypes().get(structType).remove(argumentList);
            return structType;
        }
        return specializedType;
    }

    public static @NotNull TsOdinUnionType specializeAndCacheUnion(OdinSymbolTable symbolTable, TsOdinUnionType unionType, @NotNull List<OdinArgument> argumentList) {
        TsOdinUnionType specializedType = new TsOdinUnionType();
        ArrayList<PsiElement> arguments = new ArrayList<>(argumentList);
        unionType.getSymbolTable().addSpecializedType(unionType, specializedType, arguments);
        boolean specializationCreated = specializeUnion(symbolTable,
                argumentList,
                unionType,
                specializedType);
        if (!specializationCreated) {
            unionType.getSymbolTable().getSpecializedTypes().get(unionType).remove(argumentList);
            return unionType;
        }
        return specializedType;
    }

    public static @NotNull TsOdinType specializeUnionOrGetCached(OdinSymbolTable symbolTable, TsOdinUnionType unionType, @NotNull List<OdinArgument> argumentList) {
        TsOdinType specializedType = symbolTable.getSpecializedType(unionType, new ArrayList<>(argumentList));
        if (specializedType == null) {
            specializedType = specializeAndCacheUnion(symbolTable, unionType, argumentList);
        }
        return specializedType;
    }

    public static @NotNull TsOdinStructType specializeStructOrGetCached(OdinSymbolTable symbolTable, TsOdinStructType structType, @NotNull List<OdinArgument> argumentList) {
        TsOdinType specializedType = symbolTable.getSpecializedType(structType, new ArrayList<>(argumentList));
        if (specializedType == null) {
            specializedType = specializeAndCacheStruct(symbolTable, structType, argumentList);
        }
        return (TsOdinStructType) specializedType;
    }
}
