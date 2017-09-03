package plugins.applications;

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
    private String participantRole;
    private boolean self;
    private boolean allowSelect = true;
    private String userFlag = null;
    private String rightToPay;

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

    public String getParticipantRole() {
        return participantRole;
    }

    public boolean isSelf() {
        return self;
    }

    public boolean isAllowSelect() {
        return allowSelect;
    }

    public String getUserFlag() {
        return userFlag;
    }

    public String getRightToPay() {
        return rightToPay;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("type", typeName);
        serializer.write("description", description);
        serializer.write("confirmation", needsConfirmation);
        serializer.write("price", price);
        serializer.write("participant role", participantRole);
        serializer.write("self", self);
        serializer.write("allow select", allowSelect);
        serializer.write("user flag", userFlag);
        serializer.write("right to pay", rightToPay);
    }

    @Override
    public void update(Deserializer deserializer) {
        typeName = deserializer.readString("type");
        description = deserializer.readString("description");
        needsConfirmation = deserializer.readBoolean("confirmation", true);
        price = deserializer.readInt("price", 0);
        participantRole = deserializer.readString("participant role", "PARTICIPANT");
        self = deserializer.readBoolean("self", false);
        allowSelect = deserializer.readBoolean("allow select", true);
        userFlag = deserializer.readString("user flag");
        rightToPay = deserializer.readString("right to pay", "-");
    }
}
