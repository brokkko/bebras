var kio_api =
/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId])
/******/ 			return installedModules[moduleId].exports;
/******/
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// identity function for calling harmony imports with the correct context
/******/ 	__webpack_require__.i = function(value) { return value; };
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, {
/******/ 				configurable: false,
/******/ 				enumerable: true,
/******/ 				get: getter
/******/ 			});
/******/ 		}
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = 60);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/* unknown exports provided */
/* all exports used */
/*!*******************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_global.js ***!
  \*******************************************************/
/***/ (function(module, exports) {

// https://github.com/zloirock/core-js/issues/86#issuecomment-115759028
var global = module.exports = typeof window != 'undefined' && window.Math == Math
  ? window : typeof self != 'undefined' && self.Math == Math ? self : Function('return this')();
if(typeof __g == 'number')__g = global; // eslint-disable-line no-undef

/***/ }),
/* 1 */
/* unknown exports provided */
/* all exports used */
/*!************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_descriptors.js ***!
  \************************************************************/
/***/ (function(module, exports, __webpack_require__) {

// Thank's IE8 for his funny defineProperty
module.exports = !__webpack_require__(/*! ./_fails */ 7)(function(){
  return Object.defineProperty({}, 'a', {get: function(){ return 7; }}).a != 7;
});

/***/ }),
/* 2 */
/* unknown exports provided */
/* all exports used */
/*!****************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_has.js ***!
  \****************************************************/
/***/ (function(module, exports) {

var hasOwnProperty = {}.hasOwnProperty;
module.exports = function(it, key){
  return hasOwnProperty.call(it, key);
};

/***/ }),
/* 3 */
/* unknown exports provided */
/* all exports used */
/*!**********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_object-dp.js ***!
  \**********************************************************/
/***/ (function(module, exports, __webpack_require__) {

var anObject       = __webpack_require__(/*! ./_an-object */ 6)
  , IE8_DOM_DEFINE = __webpack_require__(/*! ./_ie8-dom-define */ 21)
  , toPrimitive    = __webpack_require__(/*! ./_to-primitive */ 16)
  , dP             = Object.defineProperty;

exports.f = __webpack_require__(/*! ./_descriptors */ 1) ? Object.defineProperty : function defineProperty(O, P, Attributes){
  anObject(O);
  P = toPrimitive(P, true);
  anObject(Attributes);
  if(IE8_DOM_DEFINE)try {
    return dP(O, P, Attributes);
  } catch(e){ /* empty */ }
  if('get' in Attributes || 'set' in Attributes)throw TypeError('Accessors not supported!');
  if('value' in Attributes)O[P] = Attributes.value;
  return O;
};

/***/ }),
/* 4 */
/* unknown exports provided */
/* all exports used */
/*!***********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_to-iobject.js ***!
  \***********************************************************/
/***/ (function(module, exports, __webpack_require__) {

// to indexed object, toObject with fallback for non-array-like ES3 strings
var IObject = __webpack_require__(/*! ./_iobject */ 45)
  , defined = __webpack_require__(/*! ./_defined */ 41);
module.exports = function(it){
  return IObject(defined(it));
};

/***/ }),
/* 5 */
/* unknown exports provided */
/* all exports used */
/*!****************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_uid.js ***!
  \****************************************************/
/***/ (function(module, exports) {

var id = 0
  , px = Math.random();
module.exports = function(key){
  return 'Symbol('.concat(key === undefined ? '' : key, ')_', (++id + px).toString(36));
};

/***/ }),
/* 6 */
/* unknown exports provided */
/* all exports used */
/*!**********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_an-object.js ***!
  \**********************************************************/
/***/ (function(module, exports, __webpack_require__) {

var isObject = __webpack_require__(/*! ./_is-object */ 8);
module.exports = function(it){
  if(!isObject(it))throw TypeError(it + ' is not an object!');
  return it;
};

/***/ }),
/* 7 */
/* unknown exports provided */
/* all exports used */
/*!******************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_fails.js ***!
  \******************************************************/
/***/ (function(module, exports) {

module.exports = function(exec){
  try {
    return !!exec();
  } catch(e){
    return true;
  }
};

/***/ }),
/* 8 */
/* unknown exports provided */
/* all exports used */
/*!**********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_is-object.js ***!
  \**********************************************************/
/***/ (function(module, exports) {

module.exports = function(it){
  return typeof it === 'object' ? it !== null : typeof it === 'function';
};

/***/ }),
/* 9 */
/* unknown exports provided */
/* all exports used */
/*!************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_object-keys.js ***!
  \************************************************************/
/***/ (function(module, exports, __webpack_require__) {

// 19.1.2.14 / 15.2.3.14 Object.keys(O)
var $keys       = __webpack_require__(/*! ./_object-keys-internal */ 25)
  , enumBugKeys = __webpack_require__(/*! ./_enum-bug-keys */ 11);

module.exports = Object.keys || function keys(O){
  return $keys(O, enumBugKeys);
};

/***/ }),
/* 10 */
/* unknown exports provided */
/* all exports used */
/*!*****************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_core.js ***!
  \*****************************************************/
/***/ (function(module, exports) {

var core = module.exports = {version: '2.4.0'};
if(typeof __e == 'number')__e = core; // eslint-disable-line no-undef

/***/ }),
/* 11 */
/* unknown exports provided */
/* all exports used */
/*!**************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_enum-bug-keys.js ***!
  \**************************************************************/
/***/ (function(module, exports) {

// IE 8- don't enum bug keys
module.exports = (
  'constructor,hasOwnProperty,isPrototypeOf,propertyIsEnumerable,toLocaleString,toString,valueOf'
).split(',');

/***/ }),
/* 12 */
/* unknown exports provided */
/* all exports used */
/*!*****************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_hide.js ***!
  \*****************************************************/
/***/ (function(module, exports, __webpack_require__) {

var dP         = __webpack_require__(/*! ./_object-dp */ 3)
  , createDesc = __webpack_require__(/*! ./_property-desc */ 14);
module.exports = __webpack_require__(/*! ./_descriptors */ 1) ? function(object, key, value){
  return dP.f(object, key, createDesc(1, value));
} : function(object, key, value){
  object[key] = value;
  return object;
};

/***/ }),
/* 13 */
/* unknown exports provided */
/* all exports used */
/*!***********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_object-pie.js ***!
  \***********************************************************/
/***/ (function(module, exports) {

exports.f = {}.propertyIsEnumerable;

/***/ }),
/* 14 */
/* unknown exports provided */
/* all exports used */
/*!**************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_property-desc.js ***!
  \**************************************************************/
/***/ (function(module, exports) {

module.exports = function(bitmap, value){
  return {
    enumerable  : !(bitmap & 1),
    configurable: !(bitmap & 2),
    writable    : !(bitmap & 4),
    value       : value
  };
};

/***/ }),
/* 15 */
/* unknown exports provided */
/* all exports used */
/*!*******************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_shared.js ***!
  \*******************************************************/
/***/ (function(module, exports, __webpack_require__) {

var global = __webpack_require__(/*! ./_global */ 0)
  , SHARED = '__core-js_shared__'
  , store  = global[SHARED] || (global[SHARED] = {});
module.exports = function(key){
  return store[key] || (store[key] = {});
};

/***/ }),
/* 16 */
/* unknown exports provided */
/* all exports used */
/*!*************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_to-primitive.js ***!
  \*************************************************************/
/***/ (function(module, exports, __webpack_require__) {

// 7.1.1 ToPrimitive(input [, PreferredType])
var isObject = __webpack_require__(/*! ./_is-object */ 8);
// instead of the ES6 spec version, we didn't implement @@toPrimitive case
// and the second argument - flag - preferred type is a string
module.exports = function(it, S){
  if(!isObject(it))return it;
  var fn, val;
  if(S && typeof (fn = it.toString) == 'function' && !isObject(val = fn.call(it)))return val;
  if(typeof (fn = it.valueOf) == 'function' && !isObject(val = fn.call(it)))return val;
  if(!S && typeof (fn = it.toString) == 'function' && !isObject(val = fn.call(it)))return val;
  throw TypeError("Can't convert object to primitive value");
};

/***/ }),
/* 17 */
/* unknown exports provided */
/* all exports used */
/*!****************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_wks.js ***!
  \****************************************************/
