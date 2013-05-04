function answer_index_to_letter(ind) {
    if (ind == 1)
        return 'А';
    else if (ind == 2)
        return 'Б';
    else if (ind == 3)
        return 'В';
    else if (ind == 4)
        return 'Г';
    else
        return '&nbsp;';
}

/**
 * Найти среди классов заданного элемента тот, котрый начинается на start
 * @param el Элемент
 * @param start Начало названия класса
 * @return {String} Строка с именем класса
 */
function getClassThatStartsWith(el, start) {
    var class_list = el.attr('class').split(/\s+/);
    for (var i = 0; i < class_list.length; i++) {
        var cls = class_list[i];
        if (cls.indexOf(start) == 0)
            return cls;
    }

    return '';
}

function getNForClassThatStartsWith(el, start) {
    var cls = getClassThatStartsWith(el, start);
    return + cls.substring(start.length);
}

/**
 * Для элемента el, имеющего класс вида task-answer-selector-N определяет N
 * @param el Элемент с классом вида task-answer-selector-N
 * @return {int} N
 */
function answer_element_to_answer_index(el) {
    return getNForClassThatStartsWith(el.parent(), 'task-answer-selector-');
}

function answer_element_to_problem_index(el) {
    return getNForClassThatStartsWith(el.parents('.swPage'), 'page-problem-');
}

/**
 * Это действие оборачивает все Заголовки задач, т.е. элементы с классом task-title. Возможно, этого же можно достичь через CSS
 */
function wrap_problem_headers() {
    var all_titles = $('.task-title');

    var i = 0;
    all_titles.each(function() {
        var title = $(this);

        i ++;
        var ind = i;
        if (is_printing())
            ind = Math.floor((i + 1) / 2);

        title.text('Задача '  + ind + '. "' + title.text() + '"');
    });
    all_titles.addClass('section-title');
}

/**
 * Вставляет слово "Вопрос" перед всеми вопросами к задачам
 */
function wrap_problem_questions() {
    $('.task-question').each(function() {
        var q = $(this);
        if ($.trim(q.text()) != "")
            q.before("<div class='section-title task-question-title clear'>Вопрос</div>");
    });
}

function select_answer_selector(el) {
    var is_active = el.hasClass('active');
    if (is_active)
        return;

    el.parents('.task-answers').find('.task-answer-selector').removeClass('active');
    el.removeClass('over').addClass('active');
}

/**
 * Аккуратно располагает на странице ответы к задаче, т.е. элементы с классом task-answers
 * Для выбора расположения используется класс answers-layout-XXX, который назначен этому же элементу
 */
function wrap_problem_answers() {
    var all_answers_wrappers = $('.task-answers')
        .before("<div class='section-title task-answers-title clear'>Варианты ответов</div>")
        .append("<div class='task-answer'>Без ответа</div>");

    //цикл по блокам ответов всех задач
    all_answers_wrappers.each(function() {
        var answers_wrapper = $(this);
        var needed_frame = getClassThatStartsWith(answers_wrapper, 'answers-layout-');

        var new_answers_wrapper = $('#' + needed_frame).clone();
        new_answers_wrapper.attr('id', ''); //in new versions of jquery id was also copied for some reason
        new_answers_wrapper.addClass('task-answers');

        //цикл по ответам текущей задачи
        var ind = 1;
        answers_wrapper.find('.task-answer').each(function() {
            var ans = $(this);
            //добавить ответ в табицу
            new_answers_wrapper.find('.answer-wrapper-' + ind).append(ans);
            //создать и добавить выбор
            var new_selector = $("<div class='task-answer-selector'></div>");
            new_selector.html(answer_index_to_letter(ind));
            new_answers_wrapper.find('.task-answer-selector-' + ind).append(new_selector);

            if (ind == 5) //для последнего ответа "без ответа" установить, что он выбран
                new_selector.addClass('active');

            ind ++;
        });
        answers_wrapper.after(new_answers_wrapper);
        answers_wrapper.remove();
    });

    //назначить слушателей для всех переключателей ответа
    $('.task-answer-selector').click(function() {
        //определить номер ответа
        var ans = $(this);
        select_answer_selector(ans);
        give_answer(answer_element_to_problem_index(ans), answer_element_to_answer_index(ans));
    });

    $('.task-answer-selector').mouseenter(function() {
        var ans = $(this);
        var is_active = ans.hasClass('active');
        if (! is_active)
            ans.addClass('over');
    });

    $('.task-answer-selector').mouseleave(function() {
        $(this).removeClass('over');
    });
}

