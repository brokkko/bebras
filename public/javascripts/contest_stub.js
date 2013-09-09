var solutions_loaders_registry = {};

function register_solution_loader(problem_type, loader) {
    solutions_loaders_registry[problem_type] = loader;
}

function get_problem_index($problem_div) {
    return 0; //all problems have pid=0, because we anyway are not interested in their solutions
}

function submit_answer(problem_id, answer) {
    //do nothing
}

$(function() {

    //load solutions for all problems
    $('.problem').each(function() {
        var $problem_div = $(this);
        var type = $problem_div.find('.pr_type').text();
        solutions_loaders_registry[type]($problem_div, null);
    });

    //make links switch problems view
    $('a.switch-problem').click(function() {
        var $a = $(this);
        $a.parents('.problem-statement-answer').find('.problem').each(function() {
            var $problem = $(this);
            if ($problem.hasClass('hidden'))
                $problem.removeClass('hidden');
            else
                $problem.addClass('hidden');
        });

        return false;
    });
});