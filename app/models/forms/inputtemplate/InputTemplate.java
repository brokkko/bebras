package models.forms.inputtemplate;

import models.forms.RawForm;
import models.newforms.blocks.InputBlock;
import models.newserialization.Deserializer;
import models.newserialization.SerializableUpdatable;
import models.newserialization.SerializationType;
import models.newserialization.Serializer;
import play.api.templates.Html;
import play.i18n.Messages;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.03.13
 * Time: 23:42
 */
public abstract class InputTemplate<T> implements SerializableUpdatable { //Object -> String fields, String fields -> Object

    private InputBlock block; //TODO implement description of view by means of blocks

    protected String title;

    public String getTitle() {
        return title;
    }

    public abstract Html render(RawForm form, String field);/* {
        return block.render(form, field);
    }*/

    public abstract void write(String field, T value, RawForm rawForm);

    public abstract T read(String field, RawForm form);

    public abstract SerializationType<T> getType();

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("title", title);
    }

    @Override
    public void update(Deserializer deserializer) {
        title = deserializer.readString("title", "-");
    }

    public String[] getUserInputFields() {
        return null;
    }

}