package plugins.bebraspdf;

import controllers.actions.AuthenticatedAction;
import models.*;
import models.newproblems.ConfiguredProblem;
import models.newserialization.*;
import models.results.Info;
import models.utils.InputStreamWrapper;
import models.utils.Utils;
import models.applications.Application;
import play.Logger;
import play.cache.Cache;
import play.libs.Akka;
import play.libs.F;
import play.mvc.*;
import plugins.Plugin;
import plugins.bebraspdf.model.TaskResult;
import plugins.bebraspdf.model.UserResult;
import plugins.bebraspdf.parser.TaskPdfParser;
import scala.concurrent.duration.Duration;
import views.Menu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 17.10.13
 * Time: 16:06
 */
public class BebrasPDFs extends Plugin {

    private Date time;
    private String applicantRight = "school org";
    private final String participantRight = "trial participant";
    private final String participantRole = "TRIAL_PARTICIPANT";
    private String participantField = "from_pdf";

    @Override
    public void initPage() {
        if (!User.currentRole().hasRight(applicantRight))
            return;

        if (new Date().before(time) && !User.current().hasEventAdminRight())
            return;

        if (getParticipants() > 0 || User.current().hasEventAdminRight())
            Menu.addMenuItem("PDF условия задач", getCall(), applicantRight);
    }

    private int getParticipants() {
        User user = User.current();

        if (user == null)
            return 0;

        //noinspection unchecked
        List<Application> apps = (List<Application>) user.getInfo().get("apps"); //get apps from config

        if (apps == null)
            return 0;

        int cnt = 0;
        for (Application app : apps)
            if ("pdf".equals(app.getType()))//get apps from
                cnt += app.getSize();

        return cnt;
    }

    @Override
    public void initEvent(Event event) {
        event.registerExtraUserField(
                participantRight,
                participantField,
                new BasicSerializationType<>(Boolean.class),
                "Участие через PDF"
        );
    }

    @Override
    public Result doGet(String action, String params) {
        if (!User.currentRole().hasRight(applicantRight))
            return Results.forbidden();

        if (new Date().before(time) && !User.current().hasEventAdminRight())
            return Results.forbidden();

        if ("go".equals(action))
            return Results.ok(views.html.bebraspdf.pdf_list.render(this));

        if (action.startsWith("get-pdf-")) {
            return getPdf(action.substring("get-pdf-".length()));
        }

        return Results.notFound();
    }

    @Override
    public Result doPost(String action, String params) {
        if (!User.currentRole().hasRight(applicantRight))
            return Results.forbidden();

        switch (action) {
            case "upload_answers":
                return uploadAnswers();
        }

        return Results.notFound();
    }

    private Result uploadAnswers() {
        final Event event = Event.current();
        final UserRole participantRole = event.getRole(this.participantRole);
        if (participantRole == null)
            return Results.internalServerError("unknown user role");

        Http.MultipartFormData body = Controller.request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart answersFilePart = body.getFile("answers");
        if (answersFilePart == null) {
            Controller.flash("pdf_upload_message", "bebraspdf.error.no_file");
            return Results.redirect(getCall("go"));
        }

        String fileName = answersFilePart.getFilename().toLowerCase();
        final File file = answersFilePart.getFile();

        final User user = User.current();

        final boolean isZip = fileName.endsWith(".zip");
        final boolean isPdf = fileName.endsWith(".pdf");
        final Date requestTime = AuthenticatedAction.getRequestTime();
        if (isZip || isPdf) {
            Akka.system().scheduler().scheduleOnce(
                    Duration.Zero(),
                    new Runnable() {
                        public void run() {
                            if (isZip)
                                uploadZipFile(file, user, event, participantRole, requestTime);
                            else
                                uploadPdfFile(file, user, event, participantRole, requestTime);
                        }
                    },
                    Akka.system().dispatcher()
            );

            Controller.flash("pdf_upload_message", "bebraspdf.ok.files_uploaded");
        } else
            Controller.flash("pdf_upload_message", "bebraspdf.error.format");

        return Results.redirect(getCall("go"));
    }

    private Result getPdf(final String fname) {
        final File pdf = new File(Event.current().getEventDataFolder(), fname + ".pdf");
        final int participants = getParticipants();

        F.Promise<byte[]> promiseOfVoid = Akka.future(
                new Callable<byte[]>() {
                    public byte[] call() {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                            byte[] contents = Cache.getOrElse("bebras-pdf-" + fname, new Callable<byte[]>() {
                                @Override
                                public byte[] call() throws Exception {
                                    return Utils.readFileAsBytes(pdf);
                                }
                            }, 60 * 60);

                            for (int i = 1; i <= participants; i++) {
                                zos.putNextEntry(new ZipEntry(i + " " + fname + ".pdf"));
                                zos.write(contents);
                            }

                        } catch (Exception e) {
                            return null;
                        }

                        return baos.toByteArray();
                    }
                }
        );