var NO_ANS_MESSAGE = "Ответ не указан";
var ANS_GIVEN_MESSAGE = "Ваш ответ сохранен";
var GIVE_ANSWER_BUTTON = "Дать ответ";
var CLEAR_ANSWER_BUTTON = "Сбросить";
var UNDO_ANSWER_BUTTON = "Отменить ответ";

function wrap_problem_dynamic_answers() {
    $('.task-dynamic-answers').each(function() {
        var answers_wrapper = $(this);

        var answer_controls = $('<div class="answer-controls"></div>');

        var dyn_el = answers_wrapper.parent().find('.dynamic-app-wrapper');
//(!)        var dyn_el_sizer = dyn_el.parent();
        var div_blocker = $('<div class="flash-blocker hidden"></div>');
        var div_flash_and_blocker_wrapper = $('<div class="flash-and-blocker-wrapper"></div>');
        dyn_el.wrap(div_flash_and_blocker_wrapper);
        dyn_el.parent().prepend(div_blocker).append('<div class="flash-spacer"></div>').append(answer_controls);
        div_blocker.width(dyn_el.width());
        div_blocker.height(dyn_el.height());

//(!)        dyn_el_sizer.wrap(div_flash_and_blocker_wrapper);
//(!)        dyn_el_sizer.parent().prepend(div_blocker).append('<div class="flash-spacer"></div>').append(answer_controls);
//(!)        div_blocker.width(dyn_el.parent().width());
//(!)        div_blocker.height(dyn_el.parent().height());

        var bt_give_answer = $('<input type="button" class="dyn-give-answer">');
        var bt_remove_answer = $('<input type="button" class="dyn-clear-answer">');
        var div_info = $('<span class="dyn-answer-info">' + NO_ANS_MESSAGE + '</div>');
        bt_give_answer.val(GIVE_ANSWER_BUTTON);
        bt_remove_answer.val(CLEAR_ANSWER_BUTTON);

        answer_controls.append(div_info);
        answer_controls.append(bt_give_answer);
        answer_controls.append(bt_remove_answer);

//        function blur_handler() {
//            $(this).blur();
//            console.log('here');
//            console.log($(this).blur);
//        }
//        bt_give_answer.focus(blur_handler);
//        bt_remove_answer.focus(blur_handler);
//        bt_give_answer.blur(function() {
//            console.log('blur 1');
//        });
//        bt_remove_answer.blur(function() {
//            console.log('blur 2');
//        });

        //disable if needed
        var problem_id = getNForClassThatStartsWith(dyn_el, 'for-problem-');

        var user_dynamic_answer = null;
        if (problem_id in initial_answers)
            user_dynamic_answer = initial_answers[problem_id];
        else
            user_dynamic_answer = problems[problem_id - 1].user_answer;

        if (user_dynamic_answer && user_dynamic_answer != 5) {//if some answer was given
            dyn_disable(problem_id);
            answer_controls.addClass('answer-already-given');
            bt_give_answer.val(UNDO_ANSWER_BUTTON);
            div_info.text(ANS_GIVEN_MESSAGE);
        }
    });

    $('.dyn-give-answer').click(function(){
        var button = $(this);
        var problem_id = answer_element_to_problem_index(button);

        var answer_controls = button.parents('.answer-controls');
        var answer_info = answer_controls.find('.dyn-answer-info');

        if (solutions_shown) {
            dyn_set_data(problem_id, problems[problem_id - 1].stored_answer);
        } else if (answer_controls.hasClass('answer-already-given')) {
            answer_controls.removeClass('answer-already-given');
            give_answer(problem_id, 5, '');  //5 means no answer
            button.val(GIVE_ANSWER_BUTTON);

            dyn_enable(problem_id);

            answer_info.text(NO_ANS_MESSAGE);
        } else {
            answer_controls.addClass('answer-already-given');
            give_answer(problem_id, dyn_get_answer(problem_id), dyn_get_data(problem_id));  //5 means no answer
            button.val(UNDO_ANSWER_BUTTON);

            dyn_disable(problem_id);

            answer_info.text(ANS_GIVEN_MESSAGE);
        }
    });

    $('.dyn-clear-answer').click(function(){
        var button = $(this);
        var problem_id = answer_element_to_problem_index(button);

        if (! solutions_shown) {
            var answer_controls = button.parents('.answer-controls');
            if (answer_controls.hasClass('answer-already-given'))
                button.parent().find('.dyn-give-answer').click();
        }

        dyn_reset(problem_id);
    });

    $('.flash-blocker').click(function() {
        $(this).parents('.flash-and-blocker-wrapper').find('.dyn-give-answer').click();
    });
}