/***/ (function(module, exports, __webpack_require__) {

var store      = __webpack_require__(/*! ./_shared */ 15)('wks')
  , uid        = __webpack_require__(/*! ./_uid */ 5)
  , Symbol     = __webpack_require__(/*! ./_global */ 0).Symbol
  , USE_SYMBOL = typeof Symbol == 'function';

var $exports = module.exports = function(name){
  return store[name] || (store[name] =
    USE_SYMBOL && Symbol[name] || (USE_SYMBOL ? Symbol : uid)('Symbol.' + name));
};

$exports.store = store;

/***/ }),
/* 18 */
/* exports provided: initialize_controls, InfoPanel, Button */
/* exports used: initialize_controls, Button */
/*!*************************************!*\
  !*** ./src/kio_api/kio_controls.js ***!
  \*************************************/
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__kio_scss__ = __webpack_require__(/*! ./kio.scss */ 57);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__kio_scss___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_0__kio_scss__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__stored_solutions__ = __webpack_require__(/*! ./stored_solutions */ 37);
/* harmony export (immutable) */ __webpack_exports__["a"] = initialize_controls;
/* unused harmony export InfoPanel */
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "b", function() { return Button; });
var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }





function initialize_controls(controlsDiv, kioapi) {
    var results_info_panel = new InfoPanel("Результат", kioapi.problem.parameters());
    var record_info_panel = new InfoPanel("Рекорд", kioapi.problem.parameters());

    kioapi.results_info_panel = results_info_panel;
    kioapi.record_info_panel = record_info_panel;

    var info_panels_container = document.createElement('div');
    info_panels_container.className = 'kio-base-info-panels-container';

    info_panels_container.appendChild(results_info_panel.domNode);
    info_panels_container.appendChild(record_info_panel.domNode);
    controlsDiv.appendChild(info_panels_container);

    results_info_panel.domNode.className += " kio-base-results-info-panel";
    record_info_panel.domNode.className += " kio-base-record-info-panel";

    var button_clear = new Button('Очистить решение', function () {
        kioapi.loadSolution(kioapi.emptySolution);
    });
    var button_load_record = new Button('Загрузить рекорд', function () {
        kioapi.loadSolution(kioapi.best);
    });

    results_info_panel.domNode.appendChild(button_clear.domNode);
    record_info_panel.domNode.appendChild(button_load_record.domNode);

    controlsDiv.className = 'kio-base-controls-container';
    var spanner = document.createElement('div');
    spanner.className = 'kio-base-clear-both';
    info_panels_container.appendChild(spanner);

    var ss = new __WEBPACK_IMPORTED_MODULE_1__stored_solutions__["a" /* StoredSolutions */](kioapi);
    controlsDiv.appendChild(ss.domNode);
}

var InfoPanel = function () {
    function InfoPanel(title, params) {
        _classCallCheck(this, InfoPanel);

        //params as in problem description
        this.title = title;
        this.params = params;

        this.domNode = document.createElement('div');
        this.inject_inside(this.domNode);
    }

    _createClass(InfoPanel, [{
        key: 'setParams',
        value: function setParams(nameValueObject) {
            var ind = 0;
            var _iteratorNormalCompletion = true;
            var _didIteratorError = false;
            var _iteratorError = undefined;

            try {
                for (var _iterator = this.params[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
                    var param = _step.value;

                    if (!param.title) continue;
                    var td_val = this.value_elements[ind];
                    var value = nameValueObject[param.name];
                    td_val.innerText = this.paramViewFunction(param)(value);
                    ind++;
                }
            } catch (err) {
                _didIteratorError = true;
                _iteratorError = err;
            } finally {
                try {
                    if (!_iteratorNormalCompletion && _iterator.return) {
                        _iterator.return();
                    }
                } finally {
                    if (_didIteratorError) {
                        throw _iteratorError;
                    }
                }
            }
        }
    }, {
        key: 'paramViewFunction',
        value: function paramViewFunction(param) {
            if (!param.view) return function (v) {
                return v;
            };

            if (typeof param.view === "function") return param.view;

            return function (v) {
                return v + param.view;
            };
        }
    }, {
        key: 'inject_inside',
        value: function inject_inside(domNode) {
            domNode.className = 'kio-base-info-panel';

            this.param_name_2_param = {};
            this.param_name_2_value_element = {};

            var table = document.createElement('table');
            var table_head = document.createElement('thead');
            var table_body = document.createElement('tbody');

            table_head.className = 'kio-base-info-panel-head';

            table.appendChild(table_head);
            table.appendChild(table_body);

            //init head
            var tr_head = document.createElement('tr');
            var td_head = document.createElement('td');
            td_head.colspan = 2;
            table_head.appendChild(tr_head);
            tr_head.appendChild(td_head);
            td_head.innerText = this.title;

            //init body
            this.value_elements = [];
            var _iteratorNormalCompletion2 = true;
            var _didIteratorError2 = false;
            var _iteratorError2 = undefined;

            try {
                for (var _iterator2 = this.params[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
                    var param = _step2.value;

                    if (!param.title) //no title means the param is invisible
                        continue;

                    var tr = document.createElement('tr');
                    var td_name = document.createElement('td');
                    var td_val = document.createElement('td');
                    tr.appendChild(td_name);
                    tr.appendChild(td_val);

                    td_name.className = 'kio-base-info-panel-param-name';
                    td_val.className = 'kio-base-info-panel-param-value';

                    td_name.innerText = param.title;

                    table_body.appendChild(tr);

                    // this.param_name_2_value_element[param.name] = td_val;
                    // this.param_name_2_param[param.name] = param;
                    this.value_elements.push(td_val);
                }
            } catch (err) {
                _didIteratorError2 = true;
                _iteratorError2 = err;
            } finally {
                try {
                    if (!_iteratorNormalCompletion2 && _iterator2.return) {
                        _iterator2.return();
                    }
                } finally {
                    if (_didIteratorError2) {
                        throw _iteratorError2;
                    }
                }
            }

            domNode.appendChild(table);
        }
    }, {
        key: 'as_string',
        value: function as_string(nameValueObject) {
            if (!nameValueObject) return '-';

            var res = [];
            var _iteratorNormalCompletion3 = true;
            var _didIteratorError3 = false;
            var _iteratorError3 = undefined;

            try {
                for (var _iterator3 = this.params[Symbol.iterator](), _step3; !(_iteratorNormalCompletion3 = (_step3 = _iterator3.next()).done); _iteratorNormalCompletion3 = true) {
                    var param = _step3.value;

                    if (!param.title) continue;
                    var value = nameValueObject[param.name];
                    res.push(this.paramViewFunction(param)(value));
                }
            } catch (err) {
                _didIteratorError3 = true;
                _iteratorError3 = err;
            } finally {
                try {
                    if (!_iteratorNormalCompletion3 && _iterator3.return) {
                        _iterator3.return();
                    }
                } finally {
                    if (_didIteratorError3) {
                        throw _iteratorError3;
                    }
                }
            }

            return res.join(', ');
        }
    }]);

    return InfoPanel;
}();

var Button = function () {
    function Button(title, handler) {
        _classCallCheck(this, Button);

        this.domNode = document.createElement('button');
        this.domNode.className = "kio-base-control-button";
        this.title = title;
        $(this.domNode).click(handler);
    }

    _createClass(Button, [{
        key: 'title',
        set: function set(title) {
            this._title = title;
            this.domNode.innerText = title;
        },
        get: function get() {
            return this._title;
        }
    }]);

    return Button;
}();

/***/ }),
/* 19 */
/* unknown exports provided */
/* all exports used */
/*!****************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_cof.js ***!
  \****************************************************/
/***/ (function(module, exports) {

var toString = {}.toString;

module.exports = function(it){
  return toString.call(it).slice(8, -1);
};

/***/ }),
/* 20 */
/* unknown exports provided */
/* all exports used */
/*!***********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_dom-create.js ***!
  \***********************************************************/
