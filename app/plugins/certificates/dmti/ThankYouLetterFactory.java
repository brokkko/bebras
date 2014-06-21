package plugins.certificates.dmti;

import models.User;
import plugins.certificates.Diploma;
import plugins.certificates.DiplomaFactory;

public class ThankYouLetterFactory extends DiplomaFactory {
    @Override
    public Diploma getDiploma(User user) {
        return new ThankYouLetter(user, this);
    }
}