/**
 * Добавляет слово решение перед всеми решениями задач и скрывает их первоначально
 */
function wrap_problem_explanations() {
    $('.task-explanation').hide();

    $('.task-explanation').each(function() {
        $(this).prepend("<div class='section-title task-explanation-title clear'>Решение</div>")
    });
}

function wrap_problem_scores() {
    $('.task').each(function() {
        var task = $(this);
        var scores_div = task.find('.task-scores');
        var scores = scores_div.text();
        scores_div.remove();

        var info = $("<span class='task-scores'></span>");
        info.text(scores + ' ' + scores_text(scores));
        task.find('.task-title')/*.append("<br>")*/.append(info); //TODO try to make nonbreaking spans
    });
}

/**
 * Преобразует код страны в русское название
 * @param code код страны
 * @return {String} название по-русски
 */
function country_code_to_name(code) {
    var countries = {
        'AT': 'Австрия',
        'BG': 'Болгария',
        'CA': 'Канада',
        'CH': 'Швейцария',
        'CZ': 'Чехия',
        'DE': 'Германия',
        'EE': 'Эстония',
        'ES': 'Испания',
        'FI': 'Финляндия',
        'FR': 'Франция',
        'HU': 'Венгрия',
        'IT': 'Италия',
        'JP': 'Япония',
        'LT': 'Литва',
        'NL': 'Нидерланды',
        'PL': 'Польша',
        'RU': 'Россия',
        'SI': 'Словения',
        'SK': 'Словакия',
        'UA': 'Украина',
        'US': 'Америка'
    };

    code = code.toUpperCase();
    if (code in countries)
        return countries[code];
    else
        return code + " (неизвестная страна)";
}

/**
 * Обрабатывает информацию о флагах, находящуюся в div.country
 */
function wrap_problem_flags() {
    var flags_url = $('#flags-url').text();

    $('.task').each(function() {
        var task = $(this);
        var country = task.find('.country').text();

        var name = country_code_to_name(country);
        if (!name)
            return;

        var info = $("<span class='country-info'></span>");
        var img = $("<img>");
        img.attr('alt', country);
        img.attr('src', flags_url + country.toUpperCase() + '.png');
        info.text(name);
        info.prepend(img);
        task.find('.task-title').append(info);
    });
}

/**
 * Выбирает правильное окончание для слова "баллов" во фразе о количестве набранных баллов
 * @param s колличество баллов
 * @return {String} слово "баллов" с правильным окончанием
 */
function scores_text(s) {
    s = Math.abs(s);
    var x = s % 10;
    var y = Math.floor(s / 10) % 10;
    if (y == 1)
        return 'баллов';

    if (x == 1)
        return 'балл';
    else if (x == 0 || x >= 5)
        return 'баллов';
    else
        return 'балла';
}

function substracted_scores(scores) {
    return Math.round(scores / 3);
}

var solutions_shown = false;

/**
 * Изменяет режим на показывание баллов, заодно вычисляет набранные баллы
 */
