package ru.ipo.daedal.pack;

import au.com.bytecode.opencsv.CSVReader;
import com.itextpdf.text.DocumentException;
import ru.ipo.daedal.DaedalParser;
import ru.ipo.daedal.commands.compiler.CompilerContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PackOfDiplomas {

    public static void main(String[] args) throws IOException, DocumentException {
        String source = args[0];
        String encoding = args[1];
        char separator = args[2].charAt(0);
        String outputFile = args[3];
        String descriptionFile = args[4];
        String baseFolder = args[5];

        //load csv
        try (CSVReader in = new CSVReader(new InputStreamReader(new FileInputStream(new File(source)), encoding), separator)) {
            String[] titles = in.readNext();
            List<String[]> allRows = in.readAll();
            createOutput(outputFile, descriptionFile, titles, allRows, baseFolder);
        }
    }

    private static void createOutput(String output, String descriptionFile, String[] titles, List<String[]> allRows, String baseFolder) throws IOException, DocumentException {
        Map<String, Integer> title2ind = IntStream
                .range(0, titles.length)
                .boxed()
                .collect(
                        Collectors.toMap(i -> titles[i].trim(), i -> i)
                );

        DaedalParser parser = new DaedalParser();

        String code = new String(Files.readAllBytes(Paths.get(descriptionFile)), StandardCharsets.UTF_8);

        try (PackDocument doc = new PackDocument(output, baseFolder)) {
            for (String[] row : allRows) {
                CompilerContext compilerContext = parser.parse(code, title -> {
                    Integer ind = title2ind.get(title);
                    if (ind == null)
                        return "???";
                    else
                        return row[ind];
                });

                doc.addPage(compilerContext);
            }
        }
    }

}
