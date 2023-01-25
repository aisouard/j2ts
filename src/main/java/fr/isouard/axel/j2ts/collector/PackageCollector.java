package fr.isouard.axel.j2ts.collector;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import fr.isouard.axel.j2ts.collected.CollectedNode;
import fr.isouard.axel.j2ts.collected.CollectedPackage;

import java.util.Map;

public class PackageCollector extends VoidVisitorAdapter<Map<String, CollectedNode>> {
    @Override
    public void visit(PackageDeclaration decl, Map<String, CollectedNode> output) {
        CollectedPackage collectedPackage = new CollectedPackage();

        collectedPackage.setQualifiedName(decl.getNameAsString());
        output.putIfAbsent(decl.getNameAsString(), collectedPackage);
    }
}