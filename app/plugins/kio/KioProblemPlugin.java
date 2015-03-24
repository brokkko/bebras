package plugins.kio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Contest;
import models.Event;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.newproblems.Problem;
import models.newproblems.kio.KioProblem;
import models.newserialization.BasicSerializationType;
import models.newserialization.Deserializer;
import models.newserialization.Serializer;
import models.results.Info;
import org.bson.types.ObjectId;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import plugins.Plugin;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.Map;

//TODO get rid of the plugin. Problems should be able to send solutions by themselves
public class KioProblemPlugin extends Plugin {

    private int year = 2014;
    private KioProblemSet problemSet = KioProblemSet.getInstance(year);

    @Override
    public void initPage() {
    }

    @Override
    public void initEvent(Event event) {
        if (year == 2014) {
            registerUserFields2014(event);
            return;
        }

        for (String right : new String[]{"participant", "self participant"})
            for (int level = 0; level <= 2; level++) {
                String prefix = "kio_" + level + "_";
                event.registerExtraUserField(right, "kio_level", new BasicSerializationType<>(String.class), "Уровень");
                event.registerExtraUserField(right, prefix + "scores", new BasicSerializationType<>(String.class), "Баллы");
                event.registerExtraUserField(right, prefix + "rank", new BasicSerializationType<>(String.class), "Место");

                for (String pid : problemSet.getProblemIds(level)) {
                    for (KioParameter parameter : problemSet.getParams(level, pid))
                        event.registerExtraUserField(
                                right,
                                prefix + pid + "_" + parameter.getId(),
                                new BasicSerializationType<>(String.class),
                                parameter.getName() + " (" + problemSet.getProblemName(level, pid) + ")"
                        );
                    event.registerExtraUserField(
                            right,
                            prefix + "scores_" + pid,
                            new BasicSerializationType<>(String.class),
                            "Баллы (" + problemSet.getProblemName(level, pid) + ")"
                    );
                    event.registerExtraUserField(
                            right,
                            prefix + "rank_" + pid,
                            new BasicSerializationType<>(String.class),
                            "Место (" + problemSet.getProblemName(level, pid) + ")"
                    );
                }
            }
    }

    private void registerUserFields2014(Event event) {
        //TODO move this to KioProblemSet
        for (String right : new String[]{"participant", "self participant"})
            for (String prefix : new String[]{"kio_0_", "kio_1_", "kio_2_"}) {
                event.registerExtraUserField(right, prefix + "total_number_of_difference_graphs", new BasicSerializationType<>(String.class), "Различных созвездий");
                event.registerExtraUserField(right, prefix + "total_number_of_right_graphs", new BasicSerializationType<>(String.class), "Всего созвездий");
                event.registerExtraUserField(right, prefix + "sum_of_lines", new BasicSerializationType<>(String.class), "Длина линий");

                event.registerExtraUserField(right, prefix + "scores_stars", new BasicSerializationType<>(String.class), "Баллы (Звезды)");
                event.registerExtraUserField(right, prefix + "rank_stars", new BasicSerializationType<>(String.class), "Место (Звезды)");


                event.registerExtraUserField(right, prefix + "total_length", new BasicSerializationType<>(String.class), "Длина струй");

                event.registerExtraUserField(right, prefix + "scores_peterhof", new BasicSerializationType<>(String.class), "Баллы (Фонтаны)");
                event.registerExtraUserField(right, prefix + "rank_peterhof", new BasicSerializationType<>(String.class), "Место (Фонтаны)");


                if (prefix.equals("kio_0_")) {
                    event.registerExtraUserField(right, prefix + "statements", new BasicSerializationType<>(String.class), "Выполнено утверждений");
                    event.registerExtraUserField(right, prefix + "figures", new BasicSerializationType<>(String.class), "Установлено фигурок");
                    event.registerExtraUserField(right, prefix + "scores_tarski", new BasicSerializationType<>(String.class), "Баллы (Дом Джэка)");
                    event.registerExtraUserField(right, prefix + "rank_tarski", new BasicSerializationType<>(String.class), "Место (Дом Джэка)");
                } else {
                    event.registerExtraUserField(right, prefix + "statements", new BasicSerializationType<>(String.class), "Выполнено утверждений");
                    event.registerExtraUserField(right, prefix + "length", new BasicSerializationType<>(String.class), "Использовано условий");
                    event.registerExtraUserField(right, prefix + "scores_tarski", new BasicSerializationType<>(String.class), "Баллы (Мир Тарского)");
                    event.registerExtraUserField(right, prefix + "rank_tarski", new BasicSerializationType<>(String.class), "Место (Мир Тарского)");
                }

                event.registerExtraUserField(right, prefix + "scores", new BasicSerializationType<>(String.class), "Баллы");
                event.registerExtraUserField(right, prefix + "rank", new BasicSerializationType<>(String.class), "Место");
            }
    }