function show_solutions() {
    $('#finish-confirmation').hide();
    $('#button-finish').hide();
    $('.task-explanation').show();
    $('#finish-text').hide();
    $('#results-text').show();
    $('.bm-welcome').hide();
    $('.bm-finish').click();

    $('.task-answer-selector').unbind('click').unbind('mouseenter').unbind('mouseleave').css('cursor', 'default'); //off() in modern versions of JQuery

    var scores = 0;
    var right_answers = 0;
    var wrong_answers = 0;
    var skipped_answers = 0;

    function addClassForTaskAnswer(task, ans_ind, class_name) {
        task.find('.task-answer-selector-' + ans_ind).find('.task-answer-selector').addClass(class_name);
    }

    $('.task').each(function() {
        var task = $(this);
        var ans_right = task.find('.correct-answer').text();
        if (!ans_right)
            return;

        //get problem id
        var page = task.parents('.swPage');
        var problem_id = getNForClassThatStartsWith(page, 'page-problem-');

        var pr = problems[problem_id - 1];

        //get user answer
        var user_index;
        if (pr.is_dynamic) {
            var dyn_el = dyn_get_element(problem_id);
            if (dyn_el.getResult) {//take the result from flash if it is possible (otherwise the program is just loaded
                user_index = dyn_get_answer(problem_id);
                pr.stored_answer = dyn_get_data(problem_id);
            } else if (problem_id in initial_answers) {//or take the result from the initial data (must not work because initial_answers are cleared before showing results)
                user_index = initial_answers[problem_id];
                pr.stored_answer = initial_answers_dyn[problem_id];
            } else {
                user_index = pr.user_answer; //or take the result from the data sent from server
                pr.stored_answer = pr.dynamic_data;
            }
        } else {
            var user_selection = task.find('.active');
            user_index = answer_element_to_answer_index(user_selection);
        }

        var answer_controls = task.find('.answer-controls');

        if (user_index != 5) {
            if (user_index == ans_right) {
                scores += problems[problem_id - 1].scores;
                right_answers += 1;
                if (pr.is_dynamic)
                    answer_controls.addClass('answer-already-given'); //this is made to set color and nothing more
                else
                    addClassForTaskAnswer(task, user_index, 'answer-user-right');
            } else {
                scores -= substracted_scores(problems[problem_id - 1].scores);
                wrong_answers += 1;
                if (pr.is_dynamic)
                    answer_controls.removeClass('answer-already-given'); //this is made to set color and nothing more
                else
                    addClassForTaskAnswer(task, user_index, 'answer-user-wrong');
            }
        } else {
            if (pr.is_dynamic)
                answer_controls.removeClass('answer-already-given'); //this is made to set color and nothing more
            skipped_answers ++;
        }

        //display
        if (pr.is_dynamic) {
            var user_text;
            if (user_index == 5)
                user_text = 'Ответ не дан';
            else if (user_index == ans_right)
                user_text = 'Дан правильный ответ';
            else
                user_text = 'Дан неправильный ответ';
            answer_controls.find('.dyn-answer-info').text(user_text);

            if (user_index == 5)
                answer_controls.find('.dyn-give-answer').hide();
            else
                answer_controls.find('.dyn-give-answer').val('Показать ответ участника');
        } else {
            if (! is_printing())
                task.find('.task-answer-selector-' + ans_right).find('.task-answer-selector').addClass('answer-right');
            task.find('.task-explanation').before("<div class='section-title'>Правильный ответ: " + answer_index_to_letter(ans_right) + "</div>");
        }
    });

    //Проскроллить все задачи до самого низа
    if (status == "open") {
        $('.task').each(function() {
            this.scrollTop = this.scrollHeight;
        });
    }

    if (scores < 0)
        scores = 0;

    $('#res-scores').text(scores + ' ' + scores_text(scores));
    $('#res-right').text(right_answers);
    $('#res-wrong').text(wrong_answers);
    $('#res-skipped').text(skipped_answers);

    $('.flash-blocker').hide();

    solutions_shown = true;
}

function normalize_assets_images_links(taskDiv, assetsUrl) {
    taskDiv.find('img').each(function() {
        var img = $(this);
        var src = img.attr('src');
        img.attr('src', assetsUrl + src);
    });
}

var status = null;
var startTime = null;
var duration = null;
var problems = null;
var user_id = -1;
var initial_answers = [];
var initial_answers_dyn = [];

function wrap_dynamic(problem_id, element, assets) {
    var dyn = element.find(".dynamic");
    var swf_info = dyn.text();
    if (!swf_info)
        return;
    swf_info = swf_info.split('|');
    var swf = swf_info[0];
    var swf_size = swf_info[1];
    swf_size = swf_size.split('x');

    var swf_wrapper_id = 'dynamic-app-wrapper-' + problem_id;
    var swf_id = 'dynamic_app_' + problem_id;

    dyn.attr('id', swf_wrapper_id);

    var pr = problems[problem_id - 1];
    var dynamic_data = '';
    if (status != "open") {
        if (problem_id in initial_answers)
            dynamic_data = initial_answers_dyn[problem_id];
        else if ('dynamic_data' in pr)
            dynamic_data = pr.dynamic_data;

        if (dyn_data_is_null(dynamic_data))
            dynamic_data = '';
    }

    if (element.find('.task-answer').size() == 0) //if there are no usual answers
        pr.is_dynamic = true;

    var swf_width = + $.trim(swf_size[0]);
    var swf_height = + $.trim(swf_size[1]);
    swfobject.embedSWF(
        assets + swf,
        swf_wrapper_id,
        + swf_width, //(!) "100%"
        + swf_height, //(!) "100%"
        '9.0.0',
        '',
        {data: dynamic_data}, //data sent to flash player
        {allowScriptAccess:'always', quality:'high', wmode:'transparent'},
        {id: swf_id}
    );
    $('#' + swf_id).addClass('dynamic-app-wrapper').addClass('for-problem-' + problem_id);

//(!)    var size_wrapper = $("<div></div>");
//(!)    size_wrapper.css("width", swf_width + 'px');
//(!)    size_wrapper.css("height", swf_height + 'px');
//(!)    $('#' + swf_id).addClass('dynamic-app-wrapper').addClass('for-problem-' + problem_id).wrap(size_wrapper);
}

