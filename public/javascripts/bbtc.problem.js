(function(){

    function showing_answers($problem_div) {
        return $problem_div.find('.right-answer').length > 0;
    }

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
        if (!answer)
            var a = -1;
        else
            a = answer.a;

        choose_answer($problem_div, a);

        var selectors = $problem_div.find('.task-answer-selector');

        if (!showing_answers($problem_div)) {
            var $answer_div = $problem_div.find('.answer');
            $answer_div.click(click_answer);
            $answer_div.hover(function() {
                $(this).find('.task-answer-selector').addClass('hover');
                if (!showing_answers($problem_div))
                    $(this).css('cursor', 'pointer');
            }, function() {
                $(this).find('.task-answer-selector').removeClass('hover');
                $(this).css('cursor', '');
            });
            selectors.addClass('selectable');
        } else {
            selectors.removeClass('answer-right').removeClass('answer-user-right').removeClass('answer-user-wrong');

            var right_answer = +$problem_div.find('.right-answer').text();
            $(selectors.get(right_answer)).addClass('answer-right');
            if (a == right_answer)
                $(selectors.get(a)).addClass('answer-user-right');
            else if (a >= 0)
                $(selectors.get(a)).addClass('answer-user-wrong');
        }
    }

    function click_answer() {
        var $task_selector = $(this).find('.task-answer-selector');

        if ($task_selector.hasClass('active'))
            return;

        var answer_id = +$task_selector.find('.aid').text();

        var $problem_div = $task_selector.parents('.problem');
        var problem_id = dces2contest.get_problem_index($problem_div);
        choose_answer($problem_div, answer_id);

        dces2contest.submit_answer(problem_id, {"a": answer_id});
    }

    dces2contest.register_solution_loader('bbtc', load_solution);
})();