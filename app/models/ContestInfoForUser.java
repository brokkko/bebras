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
    private Long randSeed;

    //TODO add per problem results

    private Map<String, Object> finalResults;

    public ContestInfoForUser() {
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    public Long getRandSeed() {
        return randSeed;
    }

    public void setRandSeed(Long randSeed) {
        this.randSeed = randSeed;
    }

    public Map<String, Object> getFinalResults() {
        return finalResults;
    }

    public ContestInfoForUser(Deserializer deserializer) {
        started = (Date) deserializer.getObject("sd");
        finished = (Date) deserializer.getObject("fd");
        randSeed = (Long) deserializer.getObject("seed");

        //deserialize results
        Deserializer results = deserializer.getDeserializer("res");
        if (results != null) {
            finalResults = new HashMap<>();
            for (String field : deserializer.fieldSet())
                finalResults.put(field, deserializer.getObject(field));
        }
    }

    @Override
    public void store(Serializer serializer) {
        serializer.write("sd", started);
        serializer.write("fd", finished);
        serializer.write("seed", randSeed);

        if (finalResults != null) {
            Serializer results = serializer.getSerializer("res");

            for (Map.Entry<String, Object> field2value : finalResults.entrySet())
                results.write(field2value.getKey(), field2value.getValue());
        }
    }
}
