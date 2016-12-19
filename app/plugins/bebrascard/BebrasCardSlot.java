package plugins.bebrascard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BebrasCardSlot {

    private List<CountryData> countries = new ArrayList<>();

    public void add(CountryData cd) {
        countries.add(cd);
    }

    public void shuffle() {
        Collections.shuffle(countries);
    }

    public List<CountryData> getCountries() {
        return Collections.unmodifiableList(countries);
    }

    public int size() {
        return countries.size();
    }

    public void asJavaScriptArray(ArrayNode array) {
        for (CountryData country : countries)
            array.add(country.asJavaScriptObject());
    }
}
