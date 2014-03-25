package plugins;

import models.Contest;
import models.Event;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.newproblems.Problem;
import models.newproblems.kio.KioProblem;
import models.newserialization.BasicSerializationType;
import org.bson.types.ObjectId;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

//TODO get rid of the plugin. Problems should be able to send solutions by themselves
public class KioProblemPlugin extends Plugin {
    @Override
    public void initPage() {
    }

    @Override
    public void initEvent(Event event) {
        for (String right : new String[]{"participant", "self participant"})
            for (String prefix: new String[]{"kio_0_", "kio_1_", "kio_2_"}) {
                event.registerExtraUserField(right, prefix + "total_number_of_difference_graphs", new BasicSerializationType<>(String.class), "Различных созвездий");
                event.registerExtraUserField(right, prefix + "total_number_of_right_graphs", new BasicSerializationType<>(String.class), "Всего созвездий");
                event.registerExtraUserField(right, prefix + "sum_of_lines", new BasicSerializationType<>(String.class), "Длина линий");

                event.registerExtraUserField(right, prefix + "scores_stars", new BasicSerializationType<>(String.class), "Баллы (Звезды)");


                event.registerExtraUserField(right, prefix + "total_length", new BasicSerializationType<>(String.class), "Длина струй");

                event.registerExtraUserField(right, prefix + "scores_peterhof", new BasicSerializationType<>(String.class), "Баллы (Фонтаны)");


                if (prefix.equals("kio_0_")) {
                    event.registerExtraUserField(right, prefix + "statements", new BasicSerializationType<>(String.class), "Выполнено утверждений");
                    event.registerExtraUserField(right, prefix + "figures", new BasicSerializationType<>(String.class), "Установлено фигурок");
                    event.registerExtraUserField(right, prefix + "scores_tarski", new BasicSerializationType<>(String.class), "Баллы (Дом Джэка)");
                } else {
                    event.registerExtraUserField(right, prefix + "statements", new BasicSerializationType<>(String.class), "Выполнено утверждений");
                    event.registerExtraUserField(right, prefix + "length", new BasicSerializationType<>(String.class), "Использовано условий");
                    event.registerExtraUserField(right, prefix + "scores_tarski", new BasicSerializationType<>(String.class), "Баллы (Мир Тарского)");
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

        int level = fileName.charAt(fileName.length() - 1) - '0';

        //copy file

        File file = solutionFilePart.getFile();
        File solutionFile = problem.processFile(level, file);

        if (solutionFile != null) {
            try {
                Files.move(file.toPath(), solutionFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                flash.put(KioProblem.MESSAGE_KEY, "К сожалению, загрузка файла не удалась. Попробуйте загрузить еще раз.");
            }
        }
    }

    @Override
    public boolean needsAuthorization() {
        return true;
    }

    public static Date solutionTime(User user) {
        File dataFolder = Event.current().getEventDataFolder();
        File resultsFolder = new File(dataFolder, "solutions");

        String login = user.getLogin();

        File level0 = new File(resultsFolder, login + ".kio-0");
        File level1 = new File(resultsFolder, login + ".kio-1");
        File level2 = new File(resultsFolder, login + ".kio-2");

        long m0 = level0.lastModified();
        long m1 = level1.lastModified();
        long m2 = level2.lastModified();

        long max = Math.max(Math.max(m0, m1), m2);

        if (max == 0)
            return null;

        return new Date(max);
    }
}
