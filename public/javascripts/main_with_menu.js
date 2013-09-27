$(function () {
    //allow info-boxes show and hide
    $(".info-box-cut-shower").click(function() {
        var $shower = $(this);
        var $cut = $shower.parent('.info-box').find('.info-box-cut');
        if ($shower.hasClass('cut-open')) {
            $cut.hide(200);
            $shower.removeClass('cut-open');
        } else {
            $cut.show(200);
            $shower.addClass('cut-open');
        }

    });

    $('.info-box.initially-open .info-box-cut-shower').click();
});
