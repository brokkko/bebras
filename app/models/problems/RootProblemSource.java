package models.problems;

import models.problems.bbtc.BBTCProblemSource;
import play.Logger;
import play.Play;
import play.cache.Cache;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.05.13
 * Time: 23:25
 */
public class RootProblemSource extends InmemoryProblemSource {

    public static final String ROOT_PROBLEM_SOURCE_CACHE_NAME = "root-problem-source";

    public static RootProblemSource getInstance() {
        // (X * Tt - Y)t * X
        // (T * Xt - Yt) * X
        // Xt * (X * Tt - Y)

        try {
            return Cache.getOrElse(ROOT_PROBLEM_SOURCE_CACHE_NAME, new Callable<RootProblemSource>() {
                @Override
                public RootProblemSource call() throws Exception {
                    RootProblemSource rps = new RootProblemSource();

                    loadAllProblems(rps);

                    return rps;
                }
            }, 0);
        } catch (Exception e) {
            Logger.error("Failed to load root problem source", e);
            return null;
        }
    }

    private static void loadAllProblems(RootProblemSource rps) throws IOException {
        BBTCProblemSource bbtc = new BBTCProblemSource();

        File problemsFolder = new File(Play.application().configuration().getString("resources.folder"));

        //load bbtc-*.csv problems
        File[] problemsFiles = problemsFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("bbtc-.*\\.csv");
            }
        });

        for (File problemsFile : problemsFiles)
            bbtc.update(problemsFile);

        rps.mount("bbtc", bbtc);
    }

    public static void clear() {
        Cache.remove(ROOT_PROBLEM_SOURCE_CACHE_NAME);
        getInstance();
    }
}
