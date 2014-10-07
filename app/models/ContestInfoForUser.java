package models;

import models.newserialization.Deserializer;
import models.newserialization.Serializable;
import models.newserialization.Serializer;
import models.results.Info;
import org.bson.types.ObjectId;
import play.Logger;

import java.util.Date;
import java.util.List;

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

    private final Contest contest;

    private Info finalResults;

    private List<ObjectId> problems;

    public ContestInfoForUser(Contest contest) {
        this.contest = contest;
    }

    public ContestInfoForUser(Contest contest, Deserializer deserializer) {
        this.contest = contest;

        started = deserializer.readDate("sd");
        finished = deserializer.readDate("fd");
        randSeed = deserializer.readLong("seed");

        finalResults = contest.getResultsInfoPattern().read(deserializer, "res");
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

    public Info getFinalResults() {
        return finalResults;
    }

    public List<ObjectId> getProblems() {
        return problems;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("sd", started);
        serializer.write("fd", finished);
        if (randSeed != null)
            serializer.write("seed", randSeed);

        contest.getResultsInfoPattern().write(serializer, "res", finalResults);
    }

    public void setFinalResults(Info finalResults) {
        this.finalResults = finalResults;
    }
}