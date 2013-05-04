var problems_count;
var problems_info = []; //{type -> "", initial_answer -> {}}

var solutions_loaders_registry = {};

function register_solution_loader(problem_type, loader) {
    solutions_loaders_registry[problem_type] = loader;
}

(function () {

    var current_page = 0;
    var pages_count;

    function page_selector_click() {
        var $this = $(this);
        var clicked_page_index = +$this.find('span').text();
        select_page(clicked_page_index);
    }

    function select_page(page) {
        if (page < 0 || page >= pages_count)
            return;

        var $selectors = $('.page-selectors');
        $selectors.children().removeClass('active');
        $selectors.each(function(){
            var $selector = $(this);
            var domPageSelector = $selector.children('.page-selector').get(page);
            $(domPageSelector).addClass('active');
        });

        //show a page
        var allPages = $('.page');
        var $current_page = $(allPages.get(current_page));
        var $new_page = $(allPages.get(page));
        $current_page.animate({
            opacity: 0
        }, 200, function() {
            $current_page.css('opacity', 1).hide();
            $new_page.css('opacity', 0).show().animate({
                'opacity': 1
            }, 200);
        });

        current_page = page;
    }



    $(function() {
        pages_count = $('.page').length;
        problems_count = $('.problem').length;

        $('.page-selector').click(page_selector_click);
        $('.page-back').click(function(){select_page(current_page - 1);});
        $('.page-forward').click(function(){
            select_page(current_page + 1);
        });
    });

})();