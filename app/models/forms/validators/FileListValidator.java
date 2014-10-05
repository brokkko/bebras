package models.forms.validators;

import au.com.bytecode.opencsv.CSVReader;
import models.Event;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import play.Logger;
import play.cache.Cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 05.01.13
 * Time: 22:58
 */
public class FileListValidator extends Validator<String> {

    private String fileName;
    private String cacheKey;
    private long fileLastModified = 0;

    private String encoding;

    public FileListValidator() {
        defaultMessage = "error.msg.list_validator.not_in_list";
    }

    @Override
    public Validator.ValidationResult validate(String code) {
        if (getSchoolCodes().contains(code))
            return ok();
        else
            return message();
    }

    public Set<String> getSchoolCodes() {
        final File file = new File(Event.current().getEventDataFolder(), fileName);

        if (file.lastModified() != fileLastModified)
            Cache.remove(cacheKey);

        try {
            return Cache.getOrElse(cacheKey, new Callable<Set<String>>() {
                @Override
                public Set<String> call() throws Exception {
                    fileLastModified = file.lastModified();
                    return readVariants();
                }
            }, 0);
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    private Set<String> readVariants() throws IOException {
        File file = getFile();

        CSVReader in = new CSVReader(
                new InputStreamReader(
                        new FileInputStream(file), encoding
                ),
                ';', '"', 1
        );

        Set<String> result = new HashSet<>();

        String[] line;
        while ((line = in.readNext()) != null) {
            if (line.length < 1)
                Logger.debug("Short line in " + file + ": " + Arrays.toString(line));
            else
                result.add(line[0]);
        }

        return result;
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        encoding = deserializer.readString("encoding", "UTF-8");
        String fileName = deserializer.readString("file");

        if (fileName == null)
            throw new IllegalArgumentException("Need to specify a file for a file list validator");

        this.fileName = fileName;
        cacheKey = "file-list-validator-" + fileName + "-" + hashCode(); //TODO get rid of collisions
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("encoding", encoding);
        serializer.write("file", fileName);
    }

    public File getFile() {
        return new File(Event.current().getEventDataFolder(), fileName);
    }

    //TODO get rid of these methods

    public static File getKenguruSchoolsFile() {
        return new File(Event.current().getEventDataFolder(), "school-codes.csv");
    }
}
