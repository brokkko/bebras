/*function bebras_dyn_problem_by_type(dyn_type) {
    var $container = $('#container-' + dyn_type);
    return $container.parents('.problem');
}

function bebras_dyn_problem_show_answers($problem) {
    return $problem.find('.task-explanation').size() > 0;
}

*/

var add_bebras_dyn_problem = (function(){

    var bebrasDynamicProblem = {}; //contains objects .problem, .problemClass, .images, .solution, .initialized

    function getInfo(dyn_type) {
        if (!(dyn_type in bebrasDynamicProblem))
            bebrasDynamicProblem[dyn_type] = {};

        return bebrasDynamicProblem[dyn_type]
    }

    function problem_to_type($problem) {
        return $problem.find('.dyn-type').text();
    }

    function type_to_problem(dyn_type) {
        return $('#container-' + dyn_type).parents('.problem');
    }

    function problem_final_init(dyn_type) {
        var giveAnswer = 'Дать ответ';
        var undoAnswer = 'Отменить ответ';

        var $problem = type_to_problem(dyn_type);

        var $button = $problem.find('.bebras-dyn-button');

        var info = getInfo(dyn_type);

        var problem = info.problem;

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

        info.initialized = true;
        load_solution($problem, info.solution);
    }

    function problem_init(dyn_type, dynamicProblemClass, dynamicProblemImages) {
        var $container = $('#container-' + dyn_type);
        $container.css('height', '');
        var problem = new dynamicProblemClass('container-' + dyn_type, dynamicProblemImages);

        problem.setInitCallback(function() {
            problem_final_init(dyn_type);
        });

        getInfo(dyn_type).problem = problem;
    }

    function add_bebras_dyn_problem(dyn_type, problemInitializer, images) {
        if (!(dyn_type in bebrasDynamicProblem))
            bebrasDynamicProblem[dyn_type] = {}; //duplication because getInfo is not in scope

        var info = bebrasDynamicProblem[dyn_type];
        info.problem = null;
        info.problemClass = problemInitializer;
        info.images = images;
        info.initialized = false;

        problem_init(dyn_type, problemInitializer, images);
    }

/*
    $(function() {
        //find all problems
        $('.problem').each(function() {
            var $problem = $(this);
            var dyn_type = problem_to_type($problem);
            var dynamicProblemClass = getInfo(dyn_type).problemClass;
            var dynamicProblemImages = getInfo(dyn_type).images;

            problem_init(dyn_type, dynamicProblemClass, dynamicProblemImages);
        });
    });
*/

    function load_solution($problem, solution) {
        if (!solution)
            solution = {'r': -1, 's': ''};

        var dyn_type = problem_to_type($problem);

        var info = getInfo(dyn_type);

        if (!info.initialized)
            info.solution = solution;
        else {
            info.solution = null;
            var problem = info.problem;

            var res = solution.r;
            problem.setEnabled(res < 0);
            problem.loadSolution(solution.s);
        }
    }

    register_solution_loader('bebras-dyn', load_solution);

    return add_bebras_dyn_problem;
})();