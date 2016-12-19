package plugins.bebrascard;

import models.ServerConfiguration;
import play.Logger;
import play.cache.Cache;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountriesData {

    public static final Pattern FILE_IND_REGEXP = Pattern.compile(".*_(\\d{1,2})\\.jpg_resize\\.[a-z]+");

    public static CountriesData get() {
        try {
            return Cache.getOrElse("bebras-cards-country-info", () -> {
                try {
                    return new CountriesData(ServerConfiguration.getInstance().getPluginFile(BebrasCardsPlugin.PLUGIN_NAME, "Pictures"));
                } catch (IOException e) {
                    Logger.error("failed to read countries data", e);
                    return null;
                }
            }, 24 * 60 * 60);
        } catch (Exception e) {
            return null;
        }
    }

    private List<CountryData> countries = new ArrayList<>();

    public CountriesData(File pictures) throws IOException {
        File[] countriesFolder = pictures.listFiles();
        if (countriesFolder == null)
            throw new IOException("failed to list folder");

        for (File countryFolder : countriesFolder) {
            CountryData countryData = processCountryFolder(countryFolder);
            countries.add(countryData);
        }
    }

    private CountryData processCountryFolder(File countryFolder) throws IOException {
        File[] files = countryFolder.listFiles();
        if (files == null)
            throw new IOException("Failed to read country folder " + countryFolder);

        CountryDescriptionFile cdf = null;
        Map<Integer, File> ind2file = new HashMap<>();

        for (File file : files) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".txt"))
                cdf = new CountryDescriptionFile(file);
            else {
                Matcher m = FILE_IND_REGEXP.matcher(name);
                if (m.matches()) {
                    String indString = m.group(1);
                    int ind = Integer.parseInt(indString);
                    ind2file.put(ind, file);
                }
            }
        }

        if (cdf == null)
            throw new IOException("Failed to find txt file with info "+ countryFolder);

        List<String> items = cdf.getItems();
        if (items.size() == 0)
            throw new IOException("No info lines in country " + countryFolder);

        String countryName = items.get(0);

        List<CountryImage> images = new ArrayList<>();

        for (int i = 1; i < items.size(); i++) {
            String description = items.get(i);
            File imgFile = ind2file.get(i);
            if (imgFile == null)
                throw new IOException("No image " + i + " in " + countryFolder);

            images.add(new CountryImage(description, imgFile));
        }

        return new CountryData(countryName, countryFolder.getName(), images);
    }

    public CountriesData(List<CountryData> countries) {
        this.countries = countries;
    }

    public List<CountryData> getCountries() {
        return Collections.unmodifiableList(countries);
    }
}
