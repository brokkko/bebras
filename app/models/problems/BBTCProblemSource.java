package models.problems;

import au.com.bytecode.opencsv.CSVReader;
import models.checkers.Checker;
import models.store.MemoryStoredObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.01.13
 * Time: 14:19
 */
public class BBTCProblemSource implements ProblemsSource {

    private Map<String, Problem> problems = new HashMap<>();

    public BBTCProblemSource(String resource) throws IOException {
        InputStream resourceStream = BBTCProblemSource.class.getResourceAsStream(resource);
        InputStreamReader resourceReader = new InputStreamReader(resourceStream, "UTF8");

        CSVReader problemsReader = new CSVReader(resourceReader, ',', '"', 1);
        String [] nextLine;
        while ((nextLine = problemsReader.readNext()) != null) {
            String id = nextLine[1] + "." + nextLine[2] + "." + nextLine[3];
            String type = nextLine[4];
            String question = nextLine[5];
            int answersCount = Integer.parseInt(type);
            String[] answers = new String[answersCount];
            System.arraycopy(nextLine, 6, answers, 0, answersCount);
            String correctAnswer = nextLine[11];
            String solution = nextLine[12];

            Problem problem = new Problem();
            problem.put(Problem.CHECKER, "compare");
            problem.put(Problem.STATEMENT, question);
            problem.put("answers", Arrays.asList(answers));
            problem.put("correct", correctAnswer);
            problem.put(Problem.SOLUTION, solution);

            problems.put(id, problem);
        }
    }

    @Override
    public Problem get(String id) {
        return null;
    }
}
