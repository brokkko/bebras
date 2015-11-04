package views.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 16.08.13
 * Time: 17:45
 */
public class ListWidget implements Widget {

    private final List<ResourceLink> links;

    public ListWidget(List<ResourceLink> links) {
        this.links = links;
    }

    public ListWidget(ResourceLink... links) {
        this.links = Arrays.asList(links);
    }

    @Override
    public List<ResourceLink> links() {
        return links;
    }

    public ListWidget add(ResourceLink link) {
        List<ResourceLink> newLinks = new ArrayList<>(links);
        newLinks.add(link);
        return new ListWidget(newLinks);
    }
}
