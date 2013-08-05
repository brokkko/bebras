package models.newproblems.newproblemblock;

import models.newserialization.SerializableTreeSerializationType;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 31.07.13
 * Time: 19:04
 */
public class ProblemBlockSerializationType extends SerializableTreeSerializationType<ProblemBlock> {

    public ProblemBlockSerializationType() {
        registerClass("direct", DirectProblemBlock.class);
        registerClass("random", RandomProblemBlock.class);
    }
}