/***/ (function(module, exports, __webpack_require__) {

var isObject = __webpack_require__(/*! ./_is-object */ 8)
  , document = __webpack_require__(/*! ./_global */ 0).document
  // in old IE typeof document.createElement is 'object'
  , is = isObject(document) && isObject(document.createElement);
module.exports = function(it){
  return is ? document.createElement(it) : {};
};

/***/ }),
/* 21 */
/* unknown exports provided */
/* all exports used */
/*!***************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_ie8-dom-define.js ***!
  \***************************************************************/
/***/ (function(module, exports, __webpack_require__) {

module.exports = !__webpack_require__(/*! ./_descriptors */ 1) && !__webpack_require__(/*! ./_fails */ 7)(function(){
  return Object.defineProperty(__webpack_require__(/*! ./_dom-create */ 20)('div'), 'a', {get: function(){ return 7; }}).a != 7;
});

/***/ }),
/* 22 */
/* unknown exports provided */
/* all exports used */
/*!********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_library.js ***!
  \********************************************************/
/***/ (function(module, exports) {

module.exports = false;

/***/ }),
/* 23 */
/* unknown exports provided */
/* all exports used */
/*!************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_object-gopn.js ***!
  \************************************************************/
/***/ (function(module, exports, __webpack_require__) {

// 19.1.2.7 / 15.2.3.4 Object.getOwnPropertyNames(O)
var $keys      = __webpack_require__(/*! ./_object-keys-internal */ 25)
  , hiddenKeys = __webpack_require__(/*! ./_enum-bug-keys */ 11).concat('length', 'prototype');

exports.f = Object.getOwnPropertyNames || function getOwnPropertyNames(O){
  return $keys(O, hiddenKeys);
};

/***/ }),
/* 24 */
/* unknown exports provided */
/* all exports used */
/*!************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_object-gops.js ***!
  \************************************************************/
/***/ (function(module, exports) {

exports.f = Object.getOwnPropertySymbols;

/***/ }),
/* 25 */
/* unknown exports provided */
/* all exports used */
/*!*********************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_object-keys-internal.js ***!
  \*********************************************************************/
/***/ (function(module, exports, __webpack_require__) {

var has          = __webpack_require__(/*! ./_has */ 2)
  , toIObject    = __webpack_require__(/*! ./_to-iobject */ 4)
  , arrayIndexOf = __webpack_require__(/*! ./_array-includes */ 39)(false)
  , IE_PROTO     = __webpack_require__(/*! ./_shared-key */ 27)('IE_PROTO');

module.exports = function(object, names){
  var O      = toIObject(object)
    , i      = 0
    , result = []
    , key;
  for(key in O)if(key != IE_PROTO)has(O, key) && result.push(key);
  // Don't enum bug & hidden keys
  while(names.length > i)if(has(O, key = names[i++])){
    ~arrayIndexOf(result, key) || result.push(key);
  }
  return result;
};

/***/ }),
/* 26 */
/* unknown exports provided */
/* all exports used */
/*!*********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_redefine.js ***!
  \*********************************************************/
/***/ (function(module, exports, __webpack_require__) {

var global    = __webpack_require__(/*! ./_global */ 0)
  , hide      = __webpack_require__(/*! ./_hide */ 12)
  , has       = __webpack_require__(/*! ./_has */ 2)
  , SRC       = __webpack_require__(/*! ./_uid */ 5)('src')
  , TO_STRING = 'toString'
  , $toString = Function[TO_STRING]
  , TPL       = ('' + $toString).split(TO_STRING);

__webpack_require__(/*! ./_core */ 10).inspectSource = function(it){
  return $toString.call(it);
};

(module.exports = function(O, key, val, safe){
  var isFunction = typeof val == 'function';
  if(isFunction)has(val, 'name') || hide(val, 'name', key);
  if(O[key] === val)return;
  if(isFunction)has(val, SRC) || hide(val, SRC, O[key] ? '' + O[key] : TPL.join(String(key)));
  if(O === global){
    O[key] = val;
  } else {
    if(!safe){
      delete O[key];
      hide(O, key, val);
    } else {
      if(O[key])O[key] = val;
      else hide(O, key, val);
    }
  }
// add fake Function#toString for correct work wrapped methods / constructors with methods like LoDash isNative
})(Function.prototype, TO_STRING, function toString(){
  return typeof this == 'function' && this[SRC] || $toString.call(this);
});

/***/ }),
/* 27 */
/* unknown exports provided */
/* all exports used */
/*!***********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_shared-key.js ***!
  \***********************************************************/
/***/ (function(module, exports, __webpack_require__) {

var shared = __webpack_require__(/*! ./_shared */ 15)('keys')
  , uid    = __webpack_require__(/*! ./_uid */ 5);
module.exports = function(key){
  return shared[key] || (shared[key] = uid(key));
};

/***/ }),
/* 28 */
/* unknown exports provided */
/* all exports used */
/*!***********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_to-integer.js ***!
  \***********************************************************/
/***/ (function(module, exports) {

// 7.1.4 ToInteger
var ceil  = Math.ceil
  , floor = Math.floor;
module.exports = function(it){
  return isNaN(it = +it) ? 0 : (it > 0 ? floor : ceil)(it);
};

/***/ }),
/* 29 */
/* unknown exports provided */
/* all exports used */
/*!********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_wks-ext.js ***!
  \********************************************************/
/***/ (function(module, exports, __webpack_require__) {

exports.f = __webpack_require__(/*! ./_wks */ 17);

/***/ }),
/* 30 */,
/* 31 */,
/* 32 */
/* exports provided: initializeKioProblem */
/* all exports used */
/*!********************************!*\
  !*** ./src/kio_api/kio_api.js ***!
  \********************************/
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
Object.defineProperty(__webpack_exports__, "__esModule", { value: true });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__kio_controls__ = __webpack_require__(/*! ./kio_controls */ 18);
/* harmony export (immutable) */ __webpack_exports__["initializeKioProblem"] = initializeKioProblem;
var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }



/**
 * @param ProblemClass Problem class, it will be called as new ProblemClass(settings).
 * @param domNode html5 dom element
 * @param settings object to be passed to ProblemClass
 * @param basePath optional, base path to resolve manifest element from
 */
function initializeKioProblem(ProblemClass, domNode, settings, basePath) {
    var problem = new ProblemClass(settings);

    var loadingInfoDiv = createLoadingInfoElement();
    domNode.appendChild(loadingInfoDiv);

    if (problem.preloadManifest) {
        var queue = new createjs.LoadQueue(true, basePath);
        queue.on("complete", finalizeInitialization, null, false, { loading_queue: queue });
        queue.on("error", errorLoadingResources);
        queue.on("progress", loadingProgressChanged);

        var manifest = problem.preloadManifest();
        queue.loadManifest(manifest);
    } else finalizeInitialization(null, { loading_queue: null });

    var load_best_from_server = true;
    var load_autosaved = true;

    function finalizeInitialization(evt, _ref) {
        var loading_queue = _ref.loading_queue;

        console.debug('finalizing intialization', load_best_from_server, load_autosaved);
        if (!load_best_from_server) console.debug('trying to reinit without loading best from server');
        if (!load_autosaved) console.debug('trying to reinit without loading autosaved');

        if (load_best_from_server && load_autosaved) //remove only the first time
            domNode.removeChild(loadingInfoDiv);

        var kioapi = new KioApi(problem, domNode, loading_queue);

        kioapi.init_view(domNode, problem);

        kioapi.saveEmptySolution();

        if (load_best_from_server && !kioapi.loadSolution(kioapi.best_from_server, 'best-from-server')) {
            load_best_from_server = false;
            kioapi.uninit_view(domNode);
            finalizeInitialization(evt, { loading_queue: loading_queue });
            return;
        }
        //TODO get rid of code duplication
        if (load_autosaved && !kioapi.loadSolution(kioapi.autosavedSolution(), 'autosaved')) {
            load_autosaved = false;
            kioapi.uninit_view(domNode);
            finalizeInitialization(evt, { loading_queue: loading_queue });
            return;
        }

        kioapi.problem_is_initialized = true;
    }

    function errorLoadingResources() {
        loadingInfoDiv.innerText = "Ошибка при загрузке задачи, попробуйте обновить страницу";
    }

    function loadingProgressChanged(evt) {
        loadingInfoDiv.innerText = "Загрузка " + Math.round(100 * evt.progress) + "%";
    }

    function createLoadingInfoElement() {
        var infoDiv = document.createElement("div");
        infoDiv.className = "loading-info";
        return infoDiv;
    }
}

