package plugins.bebrascard;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BebrasCardSlot {

    private List<CountryData> countries = new ArrayList<>();

    public void add(CountryData cd) {
        countries.add(cd);
    }

    public void shuffle(Random rnd) {
        Collections.shuffle(countries, rnd);
    }

    public List<CountryData> getCountries() {
        return Collections.unmodifiableList(countries);
    }

    public int size() {
        return countries.size();
    }

    public void asJavaScriptArray(int slotInd, ArrayNode array) {
        for (CountryData country : countries) {
            String folder = country.getFolderName();
//            String name = country.getName();
            CountryImage img = country.getImages().get(slotInd);

            ObjectNode o = array.addObject();
            o.put("f", folder);
            o.put("img", img.asJavaScriptObject());
        }
    }

    public void rotate() {
        CountryData first = countries.remove(0);
        countries.add(first);
    }
}
