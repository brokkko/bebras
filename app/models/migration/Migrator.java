package models.migration;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import play.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 26.07.13
 * Time: 23:13
 */
public abstract class Migrator {

    //calls migrate(collection, translator) for several collections
    public abstract void migrate();

    //return changed or not
    protected void migrate(DBCollection collection, DBObjectTranslator translator) {
        Logger.info("Migrating collection " + collection.getName());

        try (DBCursor dbObjects = collection.find()) {
            while (dbObjects.hasNext()) {
                DBObject dbObject = dbObjects.next();
                boolean changed = translator.translate(dbObject);

                if (changed)
                    collection.save(dbObject);
            }
        }
    }
}
