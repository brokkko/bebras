package models.checkers;

import models.problems.Problem;
import models.store.StoredObject;
import play.Logger;
import play.cache.Cache;

import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.01.13
 * Time: 12:41
 */
public abstract class Checker {

    public abstract void check(StoredObject submission, Problem problem, StoredObject resultsReceiver);

    public static Checker getInstance(final String type) {

        try {
            return Cache.getOrElse("Checker-" + type, new Callable<Checker>() {
                @Override
                public Checker call() throws Exception {
                    return getCheckerByType(type);
                }
            }, 0);
        } catch (Exception e) {
            Logger.error("Failed to get checker of type " + type, e);
            throw new IllegalArgumentException("Failed to get checker");
        }
    }

    private static Checker getCheckerByType(String type) {
        switch (type) {
            case "compare":
                return new ComparatorChecker();
        }
        throw new IllegalArgumentException("No checker of type " + type);
    }

}
