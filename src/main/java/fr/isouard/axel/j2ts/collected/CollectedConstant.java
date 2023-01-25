package fr.isouard.axel.j2ts.collected;

import java.io.IOException;
import java.util.Map;

public class CollectedConstant extends CollectedNode {
    public CollectedConstant() {
        super(CollectedNodeType.CONSTANT);
    }

    @Override
    public void writeTypescriptTranslation(StringBuilder output, Map<String, CollectedNode> collectedNodes) throws IOException {
        output.append(System.lineSeparator()).append("| '").append(this.getQualifiedName()).append("'");
    }
}
