package plugins.kio;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.List;

public class KioProblemsSet2015 extends KioProblemSet {

    public List<String> getProblemIds(int level) {
        return Arrays.asList("traincars", "markov", "spider");
    }

    public String getProblemName(int level, String id) {
        switch (id) {
            case "markov":
                switch (level) {
                    case 0:
                        return "Прополка";
                    case 1:
                    case 2:
                        return "Калькулятор";
                }
            case "spider":
                return "Паук";
            case "traincars":
                return "Поезда";
        }
        return "???";
    }

    public List<KioParameter> getParams(int level, String id) {
        switch (id) {
            case "markov":
                switch (level) {
                    case 0: return kioParameters("ridgeDiff-i:различий", "ruleAmount-i:указаний", "applyOperations-i:замен");
                    case 1:
                    case 2: return kioParameters("correctAmount+i:верно~%", "ruleAmount-i:правил");
                }
            case "spider":
                return kioParameters("t-d:время~ с", "m-d:материал~ см");
            case "traincars":
                switch (level) {
                    case 0: return kioParameters("c+i:верно", "t-i:беспорядок", "h-i:действий");
                    case 1: return kioParameters("c+i:верно", "t-i:беспорядок", "uh-i:подъемов", "dh-i:спусков");
                    case 2: return kioParameters("c+i:верно", "t-i:транспозиций", "uh-i:подъемов", "dh-i:спусков");
                }
        }
        return null;
    }

    @Override
    public JsonObjectsComparator comparator(int level, String id) {
        /*if ("spider".equals(id))
            return new JsonObjectsComparator(getParams(level, id)) {
                @Override
                public int compare(JsonNode o1, JsonNode o2) {
                    return super.compare(o1, o2);
                }
            };*/

        return super.comparator(level, id);
    }
}
