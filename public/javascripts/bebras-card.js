$(function() {
    var canvas = $('.card-view').get(0);
    var ctx = canvas.getContext('2d');

    var $loadingProgress = $('.loading-progress');

    var queue = new createjs.LoadQueue();
    queue.stopOnError = true;
    queue.on("complete", loadingComplete, this);
    queue.on("progress", handleProgress, this);
    queue.on("error", handleError, this);
    var base = '/~plugin/BebrasCards/';
    queue.loadManifest([
        {id: "bg", src: base + "bg.png"},
        {id: "ex1", src: base + "Pictures/Cyprus/Cyprus_1.jpg_resize.jpg"}
    ]);

    function loadingComplete() {
        if (!error) {
            $loadingProgress.hide();
            initCard();
        }
    }

    function handleProgress(e) {
        var pr = Math.round(e.progress * 100);
        $loadingProgress.text('Загрузка открытки ' + pr + '%...');
    }

    var error = false;

    function handleError(e) {
        error = true;
        $loadingProgress.html('Ошибка загрузки открытки, попробуйте <a href="javascript:window.location.href=window.location.href">обновить</a> страницу.');
        console.error('title', e.title);
        console.error('message', e.message);
    }

    function initCard() {
        var bgImg = queue.getResult("bg");
        var w = canvas.width;
        var h = canvas.height;

        ctx.fillStyle = ctx.createPattern(bgImg, 'repeat');
        ctx.fillRect(0, 0, w, h);


    }
});