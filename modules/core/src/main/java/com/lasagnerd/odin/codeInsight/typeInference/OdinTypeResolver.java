package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine.doInferType;
import static com.lasagnerd.odin.codeInsight.typeSystem.TsOdinBuiltInTypes.RESERVED_TYPES;

@EqualsAndHashCode(callSuper = true)
public class OdinTypeResolver extends OdinVisitor {

    public static @NotNull TsOdinType resolveType(OdinSymbolTable symbolTable, @NotNull OdinType type) {
        return resolveType(0, symbolTable, null, null, type);
    }

    public static @NotNull TsOdinType resolveType(OdinSymbolTable symbolTable,
                                                  OdinDeclaredIdentifier declaredIdentifier,
                                                  OdinDeclaration declaration,
                                                  @NotNull OdinType type) {
        return resolveType(0, symbolTable, declaredIdentifier, declaration, type);
    }

    public static @NotNull TsOdinType resolveType(int level,
                                                  OdinSymbolTable symbolTable,
                                                  OdinDeclaredIdentifier declaredIdentifier,
                                                  OdinDeclaration declaration,
                                                  OdinType type) {
        OdinTypeResolver typeResolver = new OdinTypeResolver(level, symbolTable, declaration, declaredIdentifier);
        type.accept(typeResolver);
        return Objects.requireNonNullElse(typeResolver.type, TsOdinType.UNKNOWN);
    }

    public static @NotNull TsOdinMetaType findMetaType(OdinSymbolTable symbolTable,
                                                       @NotNull OdinType type) {
        return findMetaType(symbolTable, null, null, type);
    }

    public static @NotNull TsOdinMetaType findMetaType(OdinSymbolTable symbolTable,
                                                       OdinDeclaredIdentifier declaredIdentifier,
                                                       OdinDeclaration declaration,
                                                       @NotNull OdinType type) {
        if (type instanceof OdinQualifiedType || type instanceof OdinSimpleRefType) {
            TsOdinType tsOdinType = resolveType(symbolTable, declaredIdentifier, declaration, type);
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(tsOdinType.getMetaType());
            tsOdinMetaType.setName(tsOdinType.getName());
            tsOdinMetaType.setDeclaredIdentifier(tsOdinMetaType.getDeclaredIdentifier());
            tsOdinMetaType.setDeclaration(tsOdinType.getDeclaration());
            tsOdinMetaType.setSymbolTable(tsOdinType.getSymbolTable());
            tsOdinMetaType.setRepresentedType(tsOdinType);
            return tsOdinMetaType;
        }

        TsOdinMetaType tsOdinMetaType = OdinMetaTypeResolver.resolveMetaType(type);
        tsOdinMetaType.setDeclaredIdentifier(declaredIdentifier);
        tsOdinMetaType.setDeclaration(declaration);
        tsOdinMetaType.setName(declaredIdentifier != null ? declaredIdentifier.getName() : null);
        return tsOdinMetaType;
    }

    public static @NotNull TsOdinType resolveMetaType(OdinSymbolTable symbolTable, TsOdinMetaType metaType) {
        return resolveMetaType(0, symbolTable, metaType);
    }

    public static TsOdinType resolveMetaType(int level, OdinSymbolTable symbolTable, TsOdinMetaType metaType) {
        if (metaType.getRepresentedType() instanceof TsOdinBuiltInType) {
            return TsOdinBuiltInTypes.getBuiltInType(metaType.getName());
        } else {
            TsOdinType tsOdinType;
            if (metaType.getRepresentedType() != null) {
                return metaType.getRepresentedType();
            } else if (metaType.getType() != null) {
                OdinDeclaredIdentifier declaredIdentifier = metaType.getDeclaredIdentifier();
                tsOdinType = resolveType(level,
                        symbolTable,
                        declaredIdentifier,
                        metaType.getDeclaration(),
                        metaType.getType());
                tsOdinType.setDeclaration(metaType.getDeclaration());
                tsOdinType.setDeclaredIdentifier(declaredIdentifier);
                symbolTable.addKnownType(declaredIdentifier, tsOdinType);
                if (declaredIdentifier != null) {
                    tsOdinType.setName(declaredIdentifier.getName());
                }
                tsOdinType.getSymbolTable().putAll(symbolTable);
                return tsOdinType;
            }
        }
        return TsOdinType.UNKNOWN;
    }

