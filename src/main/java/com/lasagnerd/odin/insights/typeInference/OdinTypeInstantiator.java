package com.lasagnerd.odin.insights.typeInference;

import com.lasagnerd.odin.insights.OdinInsightUtils;
import com.lasagnerd.odin.insights.OdinScope;
import com.lasagnerd.odin.insights.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine.inferType;

public class OdinTypeInstantiator {

    public static @NotNull TsOdinStructType instantiateStruct(OdinScope scope, @NotNull List<OdinArgument> arguments, TsOdinStructType baseType) {
        List<TsOdinParameter> parameters = baseType.getParameters();
        if (parameters.isEmpty())
            return baseType;

        TsOdinStructType instantiatedType = new TsOdinStructType();
        OdinScope newScope = resolveArguments(scope, baseType, instantiatedType, arguments);
        instantiatedType.setLocalScope(newScope);
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

    public static @NotNull TsOdinProcedureType instantiateProcedure(@NotNull OdinScope scope, List<OdinArgument> arguments, TsOdinProcedureType baseType) {
        List<TsOdinParameter> polyParameters = baseType.getParameters();
        if (polyParameters.isEmpty())
            return baseType;

        TsOdinProcedureType instantiatedType = new TsOdinProcedureType();
        OdinScope newScope = resolveArguments(scope, baseType, instantiatedType, arguments);
        instantiatedType.setType(baseType.getType());
        instantiatedType.setName(baseType.getName());
        instantiatedType.setDeclaration(baseType.getDeclaration());
        instantiatedType.setDeclaredIdentifier(baseType.getDeclaredIdentifier());

        OdinProcedureType psiType = baseType.type();
        OdinReturnParameters returnParameters = psiType.getReturnParameters();
        if (returnParameters != null) {
            var paramEntries = returnParameters.getParamEntryList();
            for (OdinParamEntry odinParamEntry : paramEntries) {
                OdinType type = odinParamEntry.getParameterDeclaration().getTypeDefinition().getType();
                TsOdinType tsOdinType = OdinTypeResolver.resolveType(newScope, type);
                for (OdinParameter ignored : odinParamEntry.getParameterDeclaration().getParameterList()) {
                    instantiatedType.getReturnTypes().add(tsOdinType);
                }
            }

            OdinTypeDefinitionExpression typeDefinitionExpression = psiType.getReturnParameters().getTypeDefinitionExpression();
            if (typeDefinitionExpression != null) {
                OdinType returnType = typeDefinitionExpression.getType();
                TsOdinType tsOdinType = OdinTypeResolver.resolveType(newScope, returnType);
                if (tsOdinType != null) {
                    instantiatedType.getReturnTypes().add(tsOdinType);
                }
            }
        }


        return instantiatedType;
    }

    private static @NotNull OdinScope resolveArguments(
            OdinScope scope,
            TsOdinType baseType,
            TsOdinType instantiatedType,
            List<OdinArgument> arguments
    ) {
        OdinScope newScope = new OdinScope();
        newScope.putAll(scope);
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

                OdinTypeInferenceResult odinTypeInferenceResult = inferType(newScope, argumentExpression);
                TsOdinType argumentType = odinTypeInferenceResult.getType();
                if (argumentType == null) {
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
                    if (argumentType instanceof TsOdinMetaType metaType && parameterType.isTypeId()) {
                        TsOdinType resolvedMetaType = OdinTypeResolver.resolveMetaType(newScope, metaType);
                        if (resolvedMetaType != null) {
                            instantiatedType.getUnresolvedPolymorphicParameters().putAll(resolvedMetaType.getUnresolvedPolymorphicParameters());
                            instantiatedType.getResolvedPolymorphicParameters().put(tsOdinParameter.getValueName(), resolvedMetaType);
                            newScope.addType(tsOdinParameter.getValueName(), resolvedMetaType);
                        }
                    }

                    if (argumentExpression instanceof OdinTypeDefinitionExpression typeDefinitionExpression) {
                        if (typeDefinitionExpression.getType() instanceof OdinPolymorphicType polymorphicType) {
                            TsOdinType tsOdinType = OdinTypeResolver.resolveType(newScope, polymorphicType);
                            if (tsOdinType != null) {
                                OdinDeclaredIdentifier declaredIdentifier = polymorphicType.getDeclaredIdentifier();
                                tsOdinType.setDeclaration(polymorphicType);
                                tsOdinType.setDeclaredIdentifier(declaredIdentifier);
                                instantiatedType.getUnresolvedPolymorphicParameters().put(declaredIdentifier.getName(), tsOdinType);
                                instantiatedType.getResolvedPolymorphicParameters().put(tsOdinParameter.getValueName(), tsOdinType);
                                newScope.addType(tsOdinParameter.getValueName(), tsOdinType);
                            }
                        }
                    }
                }

                // argument List(A) {} -> List(A)
                // parameterType List($T) -> add entry T->A (how to get there)

                // This is the part where it all falls apart: it is wrong to ask whether the parameter is polymorphic
                // What we have to do here try to solve whatever is in polymorphic scope... those are the newly introduced
                // polymorphic parameters.
                // The information I need: which parameter of the base type has been mapped and to what type has it been mapped
                // e.g.
                // List(Point): $Item -> Point
                // List($T)   : $Item  -> $T
                // I need to know that T is still unsolved in List($T)

                Map<String, TsOdinType> resolvedTypes = new HashMap<>();
                findResolvedTypes(parameterType, argumentType, resolvedTypes);
                instantiatedType.getResolvedPolymorphicParameters().putAll(resolvedTypes);
                for (Map.Entry<String, TsOdinType> entry : resolvedTypes.entrySet()) {
                    newScope.addType(entry.getKey(), entry.getValue());
                }

            }
        }
        return newScope;
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

}
