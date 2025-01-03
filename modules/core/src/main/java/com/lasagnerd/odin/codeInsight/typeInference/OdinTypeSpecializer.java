package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.openapi.util.text.LineColumn;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.evaluation.EvEnumValue;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.evaluation.OdinExpressionEvaluator;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine.inferTypeInExplicitMode;

public class OdinTypeSpecializer {

    public static boolean specializeStruct(OdinContext outerScope,
                                           @NotNull List<OdinArgument> arguments,
                                           TsOdinStructType genericType, TsOdinStructType specializedType) {
        List<TsOdinParameter> parameters = genericType.getParameters();
        if (parameters.isEmpty())
            return false;

        specializedType.setGenericType(genericType);
        specializedType.getContext().setPackagePath(genericType.getContext().getPackagePath());
        specializedType.getContext().merge(genericType.getContext());
        OdinContext newScope = specializedType.getContext();
        resolveArguments(outerScope,
                genericType,
                genericType.getParameters(),
                specializedType,
                arguments);
        specializedType.setPsiType(genericType.getPsiType());
        specializedType.setName(genericType.getName());
        specializedType.setDeclaration(genericType.getDeclaration());
        specializedType.setDeclaredIdentifier(genericType.getDeclaredIdentifier());
        specializedType.getFields().putAll(genericType.getFields());

        OdinStructType type = genericType.type();
        List<OdinFieldDeclaration> fieldDeclarations = OdinInsightUtils.getStructFieldsDeclarationStatements(type);
        for (OdinFieldDeclaration fieldDeclaration : fieldDeclarations) {
            var fieldType = fieldDeclaration.getType();
            if (fieldType != null) {
                TsOdinType tsOdinType = resolveType(newScope, fieldType);
                for (OdinDeclaredIdentifier declaredIdentifier : fieldDeclaration.getDeclaredIdentifiers()) {
                    specializedType.getFields().put(declaredIdentifier.getName(), tsOdinType);
                }
            }
        }

        return true;
    }

    public static @NotNull TsOdinProcedureType specializeProcedure(@NotNull OdinContext outerScope,
                                                                   List<OdinArgument> arguments,
                                                                   TsOdinProcedureType genericType) {
        List<TsOdinParameter> parameters = genericType.getParameters();
        if (parameters.isEmpty())
            return genericType;

        TsOdinProcedureType specializedType = new TsOdinProcedureType();

        OdinContext context = specializedType.getContext();
        context.merge(genericType.getContext());
        context.setPackagePath(genericType.getContext().getPackagePath());

        specializedType.setPsiType(genericType.getPsiType());
        specializedType.setName(genericType.getName());
        specializedType.setDeclaration(genericType.getDeclaration());
        specializedType.setDeclaredIdentifier(genericType.getDeclaredIdentifier());
        specializedType.setReturnParameters(genericType.getReturnParameters());
        resolveArguments(outerScope,
                genericType,
                genericType.getParameters(),
                specializedType,
                arguments);


        for (TsOdinParameter tsOdinParameter : genericType.getParameters()) {
            TsOdinType tsOdinType = resolveType(context, tsOdinParameter.getPsiType());
            TsOdinParameter specializedParameter = cloneWithType(tsOdinParameter, tsOdinType);
            specializedType.getParameters().add(specializedParameter);
        }

        for (TsOdinParameter tsOdinReturnType : genericType.getReturnParameters()) {
            TsOdinType tsOdinType = resolveType(context, tsOdinReturnType.getPsiType());
            specializedType.getReturnTypes().add(tsOdinType);
        }

        return specializedType;
    }

    private static TsOdinType resolveType(OdinContext context, OdinType type) {
        return OdinTypeResolver.resolveType(new OdinTypeResolver.OdinTypeResolverParameters(context, null, null, true), type);
    }

    private static @NotNull TsOdinParameter cloneWithType(TsOdinParameter tsOdinParameter, TsOdinType tsOdinType) {
        TsOdinParameter specializedParameter = new TsOdinParameter();
        specializedParameter.setType(tsOdinType);
        specializedParameter.setParameterDeclaration(tsOdinParameter.getParameterDeclaration());
        specializedParameter.setName(tsOdinParameter.getName());
        specializedParameter.setIndex(tsOdinParameter.getIndex());
        specializedParameter.setPsiType(tsOdinParameter.getPsiType());
        specializedParameter.setExplicitPolymorphicParameter(tsOdinParameter.isExplicitPolymorphicParameter());
        specializedParameter.setAnyInt(tsOdinParameter.isAnyInt());
        specializedParameter.setDefaultValueExpression(tsOdinParameter.getDefaultValueExpression());
        return specializedParameter;
    }

