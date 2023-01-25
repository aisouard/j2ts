package fr.isouard.axel.j2ts;

import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.Log;

import com.github.javaparser.utils.SourceZip;

import fr.isouard.axel.j2ts.collected.CollectedNode;
import fr.isouard.axel.j2ts.collector.ClassCollector;
import fr.isouard.axel.j2ts.collector.ConstantCollector;
import fr.isouard.axel.j2ts.collector.EnumCollector;
import fr.isouard.axel.j2ts.collector.FieldCollector;
import fr.isouard.axel.j2ts.collector.PackageCollector;

import org.apache.commons.io.FilenameUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;

@Command(name = "j2ts", mixinStandardHelpOptions = true, version = "j2ts 1.0",
        description = "Converts a Java package to a TypeScript module")
class TypescriptTranslator implements Callable<Integer> {

    @Parameters(index = "0", description = ".jar file containing java source files")
    private File sourcesJar;

    @Option(names = {"-i", "--include"}, description = "compiled .jar packages")
    private List<File> include;

    @Override
    public Integer call() throws IOException {
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

        CombinedTypeSolver typeSolver = new CombinedTypeSolver(
                new ReflectionTypeSolver()
        );

        if (this.include != null) {
            this.include.forEach(jar -> {
                try {
                    typeSolver.add(new JarTypeSolver(jar.getAbsolutePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);

        SourceZip sourceZip = new SourceZip(this.sourcesJar.toPath());
        sourceZip.getParserConfiguration().setSymbolResolver(symbolSolver);

        Map<String, CollectedNode> collectedNodes = new HashMap<>();

        sourceZip.parse().forEach(pair -> pair.b.ifSuccessful(cu -> {
            VoidVisitor<Map<String, CollectedNode>> packageCollector = new PackageCollector();
            packageCollector.visit(cu, collectedNodes);

            VoidVisitor<Map<String, CollectedNode>> enumCollector = new EnumCollector();
            enumCollector.visit(cu, collectedNodes);

            VoidVisitor<Map<String, CollectedNode>> classCollector = new ClassCollector();
            classCollector.visit(cu, collectedNodes);

            VoidVisitor<Map<String, CollectedNode>> constantCollector = new ConstantCollector();
            constantCollector.visit(cu, collectedNodes);

            VoidVisitor<Map<String, CollectedNode>> fieldCollector = new FieldCollector();
            fieldCollector.visit(cu, collectedNodes);
        }));

        collectedNodes.values().forEach(current -> {
            CollectedNode parent = collectedNodes.get(current.getParentName());
            if (parent == null) {
                return;
            }

            parent.addChild(current);
        });

        Path basePath = Paths.get(".").resolve(FilenameUtils.removeExtension(sourcesJar.getName()));

        collectedNodes.values().stream()
                .filter(collectedNode -> collectedNode.getNodeType() == CollectedNode.CollectedNodeType.PACKAGE)
                .forEach(collectedNode -> {
                    try {
                        collectedNode.writeTypescriptTranslation(basePath, collectedNodes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new TypescriptTranslator()).execute(args);
        System.exit(exitCode);
    }
}