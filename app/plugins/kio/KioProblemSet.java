package plugins.kio;

import java.util.ArrayList;
import java.util.List;

public abstract class KioProblemSet {

    public static KioProblemSet getInstance(int year) {
        switch (year) {
            case 2014: return new KioProblemsSet2014();
            case 2015: return new KioProblemsSet2015();
        }
        return null;
    }

    public abstract List<String> getProblemIds(int level);

    public abstract String getProblemName(int level, String id);

    public abstract List<KioParameter> getParams(int level, String id);

    public JsonObjectsComparator comparator(int level, String id) {
        return new JsonObjectsComparator(getParams(level, id));
    }

    protected List<KioParameter> kioParameters(String... args) {
        List<KioParameter> params = new ArrayList<>();
        for (String arg : args)
            params.add(new KioParameter(arg));

        return params;
    }
}
