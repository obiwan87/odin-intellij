package com.lasagnerd.odin.insights.typeInference;

import com.lasagnerd.odin.insights.OdinInsightUtils;
import com.lasagnerd.odin.insights.OdinScope;
import com.lasagnerd.odin.insights.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine.inferType;

public class OdinTypeInstantiator {

    public static @NotNull TsOdinStructType instantiateStruct(OdinScope scope, @NotNull List<OdinArgument> arguments, TsOdinStructType structType) {
        List<TsOdinPolyParameter> polyParameters = structType.getPolyParameters();
        if(polyParameters.isEmpty())
            return structType;
        OdinScope newScope = resolveArguments(scope, arguments, polyParameters);

        TsOdinStructType instantiatedType = new TsOdinStructType();
        instantiatedType.setType(structType.getType());
        instantiatedType.setName(structType.getName());
        instantiatedType.setDeclaration(structType.getDeclaration());
        instantiatedType.setDeclaredIdentifier(structType.getDeclaredIdentifier());
        instantiatedType.getFields().putAll(structType.getFields());

        OdinStructType type = structType.type();
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

    public static @NotNull TsOdinProcedureType instantiateProcedure(@NotNull OdinScope scope, List<OdinArgument> arguments, TsOdinProcedureType genericType) {
        List<TsOdinPolyParameter> polyParameters = genericType.getPolyParameters();
        if(polyParameters.isEmpty())
            return genericType;

        OdinScope newScope = resolveArguments(scope, arguments, polyParameters);
        TsOdinProcedureType instantiatedType = new TsOdinProcedureType();
        instantiatedType.setType(genericType.getType());
        instantiatedType.setName(genericType.getName());
        instantiatedType.setDeclaration(genericType.getDeclaration());
        instantiatedType.setDeclaredIdentifier(genericType.getDeclaredIdentifier());

        OdinProcedureType psiType = genericType.type();
        OdinReturnParameters returnParameters = psiType.getReturnParameters();
        if (returnParameters != null) {
            OdinParamEntries paramEntries = returnParameters.getParamEntries();
            if (paramEntries != null) {
                for (OdinParamEntry odinParamEntry : paramEntries.getParamEntryList()) {
                    OdinType type = odinParamEntry.getParameterDeclaration().getTypeDefinition().getType();
                    TsOdinType tsOdinType = OdinTypeResolver.resolveType(newScope, type);
                    for (OdinParameter ignored : odinParamEntry.getParameterDeclaration().getParameterList()) {
                        instantiatedType.getReturnTypes().add(tsOdinType);
                    }
                }
            }

            OdinTypeDefinitionExpression typeDefinitionExpression = psiType.getReturnParameters().getTypeDefinitionExpression();
            if(typeDefinitionExpression != null) {
               OdinType returnType = typeDefinitionExpression.getType();
               TsOdinType tsOdinType = OdinTypeResolver.resolveType(newScope, returnType);
               if(tsOdinType != null) {
                   instantiatedType.getReturnTypes().add(tsOdinType);
               }
            }
        }



        return instantiatedType;
    }

    private static @NotNull OdinScope resolveArguments(OdinScope currentScope, List<OdinArgument> arguments, List<TsOdinPolyParameter> polyParameters) {
        OdinScope newScope = new OdinScope();
        newScope.addSymbols(currentScope);
        if (!arguments.isEmpty()) {
            for (int i = 0; i < arguments.size(); i++) {
                final int currentIndex = i;
                OdinArgument odinArgument = arguments.get(i);

                OdinExpression argumentExpression = null;
                TsOdinPolyParameter polyParameter = null;

                if (odinArgument instanceof OdinUnnamedArgument argument) {
                    argumentExpression = argument.getExpression();
                    polyParameter = polyParameters.stream()
                            .filter(p -> p.getIndex() == currentIndex)
                            .findFirst().orElse(null);
                }

                if(odinArgument instanceof OdinNamedArgument argument) {
                    polyParameter = polyParameters.stream()
                            .filter(p -> argument.getIdentifierToken().getText().equals(p.getValueName()))
                            .findFirst().orElse(null);
                    argumentExpression = argument.getExpression();
                }

                if(argumentExpression == null || polyParameter == null)
                    continue;;

                OdinTypeInferenceResult odinTypeInferenceResult = inferType(currentScope, argumentExpression);
                TsOdinType argumentType = odinTypeInferenceResult.getType();

                // This is only valid if poly parameter type is built-in type typeid

                if (argumentType instanceof TsOdinMetaType metaType && polyParameter.getType().isTypeId()) {
                    TsOdinType resolvedMetaType = OdinTypeResolver.resolveMetaType(currentScope, metaType);
                    if (resolvedMetaType != null) {
                        newScope.addType(polyParameter.getValueName(), resolvedMetaType);
                    }
                }

                // TODO else, add value of parameter to scope

                if (polyParameter.getType() instanceof OdinPolymorphicType polymorphicType) {
                    String parameterName = polymorphicType.getIdentifier().getIdentifierToken().getText();
                    if (argumentType != null) {
                        newScope.addType(parameterName, argumentType);
                    }
                }
            }
        }
        return newScope;
    }
}
