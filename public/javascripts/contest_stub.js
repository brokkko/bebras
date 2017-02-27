var dces2contest = {
    solutions_loaders_registry: {},
    register_solution_loader: function (problem_type, loader) {
        dces2contest.solutions_loaders_registry[problem_type] = loader;
    },
    get_problem_index: function ($problem_div) {
        return 0;
    },

    problem_id_2_answer: [],
    problem_id_2_data: [],

    submit_answer: function (problem_id, answer) {
        dces2contest.problem_id_2_answer[problem_id] = answer;
    },

    //TODO is not really needed
    contest_local_storage_key: function (problem_id) { // returns key to store self data
        var key = $('#problem-local-storage-key').text();
        return key ? key : 'contest-stub-problem-info-' + problem_id;
    },

    save_problem_data: function (problem_id, data_key, value) {
        if (!(problem_id in dces2contest.problem_id_2_data))
            dces2contest.problem_id_2_data[problem_id] = {};
        dces2contest.problem_id_2_data[problem_id][data_key] = value;
    },

    get_problem_data: function (problem_id, data_key) {
        if (problem_id in dces2contest.problem_id_2_data)
            return dces2contest.problem_id_2_data[problem_id][data_key];
        else
            return null;
    },

    get_all_problem_data_keys: function (problem_id) {
        var res = [];

        if (problem_id in dces2contest.problem_id_2_data) {
            var data = dces2contest.problem_id_2_data[problem_id];
            for (var key in data)
                if (data.hasOwnProperty(key))
                    res.push(key);
        }

        return res;
    }
};

$(function() {

    function load_answer_for_problem($problem_div, ans) {
        var type = $problem_div.find('.pr_type').text();
        dces2contest.solutions_loaders_registry[type]($problem_div, ans);
    }

    //load solutions for all problems
    $('.problem').each(function() {
        var $problem_div = $(this);
        load_answer_for_problem($problem_div, null);
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

        var enteredAns = dces2contest.problem_id_2_answer[0];
        var $problem_div = $($('.problem').get(1));
        load_answer_for_problem($problem_div, enteredAns);

        return false;
    });
});