var KioApi = function () {
    function KioApi(problem, domNode, loading_queue) {
        _classCallCheck(this, KioApi);

        this.problem = problem;
        this.domNode = domNode;
        this.pid = dces2contest.get_problem_index($(domNode));

        this.best_from_server = best_solutions[dces2contest.get_problem_index($(domNode))];

        this.best = null;
        this.bestResult = null;
        this.autosave_localstorage_key = 'kio-problem-' + this.problem.id() + '-autosave';

        this.loading_queue = loading_queue;

        this.problem_is_initialized = false;
    }

    //FIXME here 1


    _createClass(KioApi, [{
        key: "submitGaEvent",
        value: function submitGaEvent(category, action, label) {
            if ('ga' in window) {
                ga('set', 'nonInteraction', true);
                ga('send', 'event', {
                    eventCategory: category,
                    eventAction: action,
                    eventLabel: navigator.userAgent + (label ? ' -> ' + label : ''),
                    eventValue: 0
                });
            }
        }
    }, {
        key: "saveEmptySolution",
        value: function saveEmptySolution() {
            this.emptySolution = this.problem.solution();
        }

        //returns was loading ok or not

    }, {
        key: "loadSolution",
        value: function loadSolution(solution, message) {
            if (solution !== null) {
                try {
                    this.problem.loadSolution(solution);
                    return true;
                } catch (e) {
                    console.debug('error loading solution', solution, e);
                    this.submitGaEvent('Failed to load solution', JSON.stringify(solution), this.problem.id() + (message ? ' ' + message : ''));
                    return false;
                }
            }
            return true;
        }
    }, {
        key: "bestSolution",
        value: function bestSolution() {
            return this.best;
        }
    }, {
        key: "autosavedSolution",
        value: function autosavedSolution() {
            return JSON.parse(localStorage.getItem(this.autosave_localstorage_key));
        }
    }, {
        key: "autosaveSolution",
        value: function autosaveSolution() {
            if (!this.problem_is_initialized) return;

            localStorage.setItem(this.autosave_localstorage_key, JSON.stringify(this.problem.solution()));
        }
    }, {
        key: "newRecord",
        value: function newRecord(solution, result) {
            if (!this.problem_is_initialized) return;

            dces2contest.submit_answer(this.pid, {
                sol: JSON.stringify(solution),
                res: JSON.stringify(result)
            });
        }
    }, {
        key: "submitResult",
        value: function submitResult(result) {
            this.last_submitted_result = result;

            this.results_info_panel.setParams(result);

            this.autosaveSolution();

            var cmp = this.compareResults(result, this.bestResult);

            if (cmp > 0) {
                this.bestResult = result;
                this.best = this.problem.solution();
                this.newRecord(this.best, result);

                this.record_info_panel.setParams(result);
            }
        }
    }, {
        key: "compareResults",
        value: function compareResults(result1, result2) {
            if (result1 == null && result2 == null) return 0;
            if (result1 == null) return -1;
            if (result2 == null) return 1;

            var params = this.problem.parameters();
            var _iteratorNormalCompletion = true;
            var _didIteratorError = false;
            var _iteratorError = undefined;

            try {
                for (var _iterator = params[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
                    var param = _step.value;

                    var val1 = result1[param.name];
                    var val2 = result2[param.name];

                    if (param.normalize) {
                        val1 = param.normalize(val1);
                        val2 = param.normalize(val2);
                    }

                    var diff = param.ordering === 'maximize' ? val1 - val2 : val2 - val1;
                    if (Math.abs(diff) > 1e-9) {
                        if (diff > 0) return 1;else return -1;
                    }
                }
            } catch (err) {
                _didIteratorError = true;
                _iteratorError = err;
            } finally {
                try {
                    if (!_iteratorNormalCompletion && _iterator.return) {
                        _iterator.return();
                    }
                } finally {
                    if (_didIteratorError) {
                        throw _iteratorError;
                    }
                }
            }

            return 0;
        }
    }, {
        key: "getResource",
        value: function getResource(id) {
            return this.loading_queue.getResult(id);
        }
    }, {
        key: "init_view",
        value: function init_view(domNode, problem) {
            var problemDiv = document.createElement('div');
            var controlsDiv = document.createElement('div');

            problemDiv.className = 'kio-base-box';

            domNode.appendChild(problemDiv);
            domNode.appendChild(controlsDiv);

            __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__kio_controls__["a" /* initialize_controls */])(controlsDiv, this);

            var preferred_width = $(domNode).width() - 12;
            problem.initialize(problemDiv, this, preferred_width); //2 * margin == 6
        }
    }, {
        key: "uninit_view",
        value: function uninit_view(domNode) {
            while (domNode.firstChild) {
                domNode.removeChild(domNode.firstChild);
            }
        }
    }]);

    return KioApi;
}();

dces2contest.register_solution_loader('kio-online', load_kio_solution);

var best_solutions = [];

function load_kio_solution($problem_div, answer) {
    if (!answer) return;

    if (!answer.sol) return;

    var solution = JSON.parse(answer.sol);

    var pid = dces2contest.get_problem_index($problem_div);
    best_solutions[pid] = solution;
}

//TODO do not fail on non-parsable solutions

/***/ }),
/* 33 */
/* unknown exports provided */
/* all exports used */
/*!**********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/es6.symbol.js ***!
  \**********************************************************/
