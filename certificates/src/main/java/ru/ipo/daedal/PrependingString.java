package ru.ipo.daedal;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Project: dces2
 * Created by ilya on 14.08.16, 12:14.
 */
public class PrependingString {

    private StringBuilder text = new StringBuilder();
    private int nextIndex = 0;

    public void prepend(String text) {
        prepend(text, 0);
    }

    public void prepend(String text, int remove) {
        nextIndex -= remove;
        this.text.replace(nextIndex, nextIndex + remove, text);
    }

    public int read() {
        if (nextIndex >= text.length())
            return -1;
        return text.charAt(nextIndex++);
    }

    public int peek() {
        if (nextIndex >= text.length())
            return -1;
        return text.charAt(nextIndex);
    }

    public void reset() {
        nextIndex = 0;
    }

}