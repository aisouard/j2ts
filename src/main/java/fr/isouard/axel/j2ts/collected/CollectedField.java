package fr.isouard.axel.j2ts.collected;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class CollectedField extends CollectedNode {
    private CollectedType type;

    public CollectedField() {
        super(CollectedNodeType.FIELD);
    }

    public void setType(CollectedType type) {
        this.type = type;
        type.setParent(this);
    }

    @Override
    public void writeTypescriptTranslation(StringBuilder output, Map<String, CollectedNode> collectedNodes, Map<String, Set<String>> resolvedImports) throws IOException {
        output.append(this.getQualifiedName()).append(": ");
        this.type.writeTypescriptTranslation(output, collectedNodes, resolvedImports);
        output.append(";").append(System.lineSeparator());
    }
}
