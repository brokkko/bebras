/*function param_normalizer(param) {
    //normalize param view

    if (!param.view) {
        param.view = function(v) {
            return v + "";
        }
    } else if (typeof param.view === "string") {
        var postfix = param.view;
        param.view = function(v) {
            return v + postfix;
        }
    }

    //normalize param normalize

    if (!param.normalize) {
        param.normalize = function(v) {
            return v;
        }
    }

    return param;
}*/

function params_normalizer(params) {
    var result = Java.class;

    for (var i = 0; i < params.length; i++) {
        var param = params[i];
        result.push(param_normalizer(param))
    }

    return result;
}