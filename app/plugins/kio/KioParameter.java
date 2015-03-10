package plugins.kio;

import java.util.GregorianCalendar;

public class KioParameter {

    private String id;
    private String name;
    private int sortDirection;
    private String postfix;
    private char type; //i - int, d - double

    public KioParameter(String id, String name, int sortDirection, String postfix, char type) {
        this.id = id;
        this.name = name;
        this.sortDirection = sortDirection;
        this.postfix = postfix;
        this.type = type;
    }

    public KioParameter(String definition) {
        String[] colonSplit = definition.split(":");
        id = colonSplit[0];
        name = colonSplit[1];
        type = id.charAt(id.length() - 1);
        sortDirection = id.charAt(id.length() - 2) == '+' ? 1 : -1;
        id = id.substring(0, id.length() - 2);

        postfix = "";
        int tildaIndex = name.indexOf("~");
        if (tildaIndex >= 0) {
            postfix = name.substring(tildaIndex + 1);
            name = name.substring(0, tildaIndex);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSortDirection() {
        return sortDirection;
    }

    public String getPostfix() {
        return postfix;
    }

    public char getType() {
        return type;
    }
}