    public static boolean specializeUnion(OdinContext outerContext, List<OdinArgument> arguments, TsOdinUnionType genericType, TsOdinUnionType specializedType) {
        List<TsOdinParameter> parameters = genericType.getParameters();
        if (parameters.isEmpty())
            return false;

        specializedType.getContext().setPackagePath(genericType.getContext().getPackagePath());
        specializedType.setGenericType(genericType);
        specializedType.getContext().merge(genericType.getContext());
        specializedType.setPsiType(genericType.getPsiType());
        specializedType.setName(genericType.getName());
        specializedType.setDeclaration(genericType.getDeclaration());
        specializedType.setDeclaredIdentifier(genericType.getDeclaredIdentifier());
        resolveArguments(outerContext,
                genericType,
                genericType.getParameters(),
                specializedType,
                arguments);


        for (TsOdinUnionVariant baseField : genericType.getVariants()) {
            TsOdinType specializedFieldType = resolveType(specializedType.getContext(), baseField.getPsiType());
            TsOdinUnionVariant specializedField = new TsOdinUnionVariant();
            specializedField.setPsiType(baseField.getPsiType());
            specializedField.setType(specializedFieldType);
            specializedType.getVariants().add(specializedField);
        }

        return true;
    }

    private static void resolveArguments(
            OdinContext context,
            TsOdinType genericType,
            List<TsOdinParameter> parameters,
            TsOdinType specializedType,
            List<OdinArgument> arguments
    ) {
        OdinContext instantiationContext = specializedType.getContext();
        Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(parameters, arguments, true);
        if (argumentToParameterMap != null) {
            for (Map.Entry<OdinExpression, TsOdinParameter> entry : argumentToParameterMap.entrySet()) {
                OdinExpression argumentExpression = entry.getKey();
                TsOdinParameter tsOdinParameter = entry.getValue();

                if (!tsOdinParameter.hasPolymorphicDeclarations())
                    continue;

                TsOdinType argumentType = resolveArgumentType(argumentExpression, context);
                if (argumentType.isUnknown()) {
                    System.out.printf("Could not resolve argument [%s] type for base type %s with name %s%n in %s",
                            tsOdinParameter.getName(),
                            genericType.getClass().getSimpleName(),
                            genericType.getName(),
                            getLocationWithinFile(argumentExpression)
                    );
                    continue;
                }

                if (!argumentType.isExplicitPolymorphic()) {
                    if (!argumentType.isUndecided()) {
                        EvOdinValue value = OdinExpressionEvaluator.evaluate(context, argumentExpression);
                        if (!value.isNull()) {
                            instantiationContext.getPolymorphicValues().put(tsOdinParameter.getName(), value);
                        }
                    } else {
                        TsOdinType parameterBaseType = tsOdinParameter.getType().baseType(true);
                        if (parameterBaseType instanceof TsOdinEnumType enumType
                                && argumentExpression instanceof OdinImplicitSelectorExpression implicitSelectorExpression) {
                            EvEnumValue enumValue = OdinExpressionEvaluator.getEnumValue(enumType, implicitSelectorExpression.getIdentifier().getText());
                            if (enumValue != null) {
                                EvOdinValue value = new EvOdinValue(enumValue, tsOdinParameter.getType());
                                instantiationContext.getPolymorphicValues().put(tsOdinParameter.getName(), value);
                            }
                        }
                    }
                }

                TsOdinType parameterType = tsOdinParameter.getType();
                if (parameterType == null) {
                    continue;
                }

                if (tsOdinParameter.isExplicitPolymorphicParameter()) {
                    if (specializedType instanceof TsOdinGenericType generalizableType) {
                        generalizableType.getResolvedPolymorphicParameters().put(tsOdinParameter.getName(), argumentType);
                    }
                    instantiationContext.addPolymorphicType(tsOdinParameter.getName(), argumentType);
                }

                @NotNull OdinTypeSubstitutionResult substitutionResult = substituteTypes(argumentType, parameterType);
                for (Map.Entry<String, TsOdinType> resolvedTypeEntry : substitutionResult.types.entrySet()) {
                    instantiationContext.addPolymorphicType(resolvedTypeEntry.getKey(), resolvedTypeEntry.getValue());

                }

                for (Map.Entry<String, EvOdinValue> valueEntry : substitutionResult.values.entrySet()) {
                    instantiationContext.getPolymorphicValues().put(valueEntry.getKey(), valueEntry.getValue());
                }

                if (specializedType instanceof TsOdinGenericType generalizableType) {
                    generalizableType.getResolvedPolymorphicParameters().putAll(substitutionResult.types);
                }
            }
        }
    }