        return Results.async(
                promiseOfVoid.map(
                        new F.Function<byte[], Result>() {
                            public Result apply(byte[] file) {
                                if (file == null)
                                    return Results.ok("Не удалось загрузить файл, попробуйте еще раз");
                                Controller.response().setHeader("Content-Disposition", "attachment; filename=" + fname + ".zip");
                                return Results.ok(file).as("application/zip");
                            }
                        }
                )
        );
    }

    public Call getPdfFileCall(String fileName) {
        return getCall("get-pdf-" + fileName);
    }

    public Call getUploadPdfCall() {
        return getCall("upload_answers", false, "");
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        String sTime = deserializer.readString("time", null);
        if (sTime == null)
            time = new Date();
        else
            time = Utils.parseSimpleTime(sTime);
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);    //To change body of overridden methods use File | Settings | File Templates.
        serializer.write("time", Utils.formatDateTimeForInput(time));
    }

    // upload files

    private void processUser(InputStream in, AllParticipants allUsers, Event event, UserRole participantRole, User organizer, Date requestTime) {
        try {
            UserResult result = new TaskPdfParser().getResult(in);
            User user = allUsers.getUserByHisOrHerResults(result);

            if (user == null) {
                //ceate user
                String password = User.generatePassword();
                String login = Application.getCodeForUser(organizer) + ServerConfiguration.getInstance().getRandomString(5);
                Info info = new Info();
                info.put(User.FIELD_LOGIN, login);
                info.put(participantField, true);
                info.put("name", result.getPdfUser().getName());
                info.put("surname", result.getPdfUser().getSurname());
                info.put("grade", result.getUserClass().getName());

                user = event.createUser(password, participantRole, info, organizer);

                if (user == null)
                    throw new Exception("Failed to create user");

                allUsers.addUser(user);
            }

            int grade = result.getUserClass().getClassNumber();
            Contest contest = getContestByUser(event, grade);

            if (contest == null)
                throw new Exception("Couldn't convert grade to contest: " + grade);

            List<ConfiguredProblem> userProblems = contest.getUserProblems(user);
            List<Submission> submissionsForContest = user.getSubmissionsForContest(contest);

            for (int problemIndex = 0; problemIndex < 5; problemIndex++) {
                TaskResult taskResult = result.getTaskResults().get(problemIndex);
                String answer = taskResult.getAnswer().getPdfValue();
                int ans = -1;
                if (!answer.equals("") && !answer.equals("4"))
                    ans = Integer.parseInt(answer);

                Submission submission = submissionsForContest.get(problemIndex);
                long localTime = submission == null ? 1 : submission.getLocalTime() + 1;
                new Submission(
                        contest,
                        user.getId(),
                        localTime,
                        requestTime,
                        userProblems.get(problemIndex).getProblemId(),
                        new Info("a", ans)
                ).serialize();
            }

            user.invalidateContestResults(contest.getId());
        } catch (Exception e) {
            Logger.warn("Failed to process a file with solutions: ", e);
        }
    }

    private Contest getContestByUser(Event event, int grade) {
        String contestId = null;
        switch (grade) {
            case 1:
            case 2:
                contestId = "1-2";
                break;
            case 3:
            case 4:
                contestId = "3-4";
                break;
            case 5:
            case 6:
                contestId = "5-6";
                break;
            case 7:
            case 8:
                contestId = "7-8";
                break;
            case 9:
            case 10:
                contestId = "9-10";
                break;
            case 11:
                contestId = "11";
                break;
        }

        if (contestId == null)
            return null;

        return event.getContestById(contestId);
    }

    private void uploadPdfFile(File file, User organizer, Event event, UserRole participantRole, Date requestTime) {
        AllParticipants allUsers = new AllParticipants(participantRole.getName(), participantField, organizer.getId());

        try (InputStream fin = new FileInputStream(file)) {
            processUser(fin, allUsers, event, participantRole, organizer, requestTime);
        } catch (Exception e) {
            Logger.error("Failed to upload pdf file with user solutions", e);
        }
    }

    private void uploadZipFile(File file, User organizer, Event event, UserRole participantRole, Date requestTime) {
        AllParticipants allUsers = new AllParticipants(participantRole.getName(), participantField, organizer.getId());

        try (ZipInputStream in = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (!entry.getName().toLowerCase().endsWith(".pdf"))
                    Logger.info("User uploaded zip with not-pdf file: " + organizer.getId());
                processUser(new InputStreamWrapper(in), allUsers, event, participantRole, organizer, requestTime);
            }
        } catch (Exception e) {
            Logger.error("Failed to upload zip file with user solutions", e);
        }

    }
}
