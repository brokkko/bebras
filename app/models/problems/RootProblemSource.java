package models.problems;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.01.13
 * Time: 14:09
 */
public class RootProblemSource implements ProblemsSource {

    private Map<String, ProblemsSource> sources = new HashMap<>();

    public void mount(String root, ProblemsSource source) {
        sources.put(root + ".", source);
    }

    @Override
    public Problem get(String id) {
        for (Map.Entry<String, ProblemsSource> root0source : sources.entrySet()) {
            String root = root0source.getKey();
            if (id.startsWith(root)) {
                String delegateId = id.substring(root.length());
                return root0source.getValue().get(delegateId);
            }
        }
        return null;
    }
}
