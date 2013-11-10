var add_bebras_dyn_problem = (function(){

    var bebrasDynamicProblem = {}; //contains objects .problem, .problemClass, .images, .solution, .initialized

    function getInfo(dyn_type) {
        if (!(dyn_type in bebrasDynamicProblem))
            bebrasDynamicProblem[dyn_type] = {};

        return bebrasDynamicProblem[dyn_type]
    }

    function showing_answers($problem) {
        return $problem.find('.task-explanation').size() > 0;
    }

    function problem_to_type($problem) {
        return $problem.find('.dyn-type').text();
    }

    function type_to_problem(dyn_type) {
        return $('#container-' + dyn_type).parents('.problem');
    }

    function update_controls($problem) {
        var giveAnswer = 'Дать ответ';
        var undoAnswer = 'Отменить ответ';
        var resetAnswer = 'Сбросить решение';
        var status0 = 'Ответ не указан';
        var status1 = 'Ответ сохранен';

        var $status = $problem.find('.bebras-dyn-status');
        var $button_main = $problem.find('.bebras-dyn-button-main');
        var $button_undo = $problem.find('.bebras-dyn-button-undo');

        var dyn_type = problem_to_type($problem);
        var info = getInfo(dyn_type);
        var problem = info.problem;

        if (showing_answers($problem)) {
            problem.setEnabled(true);

            var status_dont_know = 'Ответ: не знаю';
            var status_answer_given_right = 'Дан правильный ответ';
            var status_answer_given_wrong = 'Дан неправильный ответ';
            var status_answer_show = 'Показать ответ участника';

            $button_main.hide();

            if (info.initial_solution.r < 0) {
                $status.text(status_dont_know);
                $button_undo.hide();
            } else {
                var is_right = info.initial_solution.r > 0;
                $status.text(is_right ? status_answer_given_right : status_answer_given_wrong);
                if (is_right)
                    $status.removeClass('answered'); //TODO make extra class: wrong
                else
                    $status.addClass('answered');
                $button_undo.text(status_answer_show).show();
            }

            return;
        }

        if (problem.isEnabled()) {
            $button_main.text(giveAnswer);
            $status.text(status0).removeClass('answered');
            problem.setEnabled(true);
            $button_undo.show();
        } else {
            $button_main.text(undoAnswer);
            $button_undo.hide();
            $status.text(status1).addClass('answered');
            problem.setEnabled(false);
        }

        $button_undo.text(resetAnswer);
    }

    function problem_final_init(dyn_type) {
        var $problem = type_to_problem(dyn_type);

        var $button_main = $problem.find('.bebras-dyn-button-main');
        var $button_undo = $problem.find('.bebras-dyn-button-undo');

        var info = getInfo(dyn_type);

        var problem = info.problem;

        $button_main.click(function() {
            var enabled = problem.isEnabled();
            problem.setEnabled(!enabled);

            var pid = get_problem_index($problem);
            if (enabled)
                submit_answer(get_problem_index($problem), {
                    'r': problem.getAnswer(),
                    's': problem.getSolution()
                });
            else
                submit_answer(get_problem_index($problem), {
                    'r': -1,
                    's': problem.getSolution()
                });

            update_controls($problem);
        });

        $button_undo.click(function() {
            if (showing_answers($problem))
                problem.loadSolution(info.initial_solution.s);
            else
                problem.loadSolution("");
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
        var info = getInfo(dyn_type);

        if (info.problem) //add dyn problem may be called several times because document.ready is called several times
            return;

        info.problem = null;
        info.problemClass = problemInitializer;
        info.images = images;
        info.initialized = false;
        info.initial_solution = "";

        problem_init(dyn_type, problemInitializer, images);
    }

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

            info.initial_solution = solution;

            update_controls($problem);
        }
    }

    register_solution_loader('bebras-dyn', load_solution);

    return add_bebras_dyn_problem;
})();