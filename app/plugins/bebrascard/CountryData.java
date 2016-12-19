package plugins.bebrascard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import java.util.Collections;
import java.util.List;

public class CountryData {

    private String name;
    private String folderName;
    private List<CountryImage> images;

    public CountryData(String name, String folderName, List<CountryImage> images) {
        this.name = name;
        this.folderName = folderName;
        this.images = images;
    }

    public String getName() {
        return name;
    }

    public String getFolderName() {
        return folderName;
    }

    public List<CountryImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    public JsonNode asJavaScriptObject() {
        ObjectNode o = Json.newObject();
        o.put("name", name);
        o.put("folder", folderName);
        ArrayNode a = o.putArray("c");
        for (CountryImage image : images)
            a.add(image.asJavaScriptObject());
        return o;
    }
}
