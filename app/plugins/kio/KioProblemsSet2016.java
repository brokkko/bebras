package plugins.kio;

import java.util.Arrays;
import java.util.List;

public class KioProblemsSet2016 extends KioProblemSet {

    public List<String> getProblemIds(int level) {
        return Arrays.asList("rockgarden", "mower", "mars");
    }

    public String getProblemName(int level, String id) {
        switch (id) {
            case "rockgarden":
                return "Сад камней";
            case "mower":
                return "Стая коси-роботов";
            case "mars":
                if (level < 2)
                    return "Солнечная система";
                else
                    return "Полет на марс";
        }
        return "???";
    }

    public List<KioParameter> getParams(int level, String id) {
        switch (id) {
            case "rockgarden":
                switch (level) {
                    case 0:
                    case 1:
                        return kioParameters(
                                "r+i:площадок с четырьмя (пятью) камнями",
                                "d+i:различных площадок",
                                "s+i:размер камней"
                        );
                    case 2:
                        return kioParameters("p+i:пар невидимых~%", "v-d:равномерность");
                }
            case "mower":
                return kioParameters("m+i:скошено", "s-i:шагов");
            case "mars":
                switch (level) {
                    case 0:
                    case 1:
                        return kioParameters("o+i:на обрите", "s-i:ошибка положения");
                    case 2:
                        return kioParameters("md-d:982:расстояние~тыс.км", "ms-d:3450:скорость~км/ч", "f-d:топливо");
                }
        }
        return null;
    }
}
