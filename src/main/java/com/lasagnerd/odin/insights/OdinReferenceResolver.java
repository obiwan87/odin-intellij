package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OdinReferenceResolver {
    public static Scope resolve(Scope scope, OdinExpression valueExpression) {

        OdinTypedNode odinTypedNode = new OdinTypedNode(scope);
        valueExpression.accept(odinTypedNode);
        if (odinTypedNode.isImport) {
            return OdinInsightUtils.getDeclarationsOfImportedPackage(scope, odinTypedNode.importDeclarationStatement);
        }
        return OdinTypedNode.getCompletionScopeOfType(scope, odinTypedNode.typeIdentifier);
    }

}

// What information needs to be gathered from a type resolver?
// -> What type does an expression resolve to?
// -> Where is the type declared?
// -> What does the type resolve to, meaning if it's an alias we need to get the original declaration
// -> How many values does the expression resolve to (think of multivalued returns)
// ->


class OdinTypedNode extends OdinVisitor {

    final Scope scope;

    // This is not enough information. Some types, such as multi-value return types, are not declared identifiers
    // -> anonymous types
    OdinDeclaredIdentifier typeIdentifier;

    OdinImportDeclarationStatement importDeclarationStatement;

    boolean isImport;

    public OdinTypedNode(Scope scope) {
        this.scope = scope;
    }

    @Override
    public void visitRefExpression(@NotNull OdinRefExpression refExpression) {
        Scope localScope;
        if (refExpression.getExpression() != null) {
            // solve for expression first. This defines the scope
            // extract scope
            OdinTypedNode odinTypedNode = new OdinTypedNode(this.scope);
            refExpression.getExpression().accept(odinTypedNode);
            localScope = getCompletionScopeOfType(this.scope, odinTypedNode.typeIdentifier);
        } else {
            localScope = this.scope;
        }

        // using current scope, find identifier declaration and extract type
        PsiNamedElement namedElement = localScope.findNamedElement(refExpression.getIdentifier().getText());
        if (namedElement instanceof OdinImportDeclarationStatement) {
            isImport = true;
            importDeclarationStatement = (OdinImportDeclarationStatement) namedElement;
        } else if (namedElement instanceof OdinDeclaredIdentifier declaredIdentifier) {
            OdinDeclaration odinDeclaration = OdinInsightUtils.findFirstParentOfType(declaredIdentifier, true, OdinDeclaration.class);

            if (odinDeclaration instanceof OdinImportDeclarationStatement) {
                isImport = true;
                importDeclarationStatement = (OdinImportDeclarationStatement) odinDeclaration;
            } else {
                typeIdentifier = findTypeIdentifier(this.scope, declaredIdentifier, odinDeclaration);
            }
        }
    }

    private static OdinDeclaredIdentifier findTypeIdentifier(Scope parentScope, OdinDeclaredIdentifier declaredIdentifier, OdinDeclaration odinDeclaration) {
        if (odinDeclaration instanceof OdinVariableDeclarationStatement declarationStatement) {
            var mainType = declarationStatement.getTypeDefinition().getMain().getExpression();
            return getDeclaredIdentifierOfTypeRef(parentScope, mainType);
        }

        if (odinDeclaration instanceof OdinVariableInitializationStatement initializationStatement) {
            if (initializationStatement.getTypeDefinition() != null) {
                return getDeclaredIdentifierOfTypeRef(parentScope, initializationStatement.getTypeDefinition().getMain().getExpression());
            }

            int index = initializationStatement.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            OdinExpression odinExpression = initializationStatement.getExpressionsList().getExpressionList().get(index);
            OdinTypedNode odinTypedNode = new OdinTypedNode(parentScope);
            odinExpression.accept(odinTypedNode);
            return odinTypedNode.typeIdentifier;
        }

        if (odinDeclaration instanceof OdinConstantInitializationStatement initializationStatement) {
            if (initializationStatement.getTypeDefinition() != null) {
                OdinExpression mainType = initializationStatement.getTypeDefinition().getMain().getExpression();
                return getDeclaredIdentifierOfTypeRef(parentScope, mainType);
            }
            int index = initializationStatement.getIdentifierList().getDeclaredIdentifierList().indexOf(declaredIdentifier);
            OdinExpression odinExpression = initializationStatement.getExpressionsList().getExpressionList().get(index);
            OdinTypedNode odinTypedNode = new OdinTypedNode(parentScope);
            odinExpression.accept(odinTypedNode);
            return odinTypedNode.typeIdentifier;
        }

        if (odinDeclaration instanceof OdinFieldDeclarationStatement fieldDeclarationStatement) {
            OdinExpression mainType;
            if (fieldDeclarationStatement.getTypeDefinition() instanceof OdinTypeExpression expr) {
                mainType = expr;
            } else {

                OdinTypeDefinitionExpression typeDefinition = fieldDeclarationStatement.getTypeDefinition();
                mainType = typeDefinition.getMain().getExpression();
            }

            return getDeclaredIdentifierOfTypeRef(parentScope, mainType);
        }

        if (odinDeclaration instanceof OdinParameterDeclaration parameterDeclaration) {

        }

        if (odinDeclaration instanceof OdinParameterInitialization parameterInitialization) {

        }

        if (odinDeclaration instanceof OdinProcedureDeclarationStatement procedure) {
            return procedure.getDeclaredIdentifier();
        }

        return null;
    }

