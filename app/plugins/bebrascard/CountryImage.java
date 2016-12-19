package plugins.bebrascard;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import java.io.File;

public class CountryImage {

    private String description;
    private File file;

    public CountryImage(String description, File file) {
        this.description = description;
        this.file = file;
    }

    public String getDescription() {
        return description;
    }

    public File getFile() {
        return file;
    }

    public ObjectNode asJavaScriptObject() {
        ObjectNode o = Json.newObject();

        o.put("descr", description);
        o.put("file", file.getName());

        return o;
    }
}
