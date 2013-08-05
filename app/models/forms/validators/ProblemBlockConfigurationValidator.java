package models.forms.validators;

import models.Contest;
import models.newproblems.newproblemblock.ProblemBlock;
import models.newproblems.newproblemblock.ProblemBlockFactory;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.08.13
 * Time: 2:06
 */
public class ProblemBlockConfigurationValidator extends Validator<String> {

    public ProblemBlockConfigurationValidator() {
        defaultMessage = "error.msg.problem_block_validator";
    }

    @Override
    public Validator.ValidationResult validate(String configuration) {
        ProblemBlock block = ProblemBlockFactory.getBlock(Contest.current(), configuration); //TODO this will fail if called from context without contest
        if (block == null)
            return message();

        return data(block);
    }
}
