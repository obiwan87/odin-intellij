package com.lasagnerd.odin.insights.typeInference;

import com.lasagnerd.odin.insights.OdinInsightUtils;
import com.lasagnerd.odin.insights.OdinScope;
import com.lasagnerd.odin.insights.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine.inferType;

public class OdinTypeInstantiator {

    public static @NotNull TsOdinStructType instantiateStruct(OdinScope outerScope,
                                                              @NotNull List<OdinArgument> arguments,
                                                              TsOdinStructType baseType) {
        List<TsOdinParameter> parameters = baseType.getParameters();
        if (parameters.isEmpty())
            return baseType;

        TsOdinStructType instantiatedType = new TsOdinStructType();
        instantiatedType.getScope().putAll(baseType.getScope());

        OdinScope newScope = instantiatedType.getScope();
        resolveArguments(outerScope, baseType, instantiatedType, arguments);
        instantiatedType.setType(baseType.getType());
        instantiatedType.setName(baseType.getName());
        instantiatedType.setDeclaration(baseType.getDeclaration());
        instantiatedType.setDeclaredIdentifier(baseType.getDeclaredIdentifier());
        instantiatedType.getFields().putAll(baseType.getFields());
        instantiatedType.setParameters(baseType.getParameters());

        OdinStructType type = baseType.type();
        List<OdinFieldDeclarationStatement> fieldDeclarations = OdinInsightUtils.getStructFieldsDeclarationStatements(type);
        for (OdinFieldDeclarationStatement fieldDeclaration : fieldDeclarations) {
            OdinTypeDefinitionExpression typeDefinition = fieldDeclaration.getTypeDefinition();
            TsOdinType tsOdinType = OdinTypeResolver.resolveType(newScope, typeDefinition.getType());
            for (OdinDeclaredIdentifier declaredIdentifier : fieldDeclaration.getDeclaredIdentifiers()) {
                instantiatedType.getFields().put(declaredIdentifier.getName(), tsOdinType);
            }
        }

        return instantiatedType;
    }

    public static @NotNull TsOdinProcedureType instantiateProcedure(@NotNull OdinScope outerScope,
                                                                    List<OdinArgument> arguments,
                                                                    TsOdinProcedureType baseType) {
        List<TsOdinParameter> parameters = baseType.getParameters();
        if (parameters.isEmpty())
            return baseType;

        TsOdinProcedureType instantiatedType = new TsOdinProcedureType();
        instantiatedType.getScope().putAll(baseType.getScope());
        instantiatedType.setType(baseType.getType());
        instantiatedType.setName(baseType.getName());
        instantiatedType.setDeclaration(baseType.getDeclaration());
        instantiatedType.setDeclaredIdentifier(baseType.getDeclaredIdentifier());
        instantiatedType.setReturnParameters(baseType.getReturnParameters());
        resolveArguments(outerScope, baseType, instantiatedType, arguments);

        for (TsOdinParameter tsOdinReturnType : baseType.getReturnParameters()) {
            TsOdinType tsOdinType = OdinTypeResolver.resolveType(instantiatedType.getScope(), tsOdinReturnType.getTypeDefinitionExpression().getType());
            instantiatedType.getReturnTypes().add(tsOdinType);
        }

        return instantiatedType;
    }

    public static TsOdinType instantiateUnion(OdinScope outerScope, List<OdinArgument> arguments, TsOdinUnionType baseType) {
        List<TsOdinParameter> parameters = baseType.getParameters();
        if (parameters.isEmpty())
            return baseType;

        TsOdinUnionType instantiatedType = new TsOdinUnionType();
        instantiatedType.getScope().putAll(baseType.getScope());
        instantiatedType.setType(baseType.getType());
        instantiatedType.setName(baseType.getName());
        instantiatedType.setDeclaration(baseType.getDeclaration());
        instantiatedType.setDeclaredIdentifier(baseType.getDeclaredIdentifier());
        resolveArguments(outerScope, baseType, instantiatedType, arguments);


        for (TsOdinUnionVariant baseField : baseType.getVariants()) {
            TsOdinType instantiatedFieldType = OdinTypeResolver.resolveType(instantiatedType.getScope(), baseField.getTypeDefinitionExpression().getType());
            TsOdinUnionVariant instantiatedField = new TsOdinUnionVariant();
            instantiatedField.setTypeDefinitionExpression(baseField.getTypeDefinitionExpression());
            instantiatedField.setType(instantiatedFieldType);
            instantiatedType.getVariants().add(instantiatedField);
        }

        return instantiatedType;
    }

