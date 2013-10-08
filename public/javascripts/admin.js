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
});