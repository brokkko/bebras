function get_class_that_starts_with($element, class_start) {
    var classes = $element.attr('class').split(' ');
    for (var i = 0; i < classes.length; i++) {
        if (classes[i].indexOf(class_start) == 0)
            return classes[i];
    }
    return "";
}

$(function(){
    $('a.submit').click(function () {
        var $this = $(this);
        var cls = get_class_that_starts_with($this, 'actions-');

        if (!cls)
            return true;

        var $form = $('form.' + cls);

        var $extra_input = $form.find('.extra-value');
        var title = $this.attr("title");

        if ($extra_input.size() > 0) {
            var extra_value = prompt(title, $extra_input.val());
            if (extra_value == null || extra_value == "")
                return false;
            $extra_input.val(extra_value);
        } else if (!confirm("Подтвердите выполнение операции: " + title))
            return false;

        $form.submit();
        return false;
    });

    //TODO move somewhere else or rename
    $('.contest-action').click(function() {
        if (!confirm("Вы действительно хотите сбросить результаты?"))
            return false;
        $(this).parents('form').submit();
        return false;
    });

    var contestShortFormMessage = "Переключить на краткую форму соревнований";
    var contestFullFormMessage = "Переключить на полную форму соревнований";
    var $contestViewSwitcher = $('#contests-in-short-form');
    $contestViewSwitcher.text(contestShortFormMessage).click(function() {
        if ($contestViewSwitcher.text() == contestShortFormMessage) {
            $(".content").addClass('short-contests');
            $contestViewSwitcher.text(contestFullFormMessage);
        } else {
            $(".content").removeClass('short-contests');
            $contestViewSwitcher.text(contestShortFormMessage);
        }

        return false;
    })

});