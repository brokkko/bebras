package models.newforms.blocks;

import models.forms.RawForm;
import play.api.templates.Html;

/**
 * Created by ilya
 */
public class InputFieldBlock implements InputBlock {

    private String name;
    private String type;
    private String placeholder;

    public InputFieldBlock(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public Html render(RawForm form, String field) {
        return null;
    }

}
