/**
 * Иногда ответы выходят за грань дозволенного пространства
 * Усмиряет их эта функция
 **/
var adjust_answer_width = function () {
    return; // Пока не нужна
    var $answer_container = $('#holder .task .answers');
    var info = '';
    $answer_container.each(function () {
        var $answer_container = $(this);
        var answer_container_width = $answer_container.width();
        info += ', ' + answer_container_width
        if (answer_container_width > 740) {
            $answer_container.find('.answer').each(function () {
                $(this).append('<br />');
            })
        }
    });
}

// Подгоняем по высоте все контейнеры с задачами,
// чтобы не было пустых ччерных полос.
var adjust_task_height = function () {
    var $div_exercises = $("div.task");
    // Этот кусок кода был предназначен для того, чтобы выровнять задачи по высоте.
    // Оставлю пока на всякий случай
    /*var exercise_max_height = 0;
     $div_exercises.each( function() {
     var current_height = $(this).height();
     if (current_height > exercise_max_height)
     exercise_max_height = current_height;
     });*/

    var window_height = $(window).height();
    var holder_height = $('#holder').height();
    //var task_height = $("div.task").first().height();

    //Дальше магические константы!!!!
    holder_height = window_height - 280 + 140;
    /* 250 (изменено на 280 с учетом opera 10.09.12) подобрал опытным путем, а 140 из файла styles.css */
    $('#holder').height(holder_height);
    $div_exercises.height(holder_height - 50);

    //Очень важная строка!!!! При подстройке высоты задачи обязательно надо поправить и ширину
    adjust_answer_width();
}

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

/**
 * Для элемента el, имеющего класс вида task-answer-selector-N определяет N
 * @param el Элемент с классом вида task-answer-selector-N
 * @return {int} N
 */
function answer_element_to_answer_index(el) {
    var start = 'task-answer-selector-';
    var cls = getClassThatStartsWith(el.parent(), start);
    return + cls.substring(start.length);
}

/**
 * Это действие оборачивает все Заголовки задач, т.е. элементы с классом task-title. Возможно, этого же можно достичь через CSS
 */
function wrap_problem_headers() {
    var all_titles = $('.task-title');

    all_titles.each(function() {
        var title = $(this);
        title.text('Задача "' + title.text() + '"');
    });
    all_titles.addClass('section-title');
}

/**
 * Вставляет слово "Вопрос" перед всеми вопросами к задачам
 */
function wrap_problem_questions() {
    $('.task-question').each(function() {
        $(this).before("<div class='section-title task-question-title clear'>Вопрос</div>");
    });
}

/**
 * Аккуратно располагает на странице ответы к задаче, т.е. элементы с классом task-answers
 * Для выбора расположения используется класс answers-layout-XXX, который назначен этому же элементу
 */
