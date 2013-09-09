package models.newproblems;

import models.newproblems.bbtc.BBTCProblem;
import models.newproblems.bebras.BebrasProblem;
import models.newserialization.SerializableTreeSerializationType;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 27.07.13
 * Time: 23:13
 */
public class ProblemSerializationType extends SerializableTreeSerializationType<Problem> {

    public ProblemSerializationType() {
        registerClass("bbtc", BBTCProblem.class);
        registerClass("bebras", BebrasProblem.class);
    }
}
