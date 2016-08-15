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
public class PrependingReader extends Reader {

    private Reader currentReader;
    private List<Reader> prependedReaders = new ArrayList<>(); //next reader to go is last in this list

    public void prepend(String text) {
        prepend(new StringReader(text));
    }

    public void prepend(Reader reader) {
        if (currentReader != null)
            prependedReaders.add(currentReader);
        currentReader = reader;
    }

    private void nextReader() throws IOException {
        if (prependedReaders.isEmpty()) {
            if (currentReader != null)
                currentReader.close();
            currentReader = null;
        } else
            currentReader = prependedReaders.get(prependedReaders.size() - 1);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int totalRead = 0;
        while (true) {
            if (currentReader == null)
                return -1;

            int read = currentReader.read(cbuf, off, len);

            if (read > 0) {
                totalRead += read;

                if (len > read) {
                    len -= read;
                    off += read;
                } else
                    return totalRead;
            }

            nextReader();
        }
    }

    @Override
    public int read() throws IOException {
        while (true) {
            if (currentReader == null)
                return -1;

            int res = currentReader.read();
            if (res != -1)
                return res;

            nextReader();
        }
    }

    @Override
    public void close() throws IOException {
        if (currentReader != null)
            currentReader.close();
        for (int i = prependedReaders.size() - 1; i >= 0; i--)
            prependedReaders.get(i).close();
    }
}