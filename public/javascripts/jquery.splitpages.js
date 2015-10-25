(function ($) {
    $.fn.splitPages = function(pages_class, switchers_container, extra_page_action) {
        var tasks = this;
        tasks.css('overflow', 'hidden');
        var task = tasks.find('.' + pages_class);
        task.show();

        var i = 0;

        var page_selectors = switchers_container.find('.page-selector'); //TODO generalize class .page-selector

        task.wrap('<div class="swPage" />');

        var maxHeight = 0;

        var swPage = tasks.find('.swPage');
        swPage.each(function () {
            var elem = $(this);
            var tmpHeight = elem.find('.' + pages_class).outerHeight(true);
            if (tmpHeight > maxHeight)
                maxHeight = tmpHeight;
            elem.css('float', 'left').width(tasks.width());
        });

        swPage.wrapAll('<div class="swSlider" />').css('overflow', 'auto');

        tasks.height(maxHeight);

        var swSlider = tasks.find('.swSlider');
        swSlider.append('<div class="clear" />');

        page_selectors.click(function (e) {
            var page_num = parseInt($(this).find('.-info').text()); //TODO generalize class .-info
            go_to_page(page_num);
            e.preventDefault();
        });

        var go_to_page = function(page_num) {
            swSlider.stop().animate({'margin-left': - page_num * tasks.width()}, 'slow');
            $('.content.auto-size').scrollTop(0);
            if (extra_page_action)
                extra_page_action();
        };

        var resize_action = function(){
            var $content = $('.content');
            var content_width = $content.width();
            var content_height = $content.height();
            swPage.width(content_width);
            swPage.height(content_height);

            var totalWidth = 0;
            swPage.each(function () {
                totalWidth += $(this).outerWidth(true) + 1; // 1 to fight with rounding problems
            });

            swSlider.width(totalWidth);

            //scroll to current problem
            var $active = page_selectors.filter('.active');
            $active.click();
        };

        resize_action();
        $(window).resize(resize_action);

        return this;
    }
})(jQuery);