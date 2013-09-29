//package models;
//
//import controllers.routes;
//import org.codehaus.jackson.node.ArrayNode;
//import org.codehaus.jackson.node.JsonNodeFactory;
//import org.codehaus.jackson.node.ObjectNode;
//
//import java.io.*;
//import java.util.Random;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * Created with IntelliJ IDEA.
// * User: ilya
// * Date: 23.10.12
// * Time: 16:55
// */
//public class TempProblem {
//
//    private String id;
//    private String title;
//    private String country;
//    private String statement;
//    private String question;
//    private String[] answers;
//    private String answersLayout;
//    private int correctAnswer;
//    private String explanation;
//
//    private static final int[] constraints = {1, 1, 1, 1, 2};
//
//    public Problem(String id) {
//        this.id = id;
//
//        try {
//            String problemTemplate = loadStringContents(Problem.class.getResourceAsStream("/public/problems/" + id + "/template.html"));
//
//            title = getDivContentsByClass(problemTemplate, "task-title");
//            country = getDivContentsByClass(problemTemplate, "country");
//            statement = getDivContentsByClass(problemTemplate, "task-statement");
//            question = getDivContentsByClass(problemTemplate, "task-question");
//
//            if (divAbsent(problemTemplate, "task-answers"))
//                answers = null;
//            else
//                answers = new String[]{
//                                              getDivContentsByClass(problemTemplate, "task-answer", 1),
//                                              getDivContentsByClass(problemTemplate, "task-answer", 2),
//                                              getDivContentsByClass(problemTemplate, "task-answer", 3),
//                                              getDivContentsByClass(problemTemplate, "task-answer", 4)
//                };
//            correctAnswer = Integer.parseInt(getDivContentsByClass(problemTemplate, "correct-answer"));
//            explanation = getDivContentsByClass(problemTemplate, "task-explanation");
//
//            if (answers != null)
//                answersLayout = getAnswersLayout(problemTemplate);
//            else
//                answersLayout = null;
//
//        } catch (Exception e) {
//            initializeErrorProblem(id, e);
//        }
//    }
//
//    private boolean divAbsent(String text, String clazz) {
//        Pattern p = Pattern.compile("<div[^>]+class *= *[\"'][^\"']*" + clazz + "[ \"']", Pattern.CASE_INSENSITIVE);
//        return ! p.matcher(text).find();
//    }
//
//    private String getAnswersLayout(String problemTemplate) throws Exception {
//        Matcher tasksAnswersMatcher = getDivExtractor("task-answers").matcher(problemTemplate);
//        if (! tasksAnswersMatcher.find())
//            throw new Exception("Failed to find task answers");
//        String div = tasksAnswersMatcher.group(1);
//
//        Matcher answersLayoutMatcher = Pattern.compile("answers-layout-([^ \"]+)[ \"]").matcher(div);
//        if (! answersLayoutMatcher.find())
//            throw new Exception("Failed to find task answers layout");
//
//        return answersLayoutMatcher.group(1);
//    }
//
//    //TODO reluctant .*? or may be posessive .*+
//    private Pattern getDivExtractor(String clazz) {
//        return Pattern.compile("(<div[^>]*class *= *[\"'][^\"']*" + clazz + "[ \"'][^>]*>)(.*?)</div", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
//    }
//
//    private String getDivContentsByClass(String html, String clazz) throws Exception {
//        return getDivContentsByClass(html, clazz, 1);
//    }
//
//    private String getDivContentsByClass(String html, String clazz, int ind) throws Exception {
//        Pattern divExtractor = getDivExtractor(clazz);
//        Matcher matcher = divExtractor.matcher(html);
//        for (int i = 0; i < ind; i++)
//            if (! matcher.find())
//                throw new Exception("Failed to find div with class " + clazz + " and index " + ind);
//
//        String divContents = matcher.group(2);
//
//        //remove comments
//        Matcher commentsMatcher = Pattern.compile("<!--.*?-->").matcher(divContents);
//        divContents = commentsMatcher.replaceAll("");
//
//        return divContents.trim();
//    }
//
//    private void initializeErrorProblem(String id, Exception e) {
//        title = "Error loading problem " + id;
//        country = "ru";
//        statement = e.getMessage();
//        question = e.getClass().getSimpleName();
//        answers = new String[]{title, title, title, title};
//        answersLayout = "5x1";
//        correctAnswer = 1;
//        explanation = title;
//    }
//
//    private String loadStringContents(InputStream in) throws Exception {
//        InputStreamReader reader = new InputStreamReader(in, "UTF8");
//        BufferedReader bufferedReader = new BufferedReader(reader);
//
//        CharArrayWriter writer = new CharArrayWriter();
//        char[] buffer = new char[4096];
//        int read;
//
//        while ((read = bufferedReader.read(buffer)) > 0)
//            writer.write(buffer, 0, read);
//
//        return writer.toString();
//    }
//}