    /**
     * The method substituteTypes maps $T -> Point as shown in the example below
     * declared type:         List($Item) { items: []$Item }
     * polymorphic parameter: List($T):       $Item -> $T
     *                              |              |
     *                              v              v
     * argument:              List(Point)   : $Item  -> Point
     *
     * @param argumentType  The argument type
     * @param parameterType The parameter type
     * @return a substitution map
     */
    // @formatter:on
    public static @NotNull OdinTypeSubstitutionResult substituteTypes(TsOdinType argumentType, TsOdinType parameterType) {
        Map<String, TsOdinType> resolvedTypes = new HashMap<>();
        Map<String, EvOdinValue> values = new HashMap<>();
        doSubstituteTypes(argumentType, parameterType, resolvedTypes, values);
        return new OdinTypeSubstitutionResult(values, resolvedTypes);
    }
    // @formatter:off

    private static void doSubstituteTypes(@NotNull TsOdinType argumentType,
                                          @NotNull TsOdinType parameterType,
                                          @NotNull Map<String, TsOdinType> resolvedTypes, Map<String, EvOdinValue> values) {
        // When dealing with non-distinct type aliases, we substitute the alias with its base type
        // However, distinct types shall not be substituted. This behaviour has been tested using the odin compiler
        if (argumentType instanceof TsOdinTypeAlias typeAlias && !typeAlias.isDistinct()) {
            doSubstituteTypes(typeAlias.getBaseType(), parameterType, resolvedTypes, values);
            return;
        }

        if (parameterType.isPolymorphic()) {
            resolvedTypes.put(parameterType.getName(), argumentType);
        } else {
            if (parameterType instanceof TsOdinConstrainedType constrainedType) {
                TsOdinType resolvedMainType;
                if (constrainedType.getMainType().isTypeId()) {
                    resolvedMainType = argumentType;
                } else {
                    doSubstituteTypes(argumentType, constrainedType.getMainType(), resolvedTypes, values);

                    resolvedMainType = resolvedTypes.get(constrainedType.getMainType().getName());
                }
                if (resolvedMainType != null) {
                    if (resolvedMainType instanceof TsOdinTypeAlias typeAlias) {
                        resolvedMainType = typeAlias.getBaseType();
                    }
                    doSubstituteTypes(resolvedMainType, constrainedType.getSpecializedType(), resolvedTypes, values);
                }
            }
            if (parameterType instanceof TsOdinArrayType parArrayType
                    && argumentType instanceof TsOdinArrayType argArrayType) {
                OdinExpression sizeExpression = parArrayType.getPsiSizeElement().getExpression();
                if (sizeExpression != null) {
                    TsOdinType sizeType = sizeExpression.getInferredType();
                    if (sizeType.dereference().isPolymorphic() && argArrayType.getSize() != null) {
                        values.put(sizeType.getName(), new EvOdinValue(argArrayType.getSize(), TsOdinBuiltInTypes.INT));
                    }
                }
                doSubstituteTypes(argArrayType.getElementType(), parArrayType.getElementType(), resolvedTypes, values);
            } else if (parameterType instanceof TsOdinDynamicArray parDynamicArray
                    && argumentType instanceof TsOdinDynamicArray argDynamicArray) {
                doSubstituteTypes(argDynamicArray.getElementType(), parDynamicArray.getElementType(), resolvedTypes, values);
            } else if (parameterType instanceof TsOdinSliceType parSliceType
                    && argumentType instanceof TsOdinSliceType argSliceType) {
                doSubstituteTypes(argSliceType.getElementType(), parSliceType.getElementType(), resolvedTypes, values);
            } else if (parameterType instanceof TsOdinPointerType parPointerType
                    && argumentType instanceof TsOdinPointerType argPointerType) {
                doSubstituteTypes(argPointerType.getDereferencedType(), parPointerType.getDereferencedType(), resolvedTypes, values);
            } else if (parameterType instanceof TsOdinMultiPointerType parMultiPointerType
                    && argumentType instanceof TsOdinMultiPointerType argMultiPointerType) {
                doSubstituteTypes(argMultiPointerType.getDereferencedType(), parMultiPointerType.getDereferencedType(), resolvedTypes, values);
            } else if (parameterType instanceof TsOdinMapType parMapType
                    && argumentType instanceof TsOdinMapType argMapType) {
                doSubstituteTypes(argMapType.getKeyType(), parMapType.getKeyType(), resolvedTypes, values);
                doSubstituteTypes(argMapType.getValueType(), parMapType.getValueType(), resolvedTypes, values);
            } else if (parameterType instanceof TsOdinMatrixType parMatrixType &&
                    argumentType instanceof TsOdinMatrixType argMatrixType) {
                OdinExpression columnsExpression = parMatrixType.getColumnsExpression();
                if (columnsExpression != null) {
                    TsOdinType inferredType = columnsExpression.getInferredType();
                    if (inferredType.dereference().isPolymorphic() && argMatrixType.getColumns() != null) {
                        values.put(inferredType.getName(), new EvOdinValue(argMatrixType.getColumns(), TsOdinBuiltInTypes.INT));
                    }
                }

                OdinExpression rowsExpression = parMatrixType.getRowsExpression();
                if (rowsExpression != null) {
                    TsOdinType inferredType = rowsExpression.getInferredType();
                    if (inferredType.dereference().isPolymorphic() && argMatrixType.getRows() != null) {
                        values.put(inferredType.getName(), new EvOdinValue(argMatrixType.getRows(), TsOdinBuiltInTypes.INT));
                    }
                }
                doSubstituteTypes(argMatrixType.getElementType(), parMatrixType.getElementType(), resolvedTypes, values);
            } else if (parameterType instanceof TsOdinBitSetType bitSetType &&
                    argumentType instanceof TsOdinBitSetType bitSetType1) {
                doSubstituteTypes(bitSetType1.getElementType(), bitSetType.getElementType(), resolvedTypes, values);
            } else if (parameterType instanceof TsOdinProcedureType procedureType &&
                    argumentType instanceof TsOdinProcedureType procedureType1) {
                if (procedureType.getParameters().size() == procedureType1.getParameters().size()) {

                    // Note: explicit polymorphic parameters are not allowed in this scenario, as they don't make
                    //  sense in a type definition
                    for (int i = 0; i < procedureType.getParameters().size(); i++) {
                        TsOdinParameter parameterParameter = procedureType.getParameters().get(i);
                        TsOdinParameter argumentParameter = procedureType1.getParameters().get(i);
                        doSubstituteTypes(argumentParameter.getType(), parameterParameter.getType(), resolvedTypes, values);
                    }
                }
                if (procedureType.getReturnParameters().size() == procedureType1.getReturnParameters().size()) {
                    for (int i = 0; i < procedureType.getReturnParameters().size(); i++) {
                        TsOdinParameter parameterParameter = procedureType.getReturnParameters().get(i);
                        TsOdinParameter argumentParameter = procedureType1.getReturnParameters().get(i);
                        doSubstituteTypes(argumentParameter.getType(), parameterParameter.getType(), resolvedTypes, values);
                    }
                }
            } // This should be working only structs, unions and procedures
            else if (parameterType instanceof TsOdinGenericType generalizableType && argumentType instanceof TsOdinGenericType generalizableType1) {
                if (generalizableType1.getClass().equals(generalizableType.getClass())) {
                    for (Map.Entry<String, TsOdinType> entry : generalizableType.getResolvedPolymorphicParameters().entrySet()) {
                        TsOdinType nextArgumentType = generalizableType1.getResolvedPolymorphicParameters().getOrDefault(entry.getKey(), TsOdinBuiltInTypes.UNKNOWN);
                        TsOdinType nextParameterType = entry.getValue();
                        doSubstituteTypes(nextArgumentType, nextParameterType, resolvedTypes, values);
                    }
                }
            }
        }
    }

