$(function () {
    var $form = $("form.basic-form");

    $form.submit(function () {
        $(this).find("input[type='submit']").attr("disabled", "disabled");
        return true;
    });

    $form.find('.form-undo').click(function () {
        window.location.reload();
    });
});