package plugins.answersgallery;

import models.Submission;
import models.results.Info;

public class SubmissionAndCheck {

    private Submission submission;
    private Info check;

    public SubmissionAndCheck(Submission submission) {
        this.submission = submission;
        this.check = submission.getCheckResult();
    }

    public Submission getSubmission() {
        return submission;
    }

    public Info getCheck() {
        return check;
    }
}
