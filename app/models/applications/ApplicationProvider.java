package models.applications;

import com.mongodb.DBCursor;
import models.data.ObjectsProvider;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 21.09.13
 * Time: 2:13
 */
public class ApplicationProvider implements ObjectsProvider<Application> {

    private DBCursor dbObjects = null;
    private List<Application> userApplications = Collections.emptyList();

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Application next() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