function fill_problems_data(problems, problems_holder) {
    $.each(problems, function (ind, problem) {
        var taskDiv = $('<div class="task" style="display: none"></div>'); //class hidden does not work because there is more specific css with display block

        var taskTitle = $('<div class="task-title"></div>');
        taskTitle.html(problem.title);

        var taskScores = $('<div class="task-scores"></div>"');
        taskScores.html(problem.scores);

        var taskCountry = $('<div class="country"></div>');
        taskCountry.html(problem.country);

        var taskStatement = $('<div class="task-statement"></div>');
        taskStatement.html(problem.statement);

        var taskQuestion = $('<div class="task-question"></div>');
        taskQuestion.html(problem.question);

        var is_dynamic = problem.answers === null;
        var taskAnswers;
        if (! is_dynamic) {
            taskAnswers = $('<div class="task-answers"></div>');
            taskAnswers.addClass('answers-layout-' + problem.answersLayout);
            for (var ans = 0; ans < 4; ans++) {
                var taskAnswer = $('<div class="task-answer"></div>');
                taskAnswer.html(problem.answers[ans]);
                taskAnswers.append(taskAnswer);
            }
        } else
            taskAnswers = $('<div class="task-dynamic-answers"></div>');

        var taskCorrectAnswer = $('<div class="correct-answer"></div>');
        if ('correctAnswer' in problem)
            taskCorrectAnswer.html(problem.correctAnswer);

        var taskExplanation = $('<div class="task-explanation"></div>');
        if ('explanation' in problem)
            taskExplanation.html(problem.explanation);

        taskDiv
            .append(taskTitle)
            .append(taskScores)
            .append(taskCountry)
            .append(taskStatement)
            .append(taskQuestion)
            .append(taskAnswers)
            .append(taskCorrectAnswer)
            .append(taskExplanation);

        if (is_printing()) {
            var divider = "<div class='problems-divider'>";
            taskDiv.prepend(divider);
            taskDiv.append(divider);
            taskCorrectAnswer.before(divider);
            taskCorrectAnswer.before(divider);
            taskCorrectAnswer.before(taskTitle.clone());
        }

        normalize_assets_images_links(taskDiv, problem.assets);

        problems_holder.append(taskDiv);

        problem.index = ind + 1;
        problem.is_dynamic = false; //will be set to true in wrap_dynamic if needed

        wrap_dynamic(ind + 1, taskDiv, problem.assets);
    });

    wrap_problem_headers();
    wrap_problem_scores();
    wrap_problem_questions();
    wrap_problem_answers();
    wrap_problem_dynamic_answers();
    wrap_problem_explanations();
    wrap_problem_flags();
}

function set_user_answers(problems) {
    for (var i = 1; i <= problems.length; i++) {
        var problem = problems[i - 1];

        if (problem.is_dynamic) //dynamic answers are loaded with the problems
            continue;

        var ans = null; //no ans set initially

        if (i in initial_answers)
            ans = initial_answers[i];
        else if ('user_answer' in problem)
            ans = problem.user_answer;

        if (ans != null) {
            var answer_selector = $('.page-problem-' + i).find('.task-answer-selector-' + ans).find('.task-answer-selector');
            select_answer_selector(answer_selector);
        }
    }
}

