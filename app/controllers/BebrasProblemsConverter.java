package controllers;

import controllers.actions.DcesController;
import models.ServerConfiguration;
import models.utils.Utils;
import models.newproblems.ProblemInfo;
import models.newproblems.ProblemLink;
import models.newproblems.bebras.BebrasProblem;
import org.bson.types.ObjectId;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 28.09.13
 * Time: 23:24
 */
@DcesController
public class BebrasProblemsConverter extends Controller {

    public static Result convertProblems() throws Exception {
        createProblems(new ProblemLink("bebras12"));

        return ok("conversion finished");
    }

    private static void createProblems(ProblemLink folderLink) throws Exception {
        File dataFolder = ServerConfiguration.getInstance().getResourcesFolder().getParentFile();
        File problemsFolder = new File(dataFolder, "_problems");
        problemsFolder = new File(problemsFolder, "2012");

        File[] problemsFolders = problemsFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().matches("(\\d4-)?(\\w\\w)-(\\d\\d)");
            }
        });

        for (File folder : problemsFolders) {
            BebrasProblem problem = createProblem(folder);

            if (problem == null)
                continue;

            ObjectId pid = new ObjectId();
            ProblemInfo pi = new ProblemInfo(pid, problem);
            pi.store();

            ProblemLink problemLink = folderLink.child(folder.getName());
            problemLink.setProblemId(pid);
        }
    }

    private static BebrasProblem createProblem(File problemFolder) throws Exception {
        try {
            File problemFile = new File(problemFolder, "template.html");

            String problemTemplate = Utils.inputStreamToString(new FileInputStream(problemFile));

            String title = getDivContentsByClass(problemTemplate, "task-title");
            String country = getDivContentsByClass(problemTemplate, "country");
            String statement = getDivContentsByClass(problemTemplate, "task-statement");
            String question = getDivContentsByClass(problemTemplate, "task-question");

            String[] answers;
            if (divAbsent(problemTemplate, "task-answers"))
                answers = null;
            else
                answers = new String[]{
                                              uploadImages(getDivContentsByClass(problemTemplate, "task-answer", 1), problemFolder),
                                              uploadImages(getDivContentsByClass(problemTemplate, "task-answer", 2), problemFolder),
                                              uploadImages(getDivContentsByClass(problemTemplate, "task-answer", 3), problemFolder),
                                              uploadImages(getDivContentsByClass(problemTemplate, "task-answer", 4), problemFolder)
                };
            int correctAnswer = Integer.parseInt(getDivContentsByClass(problemTemplate, "correct-answer")) - 1;
            String explanation = getDivContentsByClass(problemTemplate, "task-explanation");

            String answersLayout;
            if (answers != null)
                answersLayout = getAnswersLayout(problemTemplate);
            else
                answersLayout = null;

            if (answers == null)
                return null;

            return new BebrasProblem(
                                            title,
                                            country,
                                            uploadImages(statement, problemFolder),
                                            uploadImages(question, problemFolder),
                                            Arrays.asList(answers),
                                            answersLayout.charAt(0) - '0',
                                            correctAnswer,
                                            uploadImages(explanation, problemFolder),
                                            ""
            );

        } catch (Exception e) {
            Logger.error("Failed to convert problem", e);
            throw new Exception("Failed to load problem " + problemFolder);
        }
    }

    private static boolean divAbsent(String text, String clazz) {
        Pattern p = Pattern.compile("<div[^>]+class *= *[\"'][^\"']*" + clazz + "[ \"']", Pattern.CASE_INSENSITIVE);
        return !p.matcher(text).find();
    }

    private static String getAnswersLayout(String problemTemplate) throws Exception {
        Matcher tasksAnswersMatcher = getDivExtractor("task-answers").matcher(problemTemplate);
        if (!tasksAnswersMatcher.find())
            throw new Exception("Failed to find task answers");
        String div = tasksAnswersMatcher.group(1);

        Matcher answersLayoutMatcher = Pattern.compile("answers-layout-([^ \"]+)[ \"]").matcher(div);
        if (!answersLayoutMatcher.find())
            throw new Exception("Failed to find task answers layout");

        return answersLayoutMatcher.group(1);
    }

    private static String getDivContentsByClass(String html, String clazz) throws Exception {
        return getDivContentsByClass(html, clazz, 1);
    }

    //TODO reluctant .*? or may be posessive .*+
    private static Pattern getDivExtractor(String clazz) {
        return Pattern.compile("(<div[^>]*class *= *[\"'][^\"']*" + clazz + "[ \"'][^>]*>)(.*?)</div", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }

    private static String getDivContentsByClass(String html, String clazz, int ind) throws Exception {
        Pattern divExtractor = getDivExtractor(clazz);
        Matcher matcher = divExtractor.matcher(html);
        for (int i = 0; i < ind; i++)
            if (!matcher.find())
                throw new Exception("Failed to find div with class " + clazz + " and index " + ind);

        String divContents = matcher.group(2);

        //remove comments
        Matcher commentsMatcher = Pattern.compile("<!--.*?-->").matcher(divContents);
        divContents = commentsMatcher.replaceAll("");

        return divContents.trim();
    }

    private static String uploadImages(String html, File problemFolder) throws IOException {
        Pattern imgPattern = Pattern.compile("(<img[^>]*src *= *[\"'])([^\"']*)([\"'])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = imgPattern.matcher(html);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String imgName = matcher.group(2);
            String extension = Utils.getExtension(imgName);
            File resourceFile = ServerConfiguration.getInstance().getNewResourceFile(extension);

            String replacement = matcher.group(1) + routes.Application.returnFile(resourceFile.getName()) + matcher.group(3);

            matcher.appendReplacement(result, replacement);

            Files.copy(
                              Paths.get(problemFolder.getAbsolutePath() + '/' + imgName),
                              Paths.get(resourceFile.getAbsolutePath())
            );
        }

        matcher.appendTail(result);

        return result.toString();
    }
}
