package models.utils;

import controllers.worker.Worker;
import play.Logger;

public class SimpleProfiler {

    private long start = System.currentTimeMillis();
    private long previous = start;
    private Worker worker = null;

    public SimpleProfiler() {
    }

    public SimpleProfiler(Worker worker) {
        this.worker = worker;
    }

    public void logToLogger(String message) {
        Logger.info(log(message));
    }

    public void logToWorker(String message) {
        worker.logInfo(log(message));
    }

    public String log(String message) {
        long time = System.currentTimeMillis();
        String result  = message + " [passed: " + (time - previous) + " total: " + (time - start) + "]";
        previous = time;
        return result;
    }

}
