package models.problems.bbtc;

import au.com.bytecode.opencsv.CSVReader;
import models.problems.InmemoryProblemSource;
import models.problems.ProblemSource;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.01.13
 * Time: 14:19
 */
public class BBTCProblemSource extends InmemoryProblemSource {

    public void update(File file) throws IOException {
        try (
                InputStream resourceStream = new FileInputStream(file);
                InputStreamReader resourceReader = new InputStreamReader(resourceStream, "UTF8")
        ) {
            dataReader(resourceReader);
        }
    }

    public void update(String resource) throws IOException {
        try (
                InputStream resourceStream = BBTCProblemSource.class.getResourceAsStream(resource);
                InputStreamReader resourceReader = new InputStreamReader(resourceStream, "UTF8")
        ) {
            dataReader(resourceReader);
        }
    }

    private void dataReader(InputStreamReader resourceReader) throws IOException {
        CSVReader problemsReader = new CSVReader(resourceReader, ',', '"', 1);
        String [] nextLine;
        while ((nextLine = problemsReader.readNext()) != null) {
            String type = nextLine[4];
            String question = nextLine[5];
            int answersCount = Integer.parseInt(type);
            String[] answers = new String[answersCount];
            System.arraycopy(nextLine, 6, answers, 0, answersCount);
            String correctAnswer = nextLine[11];

//            String number = nextLine[0];
//            String solution = nextLine[12];

            ProblemSource source = getSubsourceOrCreate(nextLine[1]).getSubsourceOrCreate(nextLine[2]);
            BBTCProblem problem = new BBTCProblem(question, answers, correctAnswer);

            source.put(nextLine[3], problem);
        }
    }
}
