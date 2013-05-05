(function(){

    function get_answers_count($problem_div) {
        return +$problem_div.find('.problem-info').text();
    }

    function choose_answer($problem_div, answer_ind) {
        if (answer_ind < 0)
            answer_ind = get_answers_count($problem_div);

        var $selectors = $problem_div.find('.task-answer-selector');
        $selectors.removeClass('active');
        $($selectors.get(answer_ind)).addClass('active');
    }

    function load_solution($problem_div, answer) {
        var a = answer.a;
        choose_answer($problem_div, a);
    }

    function click_answer() {
        var $task_selector = $(this);

        if ($task_selector.hasClass('active'))
            return;

        var answer_id = +$task_selector.find('.aid').text();

        var $problem_div = $task_selector.parents('.problem');
        var problem_id = get_problem_index($problem_div);
        choose_answer($problem_div, answer_id);

        submit_answer(problem_id, {"a": answer_id});
    }

    $(function(){
        $('.task-answer-selector').click(click_answer);
    });

    register_solution_loader('bbtc', load_solution);
})();