package ru.ipo.dces2.regions;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.util.*;

public class ByRegionsGenerator {

    public static void main(String[] args) throws IOException {
        new ByRegionsGenerator();
    }

    private Map<String, List<District>> regionsList = new LinkedHashMap<>();
    private Map<String, String> region2user = new HashMap<>();
    private Map<String, String> region2code = new HashMap<>();
    private PasswordGenerator pg = new PasswordGenerator();

    private String currentRegion = "";

    public ByRegionsGenerator() throws IOException {
//        updateRegionsListFile();
        readList();
        writeListForCode();
//        writeUsersList();
    }

    private void updateRegionsListFile() throws FileNotFoundException {
        File list = new File("by_regions_list.txt");
        File newList = new File("by_regions_list_up.txt");

        try (Scanner in = new Scanner(list); PrintStream out = new PrintStream(newList)) {
            int districtIndex = 1;
            while (in.hasNextLine()) {
                String rawLine = in.nextLine();
                String line = rawLine.trim();
                if (line.isEmpty())
                    out.println(rawLine);
                else if (!rawLine.startsWith(" ")) {
                    districtIndex = 1;
                    out.println(rawLine);
                } else
                    out.println(rawLine + " #" + districtIndex++);
            }
        }
    }

    private void readList() throws FileNotFoundException {
        File list = new File("by_regions_list_up.txt");

        try (Scanner in = new Scanner(list)) {
            while (in.hasNextLine())
                processLine(in.nextLine());
        }
    }

    private void processLine(String line) {
        String rawLine = line;
        line = line.trim();

        if (line.isEmpty())
            return;

        if (rawLine.startsWith(" ")) {
            int hashPos = line.indexOf('#');
            if (hashPos < 0)
                throw new IllegalArgumentException("wrong line format: " + line);
            String code = line.substring(hashPos + 1);
            line = line.substring(0, hashPos).trim();

            regionsList.get(currentRegion).add(new District(line, code));
        } else {
            // cut user
            int hashPos = line.indexOf('#');
            int userPos = line.indexOf('@');

            if (hashPos < 0 || userPos < 0)
                throw new IllegalArgumentException("wrong line format: " + line);

            String user = line.substring(userPos + 1);
            line = line.substring(0, userPos).trim();

            String code = line.substring(hashPos + 1);
            line = line.substring(0, hashPos).trim();

            currentRegion = line;

            region2user.put(currentRegion, user);
            region2code.put(currentRegion, code); //TODO Shift+Enter does not work inside rename

            regionsList.put(currentRegion, new ArrayList<>());
        }
    }

    private void writeListForCode() throws IOException {
        File f = new File("codes list.txt");
        try (PrintStream out = new PrintStream(f)) {
            regionsList.forEach((region, districts) -> {
                out.println("\"" + region + "\",");
                for (District district : districts)
                    out.println("\"" + district.getName() + "\",");
                if (districts.isEmpty())
                    out.println("\"" + region + "\",");
            });

            out.println("-----------------------");

            regionsList.forEach((region, districts) -> {
                out.println("\"~group\",");
                String code = region2code.get(region);
                for (District district : districts)
                    out.println("\"" + code + district.getCode() + "\",");
                if (districts.isEmpty())
                    out.println("\"" + code + "\",");
            });
        }
    }

    private void writeUsersList() throws IOException {
        File outF = new File("by_local_orgs.csv");

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outF), "windows-1251"), ';', '"')) {
            String[] titles = {"login", "region_name", "password", "_reg_by", "_p_reg", "_role"};
            List<String> titlesList = Arrays.asList(titles);

            int loginField = titlesList.indexOf("login");
            int regionField = titlesList.indexOf("region_name");
            int passwordField = titlesList.indexOf("password");
            int regByField = titlesList.indexOf("_reg_by");
            int partialRegField = titlesList.indexOf("_p_reg");
            int roleField = titlesList.indexOf("_role");


            writer.writeNext(titles);

            regionsList.forEach((region, districts) -> {
                String[] line = new String[titles.length];
                line[loginField] = region2user.get(region);
                line[regionField] = region;
                line[passwordField] = generatePassword();
                line[regByField] = "iposov";
                line[partialRegField] = "(boolean)true";
                line[roleField] = "REGION_ORG";
                writer.writeNext(line);

                String code = region2code.get(region);

                for (District district : districts) {
                    line = new String[titles.length];
                    line[loginField] = code + district.getCode();
                    line[regionField] = region + ", " + district.getName();
                    line[passwordField] = generatePassword();
                    line[regByField] = region2user.get(region);
                    line[partialRegField] = "(boolean)true";
                    line[roleField] = "LOCAL_ORG";
                    writer.writeNext(line);
                }

            });
        }

    }

    private String generatePassword() {
        return pg.generate(8) + pg.generateNumber(2);
    }
}