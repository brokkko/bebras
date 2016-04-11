package plugins.kio;

public class KioParameter {

    private String id;
    private String name;
    private int sortDirection;
    private String postfix;
    private char type; //i - int, d - double

    private boolean hasBound;
    private double bound;

    public KioParameter(String id, String name, int sortDirection, String postfix, char type) {
        this(id, name, sortDirection, postfix, type, false, 0);
    }

    public KioParameter(String id, String name, int sortDirection, String postfix, char type, boolean hasBound, double bound) {
        this.id = id;
        this.name = name;
        this.sortDirection = sortDirection;
        this.postfix = postfix;
        this.type = type;
        this.hasBound = hasBound;
        this.bound = bound;
    }

    public KioParameter(String definition) {
        String[] colonSplit = definition.split(":");
        id = colonSplit[0];

        hasBound = false;

        if (colonSplit.length == 2)
            name = colonSplit[1];
        else {
            hasBound = true;
            try {
                bound = Double.valueOf(colonSplit[1]);
            } catch (NumberFormatException e) {
                bound = 0;
            }
            name = colonSplit[2];
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KioParameter that = (KioParameter) o;

        if (sortDirection != that.sortDirection) return false;
        if (type != that.type) return false;
        if (!id.equals(that.id)) return false;
        if (!name.equals(that.name)) return false;
        if (!postfix.equals(that.postfix)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + sortDirection;
        result = 31 * result + postfix.hashCode();
        result = 31 * result + (int) type;
        return result;
    }

    public double getBound() {
        return bound;
    }

    public boolean isHasBound() {
        return hasBound;
    }
}
