(function ($) {
    $.fn.splitPages = function (pages_class, switchers_container, extra_page_action) {
        var tasks = this;
        var task = tasks.find('.' + pages_class);

        var switcher = $('<div class="switcher">');

        var i = 0;
        var problem_ind = 1;
        task.each(function () {
            var el = $(this);
            el.data('height', el.outerHeight(true));

            task.slice(i, i + 1).wrapAll('<div class="swPage" />');
            var page_title = el.find('.page-link').text();
            if (!page_title)
                page_title = '<b>' + (problem_ind ++) + '</b>';

            switcher.append('<a href="" class="swShowPage"><span class="-info hidden">' + i + '</span>' + page_title + '</a>');

            i++;
        });


        switchers_container.append(switcher);

        var maxHeight = 0;
        var totalWidth = 0;

        var swPage = tasks.find('.swPage');
        swPage.each(function () {
            var elem = $(this);
            var tmpHeight = 0;
            elem.find('.task').each(function () {
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

        var hyperLinks = switchers_container.find('a.swShowPage');

        hyperLinks.click(function (e) {
            $(this).addClass('active').siblings().removeClass('active');
            var page_num = parseInt($(this).find('.-info').text());
            swSlider.stop().animate({'margin-left': - page_num * tasks.width()}, 'slow');
            e.preventDefault();
            if (extra_page_action)
                extra_page_action();
        });

        hyperLinks.eq(0).addClass('active');

        swPage.each(function () {
            var elem = $(this);
            elem.attr("id", "id");
        });

        /*switcher.css({
            'left' : '50%',
            'margin-left' : - switcher.width() / 2
        });*/
		switcher.css({
			'left' : (switchers_container.width() - switcher.width()) / 2
		});

        return this;
    }
})(jQuery);