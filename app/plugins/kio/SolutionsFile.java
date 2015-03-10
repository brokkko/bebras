package plugins.kio;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import plugins.kio.logs.KioLogReader;
import plugins.kio.logs.LogParserHandler;
import plugins.kio.logs.MachineInfo;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolutionsFile {

    private KioProblemSet problemSet;
    private int level;
    private JsonNode root;

    private final Map<String, JsonObjectsComparator> id2comp = new HashMap<>();
    ;

    public SolutionsFile(File file, int level, KioProblemSet problemSet) {
        this.problemSet = problemSet;

        this.level = level;

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        try (JsonParser p = factory.createParser(file)) {
            root = p.readValueAsTree();

            //fill id2comp
            for (String pid : problemSet.getProblemIds(level))
                id2comp.put(pid, problemSet.comparator(level, pid));
        } catch (IOException e) {
            root = null;
        }
    }

    public Map<String, JsonNode> getProblemsResults() {
        if (root == null)
            return Collections.emptyMap();

        Map<String, JsonNode> res = new HashMap<>();

        List<String> problemIds = problemSet.getProblemIds(level);
        for (String problemId : problemIds) {
            JsonNode problemNode = root.get(problemId);
            if (problemNode == null)
                continue;
            JsonNode bestResultNode = problemNode.get("best_result");
            if (bestResultNode == null)
                continue;

            res.put(problemId, bestResultNode);
        }

        return res;
    }

    public Map<String, JsonNode> getLogResults() {
        if (root == null)
            return Collections.emptyMap();

        final Map<String, JsonNode> res = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        final JsonFactory f = mapper.getFactory();
        final Pattern p = Pattern.compile("([a-zA-Z0-9_-]+): New record!");

        new KioLogReader(root).go(new LogParserHandler() {
            @Override
            public void handleSubLog(String logId, MachineInfo info) {
                //do nothing
            }

            @Override
            public void handleCommand(long time, String cmd, Object[] extraArgs) {
                Matcher m = p.matcher(cmd);
                if (!m.matches())
                    return;
                String pid = m.group(1);

                JsonObjectsComparator comparator = id2comp.get(pid);
                if (comparator == null)
                    return;

                if (extraArgs.length != 2)
                    return;
                String resultAsString = String.valueOf(extraArgs[1]);

                JsonNode newResult;
                try {
                    newResult = f.createParser(resultAsString).readValueAsTree();
                } catch (IOException e) {
                    return; //failed to parse
                }

                if (!newResult.isObject())
                    return;

                JsonNode oldResult = res.get(pid);
                if (oldResult == null || comparator.compare(oldResult, newResult) < 0)
                    res.put(pid, newResult);
            }
        });

        return res;
    }

    public Map<String, JsonNode> uniteLogAndProblemResults() {
        return unite(getLogResults(), getProblemsResults());
    }

    public Map<String, JsonNode> unite(Map<String, JsonNode> m1, Map<String, JsonNode> m2) {
        Set<String> keys = new HashSet<>(m1.keySet());
        keys.addAll(m2.keySet());

        Map<String, JsonNode> res = new HashMap<>();

        for (String key : keys) {
            JsonNode n1 = m1.get(key);
            JsonNode n2 = m2.get(key);
            if (n1 == null) {
                res.put(key, n2);
                continue;
            }
            if (n2 == null) {
                res.put(key, n1);
                continue;
            }

            JsonObjectsComparator comparator = id2comp.get(key);
            if (comparator == null)
                continue;

            res.put(key, comparator.compare(n1, n2) >= 0 ? n1 : n2);
        }

        return res;
    }

}
