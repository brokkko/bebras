package models.forms.validators;

import au.com.bytecode.opencsv.CSVReader;
import play.Logger;
import play.cache.Cache;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 05.01.13
 * Time: 22:58
 */
public class KenguruSchoolCodeValidator extends Validator<String> {

    public KenguruSchoolCodeValidator(Map<String, Object> validationParameters) {
        super(validationParameters);
        defaultMessage = "error.msg.kenguru_school_code";
    }

    @Override
    public String validate(String code) {
        if (getSchoolCodes().contains(code))
            return null;
        else
            return message();
    }

    public Set<String> getSchoolCodes() {
        try {
            return Cache.getOrElse("kenguru-school-code", new Callable<Set<String>>() {
                @Override
                public Set<String> call() throws Exception {
                    return readSchoolCodes();
                }
            }, 0);
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    private Set<String> readSchoolCodes() throws IOException {
        CSVReader in = new CSVReader(
                new InputStreamReader(
                        KenguruSchoolCodeValidator.class.getResourceAsStream("/SCHOOLS.txt"), "windows-1251"
                ),
                ';', '"', 1
        );

        Set<String> result = new HashSet<>();

        String[] line;
        while ((line = in.readNext()) != null) {
            if (line.length < 4)
                Logger.debug("Short line in SCHOOLS.csv: " + Arrays.toString(line));
            else
                result.add(line[3]);
        }

        return result;
    }
}
