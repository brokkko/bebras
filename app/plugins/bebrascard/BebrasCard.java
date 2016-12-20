package plugins.bebrascard;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import java.util.*;

public class BebrasCard {

    private static final int SLOT_SIZE = 5;
    private static int[] cSizes = {6, 5, 4, 3, 3, 2, 2, 1, 1, 1, 1, 1};

    private List<BebrasCardSlot> slots = new ArrayList<>(6);

    public BebrasCard(CountriesData data, Random rnd) {
        //6 * 5 = 6 + 5 + 4 + 3 + 3 + 3 + 2 + 1 + 1 + 1 + 1
//                a   b   c   d   e   f   g   h   i   j   k   l
//        int x = 6 + 5 + 4 + 3 + 3 + 2 + 2 + 1 + 1 + 1 + 1 + 1;

        int loops = 0;
        while (true) {
            loops++;
            if (loops > 20)
                throw new AssertionError("More than 20 loops while generating a card");
            List<CountryData> cds = new ArrayList<>(data.getCountries());
            Collections.shuffle(cds, rnd);

            for (int i = 0; i < 6; i++)
                slots.add(new BebrasCardSlot());

            for (int i = 0; i < cSizes.length; i++)
                fillSlots(cds.get(i), cSizes[i], rnd);

            for (BebrasCardSlot slot : slots)
                slot.shuffle(rnd);

            if (badTask(cds.get(0)))
                slots.clear();
            else
                break;
        }
    }

    public BebrasCardSlot getSlot(int ind) {
        return slots.get(ind);
    }

    private boolean badTask(CountryData correctCountry) {
        int cnt = 0;
        for (BebrasCardSlot slot : slots) {
            List<CountryData> countries = slot.getCountries();
            for (int i = 0; i < countries.size(); i++)
                if (countries.get(i) == correctCountry) {
                    cnt += i;
                    break;
                }
        }

        return cnt > 10;
    }

    private void fillSlots(CountryData cd, int count, Random rnd) {
        List<Integer> availableSlots = new ArrayList<>(6);
        for (int i = 0; i < slots.size(); i++)
            if (slots.get(i).size() < SLOT_SIZE)
                availableSlots.add(i);
        Collections.shuffle(availableSlots, rnd);
        for (int i = 0; i < count; i++)
            slots.get(availableSlots.get(i)).add(cd);
    }

    private List<CountryData> getAllCountries() {
        Set<CountryData> allCountries = new HashSet<>();
        for (BebrasCardSlot slot : slots)
            for (CountryData countryData : slot.getCountries())
                allCountries.add(countryData);
        return new ArrayList<>(allCountries);
    }

    public String asJavaScriptObject() {
        ObjectNode o = Json.newObject();
        o.put("base_path", controllers.routes.Resources.returnPluginFile(BebrasCardsPlugin.PLUGIN_NAME, "").url());

        ObjectNode allCountries = o.putObject("all_countries");
        for (CountryData countryData : getAllCountries())
            allCountries.put(countryData.getFolderName(), countryData.getName());

        ArrayNode slots = o.putArray("slots");
        for (int i = 0; i < this.slots.size(); i++) {
            BebrasCardSlot slot = this.slots.get(i);
            ArrayNode a = slots.addArray();
            slot.asJavaScriptArray(i, a);
        }

        return Json.stringify(o);
    }
}