/***/ (function(module, exports, __webpack_require__) {

"use strict";

// ECMAScript 6 symbols shim
var global         = __webpack_require__(/*! ./_global */ 0)
  , has            = __webpack_require__(/*! ./_has */ 2)
  , DESCRIPTORS    = __webpack_require__(/*! ./_descriptors */ 1)
  , $export        = __webpack_require__(/*! ./_export */ 43)
  , redefine       = __webpack_require__(/*! ./_redefine */ 26)
  , META           = __webpack_require__(/*! ./_meta */ 48).KEY
  , $fails         = __webpack_require__(/*! ./_fails */ 7)
  , shared         = __webpack_require__(/*! ./_shared */ 15)
  , setToStringTag = __webpack_require__(/*! ./_set-to-string-tag */ 53)
  , uid            = __webpack_require__(/*! ./_uid */ 5)
  , wks            = __webpack_require__(/*! ./_wks */ 17)
  , wksExt         = __webpack_require__(/*! ./_wks-ext */ 29)
  , wksDefine      = __webpack_require__(/*! ./_wks-define */ 56)
  , keyOf          = __webpack_require__(/*! ./_keyof */ 47)
  , enumKeys       = __webpack_require__(/*! ./_enum-keys */ 42)
  , isArray        = __webpack_require__(/*! ./_is-array */ 46)
  , anObject       = __webpack_require__(/*! ./_an-object */ 6)
  , toIObject      = __webpack_require__(/*! ./_to-iobject */ 4)
  , toPrimitive    = __webpack_require__(/*! ./_to-primitive */ 16)
  , createDesc     = __webpack_require__(/*! ./_property-desc */ 14)
  , _create        = __webpack_require__(/*! ./_object-create */ 49)
  , gOPNExt        = __webpack_require__(/*! ./_object-gopn-ext */ 52)
  , $GOPD          = __webpack_require__(/*! ./_object-gopd */ 51)
  , $DP            = __webpack_require__(/*! ./_object-dp */ 3)
  , $keys          = __webpack_require__(/*! ./_object-keys */ 9)
  , gOPD           = $GOPD.f
  , dP             = $DP.f
  , gOPN           = gOPNExt.f
  , $Symbol        = global.Symbol
  , $JSON          = global.JSON
  , _stringify     = $JSON && $JSON.stringify
  , PROTOTYPE      = 'prototype'
  , HIDDEN         = wks('_hidden')
  , TO_PRIMITIVE   = wks('toPrimitive')
  , isEnum         = {}.propertyIsEnumerable
  , SymbolRegistry = shared('symbol-registry')
  , AllSymbols     = shared('symbols')
  , OPSymbols      = shared('op-symbols')
  , ObjectProto    = Object[PROTOTYPE]
  , USE_NATIVE     = typeof $Symbol == 'function'
  , QObject        = global.QObject;
// Don't use setters in Qt Script, https://github.com/zloirock/core-js/issues/173
var setter = !QObject || !QObject[PROTOTYPE] || !QObject[PROTOTYPE].findChild;

// fallback for old Android, https://code.google.com/p/v8/issues/detail?id=687
var setSymbolDesc = DESCRIPTORS && $fails(function(){
  return _create(dP({}, 'a', {
    get: function(){ return dP(this, 'a', {value: 7}).a; }
  })).a != 7;
}) ? function(it, key, D){
  var protoDesc = gOPD(ObjectProto, key);
  if(protoDesc)delete ObjectProto[key];
  dP(it, key, D);
  if(protoDesc && it !== ObjectProto)dP(ObjectProto, key, protoDesc);
} : dP;

var wrap = function(tag){
  var sym = AllSymbols[tag] = _create($Symbol[PROTOTYPE]);
  sym._k = tag;
  return sym;
};

var isSymbol = USE_NATIVE && typeof $Symbol.iterator == 'symbol' ? function(it){
  return typeof it == 'symbol';
} : function(it){
  return it instanceof $Symbol;
};

var $defineProperty = function defineProperty(it, key, D){
  if(it === ObjectProto)$defineProperty(OPSymbols, key, D);
  anObject(it);
  key = toPrimitive(key, true);
  anObject(D);
  if(has(AllSymbols, key)){
    if(!D.enumerable){
      if(!has(it, HIDDEN))dP(it, HIDDEN, createDesc(1, {}));
      it[HIDDEN][key] = true;
    } else {
      if(has(it, HIDDEN) && it[HIDDEN][key])it[HIDDEN][key] = false;
      D = _create(D, {enumerable: createDesc(0, false)});
    } return setSymbolDesc(it, key, D);
  } return dP(it, key, D);
};
var $defineProperties = function defineProperties(it, P){
  anObject(it);
  var keys = enumKeys(P = toIObject(P))
    , i    = 0
    , l = keys.length
    , key;
  while(l > i)$defineProperty(it, key = keys[i++], P[key]);
  return it;
};
var $create = function create(it, P){
  return P === undefined ? _create(it) : $defineProperties(_create(it), P);
};
var $propertyIsEnumerable = function propertyIsEnumerable(key){
  var E = isEnum.call(this, key = toPrimitive(key, true));
  if(this === ObjectProto && has(AllSymbols, key) && !has(OPSymbols, key))return false;
  return E || !has(this, key) || !has(AllSymbols, key) || has(this, HIDDEN) && this[HIDDEN][key] ? E : true;
};
var $getOwnPropertyDescriptor = function getOwnPropertyDescriptor(it, key){
  it  = toIObject(it);
  key = toPrimitive(key, true);
  if(it === ObjectProto && has(AllSymbols, key) && !has(OPSymbols, key))return;
  var D = gOPD(it, key);
  if(D && has(AllSymbols, key) && !(has(it, HIDDEN) && it[HIDDEN][key]))D.enumerable = true;
  return D;
};
var $getOwnPropertyNames = function getOwnPropertyNames(it){
  var names  = gOPN(toIObject(it))
    , result = []
    , i      = 0
    , key;
  while(names.length > i){
    if(!has(AllSymbols, key = names[i++]) && key != HIDDEN && key != META)result.push(key);
  } return result;
};
var $getOwnPropertySymbols = function getOwnPropertySymbols(it){
  var IS_OP  = it === ObjectProto
    , names  = gOPN(IS_OP ? OPSymbols : toIObject(it))
    , result = []
    , i      = 0
    , key;
  while(names.length > i){
    if(has(AllSymbols, key = names[i++]) && (IS_OP ? has(ObjectProto, key) : true))result.push(AllSymbols[key]);
  } return result;
};

// 19.4.1.1 Symbol([description])
if(!USE_NATIVE){
  $Symbol = function Symbol(){
    if(this instanceof $Symbol)throw TypeError('Symbol is not a constructor!');
    var tag = uid(arguments.length > 0 ? arguments[0] : undefined);
    var $set = function(value){
      if(this === ObjectProto)$set.call(OPSymbols, value);
      if(has(this, HIDDEN) && has(this[HIDDEN], tag))this[HIDDEN][tag] = false;
      setSymbolDesc(this, tag, createDesc(1, value));
    };
    if(DESCRIPTORS && setter)setSymbolDesc(ObjectProto, tag, {configurable: true, set: $set});
    return wrap(tag);
  };
  redefine($Symbol[PROTOTYPE], 'toString', function toString(){
    return this._k;
  });

  $GOPD.f = $getOwnPropertyDescriptor;
  $DP.f   = $defineProperty;
  __webpack_require__(/*! ./_object-gopn */ 23).f = gOPNExt.f = $getOwnPropertyNames;
  __webpack_require__(/*! ./_object-pie */ 13).f  = $propertyIsEnumerable;
  __webpack_require__(/*! ./_object-gops */ 24).f = $getOwnPropertySymbols;

  if(DESCRIPTORS && !__webpack_require__(/*! ./_library */ 22)){
    redefine(ObjectProto, 'propertyIsEnumerable', $propertyIsEnumerable, true);
  }

  wksExt.f = function(name){
    return wrap(wks(name));
  }
}

$export($export.G + $export.W + $export.F * !USE_NATIVE, {Symbol: $Symbol});

for(var symbols = (
  // 19.4.2.2, 19.4.2.3, 19.4.2.4, 19.4.2.6, 19.4.2.8, 19.4.2.9, 19.4.2.10, 19.4.2.11, 19.4.2.12, 19.4.2.13, 19.4.2.14
  'hasInstance,isConcatSpreadable,iterator,match,replace,search,species,split,toPrimitive,toStringTag,unscopables'
).split(','), i = 0; symbols.length > i; )wks(symbols[i++]);

for(var symbols = $keys(wks.store), i = 0; symbols.length > i; )wksDefine(symbols[i++]);

$export($export.S + $export.F * !USE_NATIVE, 'Symbol', {
  // 19.4.2.1 Symbol.for(key)
  'for': function(key){
    return has(SymbolRegistry, key += '')
      ? SymbolRegistry[key]
      : SymbolRegistry[key] = $Symbol(key);
  },
  // 19.4.2.5 Symbol.keyFor(sym)
  keyFor: function keyFor(key){
    if(isSymbol(key))return keyOf(SymbolRegistry, key);
    throw TypeError(key + ' is not a symbol!');
  },
  useSetter: function(){ setter = true; },
  useSimple: function(){ setter = false; }
});

$export($export.S + $export.F * !USE_NATIVE, 'Object', {
  // 19.1.2.2 Object.create(O [, Properties])
  create: $create,
  // 19.1.2.4 Object.defineProperty(O, P, Attributes)
  defineProperty: $defineProperty,
  // 19.1.2.3 Object.defineProperties(O, Properties)
  defineProperties: $defineProperties,
  // 19.1.2.6 Object.getOwnPropertyDescriptor(O, P)
  getOwnPropertyDescriptor: $getOwnPropertyDescriptor,
  // 19.1.2.7 Object.getOwnPropertyNames(O)
  getOwnPropertyNames: $getOwnPropertyNames,
  // 19.1.2.8 Object.getOwnPropertySymbols(O)
  getOwnPropertySymbols: $getOwnPropertySymbols
});

// 24.3.2 JSON.stringify(value [, replacer [, space]])
$JSON && $export($export.S + $export.F * (!USE_NATIVE || $fails(function(){
  var S = $Symbol();
  // MS Edge converts symbol values to JSON as {}
  // WebKit converts symbol values to JSON as null
  // V8 throws on boxed symbols
  return _stringify([S]) != '[null]' || _stringify({a: S}) != '{}' || _stringify(Object(S)) != '{}';
})), 'JSON', {
  stringify: function stringify(it){
    if(it === undefined || isSymbol(it))return; // IE8 returns string on undefined
    var args = [it]
      , i    = 1
      , replacer, $replacer;
    while(arguments.length > i)args.push(arguments[i++]);
    replacer = args[1];
    if(typeof replacer == 'function')$replacer = replacer;
    if($replacer || !isArray(replacer))replacer = function(key, value){
      if($replacer)value = $replacer.call(this, key, value);
      if(!isSymbol(value))return value;
    };
    args[1] = replacer;
    return _stringify.apply($JSON, args);
  }
});

// 19.4.3.4 Symbol.prototype[@@toPrimitive](hint)
$Symbol[PROTOTYPE][TO_PRIMITIVE] || __webpack_require__(/*! ./_hide */ 12)($Symbol[PROTOTYPE], TO_PRIMITIVE, $Symbol[PROTOTYPE].valueOf);
// 19.4.3.5 Symbol.prototype[@@toStringTag]
setToStringTag($Symbol, 'Symbol');
// 20.2.1.9 Math[@@toStringTag]
setToStringTag(Math, 'Math', true);
// 24.3.3 JSON[@@toStringTag]
setToStringTag(global.JSON, 'JSON', true);

/***/ }),
/* 34 */,
/* 35 */,
/* 36 */,
/* 37 */
/* exports provided: StoredSolutions */
/* exports used: StoredSolutions */
/*!*****************************************!*\
  !*** ./src/kio_api/stored_solutions.js ***!
  \*****************************************/
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__kio_controls__ = __webpack_require__(/*! ./kio_controls */ 18);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return StoredSolutions; });
var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }



