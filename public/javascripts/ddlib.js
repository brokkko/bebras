/*
 Мини-библиотека для автоматизация типовых Drag-And-Drop задач.
 Константин Данилов / mail@xomak.net
 */

// From https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/keys
if (!Object.keys) {
    Object.keys = (function () {
        'use strict';
        var hasOwnProperty = Object.prototype.hasOwnProperty,
            hasDontEnumBug = !({toString: null}).propertyIsEnumerable('toString'),
            dontEnums = [
                'toString',
                'toLocaleString',
                'valueOf',
                'hasOwnProperty',
                'isPrototypeOf',
                'propertyIsEnumerable',
                'constructor'
            ],
            dontEnumsLength = dontEnums.length;

        return function (obj) {
            if (typeof obj !== 'object' && (typeof obj !== 'function' || obj === null)) {
                throw new TypeError('Object.keys called on non-object');
            }

            var result = [], prop, i;

            for (prop in obj) {
                if (hasOwnProperty.call(obj, prop)) {
                    result.push(prop);
                }
            }

            if (hasDontEnumBug) {
                for (i = 0; i < dontEnumsLength; i++) {
                    if (hasOwnProperty.call(obj, dontEnums[i])) {
                        result.push(dontEnums[i]);
                    }
                }
            }
            return result;
        };
    }());
}

/*
 Создает объект класса Place, описывающий экранный объект или место
 _x - x координата
 _y - y координата
 _width - ширина
 _height - высота
 _name - имя объекта (произвольное - несколько объектов могут иметь одинаковые имена)
 _type - Тип объекта (0 - место, куда можно перенести его. 1 - статический объект. 2 - перемещаемый объект)
 _vObject - визуальный объект
 Имеет следующие свойства:
 imageId,stroke,strokeWidth
 _beforeRender - функция, вызывающаяся перед рендерингом каждого элемента
 */
var Place = function (_x, _y, _width, _height, _name, _type, _vObject, _beforeRender) {
    var type = (_type) ? _type : 0;
    return {
        x: _x,
        y: _y,
        width: _width,
        height: _height,
        name: _name,
        vObject: (_vObject ? _vObject : {imageId: false, stroke: "000000", strokeWidth: 2}),
        getType: function () {
            return type;
        },
        beforeRender: (_beforeRender ? _beforeRender : false)
    };
};

/*
 Создает объект класса App, описывающий всё приложение
 elementId - id элемента холста
 _width - ширина
 _height - высота
 _pictures - массив адресов изображений, которые будут использоваться в приложении
 _places - массив объектов Place
 */
