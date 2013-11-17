package models.newproblems.bbtc;

import au.com.bytecode.opencsv.CSVReader;
import models.newproblems.ProblemInfo;
import models.newproblems.ProblemLink;
import org.bson.types.ObjectId;
import play.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 25.01.13
 * Time: 14:19
 */
public class BBTCProblemsLoader {

    public void load(File file, ProblemLink folder) throws IOException {
        try (
                InputStream resourceStream = new FileInputStream(file);
                InputStreamReader resourceReader = new InputStreamReader(resourceStream, "windows-1251")
        ) {
            load(resourceReader, folder);
        }
    }

    public void load(Reader in, ProblemLink folder) throws IOException {
        CSVReader problemsReader = new CSVReader(in, ';', '"', 1);
        String [] nextLine;
        while ((nextLine = problemsReader.readNext()) != null) {
            if (lineIsEmpty(nextLine))
                continue;

            String type = nextLine[5].trim();
            String question = nextLine[6].trim();
            int answersCount = Integer.parseInt(type);
            List<String> answers = new ArrayList<>(answersCount);
            answers.addAll(Arrays.asList(nextLine).subList(7, 7 + answersCount)); //TODO report: "manual array to collection copy" fails if copy not from array beginning
            String correctAnswer = nextLine[12].trim();

            int scores = Integer.parseInt(nextLine[13].trim());
            int penalty = Integer.parseInt(nextLine[14].trim());

            ProblemLink problemLink = folder.child(nextLine[2].trim()).child(nextLine[1].trim());
            BBTCProblem problem = new BBTCProblem(question, answers, correctAnswer, scores, penalty);

            if (problemLink.exists()) {
                ObjectId pid = problemLink.getProblemId();
                ProblemInfo pi = new ProblemInfo(pid, problem);
                pi.store();
            } else {
                ProblemInfo pi = ProblemInfo.put(problem);
                ObjectId pid = pi.getId();
                problemLink.setProblemId(pid);
            }
        }
    }

    private boolean lineIsEmpty(String[] nextLine) {
        for (String s : nextLine)
            if (s != null && !s.isEmpty())
                return false;

        return true;
    }
}
