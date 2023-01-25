package fr.isouard.axel.j2ts.collector;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import fr.isouard.axel.j2ts.collected.CollectedEnum;
import fr.isouard.axel.j2ts.collected.CollectedNode;

import java.util.Map;

public class EnumCollector extends VoidVisitorAdapter<Map<String, CollectedNode>> {
    @Override
    public void visit(ClassOrInterfaceDeclaration decl, Map<String, CollectedNode> output) {
        decl.findAll(EnumDeclaration.class, Node.TreeTraversal.DIRECT_CHILDREN)
                .forEach(enumDeclaration -> {
                    this.visit(enumDeclaration, output);
                });
    }

    @Override
    public void visit(EnumDeclaration decl, Map<String, CollectedNode> output) {
        CollectedEnum collectedEnum = new CollectedEnum();

        ClassCollector.parseParent(decl, collectedEnum);
        collectedEnum.setName(decl.getNameAsString());
        decl.getFullyQualifiedName().ifPresent(collectedEnum::setQualifiedName);

        output.put(collectedEnum.getQualifiedName(), collectedEnum);
    }
}