var StoredSolutions = function () {
    function StoredSolutions(kio_api) {
        _classCallCheck(this, StoredSolutions);

        this.kio_api = kio_api;
        this.init_interface();

        //get all solutions
        var all_data_keys = dces2contest.get_all_problem_data_keys(kio_api.pid);

        var prefix = 'save-';

        var _iteratorNormalCompletion = true;
        var _didIteratorError = false;
        var _iteratorError = undefined;

        try {
            for (var _iterator = all_data_keys[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
                var data_key = _step.value;

                if (data_key.substr(0, prefix.length) == prefix) {
                    var id = data_key.substr(prefix.length);
                    var sol = Solution.load(this, id);
                    if (sol !== null) this.solutions_node.appendChild(sol.domNode);
                }
            }
        } catch (err) {
            _didIteratorError = true;
            _iteratorError = err;
        } finally {
            try {
                if (!_iteratorNormalCompletion && _iterator.return) {
                    _iterator.return();
                }
            } finally {
                if (_didIteratorError) {
                    throw _iteratorError;
                }
            }
        }
    }

    _createClass(StoredSolutions, [{
        key: 'init_interface',
        value: function init_interface() {
            var _this = this;

            this.domNode = document.createElement('div');
            this.domNode.className = 'kio-base-solutions-container';

            var title = document.createElement('div');
            title.className = 'title';
            title.innerText = 'Сохраненные решения';
            this.domNode.appendChild(title);

            this.new_solution_node = document.createElement('div');
            this.new_solution_node.className = 'kio-base-new-solution';
            var label = document.createElement('span');
            label.innerText = 'Назовите решение:';
            this.new_name = document.createElement('input');
            this.new_solution_node.appendChild(label);
            this.new_solution_node.appendChild(this.new_name);
            this.new_solution_node.appendChild(new __WEBPACK_IMPORTED_MODULE_0__kio_controls__["b" /* Button */]('Сохранить', function (e) {
                var name = _this.new_name.value;

                if (!name) return;

                var sol = Solution.create(_this, name, _this.kio_api.problem.solution(), _this.kio_api.last_submitted_result);
                if (_this.solutions_node.childNodes.length == 0) _this.solutions_node.appendChild(sol.domNode);else _this.solutions_node.insertBefore(sol.domNode, _this.solutions_node.childNodes[0]);

                _this.new_name.value = '';
            }).domNode);
            this.message_node = document.createElement('span');
            this.new_solution_node.appendChild(this.message_node);
            this.domNode.appendChild(this.new_solution_node);

            this.solutions_node = document.createElement('tbody');
            var solutions_table = document.createElement('table');
            solutions_table.appendChild(this.solutions_node);
            this.domNode.appendChild(solutions_table);
        }
    }]);

    return StoredSolutions;
}();

var Solution = function () {
    _createClass(Solution, null, [{
        key: 'load',
        value: function load(stored_solutions, id) {
            var data_key = 'save-' + id;
            var problemDataSerialized = dces2contest.get_problem_data(stored_solutions.kio_api.pid, data_key);
            if (problemDataSerialized === '') return null;

            var problemData = JSON.parse(problemDataSerialized);
            var name = problemData.name;
            var solution = problemData.solution;
            var result = problemData.result;

            return new Solution(stored_solutions, id, name, solution, result);
        }
    }, {
        key: 'create',
        value: function create(stored_solutions, name, solution, result) {
            var id = Solution.makeid(10);
            var sol = new Solution(stored_solutions, id, name, solution, result);
            sol.save();
            return sol;
        }

        //http://stackoverflow.com/a/1349426/1826120

    }, {
        key: 'makeid',
        value: function makeid() {
            var text = "";
            var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

            for (var i = 0; i < 10; i++) {
                text += possible.charAt(Math.floor(Math.random() * possible.length));
            }return text;
        }
    }]);

    function Solution(stored_solutions, id, name, solution, result) {
        _classCallCheck(this, Solution);

        this.stored_solutions = stored_solutions;
        this.kio_api = stored_solutions.kio_api;
        this.id = id;
        this.data_key = 'save-' + id;
        this.name = name;
        this.solution = solution;
        this.result = result;
        this.init_interface();
    }

    _createClass(Solution, [{
        key: 'remove',
        value: function remove() {
            dces2contest.save_problem_data(this.kio_api.pid, this.data_key, '');
        }
    }, {
        key: 'save',
        value: function save() {
            dces2contest.save_problem_data(this.kio_api.pid, 'save-' + this.id, JSON.stringify({
                name: this.name,
                solution: this.solution,
                result: this.result
            }));
        }
    }, {
        key: 'init_interface',
        value: function init_interface() {
            var _this2 = this;

            this.domNode = document.createElement('tr');
            this.domNode.className = 'kio-base-stored-solution';

            this.nameNode = document.createElement('span');
            this.nameNode.innerText = this.name;

            this.loadButton = new __WEBPACK_IMPORTED_MODULE_0__kio_controls__["b" /* Button */]('Загрузить', function (e) {
                _this2.kio_api.loadSolution(_this2.solution);
            }).domNode;
            this.removeButton = new __WEBPACK_IMPORTED_MODULE_0__kio_controls__["b" /* Button */]('Ctrl+Удалить', function (e) {
                if (!e.ctrlKey) return;
                //remove from dom
                _this2.domNode.parentNode.removeChild(_this2.domNode);
                //remove from server
                _this2.remove();
            }).domNode;

            var td1 = document.createElement('td');
            var td2 = document.createElement('td');
            var td3 = document.createElement('td');
            td1.className = 'first';
            td3.className = 'second';
            td1.appendChild(this.nameNode);
            td2.appendChild(this.loadButton);
            td2.appendChild(this.removeButton);
            this.domNode.appendChild(td1);
            this.domNode.appendChild(td3);
            this.domNode.appendChild(td2);

            td3.innerText = this.kio_api.results_info_panel.as_string(this.result);

            //TODO порядок сохраненных решений перемешивается
        }
    }]);

    return Solution;
}();

/***/ }),
/* 38 */
/* unknown exports provided */
/* all exports used */
/*!***********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_a-function.js ***!
  \***********************************************************/
/***/ (function(module, exports) {

module.exports = function(it){
  if(typeof it != 'function')throw TypeError(it + ' is not a function!');
  return it;
};

/***/ }),
/* 39 */
/* unknown exports provided */
/* all exports used */
/*!***************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_array-includes.js ***!
  \***************************************************************/
/***/ (function(module, exports, __webpack_require__) {

// false -> Array#indexOf
// true  -> Array#includes
var toIObject = __webpack_require__(/*! ./_to-iobject */ 4)
  , toLength  = __webpack_require__(/*! ./_to-length */ 55)
  , toIndex   = __webpack_require__(/*! ./_to-index */ 54);
module.exports = function(IS_INCLUDES){
  return function($this, el, fromIndex){
    var O      = toIObject($this)
      , length = toLength(O.length)
      , index  = toIndex(fromIndex, length)
      , value;
    // Array#includes uses SameValueZero equality algorithm
    if(IS_INCLUDES && el != el)while(length > index){
      value = O[index++];
      if(value != value)return true;
    // Array#toIndex ignores holes, Array#includes - not
    } else for(;length > index; index++)if(IS_INCLUDES || index in O){
      if(O[index] === el)return IS_INCLUDES || index || 0;
    } return !IS_INCLUDES && -1;
  };
};

/***/ }),
/* 40 */
/* unknown exports provided */
/* all exports used */
/*!****************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_ctx.js ***!
  \****************************************************/
