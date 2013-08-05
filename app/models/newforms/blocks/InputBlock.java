package models.newforms.blocks;

import models.forms.InputField;
import models.forms.RawForm;
import play.api.templates.Html;

import java.util.List;

/**
 * Created by ilya
 */
public interface InputBlock {

    Html render(RawForm form, String field);

}