function problems_loaded(data/*, textStatus, jqXHR*/) {
    if ("user" in data)
        user_id = data.user;

    //restore answers
    load_list();
    fill_initial_answers();
    if (answers_list.length > 0)
        send_answers_now();

    var problems_holder = $('#problems-holder');

    //TODO don't use old code for loading problems

    if ('problems' in data) {
        problems = data.problems;
        fill_problems_data(data.problems, problems_holder);
    }

    problems_holder.append($('#finish-task'));

    problems_holder.splitPages('task', $('#main'), null, is_printing());

    $('.task').show();

    //set user answers
    if (problems && status != "open")
        set_user_answers(problems);

    status = data.status;
    if (status === "contest") {
        startTime = new Date().getTime() - data.passed;
        duration = data.duration;
        timer();
    } else if (status === "results")
        show_solutions();
    else if (status === "over") {
        initial_answers = []; //if user failed to send his answers even after the contest, then they are no more taken account
        if (hasLocalStorage())
            localStorage.removeItem(local_storage_key());
        stop_contest();
    }

    $('#problems-loader-holder').hide();

    var problem_id_to_show = + $('#problem-id-to-show').text();
    if (problem_id_to_show) {
        stop_contest();
        $($(".switcher").find(".swShowPage").get(problem_id_to_show)).click();
    }
}

function problems_failed(/*jqXHR, textStatus, errorThrown*/) {
    $('#problems-loader-holder').text("Не удалось загрузить задачи, попробуйте обновить страницу.");
}

function show_wait_for_results() {
    $('#finish-confirmation').hide();
    $('#button-finish').hide();
    $('#finish-text').hide();
    $('#wait-for-results-text').show();
    $('.bm-finish').click();
    $('.swShowPage:not(.active)').hide();
}

function stop_contest() {
    $('#clock').text('Соревнование окончено.');
    if (status == "open" || status == "results") //no duration means that we show results
        show_solutions();
    else
        show_wait_for_results();
}

function fill_initial_answers() {
    initial_answers = [];
    initial_answers_dyn = [];
    for (var i = 0; i < answers_list.length; i++) {
        var ans = answers_list[i];
        initial_answers[ans.p] = ans.a;
        if ("d" in ans)
            initial_answers_dyn[ans.p] = ans.d;
    }
}

//Когда в $() передают функцию, это то же самое, что установить обработчик для document.ready
$(function () {

    $.ajax({
        url: $('#problems-url').text(),
        type: 'POST',
        /*dataType: 'json',
        data: '{"t": ' + time + '}',
        processData: false,
        contentType: 'application/json; charset=UTF-8',*/
        success: problems_loaded,
        error: problems_failed
    });

    //Завершение соревнования
    function restore_finish_button() {
        $('#finish-confirmation').hide();
        $('#button-finish').removeClass('disabled-button');
    }

    //Кнопка заверешния
    $('#button-finish').click(function() {
        var button_finish = $('#button-finish');
        if (button_finish.hasClass('disabled-button'))
            return false;

        button_finish.addClass('disabled-button');

        $('#finish-confirmation').show();
    });

    //Кнопка отмены
    $('#finish-confirm-cancel').click(restore_finish_button);

    //Кнопка подтверждения
    $('#finish-confirm-yes').click(stop_contest);

    //показать и скрыть оставшееся время
    function toggle_contest_info() {
        $('#clock').toggle();
        $('#user-info').toggle();
        $('#grade-info').toggle();
        $('#hide-contest-info-link').toggle();
        return false;
    }

    var contest_info_wrapper = $('#contest-info-wrapper').find('img');

    contest_info_wrapper.click(toggle_contest_info);
    $('#hide-contest-info-link').click(toggle_contest_info);

    $('#answers-sending-info').find('a').click(function() {
        send_answers_now();
        return false;
    });

    var answers_sending_info_popupper = $('#answers-sending-info-popupper');

    answers_sending_info_popupper.mouseover(function() {
        var popupper = $(this);
        var w = popupper.find('.warning');
        var d = popupper.find('#answers-sending-info');
        w.hide();
        d.show();
    });

    answers_sending_info_popupper.mouseout(function() {
        var popupper = $(this);
        var w = popupper.find('.warning');
        var d = popupper.find('#answers-sending-info');
        w.show();
        d.hide();
    });
});

