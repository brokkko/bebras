function ProblemExample(container, pictures) {

    function LightsProblem(container) {
        var $container = $('#' + container);
        var $table = $('<table border="1px" style="border-collapse: collapse; text-align: center"></table>');
        var places = ["В бутыке", "В стакане", "В кувшине"];
        var what = ["Молоко", "Лимонад", "Вода"];

        $table.css('-webkit-touch-callout', 'none');
        $table.css('-webkit-user-select', 'none');
        $table.css('-khtml-user-select', 'none');
        $table.css('-moz-user-select', 'none');
        $table.css('-ms-user-select', 'none');
        $table.css('user-select', 'none');

        $table.css('margin', '1em 0');

        var $header = $('<tr style="background-color: yellow"><td></td></tr>');

        for (var w = 0; w < what.length; w++) {
            var c = $('<td></td>>');
            c.html(what[w]);
            $header.append(c);
        }
        $table.append($header);
        for (var p = 0; p < places.length; p++) {
            c = $('<tr></tr>');
            var c2 = $('<td style="background-color: yellow"></td>');
            c2.html(places[p]);
            c.append(c2);
            for (w = 0; w < what.length; w++) {
                c2 = $('<td class="e e' + p + w + '"></td>');
                c2.css('width', '4.5em');
                c2.on("click", handleClick);
                c.append(c2);
            }
            $table.append(c);
        }

        $container.append($table);

        function handleClick() {
            if (!enabled)
                return;

            var $this = $(this);
            var h = $this.html();
            if (h == '?')
                h = 'Нет';
            else if (h == 'Нет')
                h = 'Да';
            else
                h = '?';
            $this.html(h);
        }

        var enabled = true;

        var $all_td = $table.find('.e');
        $table.find('td:not(.e)').css('padding', '0.2em 0.2em');

        this.reset = function () {
            $all_td.html('?');
        };

        this.isEnabled = function () {
            return enabled;
        };

        this.setEnabled = function (state) {
            enabled = state;

            if (enabled)
                $all_td.css('color', 'black');
            else
                $all_td.css('color', '#aaaaaa');
        };

        this.reset();

        var initCallback;

        this.setInitCallback = function (_initCallback) {
            initCallback = _initCallback;

            //we are initialized just after creation, so any attempt to set up init callback is after we are initialized
            if (initCallback)
                initCallback();
        };

        this.getSolution = function () {
            var ans = '';

            for (var p = 0; p < places.length; p++)
                for (w = 0; w < what.length; w++) {
                    var h = $table.find('.e' + p + w).html();
                    if (h == "Да")
                        ans += '+';
                    else if (h == "Нет")
                        ans += '-';
                    else
                        ans += '?';
                }

            return ans;
        };

        this.loadSolution = function (solution) {
            this.reset();

            if (solution == "")
                return;

            var cnt = 0;
            for (var p = 0; p < places.length; p++)
                for (w = 0; w < what.length; w++) {
                    var h = solution.charAt(cnt++);
                    if (h == "+")
                        h = 'Да';
                    else if (h == "-")
                        h = 'Нет';
                    else
                        h = '?';
                    $table.find('.e' + p + w).html(h);
                }
        };

        this.getAnswer = function () {
            return 2;
        };
    }

    return new LightsProblem(container);
}