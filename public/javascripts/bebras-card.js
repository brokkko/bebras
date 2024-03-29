$(function () {
    var canvas = $('.card-view').get(0);
    var ctx = canvas.getContext('2d');

    var SLOT_ELEMENTS = 5;

    var $loadingProgress = $('.loading-progress');

    var queue = new createjs.LoadQueue();
    queue.stopOnError = true;
    queue.on("complete", loadingComplete, this);
    queue.on("progress", handleProgress, this);
    queue.on("error", handleError, this);
    var bc = BEBRAS_CARD_INFO.bc;
    var base = bc.base_path;
    var basePic = base + 'Pictures/';

    var manifest = [{id: "bg", src: base + "bg.png"}, {id: "stamps", src: base + "stamps.png"}];

    var currentIndices = [0, 0, 0, 0, 0, 0];
    var correctIndices = [0, 0, 0, 0, 0, 0];
    var overIndex = -1;

    var animatingInd = -1;
    var fromImg;
    var toImg;
    var animationT = 0;

    fillManifestWithImages(manifest);

    var bgImg;
    var bgPattern;

    var $description = $('.pic-description');
    var $description_country = $('.pic-description .country');
    var $description_info = $('.pic-description .info');

    $('.card-info').width(BEBRAS_CARD_INFO.width);
    $('.winner-info .rotate').click(function () {
        rotate();
    });
    $('.winner-info .shuffle').click(function () {
        //do shuffle
        function rnd(a, b, x) {
            do {
                var r = Math.floor(Math.random() * (b - a + 1)) + a;
            } while (r == x);
            return r;
        }

        currentIndices = [
            rnd(0, 4, correctIndices[0]),
            rnd(0, 4, correctIndices[1]),
            rnd(0, 4, correctIndices[2]),
            rnd(0, 4, correctIndices[3]),
            rnd(0, 4, correctIndices[4]),
            rnd(0, 4, correctIndices[5])
        ];
        if (rnd(0, 1) == 1) {
            var r = rnd(0, 5);
            currentIndices[r] = correctIndices[r];
        }

        if (opposite)
            rotate();
        else
            drawMainSide();
    });
    $('.winner-info .view-solution').click(function () {
        currentIndices = correctIndices.slice();
        allSame = true;
        if (opposite)
            rotate();
        else
            drawMainSide();
    });

    var hint_level = 0;
    var $hints = $('.hints span');
    $hints.click(function () {
        var $hint = $(this);
        $hints.removeClass('selected');
        $hint.addClass('selected');
        if ($hint.hasClass('hint-none'))
            hint_level = 0;
        else if ($hint.hasClass('hint-description'))
            hint_level = 1;
        else
            hint_level = 2;

        //update hints view
        if (hint_level == 0) {
            $description_country.hide();
            $('.pic-description .colon').hide();
            $description_info.hide();
        } else if (hint_level == 1) {
            $description_country.hide();
            $('.pic-description .colon').hide();
            $description_info.show();
        } else {
            $description_country.show();
            $('.pic-description .colon').show();
            $description_info.show();
        }
    });

    function selectAndCopyViewLink() {
        var copyTextarea = $('.copy-view-to-url').get(0);
        copyTextarea.select();

        try {
            var successful = document.execCommand('copy');
            if (successful) {
                $('.link-copied').show().hide(2000);
            }
        } catch (err) {
            console.error('Oops, unable to copy the link');
        }
    }

    $('.copy-view-to-url').click(selectAndCopyViewLink);
    $('.copy-view-to-url-click').click(selectAndCopyViewLink);

    if (!win_immediately())
        $('.todo-info').show();

    function setDescription(country, info) {
        $description_country.text(country);
        $description_info.text(info);
    }

    function win_immediately() {
        return !BEBRAS_CARD_INFO.win_url;
    }

    queue.loadManifest(manifest);
    function loadingComplete() {
        if (!error) {
            $loadingProgress.hide();
            $('.hints').show();
            initCard();
            if (win_immediately())
                win();
        }
    }

    function handleProgress(e) {
        var pr = Math.round(e.progress * 100);
        $loadingProgress.text('Загрузка открытки ' + pr + '%...');
    }

    var error = false;

    function getSlot(slotInd, ind) {
        var slotInfo = bc.slots[slotInd][ind];
        var folder = slotInfo.f;
        return {
            "f": folder,
            "n": bc.all_countries[folder],
            "d": slotInfo.img.descr,
            "file": basePic + folder + '/' + slotInfo.img.file
        };
    }

    var x0 = Math.round((BEBRAS_CARD_INFO.width - BEBRAS_CARD_INFO.img_size * 3) / 2);
    var y0 = Math.round((BEBRAS_CARD_INFO.height - BEBRAS_CARD_INFO.img_size * 2) / 2);

    function getSlotCords(slotInd) {
        var ii = slotInd % 3;
        var jj = (slotInd - ii) / 3;

        return {x: x0 + ii * BEBRAS_CARD_INFO.img_size, y: y0 + jj * BEBRAS_CARD_INFO.img_size};
    }

    function fillManifestWithImages(manifest) {
        for (var i = 0; i < 6; i++)
            for (var j = 0; j < SLOT_ELEMENTS; j++) {
                var slot = getSlot(i, j);
                manifest.push({
                    id: 's' + i + j,
                    src: slot.file
                });
            }
    }

    function handleError(e) {
        error = true;
        $loadingProgress.html('Ошибка загрузки открытки, попробуйте <a href="javascript:window.location.href=window.location.href">обновить</a> страницу.');
        console.error('title', e.title);
        console.error('message', e.message);
    }

    function draw_bg() {
        var w = canvas.width;
        var h = canvas.height;

        ctx.fillStyle = bgPattern;
        ctx.fillRect(0, 0, w, h);
    }

    function initCard() {
        bgImg = queue.getResult("bg");
        bgPattern = ctx.createPattern(bgImg, 'repeat');

        $('.todo-info').hide();

        draw_bg();

        for (var ind = 0; ind < 6; ind++)
            updateCell(ind);
        updateCell(-1);

        canvas.addEventListener('mousemove', handleMouseMove);
        canvas.addEventListener('click', handleMouseClick);

        draw_header();
    }

    function pointInCell(p, ind) {
        var sc = getSlotCords(ind);
        return p.x >= sc.x &&
            p.x <= sc.x + BEBRAS_CARD_INFO.img_size &&
            p.y >= sc.y &&
            p.y <= sc.y + BEBRAS_CARD_INFO.img_size;
    }

    function pos2cell(p) {
        for (var i = 0; i < 6; i++)
            if (pointInCell(p, i))
                return i;
        return -1;
    }

    function allCountriesAreSame() {
        var a = [];
        for (var i = 0; i < 6; i++) {
            var slot = getSlot(i, currentIndices[i]);
            a.push(slot.f);
        }

        for (i = 1; i < 6; i++)
            if (a[0] != a[i])
                return false;
        return true;
    }

    var allSame = false;
    var won = false;
    var rightCountryName = '';

    function draw_header() {
        if (allCountriesAreSame()) {
            ctx.font = "40px 'Arial', sans-serif";
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillStyle = '#666';
            ctx.fillText(rightCountryName, BEBRAS_CARD_INFO.width / 2, 45 / 2);

            ctx.fillStyle = '#444';
            ctx.font = "20px 'Arial', sans-serif";
            ctx.fillText('Страна международного конкурса по Информатике «Бобёр ' + BEBRAS_CARD_INFO.year + '»', BEBRAS_CARD_INFO.width / 2, BEBRAS_CARD_INFO.height - 45 / 2);
            $('.view-solution-container').hide();
        } else {
            //draw bottom info
            ctx.fillStyle = '#444';
            ctx.font = "20px 'Arial', sans-serif";
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillText('Соберите на открытке фотографии из одной страны...', BEBRAS_CARD_INFO.width / 2, BEBRAS_CARD_INFO.height - 45 / 2);
            $('.view-solution-container').show();
        }
    }

    function win() {
        if (!win_immediately()) {
            var win_url = BEBRAS_CARD_INFO.win_url;
            win_url = win_url.substr(0, win_url.length - 'post_p'.length) + currentIndices.join('') + '/post_p';
            won = true;
            $.post(win_url);
        }
        allSame = true;
        correctIndices = currentIndices.slice();

        rightCountryName = getSlot(0, currentIndices[0]).n;

        drawMainSide();

        $('.winner-info').show();
        // $('.winner-info .country').text(rightCountryName);
        $('.todo-info').hide();
    }

    function increaseCell(ind) {
        var old_ind = currentIndices[ind];
        currentIndices[ind]++;
        if (currentIndices[ind] >= SLOT_ELEMENTS)
            currentIndices[ind] = 0;

        var allSameNow = allCountriesAreSame();
        if (allSameNow)
            win();

        if (animatingInd != -1)
            updateCell(animatingInd);

        animatingInd = ind;
        animationT = 0;
        fromImg = getImg(ind, old_ind);
        toImg = getImg(ind, currentIndices[ind]);

        if (allSameNow != allSame)
            drawMainSide();
        allSame = allSameNow;

        requestAnimationFrame(animate);
    }

    function animate() {
        if (animatingInd < 0)
            return;

        animationT += 1 / 60 / 0.5;

        if (animationT < 1) {
            requestAnimationFrame(animate);
            updateCellAnimated(animatingInd);
        } else {
            updateCell(animatingInd);
            animatingInd = -1;
        }
    }

    function getImg(ind, jnd) {
        return queue.getResult('s' + ind + jnd);
    }

    function overelay(ind, slotCords) {
        ctx.save();
        ctx.globalAlpha = ind == overIndex ? 0 : 0.3;
        ctx.globalCompositeOperation = 'multiply';
        ctx.fillStyle = bgPattern;
        ctx.fillRect(slotCords.x, slotCords.y, BEBRAS_CARD_INFO.img_size, BEBRAS_CARD_INFO.img_size);
        ctx.restore();
    }

    function updateDescription(ind) {
        var slot = getSlot(ind, currentIndices[ind]);
        setDescription(slot.n, slot.d);
    }

    function updateCell(ind) {
        if (ind < 0) {
            $description.hide();
            return;
        } else
            $description.show();

        var slotCords = getSlotCords(ind);
        var img = getImg(ind, currentIndices[ind]);
        ctx.drawImage(img, slotCords.x, slotCords.y);

        overelay(ind, slotCords);

        updateDescription(ind);
    }

    function updateCellAnimated(ind) {
        var slotCords = getSlotCords(ind);
        var is = BEBRAS_CARD_INFO.img_size;

        /* //"square rotate"
         var w = Math.round(is * animationT);
         ctx.drawImage(fromImg, slotCords.x, slotCords.y, is - w, is);
         ctx.drawImage(toImg, slotCords.x + is - w, slotCords.y, w, is);
         */
        /* // roll over
         var w = Math.round(is * animationT);
         ctx.drawImage(fromImg, 0, 0, is - w, is, slotCords.x, slotCords.y, is - w, is);
         ctx.drawImage(toImg, is - w, 0, w, is, slotCords.x + is - w, slotCords.y, w, is);
         */

        /* //rotate 180
         var w = Math.cos(animationT * Math.PI);
         ctx.drawImage(bgImg, slotCords.x, slotCords.y, is, is);
         var img = w > 0 ? fromImg : toImg;
         w = Math.abs(w);
         ctx.drawImage(img, slotCords.x + is / 2 * (1 - w), slotCords.y, w * is, is); //1 -> is / 2, 0 -> 0
         */

        /*
         var w = Math.sin(animationT * Math.PI / 2);
         w = Math.abs(w);
         ctx.drawImage(fromImg, slotCords.x, slotCords.y, is, is);
         ctx.drawImage(toImg, slotCords.x + is / 2 * (1 - w), slotCords.y, w * is, is); //1 -> is / 2, 0 -> 0
         */

        var n = 4;
        var aMid = 2 * Math.PI / n;
        var a0 = Math.PI / 2 + aMid / 2 + animationT * aMid;
        var a1 = Math.PI / 2 - aMid / 2 + animationT * aMid;
        var a2 = Math.PI / 2 - 3 * aMid / 2 + animationT * aMid;
        // sin aMid/2 = is/2 / r
        var r = is / 2 / Math.sin(aMid / 2);
        var x0 = r * Math.cos(a0) + is / 2;
        var x1 = r * Math.cos(a1) + is / 2;
        var x2 = r * Math.cos(a2) + is / 2;

        var rx1 = Math.round(x1);
        var w1 = Math.round(is * x1 / (x1 - x0));
        var w2 = Math.round(is * (is - x1) / (x2 - x1));
        ctx.drawImage(fromImg,
            is - w1, 0, w1, is,
            slotCords.x, slotCords.y, rx1, is
        );
        ctx.drawImage(toImg,
            0, 0, w2, is,
            slotCords.x + rx1, slotCords.y, is - rx1, is
        );

        overelay(ind, slotCords);
    }

    function handleMouseMove(evt) {
        var pos = getMousePos(evt);
        var ind = pos2cell(pos);
        if (overIndex != ind) {
            var oldOver = overIndex;
            overIndex = ind;

            if (oldOver >= 0)
                updateCell(oldOver);
            if (ind >= 0)
                updateCell(ind);
        }
    }

    function handleMouseClick(evt) {
        var pos = getMousePos(evt);
        var ind = pos2cell(pos);
        if (ind >= 0) {
            increaseCell(ind);
            updateDescription(ind);
        }
        // $('.todo-info').fadeOut(1000);
    }

    function getMousePos(evt) {
        var rect = canvas.getBoundingClientRect();
        return {
            x: evt.clientX - rect.left,
            y: evt.clientY - rect.top
        };
    }

    //opposite side

    var opposite = false;

    function rotate() {
        opposite = !opposite;
        if (opposite)
            drawBackSide();
        else
            drawMainSide();
    }

    var eventListenersAdded = false;

    function drawMainSide() {
        draw_bg();

        for (var ind = 0; ind < 6; ind++)
            updateCell(ind);
        updateCell(-1);

        if (!eventListenersAdded) {
            canvas.addEventListener('mousemove', handleMouseMove);
            canvas.addEventListener('click', handleMouseClick);
            eventListenersAdded = true;
        }
        $('.winner-container').hide();

        draw_header();
    }

    function drawBackSide() {
        draw_bg();

        canvas.removeEventListener('mousemove', handleMouseMove);
        canvas.removeEventListener('click', handleMouseClick);
        eventListenersAdded = false;

        $('.winner-container').fadeIn(500);

        updateCell(-1);

        ctx.drawImage(queue.getResult("stamps"), 0, 0);
    }
});