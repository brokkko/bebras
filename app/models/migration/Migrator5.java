package models.migration;

import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.MongoConnection;

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

        DBCollection eventsCollection = MongoConnection.getEventsCollection();

        migrate(eventsCollection, new DBObjectTranslator() {
            @Override
            public boolean translate(DBObject event) {
                BasicDBList contests = (BasicDBList) event.get("contests");
                for (Object _contest : contests) {
                    DBObject contest = (DBObject) _contest;

                    //update translator field for contests
                    DBObject translator = (DBObject) contest.get("results translator");
                    if (translator != null) {
                        BasicDBList translators = new BasicDBList();
                        translators.add(translator);

                        contest.removeField("results translator");
                        contest.put("results translators", translators);
                    }
                }

                DBObject translator = (DBObject) event.get("results translator");

                if (translator != null) {
                    BasicDBList translators = new BasicDBList();
                    translators.add(translator);

                    event.removeField("results translator");
                    event.put("results translators", translators);
                }
                return true;
            }
        });
    }
}
