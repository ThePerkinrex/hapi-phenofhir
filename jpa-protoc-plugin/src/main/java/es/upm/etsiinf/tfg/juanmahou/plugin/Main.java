package es.upm.etsiinf.tfg.juanmahou.plugin;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.Config;
import es.upm.etsiinf.tfg.juanmahou.plugin.render.TableRenderer;
import es.upm.etsiinf.tfg.juanmahou.plugin.tables.Table;
import es.upm.etsiinf.tfg.juanmahou.plugin.tables.TableManager;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.ClassType;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.DescriptorPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        // Configure logging to file 'out.log'
        // (Assume external logging config.)

        if (args.length != 1) {
            throw new IllegalArgumentException("Expected the config argument");
        }

        // 1) Read request from stdin
        PluginProtos.CodeGeneratorRequest request = PluginProtos.CodeGeneratorRequest.parseFrom(System.in);

        // 2) Load config JSON from parameter
        logger.info("ARGS: {}", List.of(args));

        String configPath = args[0];
        ObjectMapper mapper = new ObjectMapper();
        Config config;
        try (FileInputStream fis = new FileInputStream(configPath)) {
            config = mapper.readValue(fis, Config.class);
        } catch (Exception e) {
            throw new RuntimeException("Config not provided or invalid", e);
        }

        logger.info("Config: {}", config);

        // 3) Register descriptors
        DescriptorPool pool = new DescriptorPool();
        for (DescriptorProtos.FileDescriptorProto fdp : request.getProtoFileList()) {
            logger.info("Adding to pool: {}", fdp.getName());
            try {
                pool.add(fdp);
            } catch (Descriptors.DescriptorValidationException e) {
                logger.error("Failed to load descriptors for {}", fdp.getName(), e);
            }

        }

        PluginProtos.CodeGeneratorResponse.Builder response = PluginProtos.CodeGeneratorResponse.newBuilder();



        // 5) Set up TableManager
        TableManager manager = new TableManager(config, pool);

        // 6) First pass: warn about missing configs
        for (DescriptorProtos.FileDescriptorProto fd : request.getProtoFileList()) {
            if (!request.getFileToGenerateList().contains(fd.getName())) continue;
            logger.info("Processing {}", fd.getName());
            for (DescriptorProtos.DescriptorProto msg : fd.getMessageTypeList()) {
                String id = fd.getPackage() + "." + msg.getName();
                if (manager.getTableInFile(fd.getName(), id) == null) {
                    logger.warn("{} is not configured ({})", id, fd.getName());
                }
            }
        }

        manager.getAllTables().forEach(t-> {
            ClassType classType = t.getJavaType();
            logger.info("Generating {}", classType);
            var builder = response.addFileBuilder();
            builder.setName(classType.toString().replace('.', '/') + ".java");
            builder.setContent(TableRenderer.render(t));
        });

        // 7) Generate model files
//        Set<Path> createdInits = new HashSet<>();
//        for (DescriptorProtos.FileDescriptorProto fd : request.getProtoFileList()) {
//            if (!request.getFileToGenerateList().contains(fd.getName())) continue;
//            String baseName = fd.getName().replaceAll("\\.proto$", "");
//            PluginProtos.CodeGeneratorResponse.File.Builder modelFile = response.addFileBuilder();
//            modelFile.setName(baseName + "_model.java");
//
//            // ensure __init__.java equivalents (package-info.java?)
//            Path protoPath = Paths.get(fd.getName());
//            Path pkgDir = protoPath.getParent();
//            while (pkgDir != null && !createdInits.contains(pkgDir)) {
//                PluginProtos.CodeGeneratorResponse.File.Builder pkgFile = response.addFileBuilder();
//                pkgFile.setName(pkgDir.resolve("package-info.java").toString());
//                pkgFile.setContent("// package-info");
//                createdInits.add(pkgDir);
//                pkgDir = pkgDir.getParent();
//            }
//
//            List<Table> tables = manager.getAllTablesInFile(fd.getName()).toList();
//            Map<String, Set<String>> dependsOn = new HashMap<>();
//            Queue<Table> queue = new LinkedList<>();
//            Set<String> built = new HashSet<>();
//            Set<String> dependencies = new HashSet<>();
//
//            for (Table tbl : tables) {
//                Set<String> reqs = new HashSet<>();
//                tbl.requirements().forEach(r -> {
//                    if (tbl.getPackage().equals(r.module())) reqs.add(r.name());
//                    else if (r.module()!=null) dependencies.add(r.module());
//                });
//                dependsOn.put(tbl.getName(), reqs);
//                queue.add(tbl);
//            }
//
//            List<String> statements = new LinkedList<>();
//            int failureCount = 0;
//            while (!queue.isEmpty()) {
//                Table tbl = queue.poll();
//                if (!built.containsAll(dependsOn.get(tbl.getName()))) {
//                    logger.warn("Requeueing {} deps={} built={} hitcycle={} ", tbl.getName(), dependsOn.get(tbl.getName()), built, failureCount);
//                    queue.add(tbl);
//                    failureCount++;
//                } else {
//                    logger.info("Generating table {} deps={} ", tbl.getName(), dependsOn.get(tbl.getName()));
//                    statements.add(tbl.toString());
//                    built.add(tbl.getName());
//                    failureCount = 0;
//                }
//                if (failureCount >= queue.size() && !queue.isEmpty()) {
//                    throw new RuntimeException("Circular dependency detected");
//                }
//            }
//
//            // imports
//            StringBuilder content = new StringBuilder();
//            dependencies.forEach(dep -> content.append("import ").append(dep).append(";\n"));
//            content.append("\n");
//            statements.forEach(stmt -> content.append(stmt).append("\n\n"));
//            modelFile.setContent(content.toString());
//        }

        // 8) Write response
        PluginProtos.CodeGeneratorResponse resp = response.build();
        resp.writeTo(System.out);
    }
}