package ru.ipo.daedal;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 1:30.
 */
public class DaedaelParserError extends RuntimeException {

    public DaedaelParserError() {
    }

    public DaedaelParserError(String message) {
        super(message);
    }

    public DaedaelParserError(String message, Throwable cause) {
        super(message, cause);
    }

    public DaedaelParserError(Throwable cause) {
        super(cause);
    }

    public DaedaelParserError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
