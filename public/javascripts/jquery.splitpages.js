(function ($) {
    $.fn.splitPages = function(pages_class, switchers_container, extra_page_action) {
        var tasks = this;
        tasks.css('overflow', 'hidden');
        var task = tasks.find('.' + pages_class);
        task.show();

        var switcher = $('<div class="switcher">');

        var i = 0;

        var page_selectors = switchers_container.find('.page-selector'); //TODO generalize class

        task.each(function () {
            var el = $(this);
            el.data('height', el.outerHeight(true));

            task.slice(i, i + 1).wrapAll('<div class="swPage" />');
            $(page_selectors.get(i)).append("<span class='-info hidden'>" + i + "</span>");

            i++;
        });


        switchers_container.append(switcher);

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

        swPage.wrapAll('<div class="swSlider" />');

        tasks.height(maxHeight);

        var swSlider = tasks.find('.swSlider');
        swSlider.append('<div class="clear" />').width(totalWidth);

        page_selectors.click(function (e) {
//            $(this).addClass('active').siblings().removeClass('active');
            var page_num = parseInt($(this).find('.-info').text());
            swSlider.stop().animate({'margin-left': - page_num * tasks.width()}, 'slow');
            e.preventDefault();
            if (extra_page_action)
                extra_page_action();
        });

        swPage.each(function () {
            var elem = $(this);
            elem.attr("id", "id");
        });

        return this;
    }
})(jQuery);