package ru.ipo.daedal;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 1:30.
 */
public class DaedalParserError extends RuntimeException {

    public DaedalParserError() {
    }

    public DaedalParserError(String message) {
        super(message);
    }

    public DaedalParserError(String message, Throwable cause) {
        super(message, cause);
    }

    public DaedalParserError(Throwable cause) {
        super(cause);
    }

    public DaedalParserError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
