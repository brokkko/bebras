package plugins.kio;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Comparator;
import java.util.List;

public class JsonObjectsComparator implements Comparator<JsonNode> {

    private List<KioParameter> params;

    public JsonObjectsComparator(List<KioParameter> params) {
        this.params = params;
        if (params == null)
            System.out.println("hm");
    }

    @Override
    public int compare(JsonNode o1, JsonNode o2) {
        for (KioParameter param : params) {
            String field = param.getId();
            int dir = param.getSortDirection();
            char type = param.getType();
            int cmp = 0;

            JsonNode f1 = o1.get(field);
            JsonNode f2 = o2.get(field);

            int nullCmp = compareNulls(f1, f2);
            if (nullCmp != 2)
                return nullCmp;

            switch (type) {
                case 'i':
                    int i1 = f1.asInt();
                    int i2 = f2.asInt();
                    cmp = i1 - i2;
                    break;
                case 'd':
                    double d1 = f1.asDouble();
                    double d2 = f2.asDouble();
                    cmp = d1 > d2 ? 1 : (d1 < d2 ? -1 : 0);
                    break;
            }
            if (dir < 0)
                cmp = -cmp;
            if (cmp != 0)
                return cmp;
        }

        return 0;
    }

    protected int compareNulls(Object n1, Object n2) {
        if (n1 == null && n2 == null)
            return 0;
        if (n1 == null)
            return -1;
        if (n2 == null)
            return 1;
        return 2;
    }
}