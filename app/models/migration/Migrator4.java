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
public class Migrator4 extends Migrator {
    @Override
    public void migrate() {
        //substitute all links to problems with problem ids

        for (DBCollection collection : MongoConnection.getContestCollections()) {
            migrate(collection, new DBObjectTranslator() {
                @Override
                public boolean translate(DBObject object) {
                    Object pid = object.get("pid");

                    if (pid == null) {
                        Logger.warn("Problem with pid null");
                        return false;
                    }

                    if (pid instanceof String) {
                        String problemId = (String) pid;

                        if (problemId.startsWith("/"))
                            problemId = problemId.substring(1);

                        int pos = problemId.lastIndexOf('/');
                        if (pos < 0) {
                            Logger.warn("pid with no /: " + pid);
                            return false;
                        }

                        String linkStart = problemId.substring(0, pos);
                        String linkEnd = problemId.substring(pos + 1);
                        int n;
                        try {
                            n = Integer.parseInt(linkEnd);
                        } catch (NumberFormatException ignored) {
                            Logger.warn("pid with not a number at end: " + pid);
                            return false;
                        }

                        linkStart = linkStart.replaceAll("bbtc/1", "bbtc/express");
                        linkStart = linkStart.replaceAll("bbtc/2", "bbtc/hard");
                        linkStart = linkStart.replaceAll("bbtc/3", "bbtc/explore");

                        List<ProblemLink> problemLinks = new ProblemLink(linkStart).listProblems();

                        if (n - 1 >= problemLinks.size())
                            return false;

                        ProblemLink pl = problemLinks.get(n - 1);

                        ObjectId newPid = pl.getProblemId();
                        if (newPid == null) {
                            Logger.warn("no link " + pl.getLink());
                            return false;
                        }

                        object.put("pid", newPid);

                        return true;
                    }

                    return false;
                }
            });
        }
    }
}
