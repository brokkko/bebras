package models.migration;

import models.Contest;
import models.Event;
import models.newproblems.ProblemLink;
import models.newproblems.bbtc.BBTCProblemsLoader;
import models.newproblems.newproblemblock.ProblemBlockFactory;
import play.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 04.08.13
 * Time: 0:05
 */
public class Migrator3 extends Migrator {
    @Override
    public void migrate() {
        Event event = Event.getInstance("bbtc");

        // load all problems

        File file;

        try {
            file = new File(event.getEventDataFolder(), "BBTC test A.csv");
            new BBTCProblemsLoader().load(file, new ProblemLink(event.getId()).child("express"));

            file = new File(event.getEventDataFolder(), "BBTC test B.csv");
            new BBTCProblemsLoader().load(file, new ProblemLink(event.getId()).child("hard"));

            file = new File(event.getEventDataFolder(), "BBTC test C.csv");
            new BBTCProblemsLoader().load(file, new ProblemLink(event.getId()).child("explore"));
        } catch (IOException e) {
            Logger.error("Failed to upload a file", e);
        }

        // add all blocks

        Contest contest;

        contest = event.getContestById("express");
        for (int i = 1; i <= 10; i++)
            contest.getProblemBlocks().add(ProblemBlockFactory.getBlock(contest, "5 first random <- /bbtc/express/"  + i));

        contest = event.getContestById("hard");
        for (int i = 1; i <= 10; i++)
            contest.getProblemBlocks().add(ProblemBlockFactory.getBlock(contest, "5 first random <- /bbtc/hard/"  + i));

        contest = event.getContestById("explore");
        for (int i = 1; i <= 5; i++)
            contest.getProblemBlocks().add(ProblemBlockFactory.getBlock(contest, "2 first random <- /bbtc/explore/"  + i));

        event.store();
    }
}
