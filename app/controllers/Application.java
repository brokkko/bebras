package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import play.Play;
import play.cache.Cache;
import play.mvc.*;

import java.io.*;

public class Application extends Controller {

    public static Result initialize() throws IOException {
        DBCollection configCollection = MongoConnection.getConfigCollection();
        if (configCollection.findOne() != null && !Play.isDev())
            return badRequest("Site is already initialized");
//            return notFound();
//        TODO bug - wrong indent after comments

        //TODO this is a temporary initialization, move it somewhere else

        configCollection.remove(new BasicDBObject()); //remove all

        DBCollection bbtcCollection = MongoConnection.getEventsCollection();
        bbtcCollection.remove(new BasicDBObject());

        //bbtc event object
        DBObject bbtcObject = (DBObject) JSON.parse(getResourceAsString("/bbtc_event.json"));
        bbtcCollection.save(bbtcObject);

        //clear Cache
        Cache.set("event-bbtc", null, 1);

        //save configuration object to make it not possible to reinit the application
        configCollection.save(new BasicDBObject());
        return ok("Site is successfully initialized");
    }

    private static String getResourceAsString(String name) throws IOException {
        InputStream inS = Application.class.getResourceAsStream(name);
        BufferedReader inR = new BufferedReader(new InputStreamReader(inS, "UTF8"));
        CharArrayWriter out = new CharArrayWriter();
        int r;
        while ((r = inR.read()) >= 0)
            out.write(r);
        inR.close();
        return out.toString();
    }

}