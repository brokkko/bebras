package plugins.kio.logs;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LogParser {

    private static final int MSG_1_BYTE_MAX = 250;
    private static final String LOG_CMD_TIMESTAMP = "timestamp";
    private static final String LOG_CMD_TIME_BYTE = "time-byte";
    
    private static final Object[] EMPTY_ARRAY = new Object[0];

    public void parse(JsonNode log, LogParserHandler handler) {
        JsonNode dataNode = log.get("data");
        if (dataNode == null)
            return;

        JsonNode base64Node = dataNode.get("base64");
        if (base64Node == null)
            return;

        byte[] rawData; //Base64.getDecoder().decode(base64Node.asText());
        try {
            //got from http://www.iharder.net/current/java/base64/
            //TODO migrate to Java 8 with Base64
            rawData = Base64.decode(base64Node.asText());
        } catch (IOException e) {
            return;
        }
        ByteArray data = new ByteArray(rawData);

        long last_log_start = 0;
        long last_log_time = 0;

        Map<Integer, String> inverseDict = new HashMap<>();
        JsonNode dict = log.get("dict");
        if (dict == null)
            return;

        Iterator<Map.Entry<String, JsonNode>> dictFields = dict.fields();
        while (dictFields.hasNext()) {
            Map.Entry<String, JsonNode> next = dictFields.next();
            inverseDict.put(next.getValue().asInt(), next.getKey());
        }

        int timeByte = 0;
        while (data.bytesAvailable() > 0) {
            int cmd = data.readUnsignedByte();
            if (cmd >= MSG_1_BYTE_MAX) {
                int rst = data.readUnsignedByte();
                cmd = MSG_1_BYTE_MAX + (cmd - MSG_1_BYTE_MAX << 8) + rst;
            }

            String command = inverseDict.get(cmd);
            if (command == null)
                throw new IllegalStateException("Unknown command in log " + cmd);

            switch (command) {
                case LOG_CMD_TIME_BYTE:
                    timeByte = data.readUnsignedByte();
                    continue;
                case LOG_CMD_TIMESTAMP:
                    timeByte = 0;
                    last_log_start = (long)data.readDouble();
                    last_log_time = 0;
                    continue;
                default:
                    last_log_time += data.readUnsignedShort() + timeByte * 65536;
                    timeByte = 0;
                    long time = 10 * last_log_time + last_log_start;

                    Object[] extraArguments;
                    String spec = getLogSpecification(command);
                    if (spec != null)
                        extraArguments = readExtraArguments(spec, data);
                    else
                        extraArguments = EMPTY_ARRAY;

                    //filter command
                    int at_pos = command.lastIndexOf('@');
                    if (at_pos >= 0)
                        command = command.substring(0, at_pos).trim(); //replace = trim right

                    handler.handleCommand(time, command, extraArguments);
            }
        }
    }

    private Object[] readExtraArguments(String spec, ByteArray data) {
        Object[] result = new Object[spec.length()];
        for (int i = 0; i < spec.length(); i++)
            switch (spec.charAt(i)) {
                case 'i':
                    result[i] = data.readInt();
                    break;
                case 'I':
                    result[i] = data.readUnsignedInt();
                    break;
                case 'b':
                    result[i] = data.readByte();
                    break;
                case 'B':
                    result[i] = data.readUnsignedByte();
                    break;
                case 's':
                    result[i] = data.readShort();
                    break;
                case 'S':
                    result[i] = data.readUnsignedShort();
                    break;
                case 't':
                    result[i] = data.readUTF();
                    break;
            }

        return result;
    }

    private static String getLogSpecification(String msg) {
        int atIndex = msg.lastIndexOf('@');
        if (atIndex >= 0)
            return msg.substring(atIndex + 1);
        else
            return null;
    }
}