    // Result
    TsOdinType type;

    private final int level;
    private final OdinSymbolTable symbolTable;
    private final OdinDeclaration typeDeclaration;
    private final OdinDeclaredIdentifier typeDeclaredIdentifier;

    // avoid stackoverflow when encountering circular references
    private final Set<OdinDeclaredIdentifier> visitedDeclaredIdentifiers = new HashSet<>();


    public OdinTypeResolver(int level, OdinSymbolTable symbolTable, OdinDeclaration typeDeclaration, OdinDeclaredIdentifier typeDeclaredIdentifier) {
        this.level = level;
        this.symbolTable = symbolTable;
        this.typeDeclaration = typeDeclaration;
        this.typeDeclaredIdentifier = typeDeclaredIdentifier;
    }

    // resolve type calls
    public @NotNull TsOdinType doResolveType(OdinSymbolTable symbolTable,
                                             OdinDeclaredIdentifier declaredIdentifier,
                                             OdinDeclaration declaration,
                                             @NotNull OdinType type) {
        return resolveType(level + 1, symbolTable, declaredIdentifier, declaration, type);
    }

    public @NotNull TsOdinType doResolveType(OdinSymbolTable symbolTable,
                                             @NotNull OdinType type) {
        return resolveType(level + 1, symbolTable, null, null, type);
    }

    public @NotNull TsOdinType doResolveMetaType(OdinSymbolTable symbolTable, TsOdinMetaType metaType) {
        return resolveMetaType(level + 1, symbolTable, metaType);
    }

