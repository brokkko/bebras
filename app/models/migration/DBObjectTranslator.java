package models.migration;

import com.mongodb.DBObject;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 26.07.13
 * Time: 23:42
 */
public interface DBObjectTranslator {

    boolean translate(DBObject object);

}
