package fr.isouard.axel.j2ts.collected;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class CollectedEnum extends CollectedNode {
    public CollectedEnum() {
        super(CollectedNodeType.ENUM);
    }

    @Override
    public void writeTypescriptTranslation(Path basePath, Map<String, CollectedNode> collectedNodes) throws IOException {
        StringBuilder output = new StringBuilder();

        this.writeTypescriptTranslation(output, collectedNodes);

        FileWriter writer = new FileWriter(basePath.resolve(this.getName() + ".ts").toString());
        writer.write(output.toString());
        writer.close();
    }

    @Override
    public void writeTypescriptTranslation(StringBuilder output, Map<String, CollectedNode> collectedNodes) throws IOException {
        output.append("export type ").append(this.getName()).append(" =");
        for (CollectedNode child: children) {
            child.writeTypescriptTranslation(output, collectedNodes);
        }
        output.append(";").append(System.lineSeparator());
    }

    @Override
    public void writeTypescriptTranslation(StringBuilder output, Map<String, CollectedNode> collectedNodes, Map<String, Set<String>> resolvedImports) throws IOException {
        this.writeTypescriptTranslation(output, collectedNodes);
    }
}
