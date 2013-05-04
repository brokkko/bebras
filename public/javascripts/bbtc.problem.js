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

    $(function(){

    });

    register_solution_loader('bbtc', load_solution);
})();