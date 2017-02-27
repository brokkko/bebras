var dces2contest = {
    solutions_loaders_registry: {},
    register_solution_loader: function (problem_type, loader) {
        dces2contest.solutions_loaders_registry[problem_type] = loader;
    },
    get_problem_index: function ($problem_div) {
        return +$problem_div.closest('.problem').find('.pid').text();
    },

    //answer is an object
    submit_answer: function (problem_id, answer) {
    },

    //TODO is not really needed
    contest_local_storage_key: function (problem_id) { // returns key to store self data
        //will be reassigned
    },

    //value is String
    save_problem_data: function (problem_id, data_key, value) {
        //will be reassigned
    },

    get_problem_data: function (problem_id, data_key) {
        //will be reassigned
    },

    get_all_problem_data_keys: function (problem_id) {
        //will be reassigned
    }
};

(function () {

    var contest_info = {
        submit_url: null,
        stop_url: null,
        passed: null,
        duration: null,
        finished: null,
        storage_id: null,
        status: null,
        problems: []
    }; //{type -> "", ans -> {}}

    //pages

    var current_page = 0;
    var pages_count;
    var start_time = null;

    function page_selector_click() {
        var $this = $(this);
        var clicked_page_index = +$this.find('span.-info').text();
        select_page_and_submit_message(clicked_page_index);
    }

    function animate_substitute($div_to_hide, $div_to_show, complete) {
        $div_to_hide.animate({
            opacity: 0
        }, 200, "swing", function () {
            $div_to_hide.css('opacity', 1).hide();
            $div_to_show.css('opacity', 0).show().animate({
                'opacity': 1
            }, 200);
            if (complete)
                complete();
        });
    }

    function select_page_and_submit_message(page) {
        select_page(page);
        submit_system_message("page", "" + page);
    }

    function find_page_selector_and_click(page) {
        $(".page-selectors .-info").each(function () {
            var $this = $(this);
            if ($this.text() == page)
                $this.parent().click();
        });
    }

    function select_page(page) {
        if (page < 0 || page >= pages_count)
            return;

        var $selectors = $('.page-selectors');
        $selectors.children().removeClass('active');
        $selectors.each(function () {
            var $selector = $(this);
            var domPageSelector = $selector.children('.page-selector').get(page);
            $(domPageSelector).addClass('active');
        });

        //show a page
        if (!scrolling_problem_change_regime()) {
            var allPages = $('.page');
            var $current_page = $(allPages.get(current_page));
            var $new_page = $(allPages.get(page));
            animate_substitute($current_page, $new_page, function () {
                window.scrollTo(0, $selectors.offset().top - 10);
            });
        }

        current_page = page;
    }

    //problem info

    function get_problems_count() {
        return contest_info.problems.length;
    }

    function get_problem_div(pid) {
        return $($('.problem').get(pid));
    }

    function scrolling_problem_change_regime() {
        return $('.contest-is-scrolling').size() > 0;
    }

    //problem data

    function system_message_problem_data(problem_id, data_key) {
        return "pdata" + problem_id + '-' + data_key;
    }

    function system_message_get_data_key(message_field) {
        var m = message_field.match(/^pdata(\d+)-(.*)$/);
        if (m)
            return {pid: +m[1], data_key: m[2]};
        else
            return null;
    }

    dces2contest.save_problem_data = function (problem_id, data_key, value) {
        var problem = contest_info.problems[problem_id];
        if (!problem.data)
            problem.data = {};

        problem.data[data_key] = value;

        submit_system_message(system_message_problem_data(problem_id, data_key), value);

        if (sending_timeout_id == null)
            send_answers_now(); //TODO if results are already shown, this forces refresh
    };

    dces2contest.get_problem_data = function (problem_id, data_key) {
        return contest_info.problems[problem_id].data[data_key];
    };

    dces2contest.get_all_problem_data_keys = function (problem_id) {
        var data = contest_info.problems[problem_id].data;
        var res = [];
        for (var key in data)
            if (data.hasOwnProperty(key))
                res.push(key);
        return res;
    };

    //loading user answers to problems

    function load_answer(pid, answer) {
        var type = contest_info.problems[pid].type;
        dces2contest.solutions_loaders_registry[type](get_problem_div(pid), answer);
    }

    function load_all_user_answers() {
        for (var i = 0; i < get_problems_count(); i++) {
            var answer = contest_info.problems[i].ans;

            for (var li = 0; li < answers_list.length; li++) {
                var submission = answers_list[li];
                if (submission.pn == i)
                    answer = submission.a;
            }

            load_answer(i, answer);
        }
    }

    function load_all_problems_data() {
        for (var li = 0; li < answers_list.length; li++) {
            var submission = answers_list[li];
            if (submission.pn == null) {
                var m = system_message_get_data_key(submission.a.f);
                if (m !== null) {
                    var problem = contest_info.problems[m.pid];
                    if (!problem.data)
                        problem.data = {};

                    problem.data[m.data_key] = submission.a.v;
                }
            }
        }
    }

    function ensure_timer_is_going() {
        if (start_time !== null)
            return;

        var now = new Date().getTime();
        start_time = now - contest_info.passed;

        if (contest_info.status == "going" && contest_info.duration > 0)
            timer();
    }

    //page loaded

    $(window).load(function () {
        ensure_timer_is_going();
    });

    $(function () {
        pages_count = $('.page').length;

        $('.page-selector').click(page_selector_click);
        $('.page-back').click(function () {
            if (scrolling_problem_change_regime())
                find_page_selector_and_click(current_page - 1);
            else
                select_page_and_submit_message(current_page - 1);
        });
        $('.page-forward').click(function () {
            if (scrolling_problem_change_regime())
                find_page_selector_and_click(current_page + 1);
            else
                select_page_and_submit_message(current_page + 1);
        });
        $('#stop-contest').find('.page-button').click(stop_contest_click);
        $('#stop-confirmation').find('.page-button').click(stop_confirmation_click);

        contest_info = $.parseJSON($('.contest-info').text());
        dces2contest.contest_local_storage_key = function (pid) {
            return 'problem' + pid + '-' + contest_info.storage_id;
        };

        answer_sending_info_show('ok');

        switch (contest_info.status) {
            case "going":
                load_list();
                load_all_problems_data();
                load_all_user_answers();

                if (contest_info.passed == 0) //if contest just started,
                    localStorage.removeItem(local_storage_key_start_time());

                ensure_timer_is_going();

                if (answers_list.length > 0)
                    send_answers_now();

                break;
            case "wait":
                stop_contest(false);

                load_list();
                if (answers_list.length > 0)
                    send_answers_now();

                break;
            case "results":
                $('#contest-time').hide(); //hide all extra time information

                load_all_problems_data();
                load_all_user_answers();

                //test if there were unsent answers
                load_list();
                if (answers_list.length > 0)
                    send_answers_now();

                break;
        }

        //split pages if needed
        if (scrolling_problem_change_regime())
            $('#all-problems-in-pages').splitPages('page', $('.content-footer'));

        if (bebras_lesson()) {
            $('.extra-navigation').hide();
            $('.page-selectors .page-selector').hide();
        }


        function bebras_lesson() {
            return document.domain == "school.bebras.ru";
        }
    });

    //stopping contest

    var stop_timeout_handler = null;

    function stop_contest_click() {
        $('#stop-confirmation').show();
        $('#stop-contest').hide();
        if (stop_timeout_handler != null)
            clearTimeout(stop_timeout_handler);
        stop_timeout_handler = setTimeout(function () {
            $('#stop-confirmation').hide();
            $('#stop-contest').show();
        }, 10000); //10 seconds
    }

    function stop_confirmation_click() {
        stop_contest(true);
    }

    function stop_contest(send_stop_request) {
        if (send_stop_request)
            $.ajax({
                url: contest_info.stop_url,
                type: 'POST',
                dataType: 'json',
                data: '{}',
                processData: false,
                contentType: 'application/json; charset=UTF-8',
                error: stop_contest_error
            });

        //don't show contest stop again
        if (stop_timeout_handler != null)
            clearTimeout(stop_timeout_handler);

        //modify extra navigation
        $('#time-status-info').text('Соревнование окончено');
        $('#time-info').hide();
        $('#stop-contest').hide();
        $('#stop-confirmation').hide();
        $('.content-footer').hide();

        //show last screen
        animate_substitute($('#all-problems-in-pages'), $('#contest-finished-info'));
    }

    function stop_contest_error() {
        //write down that contest stopped
        if (hasLocalStorage())
            localStorage.setItem(local_storage_key_stop(), "stopped");
    }

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
        if (!hasLocalStorage()) {
            answers_list = [];
            return;
        }

        var list = localStorage.getItem(local_storage_key());
        if (!list)
            answers_list = [];
        else
            answers_list = eval(list);
    }

    function send_delay(fails) {
        var delay = /*fails **/ 60000;
        if (delay > 600 * 1000)
            delay = 600 * 1000; //not greater than two minutes
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
        if (console)
            console.log('sending success');

        answer_sending_info_show('ok');

        //if we show results then refresh page to display new results from server
        if (contest_info.status == 'results')
            window.location = window.location; //TODO refresh

        //if user stopped manually, but server did not get this
        if (hasLocalStorage() && localStorage.getItem(local_storage_key_stop()) == "stopped") {
            localStorage.removeItem(local_storage_key_stop());
            stop_contest(true);
        }
    }

    function answers_error() {
        send_fails_count++;
        undo_timeout();
        var delay = send_delay(send_fails_count);
        sending_timeout_id = setTimeout(send_answers_now, delay);
        if (console)
            console.log('sending failed, delay for ' + delay);

        answer_sending_info_show('fail');
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

        answer_sending_info_show('do');
    }

    function answer_sending_info_show(id) {
        $('.answer-sending-info').hide();
        $('.answer-sending-info-' + id).show();
    }

    function give_answer(problem_id, answer) {
        if (contest_info.status != "going") //should not occur, but anyway
            return;

        ensure_timer_is_going();

        var time = new Date().getTime() - start_time;

        var duration = contest_info.duration;
        if (duration > 0 && time >= duration) //don't send answers that are after the contest had finished
            return;

        var ans = {"lt": time, "pn": problem_id, "a": answer};
        push_answer_to_list(ans);

        if (sending_timeout_id == null)
            send_answers_now();
    }

    dces2contest.submit_answer = give_answer;

    function submit_system_message(field, value) {
        ensure_timer_is_going();

        var time = new Date().getTime() - start_time;

        var ans = {
            "lt": time, "pn": null, "a": {
                "f": field,
                "v": value
            }
        };

        push_answer_to_list(ans);
    }

    //local storage

    function hasLocalStorage() {
        return typeof localStorage != 'undefined';
    }

    function local_storage_key() {
        return "answers-" + contest_info.storage_id;
    }

    function local_storage_key_stop() {
        return "stop-" + contest_info.storage_id;
    }

    function local_storage_key_start_time() {
        return "start-" + contest_info.storage_id;
    }

    //clock
    function timer() {
        var time = new Date().getTime() - start_time;

        var millisecondsLeft = contest_info.duration - time;
        if (millisecondsLeft <= 0) {
            stop_contest(false);
            return;
        }

        //print time left
        var seconds = Math.ceil(millisecondsLeft / 1000);
        var clock_time = $('#time-info');
        if (seconds > 60) {
            var min = Math.ceil(seconds / 60);
            clock_time.text(min + ' мин.');
        } else {
            clock_time.text(seconds + ' сек.');
        }

        setTimeout(timer, 1000);
    }

})();