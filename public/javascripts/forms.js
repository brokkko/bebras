$(function() {
    $("form.basic-form").submit(function() {
        $(this).find("input[type='submit']").attr("disabled", "disabled");
        return true;
    })
});