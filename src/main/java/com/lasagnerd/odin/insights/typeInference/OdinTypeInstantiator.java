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
                    System.out.println("Could not resolve argument type");
                    continue;
                }

                // This is only valid if poly parameter type is built-in type typeid
                TsOdinType parameterType = tsOdinParameter.getType();
                if (parameterType == null) {
                    System.out.println("Could not resolve parameter type");
                    continue;
                }

                if (tsOdinParameter.isValuePolymorphic()) {
                    instantiatedType.getResolvedPolymorphicParameters().put(tsOdinParameter.getValueName(), argumentType);
                    instantiationScope.addType(tsOdinParameter.getValueName(), argumentType);
                }

                // The method findResolvedTypes maps $T -> Point in the example below
                // List(Point): $Item -> Point
                // List($T)   : $Item  -> $T
                Map<String, TsOdinType> resolvedTypes = new HashMap<>();
                findResolvedTypes(parameterType, argumentType, resolvedTypes);
                instantiatedType.getResolvedPolymorphicParameters().putAll(resolvedTypes);
                for (Map.Entry<String, TsOdinType> entry : resolvedTypes.entrySet()) {
                    instantiationScope.addType(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private static void findResolvedTypes(@NotNull TsOdinType parameterType, @NotNull TsOdinType argumentType, @NotNull Map<String, TsOdinType> resolvedTypes) {
        if (parameterType.isPolymorphic() && !argumentType.isPolymorphic()) {
            resolvedTypes.put(parameterType.getName(), argumentType);
        } else {
            for (Map.Entry<String, TsOdinType> entry : parameterType.getResolvedPolymorphicParameters().entrySet()) {
                TsOdinType nextArgumentType = argumentType.getResolvedPolymorphicParameters().getOrDefault(entry.getKey(), TsOdinType.UNKNOWN);
                TsOdinType nextParameterType = entry.getValue();
                findResolvedTypes(nextParameterType, nextArgumentType, resolvedTypes);
            }
        }
    }

    @NotNull
    private static TsOdinType resolveArgumentType(OdinExpression argumentExpression, TsOdinParameter parameter, OdinScope newScope) {
        TsOdinType parameterType = parameter.getType();
        OdinTypeInferenceResult odinTypeInferenceResult = inferType(newScope, argumentExpression);
        TsOdinType argumentType = odinTypeInferenceResult.getType();
        if (argumentType instanceof TsOdinMetaType metaType && parameterType.isTypeId()) {
            TsOdinType resolvedMetaType = OdinTypeResolver.resolveMetaType(newScope, metaType);
            return Objects.requireNonNullElse(resolvedMetaType, TsOdinType.UNKNOWN);
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
