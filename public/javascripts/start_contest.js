$(function () {
    var testConnectionTime = +$('.test-connection-time').text();

    if (testConnectionTime <= 0) {
        hide_checkers();
        show_start_button();
        return;
    }

    var bebras_site_loaded = false;

    $.getJSON("/ping", function (data) {
        bebras_site_loaded = data && data.bebras == 4239;
        check();
    });

    var timerId = setTimeout(check, testConnectionTime);

    function check() {
        clearTimeout(timerId);

        hide_checkers();

        if (bebras_site_loaded)
            show_start_button();
        else
            show_no_internet_connection();
    }

    function hide_checkers() {
        $('.connection-gif').hide();
        $('.connection-text').hide();
    }

    function show_no_internet_connection() {
        $('.connection-failed').show();
        $('.connection-info').show();

        $('.detected-browser').text(whatBrowser());
    }

    function show_start_button() {
        $('.start-contest-button').show();
    }

    //from http://stackoverflow.com/questions/2400935/browser-detection-in-javascript
    function whatBrowser() {
        var ua = navigator.userAgent, tem,
            M = ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];
        if (/trident/i.test(M[1])) {
            tem = /\brv[ :]+(\d+)/g.exec(ua) || [];
            return 'IE ' + (tem[1] || '');
        }
        if (M[1] === 'Chrome') {
            tem = ua.match(/\bOPR\/(\d+)/);
            if (tem != null) return 'Opera ' + tem[1];
        }
        M = M[2] ? [M[1], M[2]] : [navigator.appName, navigator.appVersion, '-?'];
        if ((tem = ua.match(/version\/(\d+)/i)) != null) M.splice(1, 1, tem[1]);
        return M.join(' ');
    }
});