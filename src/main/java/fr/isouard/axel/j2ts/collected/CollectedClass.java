package fr.isouard.axel.j2ts.collected;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CollectedClass extends CollectedNode {
    private final List<CollectedType> extendedTypes;
    private final List<CollectedType> implementedTypes;

    public CollectedClass() {
        super(CollectedNodeType.CLASS);

        this.extendedTypes = new ArrayList<>();
        this.implementedTypes = new ArrayList<>();
    }

    public void addExtendedType(CollectedType type) {
        this.extendedTypes.add(type);
    }

    public void addImplementedType(CollectedType type) {
        this.implementedTypes.add(type);
    }

    @Override
    public void writeTypescriptTranslation(Path basePath, Map<String, CollectedNode> collectedNodes) throws IOException {
        Map<String, Set<String>> resolvedImports = new HashMap<>();
        StringBuilder output = new StringBuilder();

        this.writeTypescriptTranslation(output, collectedNodes, resolvedImports);

        resolvedImports.remove(this.getQualifiedName());
        resolvedImports.forEach((k, v) -> {
            String classPath = this.getParentName().replaceAll("\\.", "/");
            String objectPath = k.replaceAll("\\.", "/");
            String relativePath = Paths.get(classPath).relativize(Paths.get(objectPath)).toString();

            if (!relativePath.startsWith(".")) {
                relativePath = "./" + relativePath;
            }

            output.insert(0, "import { " + String.join(", ", v) + " } from '" + relativePath + "';" + System.lineSeparator());
        });

        FileWriter writer = new FileWriter(basePath.resolve(this.getName() + ".ts").toString());
        writer.write(output.toString());
        writer.close();
    }

    @Override
    public void writeTypescriptTranslation(StringBuilder output, Map<String, CollectedNode> collectedNodes, Map<String, Set<String>> resolvedImports) throws IOException {
        int nestedNodes = 0;

        output.append("export interface ").append(this.getName());
        if (this.extendedTypes.size() > 0) {
            output.append(" extends ");
            for (CollectedType extended: this.extendedTypes) {
                extended.writeTypescriptTranslation(output, collectedNodes, resolvedImports);
                output.append(", ");
            }
            output.delete(output.length() - 2, output.length());
        }

        /*
        if (this.implementedTypes.size() > 0) {
            writer.write(" implements " + this.implementedTypes.stream().map(CollectedNode::getName).collect(Collectors.joining(", ")));
        }
        */

        output.append(" { ").append(System.lineSeparator());
        for (CollectedNode child: children) {
            if (child.getNodeType() != CollectedNodeType.FIELD) {
                ++nestedNodes;
                continue;
            }

            child.writeTypescriptTranslation(output, collectedNodes, resolvedImports);
        }
        output.append("}").append(System.lineSeparator());

        if (nestedNodes == 0) {
            return;
        }

        output.append("export namespace ").append(this.getName()).append(" {").append(System.lineSeparator());
        for (CollectedNode child: children) {
            if (child.getNodeType() == CollectedNodeType.FIELD) {
                continue;
            }

            child.writeTypescriptTranslation(output, collectedNodes, resolvedImports);
        }
        output.append("}").append(System.lineSeparator());
    }
}
