package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import models.newserialization.Deserializer;
import models.newserialization.MongoDeserializer;
import models.newserialization.MongoSerializer;
import models.newserialization.Serializer;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.mvc.Http;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 26.07.13
 * Time: 20:31
 */
public class ServerConfiguration {

    private static final String CACHE_KEY = "server-configuration";
    public static final int CURRENT_DB_VERSION = 8;

    public static ServerConfiguration getInstance() {
        try {
            return Cache.getOrElse(CACHE_KEY, new Callable<ServerConfiguration>() {
                @Override
                public ServerConfiguration call() throws Exception {
                    return loadServerConfiguration();
                }
            }, 0);
        } catch (Exception e) {
            Logger.error("Failed to create configuration", e);
            return new ServerConfiguration();
        }
    }

    private static ServerConfiguration loadServerConfiguration() {
        DBCollection configCollection = MongoConnection.getConfigCollection();

        DBObject config = configCollection.findOne(new BasicDBObject("_id", 42));

        ServerConfiguration serverConfiguration = new ServerConfiguration();

        if (config != null)
            serverConfiguration.update(new MongoDeserializer(config));

        return serverConfiguration;
    }

    private int dbVersion = 1;
    private boolean maintenanceMode = false;

    private final SecureRandom random = new SecureRandom();
    private final char[] randomCharacters;

    public ServerConfiguration() {
        List<Character> chars = new ArrayList<>();

        for (char c = 'a'; c <= 'z'; c++)
            chars.add(c);

        for (char c = 'A'; c <= 'Z'; c++)
            chars.add(c);

        for (char c = '0'; c <= '9'; c++)
            chars.add(c);

        chars.add('-');
        chars.add('_');

        randomCharacters = new char[chars.size()];
        for (int i = 0; i < chars.size(); i++) {
            char c = chars.get(i);
            randomCharacters[i] = c;
        }
    }

    public int getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
        store();
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
        store();
    }

    private void update(Deserializer deserializer) {
        dbVersion = deserializer.readInt("db version", 1);
        maintenanceMode = deserializer.readBoolean("maintenance", false);
    }

    private void serialize(Serializer serializer) {
        serializer.write("_id", 42);
        serializer.write("db version", dbVersion);
        serializer.write("maintenance", maintenanceMode);
    }

    private void store() {
        MongoSerializer serializer = new MongoSerializer();
        serialize(serializer);
        MongoConnection.getConfigCollection().save(serializer.getObject());

        Cache.remove(CACHE_KEY);
    }

    public File getResourcesFolder() {
        return Play.application().getFile("data/_resources");
    }

    public File getNewResourceFile(String extension) {
        if (extension == null || extension.isEmpty())
            extension = "";
        else
            extension = "." + extension;

        String destFileName = ServerConfiguration.getInstance().getRandomString(20) + System.currentTimeMillis() + extension;
        return new File(ServerConfiguration.getInstance().getResourcesFolder(), destFileName);
    }

    public long getRandomLong() {
        return random.nextLong();
    }

    public String getRandomString(int len) {
        char[] chars = new char[len];
        int size = randomCharacters.length;

        for (int i = 0; i < len; i++)
            chars[i] = randomCharacters[random.nextInt(size)];

        return new String(chars);
    }

    public String getCurrentDomain() {
        String domain = Http.Context.current().request().host().toLowerCase();

        if (domain.contains(":"))
            domain = domain.substring(0, domain.indexOf(':'));

        if (domain.startsWith("www."))
            domain = domain.substring(4);

        if (domain.equals("localhost")) //TODO allow to set this up
//            domain = "on-line.runodog.ru";
            domain = "bebras.ru";

        return domain;
    }

    public String getDefaultDomainEvent() {
        String domain = getCurrentDomain();

        return domain.contains("bebras") ? "bebras13" : "bbtc"; //TODO allow to set this up
    }

    public File getResource(String name) {
        String base = "/data/_resources";
        return new File(Play.application().getFile(base).getAbsolutePath() + "/" + name);
    }

    public String getSkin() {
        return Event.currentId().startsWith("bebras") ? "bebras" : "bbtc"; //TODO generalize //TODO currentId may lead to exception
    }
}
