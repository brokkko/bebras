package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import controllers.actions.*;
import models.*;
import models.forms.InputForm;
import models.forms.RawForm;
import models.newproblems.newproblemblock.ProblemBlock;
import models.newserialization.FormDeserializer;
import models.newserialization.JSONDeserializer;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.contest_all_admin;
import views.html.contests_list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public static Result contestAdmin(String eventId, String contestId) {
        return ok(contest_all_admin.render(Contest.current(), Contest.current().saveToForm(Forms.getContestChangeForm()), new RawForm()));
    }

    //update contests
    public static Result doUpdateContest(String eventId, String contestId) {
        Event event = Event.current();

        InputForm contestForm = Forms.getContestChangeForm();

        FormDeserializer formDeserializer = new FormDeserializer(contestForm);

        RawForm form = formDeserializer.getRawForm();

        Contest contest = event.getContestById(contestId);

        if (form.hasErrors())
            return ok(contest_all_admin.render(contest, form, new RawForm()));

        try {
            contest.updateFromContestChangeForm(formDeserializer);
        } catch (Exception e) {
            //TODO not good to catch all exceptions
            form.reject("Не удалось разобрать соревнование: " + e.getMessage());
            return ok(contest_all_admin.render(contest, form, new RawForm()));

        }
        event.store();

        return redirect(routes.ContestAdministration.contestAdmin(eventId, contestId));
    }

    public static Result doAddBlock(String eventId, String contestId) {
        FormDeserializer deserializer = new FormDeserializer(Forms.getAddBlockForm());
        RawForm rawForm = deserializer.getRawForm();

        Contest contest = Contest.current();

        if (rawForm.hasErrors())
            return ok(contest_all_admin.render(contest, new RawForm(), rawForm));

        ProblemBlock block = (ProblemBlock) deserializer.getValidationData("config");

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

        return redirect;
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