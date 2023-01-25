package fr.isouard.axel.j2ts.collector;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import fr.isouard.axel.j2ts.collected.CollectedClass;
import fr.isouard.axel.j2ts.collected.CollectedNode;

import java.util.Map;

public class ClassCollector extends VoidVisitorAdapter<Map<String, CollectedNode>> {
    @Override
    public void visit(ClassOrInterfaceDeclaration decl, Map<String, CollectedNode> output) {
        decl.findAll(ClassOrInterfaceDeclaration.class, Node.TreeTraversal.DIRECT_CHILDREN)
                .forEach(classDeclaration -> {
                    this.visit(classDeclaration, output);
                });

        CollectedClass collectedClass = new CollectedClass();

        ClassCollector.parseParent(decl, collectedClass);
        collectedClass.setName(decl.getNameAsString());
        decl.getFullyQualifiedName().ifPresent(collectedClass::setQualifiedName);
        decl.getExtendedTypes().forEach(type -> collectedClass.addExtendedType(FieldCollector.parseType(type)));
        decl.getImplementedTypes().forEach(type -> collectedClass.addImplementedType(FieldCollector.parseType(type)));

        output.putIfAbsent(collectedClass.getQualifiedName(), collectedClass);
    }

    public static <T extends TypeDeclaration<?>> void parseParent(TypeDeclaration<T> decl, CollectedNode collectedNode) {
        if ((!decl.isClassOrInterfaceDeclaration() && !decl.isEnumDeclaration()) || decl.getParentNode().isEmpty()) {
            return;
        }

        Node parent = decl.getParentNode().get();
        if (parent.getClass() == CompilationUnit.class) {
            ((CompilationUnit)parent).getPackageDeclaration()
                    .ifPresent(packageDeclaration -> collectedNode.setParentName(packageDeclaration.getNameAsString()));
        } else if (parent.getClass() == ClassOrInterfaceDeclaration.class) {
            ((ClassOrInterfaceDeclaration)parent).getFullyQualifiedName().ifPresent(collectedNode::setParentName);
        } else {
            System.out.println("Unknown parent class");
        }
    }
}
