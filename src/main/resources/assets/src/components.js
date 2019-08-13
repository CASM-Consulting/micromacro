import {DatumFactory} from './ts/DatumFactory';

function initializeComponents(MicroMacroApp) {
    console.log("initializing components");

    MicroMacroApp.component("filter", {
        templateUrl : 'html/filter.html',
        require : 'ngModel',
        bindings : {
            key : '<'
        },
        controller : function(Filters) {
            var $ctrl = this;

            $ctrl.$onInit = () => {
                $ctrl.filters = Filters.forType($ctrl.key);
            }

        }
    });
    MicroMacroApp.component("literal", {
        templateUrl : 'html/components/literal.html',
        bindings : {
            literal : '<',
            keys : '<'
        },
        controller : function() {
            var $ctrl = this;
            $ctrl.displayName = (key) => {
                var displayName = "";
                if(key.namespace) {
                    displayName += key.namespace+"/";
                }
                displayName += key.name;
                return displayName;
            };
        }
    });
    MicroMacroApp.component("literals", {
        templateUrl : 'html/components/literals.html',
        bindings : {
            literals : '<',
            keys : '<',
            table : '<?',
            workspace : '<?'
        },
        controller : function(Queries) {
            var $ctrl = this;

            $ctrl.addLiteral = function() {
                $ctrl.literals[$ctrl.letter] = {
                    key: $ctrl.key,
                    type: $ctrl.type,
                    args: $ctrl.args
                };
                if($ctrl.workspace && $ctrl.table) {
                    Queries.setTableLiterals($ctrl.workspace, $ctrl.table, $ctrl.literals);
                }
            };

            $ctrl.setLiteral = function(letter, literal) {
                $ctrl.letter = letter;
                $ctrl.key = literal.key;
                $ctrl.type = literal.type;
                $ctrl.args = literal.args;
                $ctrl.isUncollapsed = true;
            };

            $ctrl.$onInit = () => {
                console.log($ctrl.literals);
            };

            $ctrl.deleteLiteral = (letter) => {
                delete $ctrl.literals[letter];
                if($ctrl.workspace && $ctrl.table) {
                    Queries.setTableLiterals($ctrl.workspace, $ctrl.table, $ctrl.literals);
                }
            };
        }
    });
    MicroMacroApp.component('mapConfig', {
        templateUrl : 'html/components/mapConfig.html',
        bindings : {
            map: '<?',
            queryList: '<',
            workspace: '<'
        },
        controller : function($scope, $state, $q, Tables, Queries, Maps, $stateParams) {

            var $ctrl = this;

            $scope.showNotes = true;

            
            $ctrl.tableKeyCache = {};
            $ctrl.queryTableCache = {};

            $ctrl.$onInit = () => {
                $ctrl.map || ($ctrl.map = {
                    queries : []
                });

                $ctrl.map.id = $stateParams.mapId;

                $ctrl.selectedQueries = {};

                $ctrl.map.queries.forEach(query=> {
                    $ctrl.selectedQueries[query] = true;
                });

                $ctrl.keyList = [];

                $scope.$watchCollection("$ctrl.selectedQueries", function(selected, old){
                    $ctrl.getKeys();
                });
            };


            var promiseTable = (query) => {
                if(query in $ctrl.queryTableCache) {
                    return $q( (r) => {
                        r($ctrl.queryTableCache[query]);
                    });
                } else {
                    return Queries.load($stateParams.workspaceId, query)
                        .then((data)=>{
                            $ctrl.queryTableCache[query] = data.table;
                            return data.table;
                        });
                }
            };

            var promiseSchema = (table) => {
                if(table in $ctrl.tableKeyCache) {
                    return $q( (r) => {
                        r($ctrl.tableKeyCache[table]);
                    });
                } else {
                    return Tables.schema(table)
                        .then((keys)=> {
                            $ctrl.tableKeyCache[table] = keys;
                            return keys;
                        });
                }
            };


            $ctrl.getKeys = () => {

                var dateKey = $ctrl.map.dateKey;
                var tablePromises = [];

                $ctrl.keyList = [];

                angular.forEach($ctrl.selectedQueries, (on, query) => {
                    on && tablePromises.push(promiseTable(query));
                });

                $q.all(tablePromises).then((tables) => {

                    var schemaPromises = tables.map( (table) => {
                        return promiseSchema(table);
                    });

                    $q.all(schemaPromises).then( (keyss) => {
                        var keys = keyss[0];
                        var rest = keyss.slice(1);
                        if(rest.length > 0) {
                            rest.reduce((keys, moreKeys) => {
                                angular.forEach(keys, (key, name) => {
                                    if(!(name in moreKeys)) {
                                        delete keys[name];
                                    }
                                });
                                return keys;
                            }, keys);
                        }
                        $ctrl.keyList = keyList(keys);
                        $ctrl.map.dateKey = dateKey;
                    })
                });
            }


            var keyList = (keys) => {
                var keyList = [];
                angular.forEach(keys, (item, key) => {
                    var listItem = angular.copy(item);
                    listItem.id = listItem.key();
                    keyList.push(listItem);
                });
                keyList.sort((a,b)=>{return a.id.localeCompare(b.id)});
                return keyList;
            }

            $ctrl.show = () => {

                $ctrl.map.queries = [];

                angular.forEach($ctrl.selectedQueries, (on, query) => {
                    on && $ctrl.map.queries.push(query);
                });

                Maps.save($stateParams.workspaceId, $ctrl.map.id, $ctrl.map).then(function(map){
                    var target = "workspace.maps.map.show";
                    $state.transitionTo(target,
                                        {mapId: $ctrl.map.id, map:$ctrl.map},
                                        {reload: true, inherit:true, relative: $state.$current}
                                       );
                });
            }
        }
    });

    MicroMacroApp.component('map', {
        templateUrl : 'html/components/map.html',
        bindings : {
            map : '<'
        },
        controller : function(Datums, $q, $stateParams, $http, $compile, leafletData, debounce, $window, Queries, Maps) {
            var $ctrl = this;

            var colours = ['purple', 'green', 'red', 'blue', 'orange', 'black'];


            var DATE_FORMAT = 'YYYY-MM-DD';

            var tilesDict = {
                openstreetmap: {
                    url: "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                },
                oldlondon: {
                    url: "https://nls-2.tileserver.com/fpsUZba7ERPD/{z}/{x}/{y}.png"
                }
            };


            $ctrl.saveMap = () => {
                Maps.save($stateParams.workspaceId, $ctrl.map.id, $ctrl.map).then(function(map){
                    //ok?
                });
            }

            $ctrl.changeTiles = function(tiles) {
                $ctrl.leafletMap.tiles = tilesDict[tiles];
            };

            $ctrl.leafletMap = {
                center: {
                    lat: 51.51,
                    lng: -0.11,
                    zoom: 12
                },

                defaults: {
                    //                scrollWheelZoom: true,
                    minZoom: 10,
                    maxZoom: 14,
                    wheelDebounceTime: 100,
                    //                scrollWheelZoom:false,
                    zoomSnap: 0,
                    zoomDelta: 0.1,
                    wheelPxPerZoomLevel : 10
                },

                tiles: tilesDict.oldlondon,

                layers: {
                    baselayers : {},
                    overlays: {}
                }

            };
            
            $ctrl.timelineDuration = 100;

            var timelines = [];
            var timelineControl = null;

            $ctrl.queries = [];

            $ctrl.$onInit = () => {

                //            $ctrl.map = $ctrl.map || {id:'123', queries:['select1']};

                $ctrl.map.options.heat = $ctrl.map.options.heat || {radius : 10, intensity : 1, history :20, blur:5};

                $ctrl.setHeatBlur = () => {
                    leafletData.getLayers().then(function(layers) {
                        if(layers.overlays.heat ) {
                            layers.overlays.heat.setOptions({blur:$ctrl.map.options.heat.blur});
                        }
                        $ctrl.saveMap();
                    });
                };

                $ctrl.setHeatRadius = () => {
                    leafletData.getLayers().then(function(layers) {
                        if(layers.overlays.heat) {
                            layers.overlays.heat.setOptions({radius:$ctrl.map.options.heat.radius});
                        }
                        $ctrl.saveMap();
                    });
                };


                $ctrl.geoKey = Datums.key($ctrl.map.geoKey).key();

                Maps.getQueries($stateParams.workspaceId, $ctrl.map).then((queries) => {
                    
                    $ctrl.queries = queries;
                    
                    return Maps.getDateLimits(queries, $ctrl.map).then((bounds) => {
                        $ctrl.minDate = moment(bounds[0]);
                        $ctrl.maxDate = moment(bounds[1]);
                    });

                }).then(() => {
                    
                    drawTimelines();
                });


            }

            var drawTimelines = () => {

                var heatCache = {};

                timelines.forEach( timeline => {
                    if(timeline) {
                        timeline.remove();        
                    }
                });

                if(timelineControl) {
                    timelineControl.remove();
                }

                leafletData.getMap().then(function(map) {

                    var dates = [];
                    var times = [];

                    var date = moment($ctrl.minDate);

                    while(date <= $ctrl.maxDate) {
                        var tmp = moment(date);
                        dates.push(tmp.format(DATE_FORMAT));
                        times.push(tmp.add(1000, "y").toDate().getTime())
                        date.add(1, 'days');
                    }

                    var daysCovered = $ctrl.maxDate.diff($ctrl.minDate, 'days');

                    timelineControl = L.timelineSliderControl({
                        steps: daysCovered,
                        start : $ctrl.minDate.add(1000, "y").toDate().getTime(),
                        end: $ctrl.maxDate.add(1000, "y").toDate().getTime(),
                        duration : times.length * 120,

                        // duration : daysCovered * $ctrl.timelineDuration,
                        //                enableKeyboardControls: true,
                        formatOutput: function(date){
                            return moment(date).subtract(1000, "y").format(DATE_FORMAT);
                        }
                    });
                    
                    $ctrl.lineChartData = [];
                    $ctrl.lineChartOptions = {
                        chart : {
                            height: 200,
                            x: (d) => {
                                return moment(d[0]).toDate();
                            },
                            y: (d) => {
                                return d[1];
                            },
                            xAxis: {
                                tickFormat: (d) => {
                                    return d3.time.format('%Y-%m-%d')(new Date(d));
                                },
                                axisLabel: 'Date'
                            },
                        }
                    };

                    var lineChartPromises = $ctrl.queries.map( (query, idx) => {

                        return Queries.counts(query, dates).then(counts => {
                            $ctrl.lineChartData.push({
                                key : query._id,
                                values : counts
                            });
                        });
                    });

                    $q.all(lineChartPromises).then( () => {
                        $ctrl.lineChartDataReady = true;
                    });

                    var drawHeat = (date) => {

                        var from = moment(date).subtract($ctrl.map.options.heat.history, 'days');//.format(DATE_FORMAT);
                        var to = moment(date);//.format(DATE_FORMAT);

                        var dates = [];
                        var data = [];

                        var date = moment(from);

                        while(date <= to) {
                            var tmp = moment(date);
                            dates.push(tmp.format(DATE_FORMAT));
                            date.add(1, 'days');
                        }

                        var removeIdxs = [];
                        var newCache = {};
                        for(var i = 0; i < dates.length; ++i) {
                            var date = dates[i];
                            if( date in heatCache ) {
                                removeIdxs.push(i);
                                newCache[date] = heatCache[date];
                            }
                        }

                        heatCache = newCache;

                        for(var i = removeIdxs.length-1; i >= 0; --i){
                            var idx = removeIdxs[i];
                            dates.splice(idx, 1);
                        }

                        var promises = $ctrl.queries.map((query) => {
                            return Queries.partitions(query, dates);
                        });

                        $q.all(promises).then(( queryResults )=>{
                            var merged = {};
                            dates.forEach( ( date ) => {
                                merged[date] = [];
                                queryResults.forEach( (queryResult) => {
                                    merged[date] = merged[date].concat(queryResult[date]);
                                });

                                merged[date] = Maps.data2geoJson(merged[date], $ctrl.geoKey);
                            });

                            angular.forEach(merged, (value, key)=> {
                                heatCache[key] = value;
                            });

                            var i = 0;

                            for(var d = moment(from); d.diff(to, 'days') <= 0; d.add(1, 'days') ) {
                                var dayDate = moment(d).format(DATE_FORMAT);
                                if(dayDate in heatCache) {
                                    var dayData = heatCache[dayDate].features;

                                    var intensity = (i / $ctrl.map.options.heat.history) * $ctrl.map.options.heat.intensity;

                                    for(var j = 0; j < dayData.length; ++j) {
                                        var point = [dayData[j].geometry.coordinates[1],dayData[j].geometry.coordinates[0]];
                                        data.push(point.concat(intensity));
                                    }
                                }

                                ++i;
                            }
                            if(!data) return;

                            leafletData.getLayers().then(function(layers) {
                                if(layers.overlays.heat) {
                                    if(layers.overlays.heat._map) {
                                        layers.overlays.heat.setLatLngs(data);
                                    }
                                } else {
                                    var heatOptions = {
                                        name: 'Heat Map',
                                        type: 'heat',
                                        data: data,
                                        layerOptions: {
                                            radius: $ctrl.map.options.heat.radius,
                                            blur: $ctrl.map.options.heat.blur
                                        },
                                        visible: true
                                    };

                                    //                                L.heatLayer(heatOptions).addTo(map);
                                    //
                                    $ctrl.leafletMap.layers.overlays = {
                                        heat : heatOptions
                                    };
                                }
                            });
                        });
                    }

                    timelines = $ctrl.queries.map( (query, idx) => {



                        var timeline = L.timeline(null, {
                            start : $ctrl.minDate.add(1000, "y").toDate().getTime(),
                            end: $ctrl.maxDate.add(1000, "y").toDate().getTime(),
                            // getInterval: getInterval,
                            drawOnSetTime: true,
                            pointToLayer: function(data, latlng) {
                                var colour = colours[idx];

                                return L.circleMarker(latlng, {radius:5, color:colour}).bindPopup(function(l) {
                                    return "<ul>" +
                                        "<li>Match: " + data.metadata.with[0].match + "</li>"+
                                        // "<li>Original: " + data.metadata.spanned + "</li>"+
                                    "<li>Lat: " + latlng.lat + "</li>"+
                                        "<li>Lng: " + latlng.lng+ "</li>"+
                                        // "<li>Date: " + data.metadata.date + "</li>"+
                                    // "<li>Trial: " + data.metadata.trialId + "</li>"+
                                    "</ul>";
                                });
                            }
                        });
                        timeline.times = times;
                        timelineControl.addTo(map);
                        timelineControl.addTimelines(timeline);
                        timeline.addTo(map);
                        timeline.on('change', function(e){
                            $ctrl.selectedDate = moment(e.target.time).subtract(1000, "y").toDate();
                            Maps.getData(query, $ctrl.map, $ctrl.selectedDate)
                                .then(featureCollection => {
                                    timeline.addData(featureCollection);
                                });
                            drawHeat($ctrl.selectedDate);
                        });
                        return timeline;
                    });

                    var overlays = {};
                    
                    for(var i = 0; i < $ctrl.queries.length; ++i) {
                        overlays[$ctrl.queries[i]._id] = timelines[i];
                    }

                    L.control.layers(null, overlays).addTo(map);
                });
            };
        }
    });
    MicroMacroApp.component('maps', {
        templateUrl : 'html/components/maps.html',
        bindings : {
            workspace : '<',
            mapList : '<',
            queryList : '<'
        },
        controller: function($scope, $state, $stateParams, Queries, spinnerService, Workspaces, Tables) {
            var $ctrl = this;
            $ctrl.workspaceId = $stateParams.workspaceId;

            var firstLoad =  $state.$current.name.endsWith("show");
            $ctrl.$onInit = () => {
                $ctrl.active = $ctrl.mapList.indexOf($stateParams.mapId);
                $ctrl.loadMap = (name) => {
                    firstLoad || $state.go('workspace.maps.map', {workspaceId:$stateParams.workspaceId, mapId: name});
                    firstLoad = false;
                }
            }


        }
    });



    //MicroMacroApp.component('notes', {
    //    templateUrl : 'html/note.html',
    //    bindings : {
    //        note : '<',
    //        collapsed : '<'
    //    },
    //    controller: function($$scope, $stateParams) {
    //
    //        $scope.height = 200;
    //    }
    //});

    MicroMacroApp.component('queryNotes', {
        templateUrl: 'html/components/notes.html',
        bindings: {
            notes: '<'
        },
        controller : function($scope, $element, $document, $window, $timeout, Queries, $stateParams) {

            $scope.show = true;

            var $ctrl = this;
            var elem = $element;

            var saveNotes = $ctrl.onChange = $scope.onChange = () => {
                Queries.setMeta($stateParams.workspaceId, $stateParams.queryId, "notes", $ctrl.notes);
            };

            $ctrl.$onInit = () => {
                //            alert($ctrl.notes);
            }

            $scope.addNote = function(note){
                $scope.showNotes = true;
                if (!$ctrl.notes){
                    $ctrl.notes = [];
                }
                $ctrl.notes.push(note || {"w":140,"x":400,"h":75,"y":400,"text":""});
                saveNotes();

                //            focusNewNote();
            };


            // Necessary for updating positions correctly after a note is removed from lower in the list
            $scope.getNoteShape = function(idx) {
                return {
                    top: $ctrl.notes[idx].y,
                    left: $ctrl.notes[idx].x,
                    width: $ctrl.notes[idx].w,
                    height: $ctrl.notes[idx].h,
                    'background-color': $ctrl.notes[idx].colour || '#ffd59b'
                };
            };

            $scope.copyNote = function(idx) {
                var xOffset = 30; // Npx to the right of original
                var yOffset = 30; // Npx to the right of original

                var newNote = {
                    x: $ctrl.notes[idx].x + xOffset,
                    y: $ctrl.notes[idx].y + yOffset,
                    w: $ctrl.notes[idx].w,
                    h: $ctrl.notes[idx].h,
                    text: $ctrl.notes[idx].text,
                };
                if ($ctrl.notes[idx].colour){
                    newNote.colour = $ctrl.notes[idx].colour;
                }
                $ctrl.notes.push(newNote);
                $timeout(function(){
                    $scope.focusNote($ctrl.notes.length - 1);
                })
                $scope.onChange();
            };

            $scope.removeNote = function(idx) {

                if ($ctrl.notes[idx].text && $ctrl.notes[idx].text.trim().length) {
                    var del = confirm("Are you sure you want to permanently delete this note and its contents?");
                    if(del) {
                        $ctrl.notes.splice(idx, 1);
                        $scope.onChange();
                    }
                    //                    var modal = Modals.open("confirm", {
                    //                        title: "Delete note?",
                    //                        message: "Are you sure you want to permanently delete this note and its contents?",
                    //                        confirmText: "Delete"
                    //                    });
                    //                    modal.on("confirm", function () {
                    //                        $ctrl.notes.splice(idx, 1);
                    //                        $scope.onChange();
                    //                    });
                } else {
                    // Note empty, don't ask for confirmation...
                    $ctrl.notes.splice(idx, 1);
                    $scope.onChange();
                }
            };

            $ctrl.focusNote = function(idx) {
                angular.forEach(elem.find(".basic-note"), function(note, noteIndex){
                    var noteElem = angular.element(note);
                    noteElem.css({"z-index": noteIndex==idx? 100 : 90});
                });
            };

            $ctrl.focusNoteElem = function(noteElem1) {
                angular.forEach(elem.find(".basic-note"), function(note, noteIndex){
                    var noteElem2 = angular.element(note);
                    noteElem2.css({"z-index": noteElem1[0] == noteElem2[0]? 100 : 90});
                });
            };
        }
    });


    // Modeled on componentBox
    MicroMacroApp.component('basicNote', {
        bindings: {
            idx : "<"
        },
        require: {
            notesCtrl : '^queryNotes'
        },
        controller : function ($scope, $element, $attrs, $document, $timeout){
            var scope = $scope;
            var elem = $element;
            var attrs = $attrs;
            var $ctrl = this;

            var idx;

            var MIN_HEIGHT = 28;
            var MIN_WIDTH = 40;

            var props;

            var loaded = false;

            $ctrl.$onInit = () => {
                //            alert($ctrl.notes);
                idx = parseInt($ctrl.idx)

                props = $ctrl.notesCtrl.notes[idx];

                canvasW = elem.parent().parent().parent().width();
                canvasH = elem.parent().parent().parent().parent().height();
                // Because the ng-style keeps these up to date, so trust them over props

                props = limitPos(props);

                applyProps();

            }


            var applyProps = function(){
                elem.css({
                    top: props.y,
                    left: props.x,
                    width: props.w,
                    height: props.h
                });
                var newProps = angular.copy(props);
                newProps["text"] = $ctrl.notesCtrl.notes[idx]["text"];
                newProps["colour"] = $ctrl.notesCtrl.notes[idx]["colour"];
                $ctrl.notesCtrl.notes[idx] = newProps;
            }

            var unbind = scope.$watch("notes["+idx+"]", function (val) {
                if (val && !loaded) {
                    loaded = true;
                    props = val;
                    elem.css({top: props.y, left: props.x, width: props.w, height: props.h});
                    unbind(); // Once loaded, this doesn't seem to be necessary
                }
            });

            var downX;
            var downY;

            var downLeft;
            var downTop;

            var downW;
            var downH;

            var canvasW;
            var canvasH;

            scope.handleTabKey = function(e) {
                if (e.which === 9) {
                    e.preventDefault();
                    thisElem = angular.element(e.target);
                    var start = e.target.selectionStart;
                    var end = e.target.selectionEnd;
                    props.text = thisElem.val().substring(0, start) + '    ' + thisElem.val().substring(end);
                    thisElem.val(props.text);
                    e.target.selectionStart = e.target.selectionEnd = start + 4;
                    applyProps();
                }
            }

            var doDrag = function (fns) {
                return function (event) {
                    $ctrl.notesCtrl.focusNoteElem(elem);

                    downX = event.pageX;
                    downY = event.pageY;
                    downLeft = elem[0].offsetLeft;
                    downTop = elem[0].offsetTop;
                    //                    downW = elem.width();
                    //                    downH = elem.height();
                    // More accurate widths, the ones above were reporting a slightly too small number
                    downW = elem[0].offsetWidth;
                    downH = elem[0].offsetHeight;
                    canvasW = elem.parent().parent().parent().width();
                    canvasH = elem.parent().parent().parent().parent().height();
                    // Because the ng-style keeps these up to date, so trust them over props
                    props = {x: downLeft, y: downTop, w: downW, h: downH};

                    $(window).one("mouseup", function () {
                        window.onmousemove = undefined;
                        $ctrl.notesCtrl.onChange();
                    });
                    window.onmousemove = function (event) {
                        var i = fns.length;
                        while (i--) {
                            fns[i](event);
                        }
                        applyProps();
                        return false;
                    };
                    return false;
                };
            };

            var limitPos = function(box){
                box.x = Math.max(10, Math.min(canvasW-box.w-5, Math.max(5, box.x)));
                box.y = Math.max(10, Math.min(canvasH-box.h-5, Math.max(5, box.y)));
                return box;
            };

            var onMove = function(event) {
                var diffX = event.pageX - downX;
                var diffY = event.pageY - downY;
                var newLeft = downLeft + diffX;
                var newTop = downTop + diffY;

                props = props || {y: 0, x : 0, w : 100, h : 100};

                var newProps = angular.copy(props);
                newProps.x = newLeft;
                newProps.y = newTop;

                props = limitPos(newProps);
            };

            var onResizeWest = function (event) {
                var diffX = downX - event.pageX;
                var newLeft = downLeft - diffX;
                var newWidth = downW + diffX;

                if (newLeft < 0) {
                    newWidth += newLeft;
                    newLeft = 0;
                }

                if (newWidth < MIN_WIDTH) {
                    newLeft -= MIN_WIDTH - newWidth;
                    newWidth = MIN_WIDTH;
                }

                props.x = Math.max(0, newLeft);
                props.w = newWidth;
            };

            var onResizeEast = function (event) {
                var diffX = event.pageX - downX;
                var newWidth = downW + diffX;

                if (downLeft + newWidth > canvasW) {
                    newWidth = canvasW - downLeft;
                }

                if (newWidth  < MIN_WIDTH) {
                    newWidth = MIN_WIDTH;
                }

                props.w = newWidth;
            };

            var onResizeNorth = function (event) {
                var diffY = downY - event.pageY;
                var newTop = downTop - diffY;
                var newHeight = downH + diffY;
                if (newTop < 0) {
                    newHeight += newTop;
                    newTop = 0;
                }

                if (newHeight < MIN_HEIGHT) {
                    newTop -= MIN_HEIGHT-newHeight;
                    newHeight = MIN_HEIGHT;
                }

                props.y = Math.max(0, newTop);
                props.h = newHeight;
            };

            var onResizeSouth = function (event) {
                var diffY = event.pageY - downY;
                var newHeight = downH + diffY;

                if (downTop + newHeight > canvasH) {
                    newHeight = canvasH - downTop;
                }

                if (newHeight < MIN_HEIGHT) {
                    newHeight = MIN_HEIGHT;
                }

                props.h = newHeight;
            };


            var isPopoverOpen = function(){
                var popoverScope = elem.find(".popover").scope();
                return popoverScope && popoverScope.isOpen? true : false;
            }

            var closeColorPopover = function(event){
                if (elem.find(".basic-note-colour")[0] != event.target) {
                    var popoverScope = elem.find(".popover").scope();
                    if (popoverScope && popoverScope.isOpen){
                        $timeout(function(){
                            elem.find(".basic-note-colour").click();
                        }, 0);
                    }
                    clickAwayReady = false;
                    $document.off('mousedown', closeColorPopover);
                }
            };

            var clickAwayReady = false;
            elem.find(".basic-note-colour").on('click', function(){
                if (isPopoverOpen() && !clickAwayReady){
                    $document.on('mousedown', closeColorPopover);
                    clickAwayReady = true;
                }
            });

            elem.find(".basic-note-title ").on("mousedown", doDrag([onMove]));

            elem.find('.resize-ns.north').on('mousedown', doDrag([onResizeNorth]));
            elem.find('.resize-ns.south').on('mousedown', doDrag([onResizeSouth]));
            elem.find('.resize-we.west').on('mousedown', doDrag([onResizeWest]));
            elem.find('.resize-we.east').on('mousedown', doDrag([onResizeEast]));

            elem.find('.resize-diag.nw').on('mousedown', doDrag([onResizeNorth, onResizeWest]));
            elem.find('.resize-diag.ne').on('mousedown', doDrag([onResizeNorth, onResizeEast]));
            elem.find('.resize-diag.sw').on('mousedown', doDrag([onResizeSouth, onResizeWest]));
            elem.find('.resize-diag.se').on('mousedown', doDrag([onResizeSouth, onResizeEast]));

            elem.css({"z-index": 110});

        }
    }
                           );MicroMacroApp.component('queries', {
                               templateUrl : 'html/components/queries.html',
                               bindings : {
                                   workspace : '<',
                                   queryList : '<',
                                   tables : '<'
                               },
                               controller: function($scope, $state, $stateParams, Queries, spinnerService, Workspaces, Tables) {
                                   var $ctrl = this;
                                   $ctrl.workspaceId = $stateParams.workspaceId;

                                   var firstLoad = $state.$current.name.endsWith("execute");

                                   $ctrl.$onInit = () => {
                                       $ctrl.active = $ctrl.queryList.indexOf($stateParams.queryId);
                                       $ctrl.loadQuery = (name) => {
                                           firstLoad || $state.go('workspace.queries.query', {workspaceId:$stateParams.workspaceId, queryId: name});
                                           firstLoad = false;
                                       }

                                       $ctrl.optimise = (query) => {
                                           Queries.optimise(query).then((resp)=>{
                                               alert(resp);
                                           });
                                       }

                                       $ctrl.clearCache = (queryId) => {
                                           Workspaces.clearCache($stateParams.workspaceId, queryId)
                                       }

                                       $ctrl.tableKeys = {};
                                       $ctrl.tableList = [];

                                       angular.forEach($ctrl.workspace.tableLiterals, (key, table) => {
                                           Tables.schema(table, true).then( (keys) => {
                                               $ctrl.tableKeys[table] = keys;
                                           });
                                           $ctrl.tableList.push(table);
                                       });

                                       $ctrl.workspace.tableLiterals = $ctrl.workspace.tableLiterals || {};
                                   }


                                   $ctrl.addTable = (table) => {
                                       if(!(table in $ctrl.workspace.tableLiterals) ) {
                                           $ctrl.workspace.tableLiterals[table] = {};
                                           $ctrl.tableList.push(table);
                                           $ctrl.tableList.sort();
                                           Tables.schema(table, true).then( (keys) => {
                                               $ctrl.tableKeys[table] = keys;
                                           });
                                       }
                                   }

                                   $ctrl.removeTable = (table, idx) => {
                                       delete $ctrl.workspace.tableLiterals[table];
                                       $ctrl.tableList.splice(idx,1);
                                   }

                                   $ctrl.setTableLiterals = (table) => {
                                       return (literals) => {
                                           Queries.setTableLiterals(table, literals).then((literals)=>{
                                               $ctrl.workspace.tableLiterals[table] = literals;
                                           });
                                       }
                                   }

                                   $ctrl.deleteQuery = (queryId) => {
                                       if(confirm("Are you sure you wish to delete " + queryId + "?")) {
                                           Queries.deleteQuery($ctrl.workspaceId, queryId).then((response)=>{
                                               var idx = $ctrl.queryList.indexOf(queryId);
                                               $ctrl.queryList.splice(idx,1);
                                           });
                                       }
                                   }

                               }
                           });



    MicroMacroApp.component('queryConfig', {
        templateUrl : 'html/components/queryConfig.html',
        bindings : {
            query: '<',
            tables: '<',
            keys: '<?',
            workspace: '<'
        },
        controller : function($scope, $state, Tables, Queries, $stateParams) {

            var $ctrl = this;
            $scope.showNotes = true;

            $ctrl.queryId = $stateParams.queryId;

            var keyList = (keys) => {
                $ctrl.keyList = [];
                angular.forEach(keys, (item, key) => {
                    var listItem = angular.copy(item);
                    listItem.id = listItem.key();
                    $ctrl.keyList.push(listItem);
                });
                $ctrl.keyList.sort((a,b)=>{return a.id.localeCompare(b.id)});
            }

            $ctrl.reload = () => {
                Tables.schema($ctrl.query.table).then(function(keys) {
                    $ctrl.keys = keys;
                    keyList(keys);
                });

                $ctrl.query.literals = $ctrl.workspace.tableLiterals[$ctrl.query.table];
            };


            $ctrl.$onInit = () => {
                if(!$ctrl.keys && $ctrl.query.table) {
                    $ctrl.reload();
                } else if($ctrl.keys) {
                    keyList($ctrl.keys);
                }

                $ctrl.query.literals = $ctrl.query.literals || {};
                $scope.queryVer = $stateParams.ver;
                //            $ctrl.notes = JSON.parse($ctrl.notes || "[]");

                if(!$ctrl.query.orderBy) {
                    $ctrl.query.orderBy = {};
                }

                if($ctrl.query._TYPE == "proximity" && !$ctrl.query.scope) {
                    $ctrl.query.scope = {
                    };
                }

                $ctrl.query.limit = $ctrl.query.limit || 0;
                $ctrl.sampleSize = $ctrl.sampleSize || 100;

                if($ctrl.query.isCached && $state.$current.name == "workspace.query") {
                    $ctrl.execute($ctrl.sampleSize);
                }
            };

            $ctrl.partitionQuery = () => {
                if($ctrl.query.partition) {
                    delete $ctrl.query.partition;
                } else {
                    $ctrl.query.partition = {};
                }
            };

            $scope.changeVer = () => $state.go(".", {ver:$scope.queryVer});

            $scope.undo = () => {
                --$scope.queryVer;
                $scope.changeVer();
            };

            $scope.redo = () => {
                if($scope.queryVer < 0) {
                    ++$scope.queryVer;
                    $scope.changeVer();
                }
            };


            $ctrl.execute = (sampleSize) => {
                $ctrl.query.literals = $ctrl.workspace.tableLiterals[$ctrl.query.table];
                Queries.save($stateParams.workspaceId, $ctrl.queryId, $ctrl.query).then(function(query){
                    //                alert("saved");
                    //$state.go("^.query", {workspaceId:$stateParams.workspaceId, queryId:$ctrl.queryId, ver:0});
                    $scope.queryVer = 0;
                }).then(()=>{
                    var target = ".";
                    if($state.$current.name == "workspace.queries.query") {
                        target += "execute";
                    }
                    $state.transitionTo(target,
                                        {queryId:$ctrl.queryId, page:$stateParams.page || 1, sampleSize:sampleSize},
                                        {reload: true, inherit:true, relative: $state.$current}
                                       );
                });
            }
        }
    });

    MicroMacroApp.component('queryResult', {
        templateUrl : 'html/components/queryResult.html',
        bindings : {
            query: '<',
            keys: '<',
            result: '<',
            defaultKeys: '<',
            literals : '<'
        },
        controller : function($scope, $state, $stateParams, Queries, Datums, Rows, Types) {
            var $ctrl = this;

            $ctrl.typeList = [];
            for(var key in Types) {
                $ctrl.typeList.push(key);
            }
            $ctrl.typeList.sort();

            $ctrl.pageChange = function() {
                $state.go(".", {page:$ctrl.currentPage});
            };

            $ctrl.widths = {};

            $ctrl.page = [];
            //        $ctrl.currentPage = 1;
            $ctrl.numPerPage = 10;

            $ctrl.$onInit = function() {

                //alphabetical key list
                $ctrl.keyList = [];
                for(var key in $ctrl.keys) {
                    $ctrl.keyList.push(key);
                }
                $ctrl.keyList.sort();

                //bind display keys to URL
                var bindSelectedKeys = function(){
                    var keyMap = new Map(Object.entries($ctrl.keys));

                    var findTarget = (key) => {
                        for(var i in $ctrl.result) {
                            var datum = Datums.datum($ctrl.result[i], keyMap);
                            if( datum.get(key) ) {
                                return datum.resolve(key).target.key();
                            }
                        }
                        return false;
                    };

                    $ctrl.selectedKeys = ($stateParams.display || $ctrl.defaultKeys).reduce((keys, keyName) => {
                        if($ctrl.keys[keyName].type.equals(Types.SPANS)) {
                            var target = findTarget(keyName);
                            if(target) {
                                keys[target] = true;
                            }
                        }

                        keys[keyName] = true;
                        return keys;
                    }, {});

                    var syncKeys = (selected) => {
                        if($ctrl.selectedKeys != selected && selected) {
                            $ctrl.selectedKeys = selected.reduce((keys, key)=> {
                                keys[key] = true;
                                return keys;
                            }, {});
                        }
                    };

                    syncKeys($state.params.selected);

                    $scope.$watchCollection(angular.bind(this, function() {
                        return $state.params.selected;
                    }), function(selected) {
                        syncKeys(selected);
                    });

                    $scope.$watchCollection("$ctrl.selectedKeys", function(selected, old){
                        var urlKeys = [];

                        angular.forEach($ctrl.selectedKeys, (selected, key) => {
                            if(selected) {
                                urlKeys.push(key);
                            }
                        });
                        if(angular.equals(urlKeys, $state.params.selected)) return;
                        $state.go(".", {selected:urlKeys});

                        updateData();
                    });
                }();

                //bind page number to URL
                $ctrl.currentPage = $stateParams.page;

                $scope.$watch(angular.bind(this, function() {
                    return $state.params.page;
                }), function(page){
                    if($ctrl.currentPage != page) {
                        $ctrl.currentPage = page;
                    }
                });

                if(isPartitionedOrScoped()) {
                    var key;
                    if(isScoped()) {
                        key = $ctrl.query.scope.key;
                    } else {
                        key = $ctrl.query.partition.key;
                    }
                    $ctrl.pages = Queries.binProximityResultByPartition($ctrl.result, key);
                    $ctrl.page = Rows.getRowsColumns($ctrl.pages[$ctrl.currentPage-1], $ctrl.keys, $ctrl.selectedKeys);
                    $ctrl.totalItems = $ctrl.pages.length * $ctrl.numPerPage;
                } else {
                    $ctrl.totalItems = $ctrl.result.length;
                }

                if($ctrl.query.isCached) {
                    $ctrl.cacheResults().then(updateData);
                } else {
                    updateData();
                }

                $scope.$watch('$ctrl.currentPage', function(newVal, oldVal) {
                    if(newVal != oldVal) {
                        updateData();
                    }
                });

                $ctrl.annotate = {};

            };

            $ctrl.cacheResults  = () => {
                $ctrl.loading = true;
                return Queries.count($ctrl.query).then( (count) => {
                    if(isPartitionedOrScoped()) {
                        $ctrl.totalItems = count * $ctrl.numPerPage;
                    } else {
                        $ctrl.totalItems = count;
                    }

                    $ctrl.loading = false;
                }).catch( () => {
                    $ctrl.loading = false;
                });
            }

            $ctrl.$postLink = () => {
                //            spinnerService.show('booksSpinner');
            }

            var isPartitioned = $ctrl.isPartitioned =  () => $ctrl.query.partition && $ctrl.query.partition.key ;
            var isScoped = $ctrl.isScoped = () => $ctrl.query.scope && $ctrl.query.scope.key;
            var isPartitionedOrScoped = $ctrl.isPartitionedOrScoped = () => isPartitioned() || isScoped();

            $ctrl.cols = function(max, num) {
                return Math.floor(max/num);
            }

            //        $ctrl.totalItems = npm install spin.js() => {
            //            if(isProxy()) {
            //                return $ctrl.pages.length * $ctrl.numPerPage -1;
            //            } else {
            //                return $ctrl.result.length;
            //            }
            //        };

            var resolveDisplayKeys = function() {

                $ctrl.widths = {};
                $ctrl.displayKeys = $ctrl.keyList.reduce((keys, keyName)=>{
                    if($ctrl.selectedKeys[keyName] && !$ctrl.keys[keyName].type.equals(Types.SPANS)) {
                        keys.push(keyName);
                        $ctrl.widths[keyName] = 20;
                    }
                    return keys;
                }, []);

            };

            var updateData = function() {
                if($ctrl.totalItems === 0) {
                    return;
                }
                resolveDisplayKeys();
                if(isPartitionedOrScoped()) {
                    var page = $ctrl.currentPage - 1;
                    if($ctrl.pages[$ctrl.currentPage-1]) {
                        $ctrl.page = Rows.getRowsColumns($ctrl.pages[$ctrl.currentPage-1], $ctrl.keys, $ctrl.selectedKeys);
                    } else {
                        Queries.page($ctrl.query, page).then( (data)=> {
                            $ctrl.page = Rows.getRowsColumns(data, $ctrl.keys, $ctrl.selectedKeys);
                        });
                    }
                } else {

                    var skip = ($ctrl.currentPage - 1) * $ctrl.numPerPage;
                    var limit = $ctrl.numPerPage;
                    if($ctrl.result[skip] && $ctrl.result[skip+limit-1]) {
                        $ctrl.page = Rows.getRowsColumns($ctrl.result.slice(skip, skip+limit), $ctrl.keys, $ctrl.selectedKeys);
                    } else {
                        Queries.skipLimit($ctrl.query, skip, limit).then( (data)=> {
                            $ctrl.page = Rows.getRowsColumns(data, $ctrl.keys, $ctrl.selectedKeys);
                        });
                    }
                }
            };

            $ctrl.annotateQuery = function() {
                $ctrl.loading = true;

                var updateQuery = angular.copy($ctrl.query);
                var type = Types[$ctrl.annotate.type];

                updateQuery.key = DatumFactory.key($ctrl.annotate.key, type);
                updateQuery.value = $ctrl.annotate.value;

                //            updateQuery._TYPE += "Update";

                Queries.update(updateQuery).then( (count) => {
                    alert(count + " records updated. The cache for this query must be cleared and reloaded to reflect this change in the results.");

                    $ctrl.loading = false;
                }).catch( () => {
                    $ctrl.loading = false;
                });
            };

            $ctrl.chunkCounts = function() {
                return Queries.chunkCounts($ctrl.query).then( (chunkCounts) => {
                    return chunkCounts;
                });
            };

            $ctrl.gotoId = function(id) {
                Queries.partitionPage($ctrl.query, id).then( (page) => {
                    $ctrl.currentPage = page+1;
                });
            };

        }
    });

    MicroMacroApp.component('spanText', {
        bindings : {
            spanss: '<'
        },
        templateUrl: 'html/components/spanText.html',
        controller : function($scope, Spans) {

            var $ctrl = this;

            $ctrl.$onInit = function() {

                $ctrl.segments = Spans.segments($ctrl.spanss);
                $scope.$watch("$ctrl.spanss", function() {
                    $ctrl.segments = Spans.segments($ctrl.spanss);
                });
            };
        }
    });MicroMacroApp.component('summary', {
        templateUrl : 'html/components/summary.html',
        bindings : {
            query : '<'
        },
        controller: function($scope, $state, $stateParams) {
            var $ctrl = this;
        }
    });
    MicroMacroApp.component('workspace', {
        templateUrl : 'html/components/workspace.html',
        bindings : {
            workspace : '<',
            queryList : '<',
            tables : '<'
        },
        controller: function($scope, $state, $stateParams, Queries, spinnerService, Workspaces, Tables) {
            var $ctrl = this;
            $ctrl.workspaceId = $stateParams.workspaceId;

            var first = true;

            $ctrl.loadQueries = () => {
                first || $state.go("workspace.queries")
                first = false;
            }

            $ctrl.loadMaps = () => {
                first || $state.go("workspace.maps")
                first = false;
            }

            $ctrl.$onInit = () => {
                if($state.$current.name.startsWith("workspace.queries")) {
                    $ctrl.mainActive = 0;
                } else if($state.$current.name.startsWith("workspace.maps")) {
                    $ctrl.mainActive = 1;
                }
            }
        }
    });



    MicroMacroApp.component('workspaces', {
        templateUrl: 'html/components/workspaces.html',
        controller: function ($scope, Workspaces) {

            $scope.list = function() {
                Workspaces.list().then(function(workspaces){
                    $scope.workspaces = workspaces;
                });
            };

            $scope.createWorkspace = function(name) {
                Workspaces.create(name).then(function(workspace) {
                    $scope.list();
                });
            };

            $scope.list();
        }
    });
}

export {initializeComponents};