function timer() {
    var time = new Date().getTime() - startTime;

    var millisecondsLeft = duration * 60 * 1000 - time;
    if (millisecondsLeft <= 0) {
        stop_contest();
        return;
    }

    //print time left
    var seconds = Math.ceil(millisecondsLeft / 1000);
    var clock_time = $('#clock-time');
    if (seconds > 60) {
        var min = Math.ceil(seconds / 60);
        clock_time.text(min + ' мин.');
    } else {
        clock_time.text(seconds + ' сек.');
    }

    setTimeout(timer, 1000);
}

var send_fails_count = 0; //number of fails since the last success
var answers_list = []; //list of answers to send, is maintained for browsers without local storage
var sending_timeout_id = null; //timeout id, null if nothing is planned for sending

function escape_json(s) {
    return s;
}

function list_to_json(list) {
    var data = [];

    for (var i = 0; i < list.length; i++) {
        var a = list[i];
        var ans_json = '{"t": ' + a.t + ', "p": ' + a.p + ', "a": ' + a.a;
        if ('d' in a)
            ans_json += ', "d":"' + escape_json(a.d) + '"';
        ans_json += '}';
        data.push(ans_json);
    }

    return '[' + data.join(',') + ']';
}

function push_answer_to_list(answer) {
    answers_list.push(answer);
    if (hasLocalStorage())
        localStorage.setItem(local_storage_key(), list_to_json(answers_list));
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

function send_delay(fails) {
    var delay = fails * 5000;
    if (delay > 120 * 1000)
        delay = 120 * 1000; //not greater than two minutes
    return delay;
}

function answers_success() {
    clear_list();
    sending_timeout_id = null;
    send_fails_count = 0;
    $('#answers-sending-info').hide();
}

function undo_timeout() {
    if (sending_timeout_id != null)
        clearTimeout(sending_timeout_id);
}
function answers_error() {
    send_fails_count ++;
    undo_timeout();
    var delay = send_delay(send_fails_count);
    sending_timeout_id = setTimeout(send_answers_now, delay);
    $('#answers-sending-info-popupper').show().find('span').text(delay / 1000 + ' сек.');
}

function send_answers_now() {
    undo_timeout();
    $.ajax({
        url: $('#submit-url').text(),
        type: 'POST',
        dataType: 'json',
        data: list_to_json(answers_list),
        processData: false,
        contentType: 'application/json; charset=UTF-8',
        success: answers_success,
        error: answers_error
    });
}

function give_answer(problem_id, answer, dyn_data) {
    var time = new Date().getTime() - startTime;

    if (time >= duration * 60 * 1000) //don't send answers that are after the contest had finished
        return;

    var ans = {"t":time, "p":problem_id, "a":answer};
    if (dyn_data)
        ans.d = dyn_data;
    push_answer_to_list(ans);

    if (sending_timeout_id == null)
        send_answers_now();
}

// dynamic answers

function dyn_disable_all() {
    $('.flash-blocker').show();
}

function dyn_enable_all() {
    $('.flash-blocker').hide();
}

function dyn_disable(problem_id) {
    var el = $(dyn_get_element(problem_id));
    el.parent().find('.flash-blocker').show();
//(!)    el.parent().parent().find('.flash-blocker').show();
}

function dyn_enable(problem_id) {
    var el = $(dyn_get_element(problem_id));
    el.parent().find('.flash-blocker').hide();
//(!)    el.parent().parent().find('.flash-blocker').hide();
}

function dyn_get_element(problem_id) {
    return $('#dynamic_app_' + problem_id).get(0);
}

function dyn_data_is_null(data) {
    return data == 'null' || ! data;
}

function dyn_get_data(problem_id) {
    var data = dyn_get_element(problem_id).getData();
    //convert anything
    if (dyn_data_is_null(data))
        data = '';
    return data;
}

function dyn_set_data(problem_id, data) {
    dyn_get_element(problem_id).setData(data);
}

function dyn_get_answer(problem_id) {
    var result = dyn_get_element(problem_id).getResult();
    result = result.toLowerCase();
    switch (result) {
        case 'a':
            return 1;
        case 'b':
            return 2;
        case 'c':
            return 3;
        case 'd':
            return 4;
        case 'x':
            return 5; //no answer is 5 for us
        default:
            return + result;
    }
}

function dyn_reset(problem_id) {
    dyn_set_data(problem_id, '');
}

function local_storage_key() {
    return "answers-" + user_id;
}

function hasLocalStorage() {
//    return 'localStorage' in window && window['localStorage'] !== null;
    return typeof localStorage != 'undefined';
}

function is_printing() {
    return $('#print-marker').size() > 0;
}