/***/ (function(module, exports, __webpack_require__) {

// optional / simple context binding
var aFunction = __webpack_require__(/*! ./_a-function */ 38);
module.exports = function(fn, that, length){
  aFunction(fn);
  if(that === undefined)return fn;
  switch(length){
    case 1: return function(a){
      return fn.call(that, a);
    };
    case 2: return function(a, b){
      return fn.call(that, a, b);
    };
    case 3: return function(a, b, c){
      return fn.call(that, a, b, c);
    };
  }
  return function(/* ...args */){
    return fn.apply(that, arguments);
  };
};

/***/ }),
/* 41 */
/* unknown exports provided */
/* all exports used */
/*!********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_defined.js ***!
  \********************************************************/
/***/ (function(module, exports) {

// 7.2.1 RequireObjectCoercible(argument)
module.exports = function(it){
  if(it == undefined)throw TypeError("Can't call method on  " + it);
  return it;
};

/***/ }),
/* 42 */
/* unknown exports provided */
/* all exports used */
/*!**********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_enum-keys.js ***!
  \**********************************************************/
/***/ (function(module, exports, __webpack_require__) {

// all enumerable object keys, includes symbols
var getKeys = __webpack_require__(/*! ./_object-keys */ 9)
  , gOPS    = __webpack_require__(/*! ./_object-gops */ 24)
  , pIE     = __webpack_require__(/*! ./_object-pie */ 13);
module.exports = function(it){
  var result     = getKeys(it)
    , getSymbols = gOPS.f;
  if(getSymbols){
    var symbols = getSymbols(it)
      , isEnum  = pIE.f
      , i       = 0
      , key;
    while(symbols.length > i)if(isEnum.call(it, key = symbols[i++]))result.push(key);
  } return result;
};

/***/ }),
/* 43 */
/* unknown exports provided */
/* all exports used */
/*!*******************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_export.js ***!
  \*******************************************************/
/***/ (function(module, exports, __webpack_require__) {

var global    = __webpack_require__(/*! ./_global */ 0)
  , core      = __webpack_require__(/*! ./_core */ 10)
  , hide      = __webpack_require__(/*! ./_hide */ 12)
  , redefine  = __webpack_require__(/*! ./_redefine */ 26)
  , ctx       = __webpack_require__(/*! ./_ctx */ 40)
  , PROTOTYPE = 'prototype';

var $export = function(type, name, source){
  var IS_FORCED = type & $export.F
    , IS_GLOBAL = type & $export.G
    , IS_STATIC = type & $export.S
    , IS_PROTO  = type & $export.P
    , IS_BIND   = type & $export.B
    , target    = IS_GLOBAL ? global : IS_STATIC ? global[name] || (global[name] = {}) : (global[name] || {})[PROTOTYPE]
    , exports   = IS_GLOBAL ? core : core[name] || (core[name] = {})
    , expProto  = exports[PROTOTYPE] || (exports[PROTOTYPE] = {})
    , key, own, out, exp;
  if(IS_GLOBAL)source = name;
  for(key in source){
    // contains in native
    own = !IS_FORCED && target && target[key] !== undefined;
    // export native or passed
    out = (own ? target : source)[key];
    // bind timers to global for call from export context
    exp = IS_BIND && own ? ctx(out, global) : IS_PROTO && typeof out == 'function' ? ctx(Function.call, out) : out;
    // extend global
    if(target)redefine(target, key, out, type & $export.U);
    // export
    if(exports[key] != out)hide(exports, key, exp);
    if(IS_PROTO && expProto[key] != out)expProto[key] = out;
  }
};
global.core = core;
// type bitmap
$export.F = 1;   // forced
$export.G = 2;   // global
$export.S = 4;   // static
$export.P = 8;   // proto
$export.B = 16;  // bind
$export.W = 32;  // wrap
$export.U = 64;  // safe
$export.R = 128; // real proto method for `library` 
module.exports = $export;

/***/ }),
/* 44 */
/* unknown exports provided */
/* all exports used */
/*!*****************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_html.js ***!
  \*****************************************************/
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__(/*! ./_global */ 0).document && document.documentElement;

/***/ }),
/* 45 */
/* unknown exports provided */
/* all exports used */
/*!********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_iobject.js ***!
  \********************************************************/
/***/ (function(module, exports, __webpack_require__) {

// fallback for non-array-like ES3 and non-enumerable old V8 strings
var cof = __webpack_require__(/*! ./_cof */ 19);
module.exports = Object('z').propertyIsEnumerable(0) ? Object : function(it){
  return cof(it) == 'String' ? it.split('') : Object(it);
};

/***/ }),
/* 46 */
/* unknown exports provided */
/* all exports used */
/*!*********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_is-array.js ***!
  \*********************************************************/
/***/ (function(module, exports, __webpack_require__) {

// 7.2.2 IsArray(argument)
var cof = __webpack_require__(/*! ./_cof */ 19);
module.exports = Array.isArray || function isArray(arg){
  return cof(arg) == 'Array';
};

/***/ }),
/* 47 */
/* unknown exports provided */
/* all exports used */
/*!******************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_keyof.js ***!
  \******************************************************/
/***/ (function(module, exports, __webpack_require__) {

var getKeys   = __webpack_require__(/*! ./_object-keys */ 9)
  , toIObject = __webpack_require__(/*! ./_to-iobject */ 4);
module.exports = function(object, el){
  var O      = toIObject(object)
    , keys   = getKeys(O)
    , length = keys.length
    , index  = 0
    , key;
  while(length > index)if(O[key = keys[index++]] === el)return key;
};

/***/ }),
/* 48 */
/* unknown exports provided */
/* all exports used */
/*!*****************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_meta.js ***!
  \*****************************************************/
/***/ (function(module, exports, __webpack_require__) {

var META     = __webpack_require__(/*! ./_uid */ 5)('meta')
  , isObject = __webpack_require__(/*! ./_is-object */ 8)
  , has      = __webpack_require__(/*! ./_has */ 2)
  , setDesc  = __webpack_require__(/*! ./_object-dp */ 3).f
  , id       = 0;
var isExtensible = Object.isExtensible || function(){
  return true;
};
var FREEZE = !__webpack_require__(/*! ./_fails */ 7)(function(){
  return isExtensible(Object.preventExtensions({}));
});
var setMeta = function(it){
  setDesc(it, META, {value: {
    i: 'O' + ++id, // object ID
    w: {}          // weak collections IDs
  }});
};
var fastKey = function(it, create){
  // return primitive with prefix
  if(!isObject(it))return typeof it == 'symbol' ? it : (typeof it == 'string' ? 'S' : 'P') + it;
  if(!has(it, META)){
    // can't set metadata to uncaught frozen object
    if(!isExtensible(it))return 'F';
    // not necessary to add metadata
    if(!create)return 'E';
    // add missing metadata
    setMeta(it);
  // return object ID
  } return it[META].i;
};
var getWeak = function(it, create){
  if(!has(it, META)){
    // can't set metadata to uncaught frozen object
    if(!isExtensible(it))return true;
    // not necessary to add metadata
    if(!create)return false;
    // add missing metadata
    setMeta(it);
  // return hash weak collections IDs
  } return it[META].w;
};
// add metadata on freeze-family methods calling
var onFreeze = function(it){
  if(FREEZE && meta.NEED && isExtensible(it) && !has(it, META))setMeta(it);
  return it;
};
var meta = module.exports = {
  KEY:      META,
  NEED:     false,
  fastKey:  fastKey,
  getWeak:  getWeak,
  onFreeze: onFreeze
};

/***/ }),
/* 49 */
/* unknown exports provided */
/* all exports used */
/*!**************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_object-create.js ***!
  \**************************************************************/