    public @NotNull TsOdinType doResolveType(OdinSymbolTable symbolTable, OdinExpression odinExpression) {
        TsOdinType tsOdinType = doInferType(symbolTable, odinExpression);
        if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
            return doResolveMetaType(symbolTable, tsOdinMetaType);
        }
        return tsOdinType;
    }

    // logging
    @SuppressWarnings("unused")
    public void log(String message) {
//        System.out.println("\t".repeat(level) + message);
    }

    private List<TsOdinParameter> createParameters(TsOdinType baseType, List<OdinParamEntry> paramEntries, OdinSymbolTable localScope) {
        List<TsOdinParameter> typeParameters = new ArrayList<>();
        int k = 0;
        for (var paramEntry : paramEntries) {
            OdinParameterDeclaration parameterDeclaration = paramEntry.getParameterDeclaration();

            // First, add all $Identifier expressions we encounter in this parameter to the current scope

            // Implicit polymorphism
            for (OdinPolymorphicType odinPolymorphicType : PsiTreeUtil.findChildrenOfType(paramEntry, OdinPolymorphicType.class)) {
                TsOdinType tsOdinType = doResolveType(localScope, odinPolymorphicType);
                localScope.addType(tsOdinType.getName(), tsOdinType);
                localScope.add(odinPolymorphicType.getDeclaredIdentifier());
                if (baseType instanceof TsOdinGenericType tsOdinGenericType) {
                    tsOdinGenericType.getPolymorphicParameters().put(tsOdinType.getName(), tsOdinType);
                }
            }

            // Explicit polymorphism
            // TODO this will also find the polymorphic types of above, but they will filtered out by the declaredIdentifier.getDollar() != null check
            for (OdinDeclaredIdentifier declaredIdentifier : PsiTreeUtil.findChildrenOfType(paramEntry, OdinDeclaredIdentifier.class)) {
                if (declaredIdentifier.getDollar() != null) {
                    TsOdinPolymorphicType valuePolymorphicType = new TsOdinPolymorphicType();
                    String name = declaredIdentifier.getName();
                    valuePolymorphicType.setName(name);
                    valuePolymorphicType.setDeclaredIdentifier(declaredIdentifier);
                    localScope.addType(valuePolymorphicType.getName(), valuePolymorphicType);
                    localScope.add(declaredIdentifier);
                    if (baseType instanceof TsOdinGenericType tsOdinGenericType) {
                        tsOdinGenericType.getPolymorphicParameters().put(valuePolymorphicType.getName(), valuePolymorphicType);
                    }
                }
            }

            List<TsOdinParameterSpec> parameterSpecs = TsOdinParameterSpec.from(parameterDeclaration);
            for (var declarationSpec : parameterSpecs) {
                TsOdinParameter tsOdinParameter = mapSpecToParameter(localScope, declarationSpec, k);
                typeParameters.add(tsOdinParameter);
                k++;
            }
        }

        return typeParameters;
    }

    private void initializeNamedType(TsOdinType tsOdinType) {

        tsOdinType.setDeclaredIdentifier(typeDeclaredIdentifier);
        String name = typeDeclaredIdentifier != null ? typeDeclaredIdentifier.getName() : null;
        tsOdinType.setName(name);
        tsOdinType.setDeclaration(typeDeclaration);
        if (typeDeclaredIdentifier != null) {
            this.symbolTable.addKnownType(typeDeclaredIdentifier, tsOdinType);
        }
        tsOdinType.getSymbolTable().putAll(symbolTable);
        tsOdinType.getSymbolTable().setPackagePath(symbolTable.getPackagePath());
        log("Initialized " + tsOdinType.getClass().getSimpleName() + " with name " + name);
    }

    private void resolveIdentifier(OdinIdentifier typeIdentifier) {
        PsiNamedElement declaration;
        String identifierText = typeIdentifier.getText();

        // TODO reserved types should be checked last
        //  in Odin you can define int :: struct {x,y: f32}
        if (RESERVED_TYPES.contains(identifierText)) {
            type = TsOdinBuiltInTypes.getBuiltInType(identifierText);

        } else {
            TsOdinType scopeType = symbolTable.getType(typeIdentifier.getIdentifierToken().getText());
            if (scopeType != null) {
                type = scopeType;
            } else {
                declaration = symbolTable.getNamedElement(typeIdentifier.getIdentifierToken().getText());
                if (!(declaration instanceof OdinDeclaredIdentifier declaredIdentifier)) {
                    return;
                }
                var knownType = symbolTable.getKnownTypes().get(declaredIdentifier);
                if (knownType != null) {
                    log("Cache hit for type: " + knownType.getLabel());
                    this.type = knownType;
                } else {
                    type = resolveTypeFromDeclaredIdentifier(symbolTable, declaredIdentifier);
                }
            }
        }
    }

    private TsOdinType resolveTypeFromDeclaredIdentifier(OdinSymbolTable symbolTable, OdinDeclaredIdentifier identifier) {
        OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(identifier,
                false,
                OdinDeclaration.class);

        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            return doResolveType(symbolTable, identifier, odinDeclaration, structDeclarationStatement.getStructType());
        } else if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            return doResolveType(symbolTable, identifier, odinDeclaration, enumDeclarationStatement.getEnumType());
        } else if (odinDeclaration instanceof OdinUnionDeclarationStatement unionDeclarationStatement) {
            return doResolveType(symbolTable, identifier, odinDeclaration, unionDeclarationStatement.getUnionType());
        } else if (odinDeclaration instanceof OdinProcedureDeclarationStatement procedureDeclarationStatement) {
            return doResolveType(symbolTable, identifier, odinDeclaration, procedureDeclarationStatement.getProcedureDefinition().getProcedureType());
        } else if (odinDeclaration instanceof OdinConstantInitializationStatement constantInitializationStatement) {
            List<OdinExpression> expressionList = constantInitializationStatement.getExpressionsList().getExpressionList();
            if (!expressionList.isEmpty()) {
                int index = constantInitializationStatement.getDeclaredIdentifiers().indexOf(identifier);
                if (index == -1) {
                    return TsOdinType.UNKNOWN;
                }
                if (expressionList.size() <= index) {
                    return TsOdinType.UNKNOWN;
                }

                OdinExpression odinExpression = expressionList.get(index);
                TsOdinType tsOdinType = doInferType(symbolTable, odinExpression);
                if (tsOdinType instanceof TsOdinMetaType metaType) {
                    return doResolveMetaType(symbolTable, metaType);
                }
                return TsOdinType.UNKNOWN;
            }
        } else if (odinDeclaration instanceof OdinPolymorphicType polymorphicType) {
            return doResolveType(symbolTable, identifier, odinDeclaration, polymorphicType);
        } else if (odinDeclaration instanceof OdinBitsetDeclarationStatement odinBitsetDeclarationStatement) {
            return doResolveType(symbolTable, odinBitsetDeclarationStatement.getBitSetType());
        }

        return TsOdinType.UNKNOWN;
    }

    // Visitor methods
    @Override
    public void visitQualifiedType(@NotNull OdinQualifiedType qualifiedType) {
        OdinSymbolTable packageScope = symbolTable.getScopeOfImport(qualifiedType.getPackageIdentifier().getIdentifierToken().getText());
        OdinSimpleRefType simpleRefType = qualifiedType.getSimpleRefType();
        if (simpleRefType != null) {
            this.type = doResolveType(packageScope, simpleRefType);
        }
    }

    @Override
    public void visitSimpleRefType(@NotNull OdinSimpleRefType o) {
        OdinIdentifier identifier = o.getIdentifier();
        resolveIdentifier(identifier);
    }

    @Override
    public void visitCallType(@NotNull OdinCallType o) {
        OdinType type = o.getType();
        this.type = doResolveType(symbolTable, type);

        if (this.type instanceof TsOdinStructType structType) {
            // This should not be called again if type is already been visited
            // it might be a better idea to specialize the type at inference time and not at resolution time
            this.type = OdinTypeSpecializer.specializeStructOrGetCached(symbolTable, structType, o.getArgumentList());
        }

        if (this.type instanceof TsOdinUnionType unionType) {
            // This should not be called again if type is already been visited
            this.type = OdinTypeSpecializer.specializeUnionOrGetCached(symbolTable, unionType, o.getArgumentList());
        }
    }

    @Override
    public void visitMatrixType(@NotNull OdinMatrixType o) {
        if (o.getType() != null) {
            this.type = doResolveType(symbolTable, o.getType());
        }
    }

    @Override
    public void visitSliceType(@NotNull OdinSliceType o) {
        OdinType elementPsiType = o.getType();
        TsOdinSliceType tsOdinSliceType = new TsOdinSliceType();
        if (elementPsiType != null) {
            TsOdinType tsOdinElementType = OdinTypeResolver.resolveType(symbolTable, elementPsiType);
            tsOdinSliceType.setElementType(tsOdinElementType);
            this.type = tsOdinSliceType;
        }
    }

    @Override
    public void visitMultiPointerType(@NotNull OdinMultiPointerType o) {
        TsOdinMultiPointerType tsOdinMultiPointerType = new TsOdinMultiPointerType();
        tsOdinMultiPointerType.setType(o);
        TsOdinType dereferencedType = resolveType(symbolTable, Objects.requireNonNull(o.getType()));
        tsOdinMultiPointerType.setDereferencedType(dereferencedType);

        this.type = tsOdinMultiPointerType;
    }

    @Override
    public void visitBitSetType(@NotNull OdinBitSetType o) {
        TsOdinBitSetType tsOdinBitSetType = new TsOdinBitSetType();
        OdinExpression elementTypeExpression = o.getExpression();
        TsOdinType tsOdinElementType = doResolveType(symbolTable, elementTypeExpression);
        tsOdinBitSetType.setElementType(tsOdinElementType);

        if (o.getType() != null) {
            TsOdinType tsBackingType = doResolveType(symbolTable, o.getType());
            tsOdinBitSetType.setBackingType(tsBackingType);
        }
        this.type = tsOdinBitSetType;

    }

    @Override
    public void visitUnionType(@NotNull OdinUnionType unionType) {
        TsOdinUnionType tsOdinUnionType = new TsOdinUnionType();
        tsOdinUnionType.setType(unionType);
        initializeNamedType(tsOdinUnionType);

        List<TsOdinParameter> parameters = createParameters(tsOdinUnionType, unionType.getParamEntryList(), tsOdinUnionType.getSymbolTable());
        tsOdinUnionType.setParameters(parameters);

        OdinUnionBody unionBody = unionType.getUnionBlock().getUnionBody();
        if (unionBody != null) {
            List<OdinType> types = unionBody.getTypeList();
            for (OdinType type : types) {
                TsOdinType tsOdinType = doResolveType(tsOdinUnionType.getSymbolTable(), type);

                TsOdinUnionVariant tsOdinUnionVariant = new TsOdinUnionVariant();
                tsOdinUnionVariant.setPsiType(type);
                tsOdinUnionVariant.setType(tsOdinType);
                tsOdinUnionType.getVariants().add(tsOdinUnionVariant);
            }
        }
        this.type = tsOdinUnionType;
    }

    @Override
    public void visitArrayType(@NotNull OdinArrayType arrayType) {
        TsOdinArrayType tsOdinArrayType = new TsOdinArrayType();
        tsOdinArrayType.setPsiSizeElement(arrayType.getArraySize());
        tsOdinArrayType.setType(arrayType);
        TsOdinType elementType = resolveType(symbolTable, arrayType.getTypeDefinition());
        tsOdinArrayType.setElementType(elementType);

        this.type = tsOdinArrayType;
    }

    @Override
    public void visitMapType(@NotNull OdinMapType mapType) {
        TsOdinMapType tsOdinMapType = new TsOdinMapType();
        tsOdinMapType.setSymbolTable(symbolTable);
        tsOdinMapType.setType(mapType);
        TsOdinType keyType = doResolveType(symbolTable, mapType.getKeyType());
        tsOdinMapType.setKeyType(keyType);

        TsOdinType valueType = doResolveType(symbolTable, mapType.getValueType());
        tsOdinMapType.setValueType(valueType);
        this.type = tsOdinMapType;
    }

    @Override
    public void visitPointerType(@NotNull OdinPointerType odinPointerType) {
        TsOdinPointerType tsOdinPointerType = new TsOdinPointerType();
        tsOdinPointerType.setType(odinPointerType);

        TsOdinType elementType = doResolveType(symbolTable, Objects.requireNonNull(odinPointerType.getType()));
        tsOdinPointerType.setDereferencedType(elementType);
        tsOdinPointerType.getSymbolTable().putAll(symbolTable);
        tsOdinPointerType.getSymbolTable().setPackagePath(symbolTable.getPackagePath());
        this.type = tsOdinPointerType;
    }

    @Override
    public void visitProcedureType(@NotNull OdinProcedureType procedureType) {
        TsOdinProcedureType tsOdinProcedureType = new TsOdinProcedureType();
        tsOdinProcedureType.setType(procedureType);
        initializeNamedType(tsOdinProcedureType);

        List<TsOdinParameter> parameters = createParameters(tsOdinProcedureType, procedureType.getParamEntryList(), tsOdinProcedureType.getSymbolTable());
        tsOdinProcedureType.setParameters(parameters);

        OdinReturnParameters returnParameters = procedureType.getReturnParameters();
        if (returnParameters != null) {
            // Single return value
            OdinType type = returnParameters.getType();
            if (type != null) {
                TsOdinType tsOdinType = doResolveType(tsOdinProcedureType.getSymbolTable(), type);

                TsOdinParameter tsOdinParameter = new TsOdinParameter();
                tsOdinParameter.setType(tsOdinType);
                tsOdinParameter.setPsiType(type);
                tsOdinParameter.setIndex(0);

                tsOdinProcedureType.setReturnTypes(List.of(tsOdinType));
                tsOdinProcedureType.setReturnParameters(List.of(tsOdinParameter));
                if (type instanceof OdinPolymorphicType polymorphicType) {
                    tsOdinProcedureType.getSymbolTable().addType(tsOdinType.getName(), tsOdinType);
                    tsOdinProcedureType.getSymbolTable().add(polymorphicType.getDeclaredIdentifier());
                }
            } else {
                List<OdinParamEntry> returnParameterEntries = returnParameters.getParamEntryList();
                List<TsOdinParameter> tsOdinReturnParameters = createParameters(tsOdinProcedureType, returnParameterEntries, tsOdinProcedureType.getSymbolTable());
                for (TsOdinParameter tsOdinReturnParameter : tsOdinReturnParameters) {
                    tsOdinProcedureType.getReturnTypes().add(tsOdinReturnParameter.getType());
                }
                tsOdinProcedureType.setReturnParameters(tsOdinReturnParameters);
            }
        }

        this.type = tsOdinProcedureType;
    }

    @Override
    public void visitStructType(@NotNull OdinStructType structType) {
        TsOdinStructType tsOdinStructType = new TsOdinStructType();
        tsOdinStructType.setType(structType);
        initializeNamedType(tsOdinStructType);

        List<OdinParamEntry> paramEntries = structType.getParamEntryList();

        List<TsOdinParameter> parameters = createParameters(tsOdinStructType, paramEntries, tsOdinStructType.getSymbolTable());
        tsOdinStructType.setParameters(parameters);

        this.type = tsOdinStructType;
    }

    // TODO pretty sure we don't need this anymore. This could be just a TsOdinParameter factory
    @Data
    private static class TsOdinParameterSpec {
        OdinDeclaredIdentifier nameDeclaredIdentifier;
        OdinType psiType;

        boolean isValuePolymorphic() {
            return nameDeclaredIdentifier != null && nameDeclaredIdentifier.getDollar() != null;
        }

        static List<TsOdinParameterSpec> from(OdinParameterDeclaration parameterDeclaration) {

            if (parameterDeclaration instanceof OdinParameterInitialization odinParameterInitialization) {
                TsOdinParameterSpec tsOdinParameterSpec = new TsOdinParameterSpec();
                tsOdinParameterSpec.setPsiType(odinParameterInitialization.getTypeDefinition());
                tsOdinParameterSpec.setNameDeclaredIdentifier(odinParameterInitialization.getParameter().getDeclaredIdentifier());
                return List.of(tsOdinParameterSpec);
            }

            if (parameterDeclaration instanceof OdinParameterDeclarator odinParameterDeclarator) {
                List<TsOdinParameterSpec> parameterSpecs = new ArrayList<>();
                for (OdinParameter odinParameter : parameterDeclaration.getParameterList()) {
                    TsOdinParameterSpec tsOdinParameterSpec = new TsOdinParameterSpec();
                    tsOdinParameterSpec.setPsiType(odinParameterDeclarator.getTypeDefinition());
                    tsOdinParameterSpec.setNameDeclaredIdentifier(odinParameter.getDeclaredIdentifier());
                    parameterSpecs.add(tsOdinParameterSpec);
                }
                return parameterSpecs;
            }

            if (parameterDeclaration instanceof OdinUnnamedParameter unnamedParameter) {
                TsOdinParameterSpec tsOdinParameterSpec = new TsOdinParameterSpec();
                tsOdinParameterSpec.setPsiType(unnamedParameter.getTypeDefinition());
                return Collections.singletonList(tsOdinParameterSpec);
            }

            return Collections.emptyList();
        }
    }

    private @NotNull TsOdinParameter mapSpecToParameter(OdinSymbolTable symbolTable, TsOdinParameterSpec parameterSpec, int parameterIndex) {
        TsOdinParameter tsOdinParameter = new TsOdinParameter();
        tsOdinParameter.setIdentifier(parameterSpec.getNameDeclaredIdentifier());
        if (parameterSpec.getNameDeclaredIdentifier() != null) {
            tsOdinParameter.setName(parameterSpec.getNameDeclaredIdentifier().getName());
        }
        tsOdinParameter.setExplicitPolymorphicParameter(parameterSpec.isValuePolymorphic());
        tsOdinParameter.setIndex(parameterIndex);
        if (parameterSpec.getPsiType() != null) {
            TsOdinType tsOdinType = doResolveType(symbolTable, parameterSpec.getPsiType());
            tsOdinParameter.setType(tsOdinType);
        }
        tsOdinParameter.setPsiType(parameterSpec.psiType);


        return tsOdinParameter;
    }

    @Override
    public void visitEnumType(@NotNull OdinEnumType o) {
        TsOdinEnumType tsOdinEnumType = new TsOdinEnumType();
        initializeNamedType(tsOdinEnumType);

        OdinType backingType = o.getType();
        if (backingType != null) {
            TsOdinType tsOdinType = doResolveType(symbolTable, backingType);
            if (tsOdinType instanceof TsOdinBuiltInType tsOdinBuiltInType) {
                tsOdinEnumType.setBackingType(tsOdinBuiltInType);
            }
        }

        tsOdinEnumType.setType(o);

        // TODO Set fields
        this.type = tsOdinEnumType;
    }

    @Override
    public void visitPolymorphicType(@NotNull OdinPolymorphicType polymorphicType) {
        TsOdinPolymorphicType tsOdinPolymorphicType = new TsOdinPolymorphicType();
        initializeNamedType(tsOdinPolymorphicType);
        tsOdinPolymorphicType.setDeclaration(polymorphicType);
        tsOdinPolymorphicType.setDeclaredIdentifier(polymorphicType.getDeclaredIdentifier());
        tsOdinPolymorphicType.setName(polymorphicType.getDeclaredIdentifier().getName());

        tsOdinPolymorphicType.setSymbolTable(symbolTable);

        tsOdinPolymorphicType.setType(polymorphicType);
        this.type = tsOdinPolymorphicType;
    }

    @Override
    public void visitConstrainedType(@NotNull OdinConstrainedType constrainedType) {
        OdinType mainType = constrainedType.getTypeList().get(0);
        OdinType specializedType = constrainedType.getTypeList().get(1);
        TsOdinConstrainedType tsOdinConstrainedType = new TsOdinConstrainedType();
        tsOdinConstrainedType.setType(constrainedType);
        initializeNamedType(tsOdinConstrainedType);

        TsOdinType tsOdinMainType = doResolveType(tsOdinConstrainedType.getSymbolTable(), mainType);
        TsOdinType tsOdinSpecializedType = doResolveType(tsOdinConstrainedType.getSymbolTable(), specializedType);

        tsOdinConstrainedType.setMainType(tsOdinMainType);
        tsOdinConstrainedType.setSpecializedType(tsOdinSpecializedType);

        this.type = tsOdinConstrainedType;
    }

    private static class OdinMetaTypeResolver extends OdinVisitor {

        private TsOdinMetaType metaType;

        public OdinMetaTypeResolver() {
        }

        public static TsOdinMetaType resolveMetaType(OdinType type) {
            OdinMetaTypeResolver odinMetaTypeResolver = new OdinMetaTypeResolver();
            type.accept(odinMetaTypeResolver);
            return odinMetaTypeResolver.metaType;
        }

        TsOdinMetaType createMetaType(OdinType type, TsOdinMetaType.MetaType metaType) {
            TsOdinMetaType tsOdinMetaType = new TsOdinMetaType(metaType);
            tsOdinMetaType.setType(type);

            return tsOdinMetaType;
        }

        @Override
        public void visitArrayType(@NotNull OdinArrayType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.ARRAY);
        }

        @Override
        public void visitSliceType(@NotNull OdinSliceType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.SLICE);
        }

        @Override
        public void visitProcedureType(@NotNull OdinProcedureType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.PROCEDURE);
        }

        @Override
        public void visitPointerType(@NotNull OdinPointerType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.POINTER);
        }

        @Override
        public void visitMultiPointerType(@NotNull OdinMultiPointerType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.MULTI_POINTER);
        }

        @Override
        public void visitBitSetType(@NotNull OdinBitSetType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.BIT_SET);
        }

        @Override
        public void visitMatrixType(@NotNull OdinMatrixType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.MATRIX);
        }

        @Override
        public void visitMapType(@NotNull OdinMapType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.MAP);
        }

        @Override
        public void visitStructType(@NotNull OdinStructType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.STRUCT);
        }

        @Override
        public void visitEnumType(@NotNull OdinEnumType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.ENUM);
        }

        @Override
        public void visitUnionType(@NotNull OdinUnionType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.UNION);
        }

        @Override
        public void visitPolymorphicType(@NotNull OdinPolymorphicType o) {
            this.metaType = createMetaType(o, TsOdinMetaType.MetaType.POLYMORPHIC);
        }

        @Override
        public void visitBitFieldType(@NotNull OdinBitFieldType o) {
            this.metaType = createMetaType(o.getType(), TsOdinMetaType.MetaType.BIT_FIELD);
        }

        @Override
        public void visitQualifiedType(@NotNull OdinQualifiedType o) {

        }


    }
}
