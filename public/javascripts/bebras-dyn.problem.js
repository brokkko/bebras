var bebrasDynamicProblem = {};
var bebrasDynamicProblemImages = {};

var _bebras_dyn_problems_document_already_ready = false;

function bebras_dyn_problem_by_type(dyn_type) {
    var $container = $('#container-' + dyn_type);
    return $container.parents('.problem');
}

function bebras_dyn_problem_show_answers($problem) {
    return $problem.find('.task-explanation').size() > 0;
}

function bebras_dyn_problem_init(dyn_type, dynamicProblemClass, dynamicProblemImages) {
    var giveAnswer = 'Дать ответ';
    var undoAnswer = 'Отменить ответ';

    var problem = new dynamicProblemClass('container-' + dyn_type, dynamicProblemImages);

    var $problem = bebras_dyn_problem_by_type(dyn_type);

    $problem.data('dyn_problem', problem);

    var $button = $problem.find('.bebras-dyn-button');

//    var answers = bebras_dyn_problem_show_answers($problem);
    $button.text(giveAnswer).click(function() {
        if (problem.isEnabled()) {
            $button.text(undoAnswer);
            problem.setEnabled(false);
            submit_answer(get_problem_index($problem), {
                'r' : problem.getAnswer(),
                's' : problem.getSolution()
            });
        } else {
            $button.text(giveAnswer);
            problem.setEnabled(true);
            submit_answer(get_problem_index($problem), {
                'r' : -1,
                's' : problem.getSolution()
            });
        }
    });

    var answer = $problem.data('load-answer');
    if (answer)
        bebras_dyn_problem_load_solution($problem, answer);
}

function add_bebras_dyn_problem(dyn_type, problemInitializer, images) {
    bebrasDynamicProblem[dyn_type] = problemInitializer;
    bebrasDynamicProblemImages[dyn_type] = images;

    if (_bebras_dyn_problems_document_already_ready)
        bebras_dyn_problem_init(dyn_type, problemInitializer, images);
}

function bebras_dyn_problem_load_solution($problem, answer) {
    if (!answer)
        answer = {'r': -1, 's': ''};

    var problem = $problem.data('dyn_problem');

    if (!problem) {
        $problem.data('load-answer', answer);
        return;
    }

    var res = answer.r;
    problem.setEnabled(res < 0);
    problem.loadSolution(answer.s);
}

(function(){

    $(function() {
        //find all problems
        $('.problem').each(function() {
            var $problem = $(this);
            var dyn_type = $problem.find('.dyn-type').text();
            var dynamicProblemClass = bebrasDynamicProblem[dyn_type];

            if (dynamicProblemClass)
                bebras_dyn_problem_init(dyn_type, dynamicProblemClass, bebrasDynamicProblemImages[dyn_type]);
        });

        _bebras_dyn_problems_document_already_ready = true;
    });

    register_solution_loader('bebras-dyn', bebras_dyn_problem_load_solution);
})();