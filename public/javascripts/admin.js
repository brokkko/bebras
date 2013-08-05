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

        if (!confirm("Подтвердите выполнение операции: " + $this.attr("title")))
            return false;

        $('form.' + cls).submit();
        return false;
    });
});