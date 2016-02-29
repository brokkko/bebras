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
                return "?";
        }
        return "???";
    }

    public List<KioParameter> getParams(int level, String id) {
        switch (id) {
            case "rockgarden":
                switch (level) {
                    case 0: return kioParameters(
                            "r+i:площадок с четырьмя камнями",
                            "d+i:Различных площадок",
                            "s+i:размер камней"
                    );
                    case 1: return kioParameters(
                            "r+i:площадок с пятью камнями",
                            "d+i:Различных площадок",
                            "s+i:размер камней"
                    );
                    case 2: return kioParameters("p+i:пар невидимых~%", "v-d:равномерность");
                }
            case "mower":
                return kioParameters("m+i:скошено", "m-i:шагов");
            case "mars":
                switch (level) {
                    case 0: return kioParameters();
                    case 1: return kioParameters();
                    case 2: return kioParameters();
                }
        }
        return null;
    }
}