/***/ (function(module, exports, __webpack_require__) {

// 19.1.2.2 / 15.2.3.5 Object.create(O [, Properties])
var anObject    = __webpack_require__(/*! ./_an-object */ 6)
  , dPs         = __webpack_require__(/*! ./_object-dps */ 50)
  , enumBugKeys = __webpack_require__(/*! ./_enum-bug-keys */ 11)
  , IE_PROTO    = __webpack_require__(/*! ./_shared-key */ 27)('IE_PROTO')
  , Empty       = function(){ /* empty */ }
  , PROTOTYPE   = 'prototype';

// Create object with fake `null` prototype: use iframe Object with cleared prototype
var createDict = function(){
  // Thrash, waste and sodomy: IE GC bug
  var iframe = __webpack_require__(/*! ./_dom-create */ 20)('iframe')
    , i      = enumBugKeys.length
    , lt     = '<'
    , gt     = '>'
    , iframeDocument;
  iframe.style.display = 'none';
  __webpack_require__(/*! ./_html */ 44).appendChild(iframe);
  iframe.src = 'javascript:'; // eslint-disable-line no-script-url
  // createDict = iframe.contentWindow.Object;
  // html.removeChild(iframe);
  iframeDocument = iframe.contentWindow.document;
  iframeDocument.open();
  iframeDocument.write(lt + 'script' + gt + 'document.F=Object' + lt + '/script' + gt);
  iframeDocument.close();
  createDict = iframeDocument.F;
  while(i--)delete createDict[PROTOTYPE][enumBugKeys[i]];
  return createDict();
};

module.exports = Object.create || function create(O, Properties){
  var result;
  if(O !== null){
    Empty[PROTOTYPE] = anObject(O);
    result = new Empty;
    Empty[PROTOTYPE] = null;
    // add "__proto__" for Object.getPrototypeOf polyfill
    result[IE_PROTO] = O;
  } else result = createDict();
  return Properties === undefined ? result : dPs(result, Properties);
};


/***/ }),
/* 50 */
/* unknown exports provided */
/* all exports used */
/*!***********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_object-dps.js ***!
  \***********************************************************/
/***/ (function(module, exports, __webpack_require__) {

var dP       = __webpack_require__(/*! ./_object-dp */ 3)
  , anObject = __webpack_require__(/*! ./_an-object */ 6)
  , getKeys  = __webpack_require__(/*! ./_object-keys */ 9);

module.exports = __webpack_require__(/*! ./_descriptors */ 1) ? Object.defineProperties : function defineProperties(O, Properties){
  anObject(O);
  var keys   = getKeys(Properties)
    , length = keys.length
    , i = 0
    , P;
  while(length > i)dP.f(O, P = keys[i++], Properties[P]);
  return O;
};

/***/ }),
/* 51 */
/* unknown exports provided */
/* all exports used */
/*!************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_object-gopd.js ***!
  \************************************************************/
/***/ (function(module, exports, __webpack_require__) {

var pIE            = __webpack_require__(/*! ./_object-pie */ 13)
  , createDesc     = __webpack_require__(/*! ./_property-desc */ 14)
  , toIObject      = __webpack_require__(/*! ./_to-iobject */ 4)
  , toPrimitive    = __webpack_require__(/*! ./_to-primitive */ 16)
  , has            = __webpack_require__(/*! ./_has */ 2)
  , IE8_DOM_DEFINE = __webpack_require__(/*! ./_ie8-dom-define */ 21)
  , gOPD           = Object.getOwnPropertyDescriptor;

exports.f = __webpack_require__(/*! ./_descriptors */ 1) ? gOPD : function getOwnPropertyDescriptor(O, P){
  O = toIObject(O);
  P = toPrimitive(P, true);
  if(IE8_DOM_DEFINE)try {
    return gOPD(O, P);
  } catch(e){ /* empty */ }
  if(has(O, P))return createDesc(!pIE.f.call(O, P), O[P]);
};

/***/ }),
/* 52 */
/* unknown exports provided */
/* all exports used */
/*!****************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_object-gopn-ext.js ***!
  \****************************************************************/
/***/ (function(module, exports, __webpack_require__) {

// fallback for IE11 buggy Object.getOwnPropertyNames with iframe and window
var toIObject = __webpack_require__(/*! ./_to-iobject */ 4)
  , gOPN      = __webpack_require__(/*! ./_object-gopn */ 23).f
  , toString  = {}.toString;

var windowNames = typeof window == 'object' && window && Object.getOwnPropertyNames
  ? Object.getOwnPropertyNames(window) : [];

var getWindowNames = function(it){
  try {
    return gOPN(it);
  } catch(e){
    return windowNames.slice();
  }
};

module.exports.f = function getOwnPropertyNames(it){
  return windowNames && toString.call(it) == '[object Window]' ? getWindowNames(it) : gOPN(toIObject(it));
};


/***/ }),
/* 53 */
/* unknown exports provided */
/* all exports used */
/*!******************************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_set-to-string-tag.js ***!
  \******************************************************************/
/***/ (function(module, exports, __webpack_require__) {

var def = __webpack_require__(/*! ./_object-dp */ 3).f
  , has = __webpack_require__(/*! ./_has */ 2)
  , TAG = __webpack_require__(/*! ./_wks */ 17)('toStringTag');

module.exports = function(it, tag, stat){
  if(it && !has(it = stat ? it : it.prototype, TAG))def(it, TAG, {configurable: true, value: tag});
};

/***/ }),
/* 54 */
/* unknown exports provided */
/* all exports used */
/*!*********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_to-index.js ***!
  \*********************************************************/
/***/ (function(module, exports, __webpack_require__) {

var toInteger = __webpack_require__(/*! ./_to-integer */ 28)
  , max       = Math.max
  , min       = Math.min;
module.exports = function(index, length){
  index = toInteger(index);
  return index < 0 ? max(index + length, 0) : min(index, length);
};

/***/ }),
/* 55 */
/* unknown exports provided */
/* all exports used */
/*!**********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_to-length.js ***!
  \**********************************************************/
/***/ (function(module, exports, __webpack_require__) {

// 7.1.15 ToLength
var toInteger = __webpack_require__(/*! ./_to-integer */ 28)
  , min       = Math.min;
module.exports = function(it){
  return it > 0 ? min(toInteger(it), 0x1fffffffffffff) : 0; // pow(2, 53) - 1 == 9007199254740991
};

/***/ }),
/* 56 */
/* unknown exports provided */
/* all exports used */
/*!***********************************************************!*\
  !*** ./~/babel-polyfill/~/core-js/modules/_wks-define.js ***!
  \***********************************************************/
/***/ (function(module, exports, __webpack_require__) {

var global         = __webpack_require__(/*! ./_global */ 0)
  , core           = __webpack_require__(/*! ./_core */ 10)
  , LIBRARY        = __webpack_require__(/*! ./_library */ 22)
  , wksExt         = __webpack_require__(/*! ./_wks-ext */ 29)
  , defineProperty = __webpack_require__(/*! ./_object-dp */ 3).f;
module.exports = function(name){
  var $Symbol = core.Symbol || (core.Symbol = LIBRARY ? {} : global.Symbol || {});
  if(name.charAt(0) != '_' && !(name in $Symbol))defineProperty($Symbol, name, {value: wksExt.f(name)});
};

/***/ }),
/* 57 */
/* unknown exports provided */
/*!******************************!*\
  !*** ./src/kio_api/kio.scss ***!
  \******************************/
/***/ (function(module, exports) {

// removed by extract-text-webpack-plugin

/***/ }),
/* 58 */,
/* 59 */,
/* 60 */
/* unknown exports provided */
/* all exports used */
/*!*****************************************************************************************!*\
  !*** multi ./~/babel-polyfill/~/core-js/modules/es6.symbol.js ./src/kio_api/kio_api.js ***!
  \*****************************************************************************************/
/***/ (function(module, exports, __webpack_require__) {

__webpack_require__(/*! ./node_modules/babel-polyfill/node_modules/core-js/modules/es6.symbol.js */33);
module.exports = __webpack_require__(/*! ./src/kio_api/kio_api.js */32);


/***/ })
/******/ ]);
//# sourceMappingURL=kio_api.js.map