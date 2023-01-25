package fr.isouard.axel.j2ts.collected;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CollectedNode {
    private final CollectedNodeType nodeType;
    private String name;
    private String parentName;
    private String qualifiedName;
    protected CollectedNode parent;
    protected List<CollectedNode> children;

    public CollectedNode(CollectedNodeType type) {
        this.nodeType = type;
        this.name = null;
        this.parentName = null;
        this.qualifiedName = null;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    public enum CollectedNodeType {
        NONE,
        PACKAGE,
        CLASS,
        ENUM,
        CONSTANT,
        FIELD,
        TYPE;

        public CollectedNodeType prev() {
            return values()[ordinal() - 1];
        }
    }

    public CollectedNodeType getNodeType() {
        return this.nodeType;
    }

    public String getQualifiedName() {
        return this.qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getName() { return this.name; }

    public void setName(String name) { this.name = name; }

    public String getParentName() {
        return this.parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public CollectedNode getParent() {
        return this.parent;
    }

    protected void setParent(CollectedNode parent) {
        this.parent = parent;
    }

    public void addChild(CollectedNode child) {
        this.children.add(child);
        child.parent = this;
    }

    public void writeTypescriptTranslation(Path basePath, Map<String, CollectedNode> collectedNodes) throws IOException {}

    public void writeTypescriptTranslation(StringBuilder output, Map<String, CollectedNode> collectedNodes) throws IOException {}

    public void writeTypescriptTranslation(StringBuilder writer, Map<String, CollectedNode> collectedNodes, Map<String, Set<String>> resolvedImports) throws IOException {}
}
