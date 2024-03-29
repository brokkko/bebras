package controllers;

import com.mongodb.BasicDBObject;
import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.Forms;
import models.User;
import models.forms.RawForm;
import models.newproblems.ConfiguredProblem;
import models.newproblems.Problem;
import models.newproblems.ProblemInfo;
import models.newproblems.ProblemLink;
import models.newserialization.FormDeserializer;
import models.newserialization.SerializationTypesRegistry;
import org.bson.types.ObjectId;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 07.09.13
 * Time: 20:42
 */
@LoadEvent
@Authenticated(admin = true)
@DcesController
public class Problems extends Controller {

    public static Result viewFolder(String eventId, String path) {
        return ok(problems_folder.render(new ProblemLink(path), false, new RawForm(), new RawForm()));
    }

    public static Result viewProblem(String eventId, String link) {
        link = link.replaceAll("%20", " ");
        ProblemLink pLink = new ProblemLink(link);

        if (pLink.get() == null)
            return notFound(error.render("Не удается найти задачу", new String[0]));

        return ok(problem_view.render(pLink));
    }

    //TODO code duplication with viewProblem
    public static Result viewProblem1(String eventId, String link) {
        link = link.replaceAll("%20", " ");
        ProblemLink pLink = new ProblemLink(link);

        if (pLink.get() == null)
            return notFound(error.render("Не удается найти задачу", new String[0]));

        return ok(problem_view_1.render(pLink));
    }

    public static Result viewRawProblem(String eventId, String pidAsString) {
        ObjectId pid;
        try {
            pid = new ObjectId(pidAsString);
        } catch (IllegalArgumentException ignored) {
            return notFound(error.render("Не удается найти задачу", new String[0]));
        }

        ProblemInfo info = ProblemInfo.get(pid);

        if (info == null)
            return notFound(error.render("Не удается найти задачу", new String[0]));

        return ok(problem_view_raw.render(pid, info.getProblem()));
    }

    //TODO generalize to move to other folders
    public static Result renameProblem(String eventId, String path) {
        ProblemLink link = new ProblemLink(path);
        ProblemLink parent = link.getParentLink();

        RawForm form = new RawForm();
        form.bindFromRequest();
        String newPath = form.get("extra-value");
        if (!newPath.startsWith("/"))
            newPath = (parent != null ? parent.getLink() + '/' : "") + newPath;
        else
            newPath = newPath.substring(1);

        newPath = removeUnsupportedSymbols(newPath);

        if (!link.move(newPath))
            return redirect(controllers.routes.Problems.viewProblem(eventId, path));

        return redirect(routes.Problems.viewProblem(eventId, newPath));
    }

    private static String removeUnsupportedSymbols(String path) {
        return path.replaceAll("[^\\-a-zA-Z0-9_/]", "");
    }

    public static Result removeLink(String eventId, String path) {
        removeLinksSubtree(path);

        ProblemLink link = new ProblemLink(path);
        String parent = link.getParent();

        if (parent == null)
            return redirect(routes.EventAdministration.admin(eventId));
        else
            return redirect(routes.Problems.viewFolder(eventId, parent));
    }

    public static void removeLinksSubtree(String path) {
        for (String regex : new String[]{"^" + path + "/", "^" + path + "$"}) //TODO optimize two requests
            MongoConnection.getProblemDirsCollection().remove(new BasicDBObject(
                    "link",
                    new BasicDBObject("$regex", regex)
            ));
    }

    public static Result createFolder(String eventId, String folderPath) {
        FormDeserializer deserializer = new FormDeserializer(Forms.getCreateFolderForm());
        RawForm form = deserializer.getRawForm();

        if (form.hasErrors())
            return ok(problems_folder.render(new ProblemLink(folderPath), false, form, new RawForm()));

        String name = deserializer.readString("new_folder_name");

        ProblemLink link = new ProblemLink(new ProblemLink(folderPath), name);
        link.mkdirs();

        return redirect(routes.Problems.viewFolder(eventId, link.getLink()));
    }

    public static Result createProblem(String eventId, String folderPath) throws IllegalAccessException, InstantiationException {
        FormDeserializer deserializer = new FormDeserializer(Forms.getCreateProblemForm());
        RawForm form = deserializer.getRawForm();

        if (form.hasErrors())
            return ok(problems_folder.render(new ProblemLink(folderPath), false, new RawForm(), form));

        String name = deserializer.readString("new_problem_name");
        String type = deserializer.readString("new_problem_type");

        Problem problem = SerializationTypesRegistry.PROBLEM.getClass(type).newInstance();
        ProblemLink link = new ProblemLink(new ProblemLink(folderPath), name);
        ProblemInfo pi = ProblemInfo.put(problem);
        link.setProblemId(pi.getId());

        return redirect(routes.Problems.viewProblem(eventId, link.getLink()));
    }

    public static Result updateProblem(String eventId, String problemLink) { //TODO is it used, and what is the difference with editProblem?
        ProblemLink link = new ProblemLink(problemLink);
        Problem problem = link.get();

        if (problem == null)
            return notFound(error.render("Не удается найти задачу", new String[0]));

//        if (false)
//            flash("errors", "some errors"); //TODO show errors

        //update problem
        RawForm form = new RawForm();
        form.bindFromRequest();
        problem.updateProblem(form);

        //save problem
        ObjectId pid = link.getProblemId();
        ProblemInfo pi = new ProblemInfo(pid, problem);
        pi.store();

        return redirect(routes.Problems.viewProblem(eventId, problemLink));
    }

    public static Result editProblem(String eventId, String problemLink) {
        RawForm form = new RawForm();
        form.bindFromRequest();

        try {
            ObjectId pid = new ObjectId(problemLink);
            return editProblemById(eventId, pid, form);
        } catch (IllegalArgumentException e) {
            return editProblemByLink(eventId, problemLink, form);
        }
    }

    private static Result editProblemByLink(String eventId, String problemLink, RawForm form) {
        ProblemLink link = new ProblemLink(problemLink);
        Problem problem = link.get();

        if (problem == null)
            return notFound(error.render("Не удается найти задачу", new String[0]));

        problem.updateProblem(form);

        ProblemInfo problemInfo = new ProblemInfo(link.getProblemId(), problem);
        problemInfo.store();

        return redirect(routes.Problems.viewProblem(eventId, problemLink));
    }

    private static Result editProblemById(String eventId, ObjectId pid, RawForm form) {
        ProblemInfo info = ProblemInfo.get(pid);

        if (info == null)
            return notFound(error.render("Не удается найти задачу", new String[0]));

        info.getProblem().updateProblem(form);

        info.store();

        return redirect(routes.Problems.viewRawProblem(eventId, pid.toHexString()));
    }
}
