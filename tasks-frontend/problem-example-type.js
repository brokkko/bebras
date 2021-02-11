/* Чтобы реализовать новый тип задачи, нужно создать загрузчик задачи.
 Загрузчику передается div, в котором находится разметка задачи и объект answer с
 ответом.

 div с условием — это jquery объект, а не элемент DOM. Вероятно, здесь в будущем от jquery
 надо будет избавиться, лучше сразу написать код так, чтобы было неважно, это элемент DOM или
 это jquery-объект.

 Ответ answer. При первом запуске задачи у участника ответ будет null. При других запусках,
 answer может быть одним из тех ответов, которые участник успел дать раньше. Например,
 при перезагрузке страницы answer может быть последним данным ответом.
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

    // добавить слушатели нажатий на каждый из ответов
    const all_answers = document.getElementsByClassName('example-type-answer');
    for (let i = 0; i < all_answers.length; i++)
        all_answers[i].addEventListener('click', function (e) {
            let clicked_element = e.target;
            dces2contest.submit_answer(problem_id, {
                'r': clicked_element.innerText
            });

            //установить класс selected на выбранный ответ
            for (let i = 0; i < all_answers.length; i++)
                all_answers[i].classList.remove('selected');
            clicked_element.classList.add('selected');
        });
}

dces2contest.register_solution_loader('example-type', solution_loader);
