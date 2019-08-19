function collapse() {
    return {
        newCollapseZone: function (oneAtATime) {
            var _collapseFlags = {};
            var _zoneId = null;
            var _elems = {};

            var _toggle = function (elemID, toggle) {
                var elem = _elems[elemID];
                if (elem) {
                    var tog;
                    if (toggle === true || toggle === false) {
                        tog = !!toggle;
                    } else {
                        tog = !_collapseFlags[elemID];
                    }
                    if (tog) {
                        if (elem.hasClass("sg-collapse")) {
                            elem.removeClass("out");
                            var h = elem.height();
                            elem.css({height: 0});
                            elem.addClass("transitioning");
                            elem[0].offsetHeight;
                            elem.css({height: h});
                            setTimeout(function () {
                                elem.removeClass("transitioning");
                                elem.css({height: "auto"});
                            }, 340);
                        } else {
                            elem.addClass("sg-collapse");
                        }
                    } else {
                        if (elem.hasClass("sg-collapse")) {
                            elem.css({height: elem.height()});
                            elem.addClass("transitioning");
                            elem[0].offsetHeight;
                            elem.css({height: 0});
                            setTimeout(function () {
                                elem.removeClass("transitioning");
                                elem.addClass("out");
                                elem.css({height: "auto"});
                            }, 340);
                        } else {
                            elem.addClass("sg-collapse")
                            elem.addClass("out");
                        }
                    }
                    _collapseFlags[elemID] = tog;
                    localStorage["collapse:" + _zoneId] = JSON.stringify(_collapseFlags);
                }
            };

            return {
                _setZoneId: function (id) {
                    if (_zoneId) {
                        localStorage["collapse:" + id] = JSON.stringify(_collapseFlags);
                        delete localStorage["collapse:" + +_zoneId];
                        _zoneId = id;
                    } else {
                        _zoneId = id;
                        var existingData = localStorage["collapse:" + _zoneId];
                        if (existingData) {
                            _collapseFlags = JSON.parse(existingData);
                        }
                    }
                },
                _registerElem: function (id, elem) {
                    _elems[id] = elem;
                    this.toggle(id, this.isOpen(id));
                },
                _renameElem: function (oldId, newId) {
                    _elems[newId] = _elems[oldId];
                    delete _elems[oldId];
                    _collapseFlags[newId] = _collapseFlags[oldId];
                    delete _collapseFlags[oldId];
                },
                isCollapsed: function (elemID) {
                    return !_collapseFlags[elemID];
                },
                isOpen: function (elemID) {
                    return !!_collapseFlags[elemID];
                },
                toggle: function (elemID, toggle) {
                    if(oneAtATime) {
                        _toggle(elemID, toggle);
                        for(var id in _elems) {
                            if(id != elemID && ( elemID.indexOf(id) != 0 && id.indexOf('/') - 1 != elemID.length ) ) {
                                _toggle(id, false);
                            }
                        }
                    } else {
                        _toggle(elemID, toggle);
                    }
                }
            }
        }
    }
}

export default collapse;