    public record OdinTypeSubstitutionResult(Map<String, EvOdinValue> values, Map<String, TsOdinType> types) {
    }

    @NotNull
    private static TsOdinType resolveArgumentType(OdinExpression argumentExpression, OdinContext context) {
        TsOdinType argumentType = inferTypeInExplicitMode(context, argumentExpression);
        if (argumentType instanceof TsOdinTypeReference typeReference) {
            TsOdinType tsOdinType = typeReference.referencedType();
            if (tsOdinType.isExplicitPolymorphic()) {
                TsOdinPolymorphicType tsOdinPolymorphicType = (TsOdinPolymorphicType) tsOdinType;
                TsOdinType type = context.getPolymorphicType(tsOdinPolymorphicType.getName());
                if (type == null) {
                    return TsOdinBuiltInTypes.UNKNOWN;
                }
                return type;
            }
            return typeReference.getRepresentedType();
        }

        // Case 3: The argument has been resolved to a proper type. Just add the mapping
        return argumentType;
    }

    public static @NotNull String getLocationWithinFile(PsiElement psiElement) {
        PsiFile containingFile = psiElement.getContainingFile();
        if (containingFile != null && containingFile.getVirtualFile() != null) {
            LineColumn lineColumn = StringUtil.offsetToLineColumn(containingFile.getText(), psiElement.getTextOffset());

            return "%s:%d:%d%n".formatted(containingFile.getVirtualFile().getPath(), lineColumn.line, lineColumn.column);
        }
        return "<unknown location>";
    }

