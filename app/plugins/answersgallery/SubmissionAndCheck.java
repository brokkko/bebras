package plugins.answersgallery;

import models.Contest;
import models.Submission;
import models.User;
import models.newproblems.ConfiguredProblem;
import models.results.Info;
import models.results.InfoPattern;
import models.results.Translator;
import play.Logger;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SubmissionAndCheck {

    private final Info check;
    private final InfoPattern checkPattern;
    private final User user;
    private final Submission lastSubmission;

    public SubmissionAndCheck(Submission submission) {
        this(Collections.singletonList(submission));
    }

    /**
     * @param submissions all submissions must have one user and one contest
     */
    public SubmissionAndCheck(List<Submission> submissions) {
        if (submissions.size() == 0) {
            check = new Info();
            checkPattern = new InfoPattern();
            lastSubmission = null;
            user = null;
            return;
        }

        lastSubmission = submissions.get(submissions.size() - 1);

        Contest contest = lastSubmission.getContest();
        Translator translator = contest.getResultTranslator();

        checkPattern = translator.getInfoPattern();
        checkPattern.unregister("rank");
        checkPattern.unregister("scores");

        user = User.getUserById(lastSubmission.getUser());

        //extract task settings for a problem
        Info problemConfig = contest.getAllPossibleProblems().stream()
                .filter(cp -> cp.getProblemId().equals(lastSubmission.getProblemId()))
                .map(ConfiguredProblem::getSettings)
                .findFirst().orElseGet(Info::new);

        check = translator.translate(
                submissions.stream().map(Submission::getCheckResult).collect(Collectors.toList()),
                Collections.nCopies(submissions.size(), problemConfig),
                user
        );
    }

    //TODO this will probably be deprecated, because it is not always known, which submission best characterizes a solution
    public Submission getSubmission() {
        return lastSubmission;
    }

    public Info getCheck() {
        return check;
    }

    public InfoPattern getCheckPattern() {
        return checkPattern;
    }

    public User getUser() {
        return user;
    }
}
