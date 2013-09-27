package views.widgets;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 27.09.13
 * Time: 22:43
 */
public class WymEditorWidget implements Widget {

    private static final ResourceLink jsEditor = new ResourceLink("wymeditor/jquery.wymeditor.min.js");
    private static final ResourceLink jsUploadPlugin = new ResourceLink("wymeditor/plugins/image_upload/jquery.wymeditor.image_upload.js");

    @Override
    public List<ResourceLink> links() {
        return Arrays.asList(jsEditor, jsUploadPlugin);
    }

    public static WymEditorWidget get() {
        return new WymEditorWidget();
    }
}
