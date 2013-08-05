$(function () {
    var configureText = 'Настроить...';
    var hideText = 'Спрятать настройки...';

    var $contestAdmin = $('.contest-administration-show-hide');
    var $shower = $contestAdmin.find('.shower');

    $shower.text(configureText);
    $shower.click(function () {
        var $this = $(this);
        var $config = $this.parent().find('.contest-administration');
        $config.toggle(200);
        if ($this.text() == configureText)
            $this.text(hideText);
        else
            $this.text(configureText);
    });

    //don't hide those elements that are marked with dont-hide-me
    $contestAdmin.each(function() {
        var $this = $(this);
        var $ca = $this.find('.contest-administration');
        if ($ca.hasClass('dont-hide-me')) {

            $shower = $this.find('.shower');
            var $config = $this.parent().find('.contest-administration');
            $config.show();
            $shower.text(hideText);

            var parent = $this.parent('.contest-description').find('.big-title');
            console.log(parent.offset().top);
            window.scrollTo(0, parent.offset().top);
        }
    });
});
