package es.upm.etsiinf.tfg.juanmahou.plugin.tables;

import com.google.protobuf.Descriptors;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.Config;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.ConfigTable;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.DescriptorPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

import static es.upm.etsiinf.tfg.juanmahou.plugin.util.HashUtils.hashSuffix;

public class TableManager {
    private static final Logger logger = LoggerFactory.getLogger(TableManager.class);

    private final Config config;
    private final DescriptorPool pool;
    private final Map<String, Table> tablePool = new HashMap<>();
    private final Map<String, Set<String>> fileTables = new HashMap<>();

    /**
     * @param config the configuration containing message-to-table mappings
     * @param pool   a map of fully-qualified type names to their protobuf Descriptors
     */
    public TableManager(Config config, DescriptorPool pool) {
        this.config = Objects.requireNonNull(config, "config");
        this.pool = Objects.requireNonNull(pool, "pool");
    }

    /**
     * Retrieve the raw ConfigTable for a given message id.
     */
    public ConfigTable getConfig(String id) {
        return config.getMessages().get(id);
    }

    /**
     * Get or create a Table for the given message id.  Returns null if not configured or if marked as protobuf-only.
     * @param id     fully-qualified message name (e.g. "my.pkg.MessageType")
     * @param origin context identifier for logging
     */
    public Table getTable(String id, String origin) {
        if (tablePool.containsKey(id)) {
            return tablePool.get(id);
        }

        ConfigTable cfg = getConfig(id);
        if (cfg == null) {
            logger.error("Table with id {} not configured, ignoring", id);
            tablePool.put(id, null);
            return null;
        }
        if (cfg.isAsProtobuf()) {
            logger.warn("Table with id {} is just to be used as protobuf", id);
            tablePool.put(id, null);
            return null;
        }

        logger.info("Loading table with id {}", id);
        Descriptors.Descriptor msg = pool.get(id);
        if (msg == null) {
            throw new IllegalStateException("Descriptor not found for id: " + id);
        }

        String name = (cfg.getName() != null)
                ? cfg.getName()
                : msg.getName() + "_" + hashSuffix(id);

        Table table = Table.createRegularTable(this, cfg, name, msg, origin);
        tablePool.put(id, table);
        return table;
    }

    /**
     * Load a Table and record its origin file.
     * @param file proto filename
     * @param id   message id
     */
    public Table getTableInFile(String file, String id) {
        logger.info("Loading in file {}: {}", file, id);
        Table table = getTable(id, "main#" + file);
        if (table != null) {
            fileTables.computeIfAbsent(file, k -> new HashSet<>()).add(id);
        }
        return table;
    }

    /**
     * Expose the descriptor pool (map).
     */
    public DescriptorPool getPool() {
        return pool;
    }

    /**
     * Iterate over all Tables collected from a given proto file.
     */
    public Stream<Table> getAllTablesInFile(String file) {
        return fileTables.getOrDefault(file, Collections.emptySet()).stream().map(id -> getTable(id, "all_in_file")).filter(Objects::nonNull);
    }

    public Stream<Table> getAllTables() {
        return this.tablePool.values().stream().filter(Objects::nonNull);
    }
}
