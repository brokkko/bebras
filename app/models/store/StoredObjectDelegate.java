package models.store;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.01.13
 * Time: 11:10
 */
public abstract class StoredObjectDelegate extends StoredObject {

    private StoredObject storedObject;

    protected StoredObjectDelegate(StoredObject storedObject) {
        this.storedObject = storedObject;
    }

    @Override
    public Object get(String field) {
        return storedObject.get(field);
    }

    @Override
    public void put(String field, Object value) {
        storedObject.put(field, value);
    }

    @Override
    public void store() {
        storedObject.store();
    }

    @Override
    public Set<String> keySet() {
        return storedObject.keySet();
    }
}
