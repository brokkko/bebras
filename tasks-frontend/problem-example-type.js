//заворачиваем весь код в функцию, чтобы не создавать лишних глобальных объектов
(function () {
    /*
    В этом примере мы сделаем простейшую задачу, в которой можно выбирать один из вариантов ответа,
    а еще можно менять цвет фона. Последнее нужно, чтобы продемонстрировать, как в системе сохраняются
    данные, важные для участника, но не для жюри. В реальности это может использоваться и для персонализации
    интерфейса, и для того, чтобы участник сохранял промежуточные решения, чтобы потом к ним возвращаться.
    Другими словами, мы можем посылать серверу и данные для жюри (ответы к заданиям), и данные для себя.

    Далее по коду мы будем пользоваться глобальным объектом dces2contest, содержащим все необходимые
    API методы.

    Чтобы реализовать новый тип задачи, нужно создать загрузчик задачи.
    Загрузчику передается div, в котором находится разметка задачи и объект answer с
    ответом.

    Загрузчик задачи может быть вызван несколько раз для одного и того же div. Поэтому приходится
    самостоятельно определять, первый ли раз запускается загрузчик, в зависимости от этого решать,
    надо ли производить базовую инициализацию задачи, например, устанавливать слушатели.

    div с условием — это jquery объект, а не элемент DOM. Вероятно, здесь в будущем от jquery
    надо будет избавиться, лучше сразу написать код так, чтобы было неважно, это элемент DOM или
    это jquery-объект.

    Ответ answer. При первом запуске задачи у участника ответ будет null. При других запусках,
    answer может быть одним из тех ответов, которые участник успел дать раньше. Например,
    при перезагрузке страницы answer может быть последним ответом до перезагрузки.
    Объект answer - это один из ответов, который был передан вызовом метода dces2contest.submit_answer()
     */
    function solution_loader(problem_div, answer) {
        // получаем идентификатор задачи, потому что на странице потенциально может быть много задач.
        // Надо передавать именно тот problem_div, который был передан в loader. Независимо от того,
        // это элемент DOM или DOM в jquery.
        const problem_id = dces2contest.get_problem_index(problem_div);

        //если передан jquery объект, то надо достать из него обычный DOM
        if ('get' in problem_div)
            problem_div = problem_div.get(0);

        //проверяем, инициализировалсь ли задача ранее
        let is_initialized = problem_div.classList.contains('initialized');

        // перебираем все span с ответами и настраиваем их: устанавливаем слушателя,
        // ставим класс selected при необходимости
        const all_answers = problem_div.querySelectorAll('.example-type-answer');
        for (let i = 0; i < all_answers.length; i++) {
            let answer_span = all_answers[i];

            if (!is_initialized)
                answer_span.addEventListener('click', answer_click_listener);

            let need_class_selected = answer && answer.r === answer_span.innerText;
            if (need_class_selected)
                answer_span.classList.add('selected');
            else
                answer_span.classList.remove('selected');
        }

        //доведем инициализацию до конца
        if (!is_initialized) {
            //раскрасим кнопки выбора фона
            let all_bg_selectors = problem_div.querySelectorAll('.example-type-bg-selector');
            for (let i = 0; i < all_bg_selectors.length; i++) {
                let bg_selector = all_bg_selectors[i];
                bg_selector.style['background-color'] = bg_selector.innerText;
                bg_selector.addEventListener('click', bg_selector_click_listener);
            }

            //установим цвет фона из сохраненных данных
            let bg = dces2contest.get_problem_data(problem_id, 'bg');
            if (!bg)
                bg = all_bg_selectors[0].innerText;
            problem_div.style['background-color'] = bg;

            //укажем, что инициализация совершена, больше ее не нужно повторять.
            problem_div.classList.add('initialized');
        }

        //слушатели нажатий опишем отдельно
        function answer_click_listener(e) {
            let clicked_element = e.target;
            dces2contest.submit_answer(problem_id, {
                'r': clicked_element.innerText
            });

            //установить класс selected на выбранный ответ
            for (let i = 0; i < all_answers.length; i++)
                all_answers[i].classList.remove('selected');
            clicked_element.classList.add('selected');
        }

        function bg_selector_click_listener(e) {
            let clicked_element = e.target;
            let bg = clicked_element.innerText;
            dces2contest.save_problem_data(problem_id, 'bg', bg);
            problem_div.style['background-color'] = bg;
        }
    }

    dces2contest.register_solution_loader('example-type', solution_loader);

})();
