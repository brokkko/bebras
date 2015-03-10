package plugins.kio.logs;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

public class KioLogReader {

    private JsonNode root;

    public KioLogReader(JsonNode root) {
        this.root = root;
    }

    public KioLogReader(File input) {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jfactory = mapper.getFactory();

        try (JsonParser p = jfactory.createParser(input)) {
            root = p.readValueAsTree();
        } catch (IOException e) {
            root = null;
            e.printStackTrace();
        }
    }

    public void go(LogParserHandler handler) {
        JsonNode kioBase = root.get("kio_base");

        if (kioBase == null)
            return;

        JsonNode logBase = kioBase.get("log");

        if (logBase == null)
            return;

        Iterator<Map.Entry<String, JsonNode>> fields = logBase.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> next = fields.next();
            outputLogWithMachineInfo(next.getKey(), next.getValue(), handler);
        }
    }

    private void outputLogWithMachineInfo(String logId, JsonNode log, LogParserHandler handler) {
        /*
        out.println("OS:             " + info.get("os"));
        out.println("Manufacturer:   " + info.get("manufacturer"));
        out.println("CPU:            " + info.get("cpu"));
        out.println("Player version: " + info.get("version"));
        out.println("Language:       " + info.get("language"));
        out.println("Player type:    " + info.get("playerType"));
        out.println("DPI:            " + info.get("dpi"));
        out.println("Screen width:   " + info.get("screenWidth"));
        out.println("Screen height:  " + info.get("screenHeight"));
        */

        JsonNode info = log.get("machine_info");

        JsonNode osNode = info.get("os");
        JsonNode manufacturerNode = info.get("manufacturer");
        JsonNode cpuNode = info.get("cpu");
        JsonNode versionNode = info.get("version");
        JsonNode languageNode = info.get("language");
        JsonNode playerTypeNode = info.get("playerType");
        JsonNode dpiNode = info.get("dpi");
        JsonNode screenWidthNode = info.get("screenWidth");
        JsonNode screenHeightNode = info.get("screenHeight");

        handler.handleSubLog(logId, new MachineInfo(
                osNode == null ? null : osNode.asText(),
                manufacturerNode == null ? null : manufacturerNode.asText(),
                cpuNode == null ? null : cpuNode.asText(),
                versionNode == null ? null : versionNode.asText(),
                languageNode == null ? null : languageNode.asText(),
                playerTypeNode == null ? null : playerTypeNode.asText(),
                dpiNode == null ? null : dpiNode.asText(),
                screenWidthNode == null ? null : screenWidthNode.asText(),
                screenHeightNode == null ? null : screenHeightNode.asText()
        ));

        LogParser logParser = new LogParser();
        logParser.parse(log, handler);
    }

}
