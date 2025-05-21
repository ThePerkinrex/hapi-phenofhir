package es.upm.etsiinf.tfg.juanmahou.plugin;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DependencyManager {
    private static DependencyManager instance;
    public static DependencyManager getInstance() {
        if(instance == null) {
            instance = new DependencyManager();
        }
        return instance;
    }

    private final Map<String, List<String>> dependencies;

    public DependencyManager() {
        dependencies = new HashMap<>();
    }

    public void register(String name) {
        if (dependencies.containsKey(name)) throw new RuntimeException(name + " is registered");
        dependencies.put(name, new ArrayList<>());
    }

    public void addDependency(String dependent, String dependency) {
        if (!dependencies.containsKey(dependent)) throw new RuntimeException(dependent + " is not registered");
        dependencies.get(dependent).add(dependency);
    }

    /**
     * Render the graph in DOT format, grouping all nodes with the same
     * number of outgoing dependencies onto the same Graphviz rank.
     */
    public String toDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph dependencies {\n")
                .append("    rankdir=TB;\n") // top-to-bottom
                .append("\n    // edges\n");

        // 1) Emit edges (and singletons)
        for (Map.Entry<String, List<String>> entry : dependencies.entrySet()) {
            String from = entry.getKey();
            List<String> targets = entry.getValue();
            if (targets.isEmpty()) {
                sb.append(String.format("    \"%s\";%n", from));
            } else {
                for (String to : targets) {
                    sb.append(String.format("    \"%s\" -> \"%s\";%n", from, to));
                }
            }
        }

        // 2) Group nodes by their out-degree
        sb.append("\n    // group by number of dependencies\n");
        // TreeMap so ranks go in ascending order
        Map<Integer, List<String>> byCount = new TreeMap<>();
        for (Map.Entry<String, List<String>> entry : dependencies.entrySet()) {
            int count = entry.getValue().size();
            byCount.computeIfAbsent(count, k -> new ArrayList<>())
                    .add(entry.getKey());
        }
        for (Map.Entry<Integer, List<String>> rankEntry : byCount.entrySet()) {
            int depCount = rankEntry.getKey();
            List<String> nodes = rankEntry.getValue();
            sb.append("    { rank = same;  // ")
                    .append(depCount)
                    .append(depCount == 1 ? " dependency" : " dependencies")
                    .append("\n");
            for (String node : nodes) {
                sb.append("        \"")
                        .append(node)
                        .append("\";\n");
            }
            sb.append("    }\n");
        }

        sb.append("}\n");
        return sb.toString();
    }
}
