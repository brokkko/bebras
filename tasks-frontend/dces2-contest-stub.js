let problem_type_being_debugged = '';

let dces2contest = {

    register_solution_loader: function (problem_type, loader) {
        solutions_loaders_registry[problem_type] = loader;
        problem_type_being_debugged = problem_type;
    },

    get_problem_index: function ($problem_div) {
        //Сейчас получится отлаживать только одну задачу на странице.
        return 0;
    },

    submit_answer: function (problem_id, answer) {
        let answerString = JSON.stringify(answer);
        localStorage.setItem(contest_local_storage_key(), answerString);

        console.log('submitting to server', answer);
        document.getElementById('current-solution').innerText = answerString;
    },

    save_problem_data: function (problem_id, data_key, value) {
        let key = get_local_storage_key_for_data_key(problem_id, data_key);
        localStorage.setItem(key, value);
    },

    get_problem_data: function (problem_id, data_key) {
        let key = get_local_storage_key_for_data_key(problem_id, data_key);
        return localStorage.getItem(key);
    },

    get_all_problem_data_keys: function (problem_id) {
        let res = [];
        let problem_key = contest_local_storage_key(problem_id);
        let prefix = problem_key + '-';
        let prefix_len = prefix.length;

        //iterate all keys http://stackoverflow.com/a/8419509/1826120
        for (let i = 0; i < localStorage.length; i++) {
            let key = localStorage.key(i);
            if (key.substr(0, prefix_len) === prefix)
                res.push(key.substr(prefix.length));
        }

        return res;
    }
};

const solutions_loaders_registry = {};

//TODO it is better to use problem string id, but we do not have access to it here
function contest_local_storage_key(problem_id) {
    let contest_id = document.getElementById('contest-id').innerText;
    return 'contest-stub-storage-' + contest_id + problem_id
}

function get_local_storage_key_for_data_key(problem_id, data_key) {
    return contest_local_storage_key(problem_id) + '-' + data_key;
}

document.addEventListener('DOMContentLoaded', function () {
    let best = JSON.parse(localStorage.getItem(contest_local_storage_key()));
    let problem_divs = document.getElementsByClassName('problem');

    let problem_div = problem_divs[0];
    //jquery mock
    let $problem_div = {
        get: function (index) {
            return problem_div;
        }
    };

    let solution_loader = solutions_loaders_registry[problem_type_being_debugged];
    solution_loader($problem_div, best);

    document.getElementById('load-solution').addEventListener('click', function() {
        let solution_string = document.getElementById('solution-to-load').value;
        let solution = JSON.parse(solution_string);
        solution_loader($problem_div, solution);
        dces2contest.submit_answer(dces2contest.get_problem_index($problem_div), solution);
    });
});