function wrap_problem_answers() {
    $('.task-answers').each(function() {
        $(this).before("<div class='section-title task-answers-title clear'>Варианты ответов</div>")
    });

    //добавить ответ "без ответа"
    var all_answers_wrappers = $('.task-answers').append("<div class='task-answer'>Без ответа</div>");

    //цикл по блокам ответов всех задач
    all_answers_wrappers.each(function() {
        var answers_wrapper = $(this);
        var needed_frame = getClassThatStartsWith(answers_wrapper, 'answers-layout-');

        var new_answers_wrapper = $('#' + needed_frame).clone();
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
        var is_active = ans.hasClass('active');
        if (is_active)
            return;

        ans.parents('.task-answers').find('.task-answer-selector').removeClass('active');
        ans.removeClass('over').addClass('active');
//        alert(answer_element_to_answer_index(ans)); //вернет номер нажатого ответа
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

/**
 * Добавляет слово решение перед всеми решениями задач и скрывает их первоначально
 */
function wrap_problem_explanations() {
    $('.task-explanation').hide();

    $('.task-explanation').each(function() {
        $(this).prepend("<div class='section-title task-explanation-title clear'>Решение</div>")
    });
}

/**
 * Преобразует код страны в русское название
 * @param code код страны
 * @return {String} название по-русски
 */
function country_code_to_name(code) {
    if (code == 'de')
        return "Германия";
    else if (code == 'at')
        return "Австрия";
    else if (code == 'lt')
        return "Литва";
    else if (code == 'ch')
        return "Швейцария";
    else if (code == 'cz')
        return "Чехия";
    else if (code == 'nl')
        return "Нидерланды";
    else if (code == 'sk')
        return "Словакия";
    else
        return "";
}

/**
 * Обрабатывает информацию о флагах, находящуюся в div.country
 */
function process_flags() {
    $('.task').each(function() {
        var task = $(this);
        var country = task.find('.country').text();

        var name = country_code_to_name(country);
        if (!name)
            return;

        var info = $("<span class='country-info'></span>");
        var img = $("<img>");
        img.attr('alt', country);
        img.attr('src', '/assets/images/flags/' + country.toUpperCase() + '.png');
        info.text(name);
        info.prepend(img);
        task.find('.task-title').append(info);

        //У швейцарии квадратный флаг. Это нужно из-за IE, потому что он при масштабировании картинок,
        //если в css указать только высоту картинки, оставит ширину неизменной. Другие браузеры при уменьшении
        //высоты картинки пропорционально уменьшают ширину. Поэтому для изменения размеров картинки указаны и высота,
        //и ширина, и для швейцарии отдельно надо позабоиться о квадратности флага
        //Проблему можно вообще избежать, если изначально все картинки с флагами сделать нужного размера
		if (country == 'ch')
			img.addClass('sq');
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

/**
 * Изменяет режим на показывание баллов, заодно вычисляет набранные баллы
 */
function show_solutions() {
    $('#finish-confirmation').hide();
    $('#button-finish').hide();
    $('.task-explanation').show();
    $('#finish-text').hide();
    $('#results-text').show();

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
        task.find('.task-answer-selector-' + ans_right).find('.task-answer-selector').addClass('answer-right');

        var user_selection = task.find('.active');
        var user_index = answer_element_to_answer_index(user_selection);
        if (user_index != 5) {
            if (user_index == ans_right) {
                scores += 3;
                right_answers += 1;
                addClassForTaskAnswer(task, user_index, 'answer-user-right');
            } else {
                scores -= 1;
                wrong_answers += 1;
                addClassForTaskAnswer(task, user_index, 'answer-user-wrong');
            }
        } else
            skipped_answers ++;
        task.find('.task-explanation').before("<div class='section-title'>Правильный ответ: " + answer_index_to_letter(ans_right) + "</div>");
    });

    //Проскроллить все задачи до самого низа
    $('.task').each(function() {
        this.scrollTop = this.scrollHeight;
    });

    if (scores < 0)
        scores = 0;

    $('#res-scores').text(scores + ' ' + scores_text(scores));
    $('#res-right').text(right_answers);
    $('#res-wrong').text(wrong_answers);
    $('#res-skipped').text(skipped_answers);
}

//Когда в $() передают функцию, это то же самое, что установить обработчик для document.ready
$(function () {

    //split tasks on pages
    $('#holder').splitPages('task', $('#main'), restore_finish_button);

    adjust_task_height();

    wrap_problem_headers();
    wrap_problem_questions();
    wrap_problem_answers();
    wrap_problem_explanations();
    process_flags();

    $('#finish').click(function () {
        $(".correct").show();
        $("#finish").hide();
        adjust_task_height();

        var answers_ok = 0;
        $(".task").each(function () {
            var $task = $(this);
            var task_id = $task.attr('id');
            var correct_answer = $task.find('#correct_num_' + task_id).text();
            correct_answer = parseInt(correct_answer);
            var $radio_correct = $task.find('#answer_' + task_id + '_' + correct_answer);
            if ($radio_correct.attr('checked') == true) {
                $task.find('.correct').css('background-color', '#090');
                answers_ok++;
            } else {
                $task.find('.correct').css('background-color', '#900');
                $task.find('.correct').css('color', '#FFF');
                $task.find('.correct').css('font-weight', 'bold');
            }
        });

        // Совсем не спеша прокручиваем к правильному ответу
        $("div.task").animate({scrollTop : 100500}, 10000);

        if (answers_ok == 9) {
            alert('Поздравляем! Вы ответили верно на все 9 тренировочных задач!');
        } else {
            alert('Правильных ответов: ' + answers_ok + ' из 9. Пролистайте задачи, внизу Вы найдете правильные ответы. Неверные ответы отмечены красным.');
        }

    });

    $(window).resize(adjust_task_height);

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
        //hide again after 3 secs
    });

    //Кнопка отмены
    $('#finish-confirm-cancel').click(restore_finish_button);

    //Кнопка подтверждения
    $('#finish-confirm-yes').click(show_solutions);

});