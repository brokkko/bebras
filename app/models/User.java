package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.MongoConnection;
import org.bson.types.ObjectId;
import play.Play;
import play.mvc.Http;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 31.12.12
 * Time: 1:44
 */
public class User {

    public StoredObject storedObject;

    public User(StoredObject storedObject) {
        this.storedObject = storedObject;
    }

    public StoredObject getStoredObject() {
        return storedObject;
    }

    public String getLogin() {
        return storedObject.getString("login");
    }

    public String getEmail() {
        return storedObject.getString("email");
    }

    public boolean testPassword(String password) {
        return passwordHash(password).equals(storedObject.getString("passHash"));
    }

    private String passwordHash(String password) {
        //TODO understand this code
        //http://stackoverflow.com/questions/2860943/suggestions-for-library-to-hash-passwords-in-java
        try {
            byte[] salt = new BigInteger(Play.application().configuration().getString("salt"), 16).toByteArray();
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            byte[] hash = f.generateSecret(spec).getEncoded();
            return new BigInteger(1, hash).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (InvalidKeySpecException e) {
            return null;
        }
    }

    public static User current() {
        Map<String, Object> contextArgs = Http.Context.current().args;

        User user = (User) contextArgs.get("user");
        if (user == null) {
            String username = Http.Context.current().request().username();
            user = getInstance(username, false);
            contextArgs.put("user", user);
        }

        return user;
    }

    public static User getInstance(String username, boolean byLogin) {
        DBCollection usersCollection = MongoConnection.getUsersCollection();

        DBObject query = new BasicDBObject("event_id", Event.current().getOid());

        if (byLogin)
            query.put("login", username);
        else
            query.put("_id", new ObjectId(username));

        DBObject userObject = usersCollection.findOne(query);
        if (userObject == null)
            return null;
        else
            return new User(new MongoObject(usersCollection.getName(), userObject));
    }
}