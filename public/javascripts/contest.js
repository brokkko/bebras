var solutions_loaders_registry = {};

function register_solution_loader(problem_type, loader) {
    solutions_loaders_registry[problem_type] = loader;
}

function get_problem_index($problem_div) {
    return +$problem_div.find('.pid').text();
}

var submit_answer; //function (problem_id, answer)

(function () {

    var contest_info = {
        submit_url: null,
        passed: null,
        duration: null,
        finished: null,
        storage_id: null,
        problems: []
    }; //{type -> "", ans -> {}}

    //pages

    var current_page = 0;
    var pages_count;
    var start_time;

    function page_selector_click() {
        var $this = $(this);
        var clicked_page_index = +$this.find('span').text();
        select_page(clicked_page_index);
    }

    function select_page(page) {
        if (page < 0 || page >= pages_count)
            return;

        var $selectors = $('.page-selectors');
        $selectors.children().removeClass('active');
        $selectors.each(function(){
            var $selector = $(this);
            var domPageSelector = $selector.children('.page-selector').get(page);
            $(domPageSelector).addClass('active');
        });

        //show a page
        var allPages = $('.page');
        var $current_page = $(allPages.get(current_page));
        var $new_page = $(allPages.get(page));
        $current_page.animate({
            opacity: 0
        }, 200, function() {
            $current_page.css('opacity', 1).hide();
            $new_page.css('opacity', 0).show().animate({
                'opacity': 1
            }, 200);
        });

        current_page = page;
    }

    //problem info

    function get_problems_count() {
        return contest_info.problems.length;
    }

    function get_problem_div(pid) {
        return $($('.problem').get(pid));
    }

    //loading user answers to problems

    function load_answer(pid, answer) {
        var type = contest_info.problems[pid].type;
        solutions_loaders_registry[type](get_problem_div(pid), answer);
    }

    function load_all_user_answers() {
        for (var i = 0; i < get_problems_count(); i++) {
            var answer = contest_info.problems[i].ans;

            for (var li = 0; li < answers_list.length; li++) {
                var submission = answers_list[li];
                if (submission.pid == i)
                    answer = submission.a;
            }

            if (answer != null)
                load_answer(i, answer);
        }
    }

    //page loaded

    $(function() {
        pages_count = $('.page').length;

        $('.page-selector').click(page_selector_click);
        $('.page-back').click(function(){select_page(current_page - 1);});
        $('.page-forward').click(function(){select_page(current_page + 1);});

        contest_info = $.parseJSON($('.contest-info').text());

        load_list();

        load_all_user_answers();

        start_time = new Date().getTime() - contest_info.passed;

        if (answers_list.length > 0)
            send_answers_now();
    });

    //sending answers

    var send_fails_count = 0; //number of fails since the last success
    var answers_list = []; //list of answers to send
    var sending_timeout_id = null; //timeout id, null if nothing is planned for sending

    function push_answer_to_list(answer) {
        answers_list.push(answer);
        if (hasLocalStorage())
            localStorage.setItem(local_storage_key(), JSON.stringify(answers_list));
    }

    function clear_list() {
        answers_list = [];
        if (hasLocalStorage())
            localStorage.setItem(local_storage_key(), '');
    }

    function load_list() {
        if (! hasLocalStorage()) {
            answers_list = [];
            return;
        }

        var list = localStorage.getItem(local_storage_key());
        if (! list)
            answers_list = [];
        else
            answers_list = eval(list);
    }

    function send_delay(fails) { //TODO make wait longer
        var delay = fails * 5000;
        if (delay > 120 * 1000)
            delay = 120 * 1000; //not greater than two minutes
        return delay;
    }

    function undo_timeout() {
        if (sending_timeout_id != null)
            clearTimeout(sending_timeout_id);
        sending_timeout_id = null;
    }

    function answers_success() {
        clear_list();
        sending_timeout_id = null;
        send_fails_count = 0;
        console.log('send success');
    }

    function answers_error() {
        send_fails_count ++;
        undo_timeout();
        var delay = send_delay(send_fails_count);
        sending_timeout_id = setTimeout(send_answers_now, delay);
        console.log('send failed, delay for ' + delay);
    }

    function send_answers_now() {
        undo_timeout();
        $.ajax({
            url: contest_info.submit_url,
            type: 'POST',
            dataType: 'json',
            data: JSON.stringify(answers_list),
            processData: false,
            contentType: 'application/json; charset=UTF-8',
            success: answers_success,
            error: answers_error
        });
    }

    function give_answer(problem_id, answer) {
        var time = new Date().getTime() - start_time;

        var duration = contest_info.duration;
        if (duration > 0 && time >= duration) //don't send answers that are after the contest had finished
            return;

        var ans = {"lt": time, "pid": problem_id, "a": answer};
        push_answer_to_list(ans);

        if (sending_timeout_id == null)
            send_answers_now();
    }

    submit_answer = give_answer;

    //local storage

    function hasLocalStorage() {
        return typeof localStorage != 'undefined';
    }

    function local_storage_key() {
        return "answers-" + contest_info.storage_id;
    }

})();