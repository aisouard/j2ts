package fr.isouard.axel.j2ts.collector;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import fr.isouard.axel.j2ts.collected.CollectedConstant;
import fr.isouard.axel.j2ts.collected.CollectedNode;

import java.util.Map;

public class ConstantCollector extends VoidVisitorAdapter<Map<String, CollectedNode>> {
    @Override
    public void visit(EnumConstantDeclaration decl, Map<String, CollectedNode> output) {
        CollectedConstant collectedConstant = new CollectedConstant();

        collectedConstant.setQualifiedName(decl.getNameAsString());

        if (decl.getParentNode().isPresent()) {
            Node parent = decl.getParentNode().get();
            if (parent.getClass() == EnumDeclaration.class) {
                ((EnumDeclaration)parent).getFullyQualifiedName().ifPresent(collectedConstant::setParentName);
            } else {
                System.out.println("Unknown parent type");
            }
        }

        output.put(collectedConstant.getParentName() + "." + collectedConstant.getQualifiedName(), collectedConstant);
    }
}