    @Override
    public Result doGet(String action, String params) {
        return null;
    }

    @Override
    public Result doPost(String action, String params) {
        User user = User.current();
        Event event = Event.current();

        List<Contest> contests = event.getContestsAvailableForUser(user);

        ObjectId problemKioId = new ObjectId(params);

        //search for the problem

        for (Contest contest : contests)
            for (List<ConfiguredProblem> configuredProblems : contest.getPagedUserProblems(user))
                for (ConfiguredProblem configuredProblem : configuredProblems) {
                    Problem problem = configuredProblem.getProblem();
                    if (problem instanceof KioProblem && problemKioId.equals(((KioProblem) problem).getKioId())) {
                        Http.Flash flash = Http.Context.current().flash();

                        if (contest.getFinish().before(new Date())) {
                            flash.put(KioProblem.MESSAGE_KEY, "Решение не может быть принято, так как соревнование уже закончилось.");
                        } else {
                            processProblem((KioProblem) problem);

                            if (!flash.containsKey(KioProblem.MESSAGE_KEY))
                                flash.put(KioProblem.MESSAGE_KEY, "ok");
                        }

                        return Results.redirect(controllers.routes.UserInfo.contestsList(event.getId()));
                    }
                }

        return Results.notFound();
    }

    private void processProblem(KioProblem problem) {
        User user = User.current();

        Http.MultipartFormData body = Http.Context.current().request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart solutionFilePart = body.getFile("solution");

        Http.Flash flash = Http.Context.current().flash();

        if (solutionFilePart == null) {
            flash.put(KioProblem.MESSAGE_KEY, "Необходимо приложить файл");
            return;
        }

        //test file extension
        String fileName = solutionFilePart.getFilename();

        String message;

        if (!fileName.endsWith(".kio-0") && !fileName.endsWith(".kio-1") && !fileName.endsWith(".kio-2")) {
            int pntPos = fileName.lastIndexOf('.');

            if (pntPos < 0)
                message = "Ваш файл не имеет расширения, а должен иметь расширение \"kio-0\", \"kio-1\" или \"kio-2\". " +
                        "Убедитесь, что вы загружаете правильный файл.";
            else
                message = "Расширение вашего файла \"" + fileName.substring(pntPos + 1) + "\", а должно быть \"kio-0\", \"kio-1\" или \"kio-2\". " +
                        "Убедитесь, что вы загружаете правильный файл.";

            flash.put(KioProblem.MESSAGE_KEY, message);

            return;
        }

        int level = userFileNameToLevel(fileName);

        //copy file

        File file = solutionFilePart.getFile();
        File solutionFile = problem.processFile(level, file);

        if (solutionFile != null) {
            //first store previous version
            try {
                File previousVersion = new File(solutionFile.getAbsolutePath() + ".old." + new Date().getTime());
                Files.move(solutionFile.toPath(), previousVersion.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
            }

            try {
                Files.move(file.toPath(), solutionFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                flash.put(KioProblem.MESSAGE_KEY, "К сожалению, загрузка файла не удалась. Попробуйте загрузить еще раз.");
            }
        }
    }

    private int userFileNameToLevel(String fileName) {
        return fileName.charAt(fileName.length() - 1) - '0';
    }

    @Override
    public boolean needsAuthorization() {
        return true;
    }

    public static Date solutionTime(User user) {
        File file = solutionFile(user);
        if (file == null)
            return null;

        long lm = file.lastModified();
        if (lm == 0)
            return null;

        return new Date(lm);
    }

    public static class RecordResultsInfo {
        public int level;
        public KioProblemSet problemSet;
        public Map<String, JsonNode> recordResults;

        public RecordResultsInfo(int level, KioProblemSet problemSet, Map<String, JsonNode> recordResults) {
            this.level = level;
            this.problemSet = problemSet;
            this.recordResults = recordResults;
        }
    }

    public RecordResultsInfo recordResults(User user) {
        Info info = user.getInfo();
        String levelS = (String) info.get("kio_level");
        if (levelS == null)
            return null;
        int level = Integer.parseInt(levelS);
        String prefix = "kio_" + level + "_";

        Map<String, JsonNode> result = new HashMap<>();
        for (String pid : problemSet.getProblemIds(level)) {
            ObjectNode pNode = JsonNodeFactory.instance.objectNode();
            for (KioParameter parameter : problemSet.getParams(level, pid)) {
                pNode.put(parameter.getId(), (String) info.get(prefix + pid + "_" + parameter.getId()));
            }
            pNode.put("_scores", (String) info.get(prefix + "scores_" + pid));
            pNode.put("_rank", (String) info.get(prefix + "rank_" + pid));
            result.put(pid, pNode);
        }
        ObjectNode _all = JsonNodeFactory.instance.objectNode();
        _all.put("_scores", (String) info.get(prefix + "scores"));
        _all.put("_rank", (String) info.get(prefix + "rank"));
        result.put("_all", _all);

        return new RecordResultsInfo(level, problemSet, result);
    }

    //TODO make this optional variant instead of recordResults, put it to settings
    public RecordResultsInfo recordResultsFromFiles(User user) {
        File solutionFile = solutionFile(user);

        if (solutionFile == null)
            return null;

        final KioProblemSet problemSet = KioProblemSet.getInstance(year);

        final int level = userFileNameToLevel(solutionFile.getName());

        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + solutionFile.getName() + ".old.*");
        SolutionsFile solution = new SolutionsFile(solutionFile, level, problemSet);
        final List<Map<String, JsonNode>> results = new ArrayList<>();

        try {
            Files.walkFileTree(solutionFile.toPath().getParent(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Objects.requireNonNull(file);
                    Objects.requireNonNull(attrs);

                    Path name = file.getFileName();
                    if (matcher.matches(name)) {
                        SolutionsFile solution = new SolutionsFile(file.toFile(), level, problemSet);
                        Map<String, JsonNode> newResults = solution.uniteLogAndProblemResults();
                        results.add(newResults);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            return null;
        }

        Map<String, JsonNode> result = solution.uniteLogAndProblemResults();
        for (Map<String, JsonNode> newResult : results)
            result = solution.unite(result, newResult); //TODO not really well written, because solution also corresponds to file, not only to problemSet

        return new RecordResultsInfo(level, problemSet, result);
    }

    /*public Html renderUserRecords(User user) {
        RecordResultsInfo results = null;
        try {
            results = recordResults(user);
        } catch (IOException e) {
            Logger.error("Failed to render user Records", e);
            results = null;
        }

        if (results == null)
            return null;

        return views.html.kio.kio_problem_result.render(results.level, results.problemSet, results.recordResults);
    }*/

    public static File solutionFile(User user) {
        File dataFolder = Event.current().getEventDataFolder();
        File resultsFolder = new File(dataFolder, "solutions");

        String login = user.getLogin();

        File level0 = new File(resultsFolder, login + ".kio-0");
        File level1 = new File(resultsFolder, login + ".kio-1");
        File level2 = new File(resultsFolder, login + ".kio-2");

        long m0 = level0.lastModified();
        long m1 = level1.lastModified();
        long m2 = level2.lastModified();

        if (m0 > m1 && m0 > m2)
            return level0;
        if (m1 > m0 && m1 > m2)
            return level1;
        if (m2 > m0 && m2 > m1)
            return level2;

        return null;
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        serializer.write("year", year);

    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        year = deserializer.readInt("year", 2014);

        problemSet = KioProblemSet.getInstance(year);
    }

    public int getYear() {
        return year;
    }

    public String getLink(int level) {
        return "http://ipo.spb.ru/kio-files/kio-" + (year % 100) + "/KIO_" + level + "_ru.zip";
    }

}
