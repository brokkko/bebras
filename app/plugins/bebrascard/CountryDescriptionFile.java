package plugins.bebrascard;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CountryDescriptionFile {

    private final List<String> items;

    public CountryDescriptionFile(File file) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "windows-1251"))) {
            List<String> items = new ArrayList<>();
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    if (text.length() > 0) {
                        items.add(text.toString());
                        text = new StringBuilder();
                    }
                } else {
                    if (text.length() > 0)
                        text.append(' ');
                    text.append(line);
                }
            }

            if (text.length() > 0)
                items.add(text.toString());

            this.items = items;
        }
    }

    public List<String> getItems() {
        return Collections.unmodifiableList(items);
    }
}
