package fr.isouard.axel.j2ts.collected;

import java.io.IOException;
import java.util.*;

public class CollectedType extends CollectedNode {
    private final List<CollectedType> parameters;

    public CollectedType() {
        super(CollectedNodeType.TYPE);

        this.parameters = new ArrayList<>();
    }

    public void addParameter(CollectedType parseType) {
        this.parameters.add(parseType);
    }

    @Override
    public void writeTypescriptTranslation(StringBuilder output, Map<String, CollectedNode> collectedNodes, Map<String, Set<String>> resolvedImports) throws IOException {
        String typeName = this.getName();

        if (collectedNodes.containsKey(typeName)) {
            CollectedNode parent = collectedNodes.get(typeName);
            typeName = this.getFullName(parent).substring(1);

            String baseName = this.getBaseName(parent);
            resolvedImports.putIfAbsent(parent.getParentName(), new HashSet<>());
            resolvedImports.get(parent.getParentName()).add(baseName);
        } else {
            typeName = this.getTypescriptTypeName();
        }

        output.append(typeName);
        if (this.parameters.size() == 0) {
            return;
        }

        output.append("<");
        for (CollectedType parameter: this.parameters) {
            parameter.writeTypescriptTranslation(output, collectedNodes, resolvedImports);
            output.append(", ");
        }
        output.delete(output.length() - 2, output.length());
        output.append(">");
    }

    private String getBaseName(CollectedNode parent) {
        if (parent.getParent().getNodeType() == CollectedNodeType.PACKAGE) {
            return parent.getName();
        }
        return this.getBaseName(parent.getParent());
    }

    private String getFullName(CollectedNode parent) {
        if (parent.getNodeType() == CollectedNodeType.PACKAGE) {
            return "";
        }

        return this.getFullName(parent.getParent()) + "." + parent.getName();
    }

    private String getTypescriptTypeName() {
        switch (this.getName()) {
            case "java.lang.Boolean":
                return "boolean";

            case "java.lang.Class":
            case "java.lang.Object":
                return "any";

            case "java.util.Date":
            case "java.util.LocalDate":
                return "Date";

            case "java.math.BigInteger":
            case "java.math.Byte":
            case "java.lang.Double":
            case "java.lang.Float":
            case "java.lang.Integer":
            case "java.lang.Long":
                return "number";

            case "java.util.List":
                return "Array";

            case "java.util.Map":
                return "Record";

            case "java.lang.String":
                return "string";

            default:
                System.out.println("Unknown type " + this.getName());
                return "any";
        }
    }
}
