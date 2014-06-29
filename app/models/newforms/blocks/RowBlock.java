package models.newforms.blocks;

import models.forms.RawForm;
import play.api.templates.Html;

/**
 * Created by ilya
 */
public class RowBlock implements InputBlock {

    private InputBlock[] blocks;
    private double[] widths;

    public RowBlock(InputBlock[] blocks, double[] widths) {
        this.blocks = blocks;
        this.widths = widths;
    }

    @Override
    public Html render(RawForm form, String field) {
        return null;
    }
}
