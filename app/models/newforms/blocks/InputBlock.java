package models.newforms.blocks;

import models.forms.RawForm;
import play.api.templates.Html;

/**
 * Created by ilya
 */
public interface InputBlock {

    Html render(RawForm form, String field);

}
