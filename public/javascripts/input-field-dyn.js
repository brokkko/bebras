function InputFieldDynProblem(container, max_length, validator) { //validator return false if OK and error msg otherwise
    var $container = $('#' + container);
    $container.css('margin', '1em 0');

    if (validator === 'number')
        validator = function(x) {
            if (isNaN(+x))
                return "Это не число!";
            return false;
        };

    var $label = $('<span style="font-family: Arial, sans-serif">Введите ответ: </span>');
    var $in = $('<input style="background-color: #eee; border: 2px solid #444; color: #a00000; font-size:1em; font-family: Arial, sans-serif; padding: 0 2px" type="text" maxlength="' + max_length + '" size="' + max_length + '">');
    var $in_disabled = $('<span style="font-family: Arial, sans-serif; color: #008000; padding: 3px 4px" class="hidden"></span>');
    var $error = $('<span style="margin-left: 1em;color: red" class="hidden">Ошибка</span>');

    $in.change(function() {
        if (!validator)
            return;

        var v = $in.val();
        if (v === "" || v === null) {
            $error.hide();
            return;
        }

        var msg = validator(v);
        if (msg === false)
            $error.hide();
        else {
            $error.text(msg);
            $error.show();
        }
    });

    $container.append($label, $in, $in_disabled, $error);

    var initCallback;
    var enabled = true;

    this.setInitCallback = function (_initCallback) {
        initCallback = _initCallback;

        //we are initialized just after creation, so any attempt to set up init callback is after we are initialized
        if (initCallback)
            initCallback();
    };

    this.isEnabled = function () {
        return enabled;
    };

    this.setEnabled = function (state) {
        if (state) {
            $in.show();
            $in_disabled.hide();
        } else {
            $in.hide();
            $in_disabled.show();
            $in_disabled.text($in.val());
        }

        enabled = state;
        return true;
    };

    this.getSolution = function () {
        return $in.val();
    };

    this.loadSolution = function(solution) {
        $in.val(solution);
    };

    this.getAnswer = function () {
        return 2;
    };
}