    private static @NotNull TsOdinType createPolymorphicType(OdinContext newScope, OdinPolymorphicType polymorphicType) {
        TsOdinType tsOdinType = resolveType(newScope, polymorphicType);
        OdinDeclaredIdentifier declaredIdentifier = polymorphicType.getDeclaredIdentifier();
        tsOdinType.setDeclaration(polymorphicType);
        tsOdinType.setDeclaredIdentifier(declaredIdentifier);
        return tsOdinType;
    }


    public static @NotNull TsOdinStructType specializeAndCacheStruct(OdinContext context,
                                                                     TsOdinStructType structType,
                                                                     @NotNull List<OdinArgument> argumentList) {
        TsOdinStructType specializedType = new TsOdinStructType();
        ArrayList<PsiElement> arguments = new ArrayList<>(argumentList);
        structType.getContext().addSpecializedType(structType, specializedType, arguments);

        boolean specializationCreated = specializeStruct(context,
                argumentList,
                structType,
                specializedType);
        if (!specializationCreated) {
            structType.getContext().getSpecializedTypes().get(structType).remove(argumentList);
            return structType;
        }
        return specializedType;
    }

    public static @NotNull TsOdinUnionType specializeAndCacheUnion(OdinContext context,
                                                                   TsOdinUnionType unionType,
                                                                   @NotNull List<OdinArgument> argumentList) {
        TsOdinUnionType specializedType = new TsOdinUnionType();
        ArrayList<PsiElement> arguments = new ArrayList<>(argumentList);
        unionType.getContext().addSpecializedType(unionType, specializedType, arguments);
        boolean specializationCreated = specializeUnion(context,
                argumentList,
                unionType,
                specializedType);
        if (!specializationCreated) {
            unionType.getContext().getSpecializedTypes().get(unionType).remove(argumentList);
            return unionType;
        }
        return specializedType;
    }

    public static @NotNull TsOdinUnionType specializeUnionOrGetCached(OdinContext context,
                                                                      TsOdinUnionType unionType,
                                                                      @NotNull List<OdinArgument> argumentList) {
        TsOdinType specializedType = context.getSpecializedType(unionType, new ArrayList<>(argumentList));
        if (specializedType == null) {
            specializedType = specializeAndCacheUnion(context, unionType, argumentList);
        }
        return (TsOdinUnionType) specializedType;
    }

    public static @NotNull TsOdinStructType specializeStructOrGetCached(OdinContext context,
                                                                        TsOdinStructType structType,
                                                                        @NotNull List<OdinArgument> argumentList) {
        TsOdinType specializedType = context.getSpecializedType(structType, new ArrayList<>(argumentList));
        if (specializedType == null) {
            specializedType = specializeAndCacheStruct(context, structType, argumentList);
        }
        return (TsOdinStructType) specializedType;
    }
}
