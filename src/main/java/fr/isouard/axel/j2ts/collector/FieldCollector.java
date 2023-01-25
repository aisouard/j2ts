package fr.isouard.axel.j2ts.collector;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import fr.isouard.axel.j2ts.collected.CollectedField;
import fr.isouard.axel.j2ts.collected.CollectedNode;
import fr.isouard.axel.j2ts.collected.CollectedType;

import java.util.Map;

public class FieldCollector extends VoidVisitorAdapter<Map<String, CollectedNode>> {
    @Override
    public void visit(FieldDeclaration decl, Map<String, CollectedNode> output) {
        if (decl.getParentNode().isEmpty()) {
            return;
        }

        StringBuilder parentName = new StringBuilder();
        Node parent = decl.getParentNode().get();

        if (parent.getClass() == ClassOrInterfaceDeclaration.class) {
            ((ClassOrInterfaceDeclaration)parent).getFullyQualifiedName().ifPresent(parentName::append);
        } else if (parent.getClass() == EnumDeclaration.class) {
            ((EnumDeclaration)parent).getFullyQualifiedName().ifPresent(parentName::append);
        } else {
            System.out.println("Unknown class");
            return;
        }

        decl.getVariables().forEach(var -> {
            CollectedField collectedField = new CollectedField();

            collectedField.setQualifiedName(var.getNameAsString());
            collectedField.setParentName(parentName.toString());
            this.parseVariable(var, collectedField);

            output.put(collectedField.getParentName() + "." + collectedField.getQualifiedName(), collectedField);
        });
    }

    private void parseVariable(VariableDeclarator variable, CollectedField field) {
        CollectedType collectedType = FieldCollector.parseType(variable.getType());
        field.setType(collectedType);
    }

    public static CollectedType parseType(Type type) {
        CollectedType collectedType = new CollectedType();

        try {
            FieldCollector.parseTypeQualifiedName(type.resolve(), collectedType);
        } catch (UnsolvedSymbolException e) {
            FieldCollector.parseTypeName(type, collectedType);
        }

        FieldCollector.parseTypeParameters(type, collectedType);

        return collectedType;
    }

    public static CollectedType parseType(ResolvedType resolvedType) {
        CollectedType collectedType = new CollectedType();

        FieldCollector.parseTypeQualifiedName(resolvedType, collectedType);

        return collectedType;
    }

    private static void parseTypeName(Type type, CollectedType collectedType) {
        String name = null;

        if (type.isClassOrInterfaceType()) {
            name = type.asClassOrInterfaceType().getNameWithScope();
        } else if (type.isPrimitiveType()) {
            name = type.asPrimitiveType().getType().name();
        } else if (type.isArrayType()) {
            name = "List";
            collectedType.addParameter(FieldCollector.parseType(type.asArrayType().getComponentType()));
        } else {
            System.out.println("Unknown type");
        }

        collectedType.setName(name);
    }

    private static void parseTypeQualifiedName(ResolvedType resolvedType, CollectedType collectedType) {
        String qualifiedName;

        if (resolvedType.isReferenceType()) {
            qualifiedName = resolvedType.asReferenceType().getQualifiedName();
        } else if (resolvedType.isPrimitive()) {
            qualifiedName = resolvedType.asPrimitive().getBoxTypeQName();
        } else if (resolvedType.isArray()) {
            qualifiedName = "java.util.List";
            collectedType.addParameter(FieldCollector.parseType(resolvedType.asArrayType().getComponentType()));
        } else if (resolvedType.isWildcard()) {
            qualifiedName = "?";
        } else {
            System.out.println("Unknown type");
            return;
        }

        collectedType.setName(qualifiedName);
        collectedType.setQualifiedName(qualifiedName);
    }

    private static void parseTypeParameters(Type type, CollectedType collectedType) {
        if (!type.isClassOrInterfaceType()
                || type.asClassOrInterfaceType().getTypeArguments().isEmpty()) {
            return;
        }

        type.asClassOrInterfaceType()
                .getTypeArguments().get()
                .forEach(arg -> collectedType.addParameter(FieldCollector.parseType(arg)));
    }
}
