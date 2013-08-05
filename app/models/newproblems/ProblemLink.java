package models.newproblems;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.Utils;
import models.newserialization.Deserializer;
import models.newserialization.MongoDeserializer;
import org.bson.types.ObjectId;
import play.Logger;
import play.cache.Cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 27.07.13
 * Time: 16:32
 */
public class ProblemLink {

    public static final String FIELD_LINK = "link";
    public static final String FIELD_PROBLEM = "pid";

    private final String link;

    public ProblemLink(String link) {
        if (link == null)
            throw new IllegalArgumentException("Problem link can not be null");

        this.link = normalize(link);
    }

    private String normalize(String link) {
        if (link.startsWith("/"))
            link = link.substring(1);
        if (link.endsWith("/"))
            link = link.substring(0, link.length() - 1);
        return link;
    }

    public ProblemLink(Deserializer deserializer) {
        this.link = deserializer.readString(FIELD_LINK);
        //loads and caches info
        getInfo(deserializer);
    }

    public ProblemLink(ProblemLink folder, String name) {
        this.link = folder.getLink() + '/' + name;
    }

    public String getLink() {
        return link;
    }
    public Problem get() {
        return getInfo().getProblem();
    }

    public ProblemLink child(String child) {
        return new ProblemLink(this, child);
    }

    public boolean exists() {
        return getInfo().isExists();
    }

    public ObjectId getId() {
        return getInfo().getId();
    }

    public ObjectId getProblemId() {
        return getInfo().getPid();
    }

    public boolean isFolder() {
        return getInfo().isFolder();
    }

    public boolean isProblem() {
        return getInfo().isProblem();
    }

    //TODO need to cache this
    public List<ProblemLink> listProblems() {
        return list(true, false);
    }

    public List<ProblemLink> listFolders() {
        return list(false, true);
    }

    public List<ProblemLink> list() {
        return list(false, false);
    }

    private List<ProblemLink> list(boolean filterProblems, boolean filterFolders) {
        BasicDBObject query = new BasicDBObject(
                FIELD_LINK, new BasicDBObject("$regex", "^" + link + "/[^/]+$")
        );

        ArrayList<ProblemLink> links = new ArrayList<>();

        try (DBCursor cursor = MongoConnection.getProblemDirsCollection().find(query)) {
            while (cursor.hasNext()) {
                MongoDeserializer deserializer = new MongoDeserializer(cursor.next());
                ProblemLink link = new ProblemLink(deserializer);

                if (filterFolders && !link.isFolder())
                    continue;
                if (filterProblems && !link.isProblem())
                    continue;

                links.add(link);
            }
        }

        Collections.sort(links, new Comparator<ProblemLink>() {
            @Override
            public int compare(ProblemLink l1, ProblemLink l2) {
                if (l1 == null)
                    return l2 == null ? 0 : -1;
                if (l2 == null)
                    return 1;
                return Utils.compareStrings(l1.getName(), l2.getName()); //may take links and not names
            }
        });

        return links;
    }

    public String getName() {
        String[] split = link.split("/");
        return split[split.length - 1];
    }

    public void mkdir() {
        if (isProblem()){
            Logger.warn("Trying to make problem a folder");
            return;
        }

        if (exists())
            return;

        DBObject res = new BasicDBObject(FIELD_LINK, link);
        res.put(FIELD_PROBLEM, null);
        MongoConnection.getProblemDirsCollection().save(res);

        Cache.remove(cacheKey());
    }

    public void mkdirs() {
        String[] path = link.split("/");
        String l = path[0];
        for (String pathElement : path) {
            if (!l.isEmpty())
                l += '/';
            l += pathElement;

            new ProblemLink(l).mkdir();
        }
    }

    public void setProblemId(ObjectId pid) {
        if (isFolder()) {
            Logger.warn("Trying to link folder to problem");
            return;
        }

        //will be created if does not exist

        DBObject res = new BasicDBObject();
        res.put("_id", getId());
        res.put(FIELD_LINK, link);
        res.put(FIELD_PROBLEM, pid);
        MongoConnection.getProblemDirsCollection().save(res);

        Cache.remove(cacheKey());
    }

    public void remove() {
        if (!exists())
            return;

        MongoConnection.getProblemDirsCollection().remove(new BasicDBObject("_id", getId()));

        Cache.remove(cacheKey());
    }

    private String cacheKey() {
        return "problem-link-" + link;
    }

    private LinkInfo getInfo() {
        return getInfo(null);
    }

    private LinkInfo getInfo(final Deserializer deserializer) {
        try {
            return Cache.getOrElse(cacheKey(), new Callable<LinkInfo>() {
                @Override
                public LinkInfo call() throws Exception {
                    if (deserializer == null)
                        return loadInfo();
                    else
                        return new LinkInfo(deserializer);
                }
            }, 10 * 60); // 10 minutes
        } catch (Exception e) {
            Logger.error("Failed to load info", e);
            return new LinkInfo();
        }
    }

    private LinkInfo loadInfo() {
        DBObject db = MongoConnection.getProblemDirsCollection().findOne(new BasicDBObject(FIELD_LINK, link));
        if (db == null)
            return new LinkInfo();

        return new LinkInfo(new MongoDeserializer(db));
    }

    private class LinkInfo {
        private ObjectId id;

        private boolean exists;
        private boolean folder;
        private ObjectId pid;

        private Problem problem; //it is also cached

        private LinkInfo() {
            this(null, false, false, null, null);
        }

        private LinkInfo(ObjectId id, boolean exists, boolean folder, ObjectId pid, Problem problem) {
            this.id = id;
            this.exists = exists;
            this.folder = folder;
            this.pid = pid;
            this.problem = problem;
        }

        public LinkInfo(Deserializer deserializer) {
            id = deserializer.readObjectId("_id");
            exists = true;
            pid = deserializer.readObjectId(FIELD_PROBLEM);
            folder = pid == null;
        }

        private ObjectId getId() {
            return id;
        }

        private boolean isExists() {
            return exists;
        }

        private boolean isFolder() {
            return exists && folder;
        }

        private boolean isProblem() {
            return exists && !folder;
        }

        private ObjectId getPid() {
            return pid;
        }

        private Problem getProblem() {
            if (folder)
                return null;
            if (problem == null) {
                ProblemInfo problemInfo = ProblemInfo.get(pid);
                problem = problemInfo.getProblem();
            }

            return problem;
        }
    }
}
