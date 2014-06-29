package models.newforms.blocks;

import models.forms.RawForm;
import play.api.templates.Html;

import java.util.Collections;
import java.util.List;

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
