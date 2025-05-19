package es.upm.etsiinf.tfg.juanmahou.plugin.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;

public class TableTemplateManager {
    private static final Logger log = LoggerFactory.getLogger(TableTemplateManager.class);
    public static final STGroup GROUP = loadGroup();

    private static STGroup loadGroup() {
        STGroup g = new STGroupDir("templates");
        log.info("GROUP.getNames(): {}", g.getTemplateNames());
        return g;
    }

    public static ST getTableST() {
        return GROUP.getInstanceOf("table");
    }
}
