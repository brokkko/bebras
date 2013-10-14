package plugins;

import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.Serializer;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 14.10.13
 * Time: 23:08
 */
public class ApplicationType implements SerializableUpdatable {

    private String typeName;
    private String description;
    private boolean needsConfirmation;
    private int price; /* TODO make price float */

    public String getTypeName() {
        return typeName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isNeedsConfirmation() {
        return needsConfirmation;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("type", typeName);
        serializer.write("description", description);
        serializer.write("confirmation", needsConfirmation);
        serializer.write("price", price);
    }

    @Override
    public void update(Deserializer deserializer) {
        typeName = deserializer.readString("type");
        description = deserializer.readString("description");
        needsConfirmation = deserializer.readBoolean("confirmation", true);
        price = deserializer.readInt("price", 0);
    }
}
