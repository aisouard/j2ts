package fr.isouard.axel.j2ts.collected;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class CollectedPackage extends CollectedNode {

    public CollectedPackage() {
        super(CollectedNodeType.PACKAGE);
    }

    @Override
    public void writeTypescriptTranslation(Path basePath, Map<String, CollectedNode> collectedNodes) throws IOException {
        String packagePath = this.getQualifiedName().replaceAll("\\.", File.separator);
        Files.createDirectories(basePath.resolve(packagePath));

        System.out.println("Writing package definition " + this.getQualifiedName());

        FileWriter writer = new FileWriter(basePath.resolve(packagePath).resolve("index.ts").toString());
        for (CollectedNode child: children) {
            writer.write("export * from './" + child.getName() + "';" + System.lineSeparator());
            child.writeTypescriptTranslation(basePath.resolve(packagePath), collectedNodes);
        }
        writer.close();
    }
}
