(function ($) {
    $.fn.splitPages = function(pages_class, switchers_container, extra_page_action) {
        var tasks = this;
        tasks.css('overflow', 'hidden');
        var task = tasks.find('.' + pages_class);
        task.show();

        var i = 0;

        var page_selectors = switchers_container.find('.page-selector'); //TODO generalize class .page-selector

        task.each(function () {
            var el = $(this);
            el.data('height', el.outerHeight(true));

            task.slice(i, i + 1).wrapAll('<div class="swPage" />');

            i++;
        });

        var maxHeight = 0;
        var totalWidth = 0;

        var swPage = tasks.find('.swPage');
        swPage.each(function () {
            var elem = $(this);
            var tmpHeight = 0;
            elem.find('.' + pages_class).each(function () {
                tmpHeight += $(this).data('height');
            });
            if (tmpHeight > maxHeight)
                maxHeight = tmpHeight;
            totalWidth += elem.outerWidth();
            elem.css('float', 'left').width(tasks.width());
        });

        swPage.wrapAll('<div class="swSlider" />').css('overflow', 'auto');

        var resize_action = function(){
            var $content = $('.content');
            var content_width = $content.width();
            var content_height = $content.height();
            swPage.width(content_width);
            swPage.height(content_height);

            //scroll to current problem
            var $active = page_selectors.filter('.active');
            $active.click();
        };

        tasks.height(maxHeight);

        var swSlider = tasks.find('.swSlider');
        swSlider.append('<div class="clear" />').width(totalWidth);

        page_selectors.click(function (e) {
            var page_num = parseInt($(this).find('.-info').text()); //TODO generalize class .-info
            swSlider.stop().animate({'margin-left': - page_num * tasks.width()}, 'slow');
            $('.content.auto-size').scrollTop(0);
            e.preventDefault();
            if (extra_page_action)
                extra_page_action();
        });

        resize_action();
        $(window).resize(resize_action);

        return this;
    }
})(jQuery);