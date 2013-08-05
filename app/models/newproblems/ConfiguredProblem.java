package models.newproblems;

import models.results.Info;
import org.bson.types.ObjectId;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 27.07.13
 * Time: 13:55
 */
public class ConfiguredProblem {

    private ObjectId problemId;
    private Problem problem;
    private String name;
    private Info settings;

    public ConfiguredProblem(ObjectId problemId, Problem problem, String name, Info settings) {
        this.problemId = problemId;
        this.problem = problem;
        this.name = name;
        this.settings = settings;
    }

    public ObjectId getProblemId() {
        return problemId;
    }

    public Problem getProblem() {
        return problem;
    }

    public Info getSettings() {
        return settings;
    }

    public String getName() {
        return name;
    }
}
