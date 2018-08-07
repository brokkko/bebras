$(function () {
    var $form = $("form.basic-form");

    $form.submit(function () {
        $(this).find("input[type='submit']").attr("disabled", "disabled");
        return true;
    });

    $form.find('.form-undo').click(function () {
        window.location.reload();
    });

    $form.find('.hint-provider')
        .focusin(show_hint)
        .focusout(hide_hint);

    function show_hint(e) {
        var $el = $(this);
        var hint_text = $el.data('hint');
        console.log('showing hint ' + hint_text);
        if (!hint_text)
            return;
        var $hint = $('<div>');
        var el_pos = $el.position();
        $hint.css('top', el_pos.top + 'px');
        $hint.css('left', el_pos.left + $el.outerWidth() + 'px');
        $hint.addClass('hint-popup');
        $hint.html(hint_text);
        $el.parent().append($hint);
    }

    function hide_hint(e) {
        var $el = $(this);
        $el.parent().find('.hint-popup').remove();
    }
});