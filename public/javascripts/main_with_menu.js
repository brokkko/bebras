$(function () {
    var adjust_contents_size = function() {
        var window_height = $window.height();
        var header_height = $('header').outerHeight();
        var footer_height = $('footer').outerHeight();
        var content_header_height = $('.content-header').outerHeight();
        var content_footer_height = $('.content-footer').outerHeight();
        console.log(header_height, footer_height, content_footer_height);
        //padding: 0 0, margin: 4 4, border 2 2
        $('.content.auto-size').height(window_height - content_footer_height - content_header_height - 2 - 2 - 4 - 4 - footer_height - header_height);
    };

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

    var $window = $(window);
    adjust_contents_size();
    $window.resize(adjust_contents_size);
});