    @Override
    public void visitCompoundLiteralExpression(@NotNull OdinCompoundLiteralExpression o) {
        if (o.getCompoundLiteral() instanceof OdinCompoundLiteralTyped typed) {
            typeIdentifier = getDeclaredIdentifierOfTypeRef(this.scope, typed.getTypeExpression());
        }
    }

    private static OdinDeclaredIdentifier getDeclaredIdentifierOfTypeRef(Scope parentScope, @Nullable OdinExpression typeExpression) {
        if (typeExpression instanceof OdinQualifiedType odinQualifiedType) {
            if (odinQualifiedType.getPackageIdentifier() != null) {
                Scope packageScope = parentScope.getScopeOfImport(odinQualifiedType.getPackageIdentifier().getIdentifierToken().getText());
                return (OdinDeclaredIdentifier) packageScope.findNamedElement(odinQualifiedType.getTypeIdentifier().getIdentifierToken().getText());
            } else {
                return (OdinDeclaredIdentifier) parentScope.findNamedElement(((OdinQualifiedType) typeExpression).getTypeIdentifier()
                        .getIdentifierToken()
                        .getText());
            }
        }
        return null;
    }

    @Override
    public void visitCallExpression(@NotNull OdinCallExpression o) {
        // Get type of expression. If it is callable, retrieve the return type and set that as result
        OdinExpression expression = o.getExpression();
        OdinTypedNode odinTypedNode = new OdinTypedNode(this.scope);
        expression.accept(odinTypedNode);
        OdinDeclaredIdentifier caller = odinTypedNode.typeIdentifier;
        OdinDeclaration callerDeclaration = OdinInsightUtils.getDeclaration(caller);

        OdinExpression type = null;
        if (callerDeclaration instanceof OdinProcedureDeclarationStatement procedure) {
            type = getReturnType(procedure);
        }

        if (type instanceof OdinQualifiedType typeRef) {
            this.typeIdentifier = getDeclaredIdentifierOfTypeRef(odinTypedNode.scope, typeRef);
        }
    }

    @Nullable
    private static OdinExpression getReturnType(OdinProcedureDeclarationStatement procedure) {
        OdinExpression type = null;
        OdinReturnParameters returnParameters = procedure.getProcedureType().getReturnParameters();
        if (returnParameters != null) {


            OdinExpression returnExpression = returnParameters.getTypeDefinitionExpression();
            OdinParamEntries paramEntries = returnParameters.getParamEntries();
            if (paramEntries != null) {
                if (paramEntries.getParamEntryList().size() == 1) {
                    OdinTypeDefinitionExpression typeDefinition1 = paramEntries.getParamEntryList().get(0).getParameterDeclaration().getTypeDefinition();
                    type = typeDefinition1.getMain().getExpression();
                }

            } else if (returnExpression instanceof OdinQualifiedType typRef) {
                type = typRef;
            }
        }
        return type;
    }

    @Override
    public void visitIndexExpression(@NotNull OdinIndexExpression o) {
        // get type of expression. IF it is indexable (array, matrix, bitset, map), retrieve the indexed type and set that as result
        OdinExpression expression = o.getExpression();
        OdinTypedNode odinTypedNode = new OdinTypedNode(this.scope);
        expression.accept(odinTypedNode);
        OdinDeclaredIdentifier indexedType = odinTypedNode.typeIdentifier;
    }

    @Override
    public void visitDereferenceExpression(@NotNull OdinDereferenceExpression o) {
        // get type of expression. If it is a pointer type, retrieve the type without the pointer and set that as result
        super.visitDereferenceExpression(o);
    }

    static Scope getCompletionScopeOfType(Scope scope, PsiElement identifier) {
        if (!(identifier instanceof OdinDeclaredIdentifier))
            return null;

        OdinDeclaration odinDeclaration = OdinInsightUtils.findFirstParentOfType(identifier,
                false,
                OdinDeclaration.class);

        if (odinDeclaration instanceof OdinStructDeclarationStatement structDeclarationStatement) {
            return scope.with(getStructFields(structDeclarationStatement));
        }

        if (odinDeclaration instanceof OdinEnumDeclarationStatement enumDeclarationStatement) {
            return scope.with(getEnumFields(enumDeclarationStatement));
        }

        if (odinDeclaration instanceof OdinImportDeclarationStatement importDeclarationStatement) {
            return OdinInsightUtils.getDeclarationsOfImportedPackage(scope, importDeclarationStatement);
        }

        return Scope.EMPTY;
    }

    @NotNull
    private static List<PsiNamedElement> getEnumFields(OdinEnumDeclarationStatement enumDeclarationStatement) {
        OdinEnumBody enumBody = enumDeclarationStatement.getEnumType()
                .getEnumBlock()
                .getEnumBody();

        if (enumBody == null)
            return Collections.emptyList();

        return enumBody
                .getEnumValueDeclarationList()
                .stream()
                .map(OdinEnumValueDeclaration::getDeclaredIdentifier)
                .collect(Collectors.toList());
    }

    private static List<PsiNamedElement> getStructFields(OdinStructDeclarationStatement structDeclarationStatement) {
        OdinStructBody structBody = structDeclarationStatement
                .getStructType()
                .getStructBlock()
                .getStructBody();

        if (structBody == null) {
            return Collections.emptyList();
        }

        return structBody.getFieldDeclarationStatementList().stream()
                .flatMap(x -> x.getDeclaredIdentifiers().stream())
                .collect(Collectors.toList());
    }
}

@Data
class OdinTypeReference {
    OdinDeclaredIdentifier declaredIdentifier;

}