var App = function (elementID, _width, _height, _pictures, _places, _auto_start) {
    var stage = new Kinetic.Stage({
        container: elementID,
        width: _width,
        height: _height
    });

    var size = [_width, _height];
    var placesLayer = new Kinetic.Layer();
    var greyLayer = new Kinetic.Layer();

    var magnetPlaces = [];
    var pictures = _pictures;
    var places = _places;

    var disabledCallback = false;
    var initCallback = false;
    var enabled = true;

    for (var key = 0; key < places.length; key++)
        places[key].id = key;

    var picturesLoaded = 0;
    var objects = [];

    var pictureLoaded = function () {
        picturesLoaded++;
        if (picturesLoaded == Object.keys(pictures).length) {
            drawPlaces();
        }
    };

    var loadPictures = function () {
        for (var key in pictures)
            if (pictures.hasOwnProperty(key)) {
                objects[key] = new Image();
                objects[key].onload = pictureLoaded;
                objects[key].src = pictures[key];
            }
    };

    var greyClicked = function () {
        if (disabledCallback) disabledCallback();
    };

    var drawPlaces = function () {
        for (var key = 0; key < places.length; key++) {
            var place = places[key];
            var object;

            if (place.vObject.imageId) {
                var imgConfig = {
                    x: place.x,
                    y: place.y,
                    width: place.width,
                    height: place.height,
                    image: objects[place.vObject.imageId]
                };
                if (place.vObject.crop)
                    imgConfig.crop = place.vObject.crop;
                object = new Kinetic.Image(imgConfig);
            } else
                object = new Kinetic.Rect({
                    x: place.x,
                    y: place.y,
                    width: place.width,
                    height: place.height,
                    strokeWidth: place.vObject.strokeWidth,
                    stroke: place.vObject.stroke
                });

            //empty place
            if (place.getType() == 0)
                magnetPlaces[place.id] = {x: place.x, y: place.y, current: false, id: place.id};

            //draggable object
            if (place.getType() == 2) {
                object.setDraggable("true");
                object.ref = place;
                object.is_dragging = false;

                object.on('dragstart', function () {
                    this.is_dragging = true;
                    this.setZIndex(1000);
                    if (this.ref.current) {
                        magnetPlaces[this.ref.current].current = false;
                        this.ref.current.current = false;
                        this.ref.current = false;
                    }
                });

                object.on('dragend', function () {
                    if (!this.is_dragging)
                        return;
                    this.is_dragging = false;
                    var minDist = -1;
                    var minPlaceKey;

                    var x = this.getX();
                    var y = this.getY();
                    for (var key2 = 0; key2 < places.length; key2++) {
                        if (!(key2 in magnetPlaces))
                            continue;
                        var magnetPlace = magnetPlaces[key2];
                        if (!magnetPlace.current) {
                            var dist = Math.sqrt((x - magnetPlace.x) * (x - magnetPlace.x) + (y - magnetPlace.y) * (y - magnetPlace.y));
                            if (dist < minDist || minDist == -1) {
                                minDist = dist;
                                minPlaceKey = key2;
                            }
                        }
                    }
                    if (minDist != -1 && (minDist < this.getWidth() || minDist < this.getHeight())) {
                        var magnetPlaceId = magnetPlaces[minPlaceKey].id;
                        var dstX = places[magnetPlaceId].x + places[magnetPlaceId].width / 2 - this.getWidth() / 2;
                        var dstY = places[magnetPlaceId].y + places[magnetPlaceId].height / 2 - this.getHeight() / 2;
                        this.transitionTo({x: dstX, y: dstY, duration: 0.3});
                        magnetPlaces[minPlaceKey].current = this.ref.id;
                        this.ref.current = minPlaceKey;
                    } else
                        this.transitionTo({x: this.ref.x, y: this.ref.y, duration: 0.1});
                });
            }

            if (place.beforeRender)
                place.beforeRender(object);
            placesLayer.add(object);
            place.screenObject = object;
        }

        stage.add(placesLayer);

        //grey layer

        var rect = new Kinetic.Rect({
            width: size[0],
            height: size[1],
            fill: '#eeeeee',
            strokeWidth: 0
        });

        rect.on('click', greyClicked);
        greyLayer.setOpacity(0.7);
        greyLayer.add(rect);

        stage.add(greyLayer);
        greyLayer.setVisible(false);

        if (initCallback)
            initCallback();
    };

    var task = {
        //Функция для старта
        start: function () { //not needed in all dyn problems
            loadPictures();
        },
        setDisabledCallback: function (_disabledCallback) { //not needed (used?) in all dyn problems
            disabledCallback = _disabledCallback;
        },
        setInitCallback: function (_initCallback) {
            initCallback = _initCallback;
        },
        isEnabled: function () {
            return enabled;
        },
        setEnabled: function (state) {
            enabled = state;
            //TODO do not set visibility if layer is still not put on the stage
            greyLayer.setVisible(!state);
            return true;
        },
        getSize: function () { // not used in all dyn problems
            return size;
        },
        getSolution: function () {
            var returnObject = {};
            var busyPlaceFound = false;
            for (var key = 0; key < places.length; key++) {
                if (!(key in magnetPlaces))
                    continue;
                var place = magnetPlaces[key];
                returnObject[place.id] = place.current ? place.current : -1;
                if (place.current) busyPlaceFound = true;
            }
            if (!busyPlaceFound) return "";
            else return JSON.stringify(returnObject);
        },
        loadSolution: function (solution) {
            for (var key = 0; key < places.length; key++) {
                if (!(key in magnetPlaces))
                    continue;
                var currentMagnet = magnetPlaces[key];
                if (currentMagnet.current) {
                    places[currentMagnet.current].current = false;
                    places[currentMagnet.current].screenObject.setX(places[currentMagnet.current].x);
                    places[currentMagnet.current].screenObject.setY(places[currentMagnet.current].y);
                    currentMagnet.current = false;
                }
            }
            if (solution.length != 0) {
                var solutionObject = JSON.parse(solution);
                for (key = 0; key < places.length; key++) {
                    if (!(key in solutionObject))
                        continue;
                    var objectId = solutionObject[key];
                    if (objectId != -1) {
                        magnetPlaces[key].current = objectId;
                        var magnetPlaceId = magnetPlaces[key].id;
                        var dstX = places[magnetPlaceId].x + places[magnetPlaceId].width / 2 - places[objectId].screenObject.getWidth() / 2;
                        var dstY = places[magnetPlaceId].y + places[magnetPlaceId].height / 2 - places[objectId].screenObject.getHeight() / 2;
                        places[objectId].screenObject.setX(dstX);
                        places[objectId].screenObject.setY(dstY);
                        places[objectId].current = key;
                    }
                }
            }
            placesLayer.draw();
            return true;
        },
        getAnswer: function () {
            return 2;
        },
        getOutput: function () { //not needed in all dyn problems
            var returnObject = {};
            for (var key = 0; key < places.length; key++) {
                if (!(key in magnetPlaces))
                    continue;
                var place = magnetPlaces[key];
                returnObject[places[place.id].name] = place.current ? places[place.current].name : -1;
            }
            return returnObject;
        }
    };

    if (_auto_start === true)
        task.start();

    return task;
};