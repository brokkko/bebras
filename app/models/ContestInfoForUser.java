package models;

import models.serialization.Deserializer;
import models.serialization.Serializable;
import models.serialization.Serializer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.05.13
 * Time: 10:32
 */
public class ContestInfoForUser implements Serializable {

    private Date started;
    private Date finished;

    //TODO add per problem results

    private Map<String, Object> finalResults;

    public ContestInfoForUser() {
    }

    public Date getStarted() {
        return started;
    }

    public Date getFinished() {
        return finished;
    }

    public Map<String, Object> getFinalResults() {
        return finalResults;
    }

    public ContestInfoForUser(Deserializer deserializer) {
        started = (Date) deserializer.getObject("sd");
        finished = (Date) deserializer.getObject("fd");

        Deserializer results = deserializer.getDeserializer("res");
        if (results == null)
            return;

        finalResults = new HashMap<>();
        for (String field : deserializer.fieldSet())
            finalResults.put(field, deserializer.getObject(field));
    }

    @Override
    public void store(Serializer serializer) {
        serializer.write("sd", started);
        serializer.write("fd", finished);

        if (finalResults == null)
            return;

        Serializer results = serializer.getSerializer("res");

        for (Map.Entry<String, Object> field2value : finalResults.entrySet())
            results.write(field2value.getKey(), field2value.getValue());
    }
}
