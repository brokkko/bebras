package models.newforms.blocks;

import models.forms.RawForm;
import play.twirl.api.Html;

/**
 * Created by ilya
 */
public class LabelBlock implements InputBlock {

    private String text;

    public LabelBlock(String text) {
        this.text = text;
    }

    @Override
    public Html render(RawForm form, String field) {
        return null;
    }

}
