package plugins.kio;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Comparator;
import java.util.List;

public class JsonObjectsComparator implements Comparator<JsonNode> {

    private List<KioParameter> params;

    public JsonObjectsComparator(List<KioParameter> params) {
        this.params = params;
    }

    @Override
    public int compare(JsonNode o1, JsonNode o2) {
        //if some parameter is absent, then make element null

        o1 = normalize(o1);
        o2 = normalize(o2);

        if (o1 == null && o2 == null)
            return 0;
        if (o1 == null)
            return -1;
        if (o2 == null)
            return 1;

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

                    if (param.isHasBound()) {
                        if (dir > 0) {
                            d1 = Math.min(d1, param.getBound());
                            d2 = Math.min(d2, param.getBound());
                        } else {
                            d1 = Math.max(d1, param.getBound());
                            d2 = Math.max(d2, param.getBound());
                        }
                    }

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

    private JsonNode normalize(JsonNode o) {
        if (o == null)
            return null;
        for (KioParameter param : params)
            if (o.get(param.getId()) == null)
                return null;
        return o;
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