    private static void resolveArguments(
            OdinScope outerScope,
            TsOdinType baseType,
            TsOdinType instantiatedType,
            List<OdinArgument> arguments
    ) {
        OdinScope instantiationScope = instantiatedType.getScope();

        if (!arguments.isEmpty()) {
            for (int i = 0; i < arguments.size(); i++) {
                final int currentIndex = i;
                OdinArgument odinArgument = arguments.get(i);

                OdinExpression argumentExpression = null;
                TsOdinParameter tsOdinParameter = null;

                if (odinArgument instanceof OdinUnnamedArgument argument) {
                    argumentExpression = argument.getExpression();
                    tsOdinParameter = baseType.getParameters().stream()
                            .filter(p -> p.getIndex() == currentIndex)
                            .findFirst().orElse(null);
                }

                if (odinArgument instanceof OdinNamedArgument argument) {
                    tsOdinParameter = baseType.getParameters().stream()
                            .filter(p -> argument.getIdentifierToken().getText().equals(p.getValueName()))
                            .findFirst().orElse(null);
                    argumentExpression = argument.getExpression();
                }

                if (argumentExpression == null || tsOdinParameter == null)
                    continue;

                TsOdinType argumentType = resolveArgumentType(argumentExpression, tsOdinParameter, outerScope);
                if (argumentType.isUnknown()) {
                    System.out.printf("Could not resolve argument [%s] type for base type %s with name %s%n", tsOdinParameter.getValueName(), baseType.getClass().getSimpleName(), baseType.getName());
                    continue;
                }

                // This is only valid if poly parameter type is built-in type typeid
                TsOdinType parameterType = tsOdinParameter.getType();
                if (parameterType == null) {
                    System.out.println("Could not resolve parameter type");
                    continue;
                }

                if (tsOdinParameter.isValuePolymorphic()) {
                    instantiatedType.getPolymorphicParameters().put(tsOdinParameter.getValueName(), argumentType);
                    instantiationScope.addType(tsOdinParameter.getValueName(), argumentType);
                }


                Map<String, TsOdinType> resolvedTypes = substituteTypes(parameterType, argumentType);
                instantiatedType.getPolymorphicParameters().putAll(resolvedTypes);
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
     *                              |                   |
     *                              v                   v
     * argument:              List(Point)   : $Item  -> Point
     * @param parameterType The parameter type
     * @param argumentType The argument type
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
            else {
                for (Map.Entry<String, TsOdinType> entry : parameterType.getPolymorphicParameters().entrySet()) {
                    TsOdinType nextArgumentType = argumentType.getPolymorphicParameters().getOrDefault(entry.getKey(), TsOdinType.UNKNOWN);
                    TsOdinType nextParameterType = entry.getValue();
                    doSubstituteTypes(nextParameterType, nextArgumentType, resolvedTypes);
                }
            }
        }
    }

    @NotNull
    private static TsOdinType resolveArgumentType(OdinExpression argumentExpression, TsOdinParameter parameter, OdinScope newScope) {
        TsOdinType parameterType = parameter.getType();
        OdinTypeInferenceResult odinTypeInferenceResult = inferType(newScope, argumentExpression);
        TsOdinType argumentType = odinTypeInferenceResult.getType();
        if (argumentType instanceof TsOdinMetaType metaType && (parameterType.isTypeId()
                || parameterType.getMetaType() == metaType.getRepresentedMetaType())) {
            return OdinTypeResolver.resolveMetaType(newScope, metaType);
        }

        // Case 2: The argumentExpression is a polymorphic type. In that case we know its type already and
        // introduce a new unresolved polymorphic type and map the parameter to that
        else if (argumentExpression instanceof OdinTypeDefinitionExpression typeDefinitionExpression) {
            if (typeDefinitionExpression.getType() instanceof OdinPolymorphicType polymorphicType) {
                return createPolymorphicType(newScope, polymorphicType);
            }
            return TsOdinType.UNKNOWN;
        }
        // Case 3: The argument has been resolved to a proper type. Just add the mapping
        return Objects.requireNonNullElse(argumentType, TsOdinType.UNKNOWN);
    }

    private static @NotNull TsOdinType createPolymorphicType(OdinScope newScope, OdinPolymorphicType polymorphicType) {
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(newScope, polymorphicType);
        OdinDeclaredIdentifier declaredIdentifier = polymorphicType.getDeclaredIdentifier();
        tsOdinType.setDeclaration(polymorphicType);
        tsOdinType.setDeclaredIdentifier(declaredIdentifier);
        return tsOdinType;
    }


}
