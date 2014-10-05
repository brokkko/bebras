package controllers;

import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadContest;
import controllers.actions.LoadEvent;
import models.*;
import models.forms.InputForm;
import models.forms.RawForm;
import models.newproblems.newproblemblock.ProblemBlock;
import models.newproblems.newproblemblock.ProblemBlockFactory;
import models.newserialization.FormDeserializer;
import models.results.Info;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.contest_admin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.08.13
 * Time: 0:18
 */
@LoadEvent
@LoadContest
@Authenticated(admin = true)
@DcesController
public class ContestAdministration extends Controller {

    @SuppressWarnings("UnusedParameters")
    public static Result contestAdmin(String eventId, String contestId) {
        return ok(contest_admin.render(Contest.current(), Contest.current().saveToForm(Forms.getContestChangeForm()), new RawForm()));
    }

    //update contests
    public static Result doUpdateContest(String eventId, String contestId) {
        Event event = Event.current();

        InputForm contestForm = Forms.getContestChangeForm();

        FormDeserializer formDeserializer = new FormDeserializer(contestForm);

        RawForm form = formDeserializer.getRawForm();

        Contest contest = event.getContestById(contestId);

        if (form.hasErrors())
            return ok(contest_admin.render(contest, form, new RawForm()));

        try {
            contest.updateFromContestChangeForm(formDeserializer);
        } catch (Exception e) {
            //TODO not good to catch all exceptions
            form.reject("Не удалось разобрать соревнование: " + e.getMessage());
            return ok(contest_admin.render(contest, form, new RawForm()));

        }
        event.store();

        return redirect(routes.ContestAdministration.contestAdmin(eventId, contestId));
    }

    public static Result doAddBlock(String eventId, String contestId) {
        Contest contest = Contest.current();

        FormDeserializer deserializer = new FormDeserializer(contest.getAddBlockInputForm());
        RawForm rawForm = deserializer.getRawForm();

        if (rawForm.hasErrors())
            return ok(contest_admin.render(contest, new RawForm(), rawForm));

        String configuration = deserializer.readString("_config");
        Info translatorConfiguration = contest.getResultTranslator().getConfigInfoPattern().read(deserializer);

        ProblemBlock block = ProblemBlockFactory.getBlock(contest, configuration, translatorConfiguration);

        if (block == null) {
            rawForm.reject("_config", "Ошибка в строке конфигурации");
            return ok(contest_admin.render(contest, new RawForm(), rawForm));
        }

        contest.getProblemBlocks().add(block);

        Event.current().store();

        return redirect(routes.ContestAdministration.contestAdmin(eventId, contestId));
    }

    public static Result moveBlockUp(String eventId, String contestId, Integer index) {
        Contest contest = Contest.current();
        Result redirect = redirect(routes.ContestAdministration.contestAdmin(eventId, contestId));

        List<ProblemBlock> problemBlocks = contest.getProblemBlocks();

        if (index < 1 || index >= problemBlocks.size())
            return redirect;

        ProblemBlock block = problemBlocks.get(index);
        problemBlocks.remove(index.intValue());
        problemBlocks.add(index - 1, block);

        Event.current().store();

        return redirect;
    }

    public static Result moveBlockDown(String eventId, String contestId, Integer index) {
        Contest contest = Contest.current();
        Result redirect = redirect(routes.ContestAdministration.contestAdmin(eventId, contestId));

        List<ProblemBlock> problemBlocks = contest.getProblemBlocks();

        if (index < 0 || index >= problemBlocks.size() - 1)
            return redirect;

        ProblemBlock block = problemBlocks.get(index);
        problemBlocks.remove(index.intValue());
        problemBlocks.add(index + 1, block);

        Event.current().store();

        return redirect;
    }

    public static Result removeBlock(String eventId, String contestId, Integer index) {
        Contest contest = Contest.current();
        Result redirect = redirect(routes.ContestAdministration.contestAdmin(eventId, contestId));

        List<ProblemBlock> problemBlocks = contest.getProblemBlocks();

        if (index < 0 || index >= problemBlocks.size())
            return redirect;

        problemBlocks.remove(index.intValue());

        Event.current().store();

        return redirect;
    }

    //---------- contests add, move, remove

    //helper method to move and remove contests
    private static Result moveOrRemoveContest(String eventId, String contestId, int dir) {
        Contest contest = Contest.current();
        Result redirect = redirect(routes.UserInfo.contestsList(eventId));

        //search for contest in contests
        Event event = Event.current();
        ArrayList<Contest> contestsList = new ArrayList<>(event.getContests());

        int index = -1;
        for (int i = 0; i < contestsList.size(); i++)
            if (contestsList.get(i).getId().equals(contestId)) {
                index = i;
                break;
            }

        if (index + dir < 0 || index + dir >= contestsList.size())
            return redirect;

        contestsList.remove(index);
        if (dir != 0)
            contestsList.add(index + dir, contest);

        event.setContests(contestsList);
        event.store();

        //TODO remove users data

        //TODO remove corresponding collections

        return redirect;
    }

    public static Result doInvalidateContestsAndEventResults(final String eventId, final String contestId) {
        F.Promise<Boolean> promiseOfVoid = Akka.future(
                new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        Event event = Event.getInstance(eventId);
                        User.invalidateAllContestResults(event, event.getContestById(contestId));
                        return true;
                    }
                }
        );

        return async(
                promiseOfVoid.map(
                        new F.Function<Boolean, Result>() {
                            public Result apply(Boolean result) {
                                flash("message", "All results successfully invalidated");
                                return redirect(routes.ContestAdministration.contestAdmin(eventId, contestId));
                            }
                        }
                )
        );
    }

    public static Result moveContestUp(String eventId, String contestId) {
        return moveOrRemoveContest(eventId, contestId, -1);
    }

    public static Result moveContestDown(String eventId, String contestId) {
        return moveOrRemoveContest(eventId, contestId, +1);
    }

    public static Result removeContest(String eventId, String contestId) {
        return moveOrRemoveContest(eventId, contestId, 0);
    }

}
