package models.migration;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.newproblems.ProblemLink;
import org.bson.types.ObjectId;
import play.Logger;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.08.13
 * Time: 0:05
 */
public class Migrator5 extends Migrator {
    @Override
    public void migrate() {
        //substitute all links to problems with problem ids

        DBCollection collection = MongoConnection.getUsersCollection();

        migrate(collection, new DBObjectTranslator() {
            @Override
            public boolean translate(DBObject object) {
                return false;
            }
        });
    }
}
