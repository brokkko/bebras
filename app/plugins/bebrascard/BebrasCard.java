package plugins.bebrascard;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BebrasCard {

    private static final int SLOT_SIZE = 5;
    private static int[] cSizes = {6, 5, 4, 3, 3, 2, 2, 1, 1, 1, 1, 1};

    private List<BebrasCardSlot> slots = new ArrayList<>(6);

    public BebrasCard(CountriesData data, Random rnd) {
        //6 * 5 = 6 + 5 + 4 + 3 + 3 + 3 + 2 + 1 + 1 + 1 + 1
//                a   b   c   d   e   f   g   h   i   j   k   l
//        int x = 6 + 5 + 4 + 3 + 3 + 2 + 2 + 1 + 1 + 1 + 1 + 1;

        List<CountryData> cds = new ArrayList<>(data.getCountries());
        Collections.shuffle(cds, rnd);

        for (int i = 0; i < 6; i++)
            slots.add(new BebrasCardSlot());

        for (int i = 0; i < cSizes.length; i++)
            fillSlots(cds.get(i), cSizes[i], rnd);
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

    public String asJavaScriptObject() {
        ObjectNode o = Json.newObject();
        o.put("base_path", controllers.routes.Resources.returnPluginFile(BebrasCardsPlugin.PLUGIN_NAME, "").url());

        ArrayNode slots = o.putArray("slots");
        for (BebrasCardSlot slot : this.slots)
            slot.asJavaScriptArray(slots);

        return Json.stringify(o);
    }
}
