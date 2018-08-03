'use strict';

app.controller('OBMapController', function($scope, $rootScope, $http, $compile, leafletData, debounce, $window) {

    var DATE_FORMAT = 'YYYY-MM-DD';
    var TRIAL_ID_KEY = 'trial_id';

    var dateOptions = {
        formatYear: 'yyyy',
        maxDate: new Date(1913, 4, 1),
        minDate: new Date(1674, 4, 29),
        startingDay: 1
    };

    $scope.heatmapOptions = {
        //do local storage stuff ^^
    };

    var restoreConfig = function() {

        var restoreDate = function(obj, key) {
            obj[key] = new Date(obj[key]);
        };
        var stored = JSON.parse($window.localStorage.getItem("config"));

        if(stored) {
            stored.from = new Date(stored.from);
            stored.to = new Date(stored.to);
            $scope.config = stored;
        } else {
            $scope.config = {
                from: new Date(1803 ,1, 1),
                to: new Date(1803, 12, 31),
                scoreThresh : 0
            };
        }
    };

    restoreConfig();

    $scope.$watch("config.table", function(val, old) {
        if((!$scope.config.keys && $scope.config.table) || (val && val != old)) {
            $scope.listKeys();
        }
    }, true);

    $scope.$watch("config", function(val, old) {
        if(val && val != old) {
            $window.localStorage.setItem("config", JSON.stringify(val) );
        }
    }, true);


    $scope.fromDateOptions = angular.extend({}, dateOptions, {
        initDate: $scope.config.from
    });

    $scope.toDateOptions = angular.extend({}, dateOptions, {
        initDate: $scope.config.to
    });

    $scope.heatmapIntensity = 10;
    $scope.heatmapDecay = 20;
    $scope.timelineDuration = 100;
    $scope.heatmapRadius = 10;
    $scope.heatmapBlur = 15;

    $scope.$watch("heatmapRadius", function(val, old ) {
        if(val && val != old) {
            leafletData.getLayers().then(function(layers) {
                layers.overlays.heat.setOptions({radius:val})
            });
        }
    });

    $scope.$watch("heatmapBlur", function(val, old ) {
        if(val && val != old) {
            leafletData.getLayers().then(function(layers) {
                layers.overlays.heat.setOptions({blur:val})
            });
        }
    });


    $scope.matchLLByDate = {};

    var tilesDict = {
        openstreetmap: {
            url: "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        },
        oldlondon: {
            url: "https://nls-2.tileserver.com/fpsUZba7ERPD/{z}/{x}/{y}.png"
        }
    };

    $scope.$watch("selectedDate", debounce(function(val, old) {
        if(val != old && val) {

            var date =  moment(val).format(DATE_FORMAT);

//            updateTrials(date);
        }
    }, 200));

    var updateTrials = function(date) {
        var date = moment(date).format(DATE_FORMAT);

        $http.get("api/ob/trials-by-date", {
            params : {
                date : date
            }
        }).then(function(response) {
            $scope.trials = response.data;

        });
        drawHeat(date);
    };

    $scope.$watch("selectedTrialId", function(val, old) {
        if(val != old && val) {
            $http.get("api/ob/trials-by-id", {
                params : {
                    id : val
                }
            }).then(function(response) {
                var matchedMap = {};
                var unmatchedMap = {};
                for(var i = 0; i < response.data.data.length; ++i) {
                    matchedMap[i] = {};
                    for(var j = 0; j < response.data.data[i].spans.placeNameMatch.length; ++j) {
                        var span = response.data.data[i].spans.placeNameMatch[j];
                        for(var k = span.from; k < span.to; ++k) {
                            matchedMap[i][k] = span.value;
                        }
                    }

                    unmatchedMap[i] = {};
                    for(var j = 0; j < response.data.data[i].spans.placeName.length; ++j) {
                        var span = response.data.data[i].spans.placeName[j];
                        for(var k = span.from; k < span.to; ++k) {
                            if(!matchedMap[i][k]) {
                                unmatchedMap[i][k] = true;
                            }
                        }
                    }

                }
                $scope.selectedTrial = response.data;
                $scope.selectedTrial.matchedMap = matchedMap;
                $scope.selectedTrial.unmatchedMap = unmatchedMap;
                drawMatches($scope.matchesByTrial[val] || []);
            });
        }
    });

    angular.extend($scope, {
        center: {
            lat: 51.51,
            lng: -0.11,
            zoom: 12
        },

        defaults: {
            scrollWheelZoom: true,
            minZoom: 10,
            maxZoom: 14,
            wheelDebounceTime: 100,
            scrollWheelZoom:false
        },
        tiles: tilesDict.oldlondon,
        layers: {}

    });

//    $scope.layers.overlays = {
//            heat: {
//                name: 'Heat Map',
//                type: 'heat',
//                data: [
//                    [51.5074, 0.1278, 20],
//                    [51.5075, 0.1279, 20],
//                    [51.5086, 0.1290, 20],
//                    [51.5087, 0.1291, 20]
//                ],
//                layerOptions: {
//                    radius: 10,
//                    blur: 5
//                },
//                visible: true
//            }
//        };

    var drawHeat = function(date) {

        var from = moment(date).subtract($scope.heatmapDecay, 'days').format(DATE_FORMAT);
        var to = moment(date).format(DATE_FORMAT);

        var data = [];
        var i = 1;
        for(var d = moment(from); d.diff(to, 'days') <= 0; d.add(1, 'days') ) {
            var dayDate = moment(d).format(DATE_FORMAT);
            var dayData = $scope.matchLLByDate[dayDate] || [];

            var intensity = (i / $scope.heatmapDecay) * $scope.heatmapIntensity;

            for(var j = 0; j < dayData.length; ++j) {
                var trialId = dayData[j].trialId;
                var score = $scope.scoresByTrialId[trialId];
                if(score > $scope.config.scoreThresh) {

                    var point = dayData[j].latlng;

                    data.push(point.concat(intensity));
                } else {
                    console.log(trialId + " under thresh with " + score);
                }
            }

            ++i;
        }

        if(!data) return;

        leafletData.getLayers().then(function(layers) {
            if(layers.overlays.heat) {
                layers.overlays.heat.setLatLngs(data);
            } else {
                $scope.layers.overlays = {
                    heat : {
                        name: 'Heat Map',
                        type: 'heat',
                        data: data,
                        layerOptions: {
                            radius: $scope.heatmapRadius,
                            blur: $scope.heatmapBlur
                        },
                        visible: true
                    }
                };
            }
        });



    };

    $scope.changeTiles = function(tiles) {
        $scope.tiles = tilesDict[tiles];
    };


    $scope.search = function() {
        $http.get("/api/places/query", {
            params : {
                name : $scope.query
            }
        }).then(function(response){

            drawMatches(response.data);
        });
    };

    $scope.extract = function() {
        $http.get("/api/places/ob", {
            params : {
                name : $scope.query
            }
        }).then(function(response){

            drawMatches(response.data);

        });
    };

    var responseToFeatures = function(response) {
    };

    $scope.getAll = function() {
        $http.get("/api/ob/matches", {
            params : {
            }
        }).then(function(response){

            $scope.matchesByTrial = {};

            var features = [];

            for(var i = 0; i < response.data.length; ++i) {
                var match = response.data[i];
                var feature = {
                    geometry: {
                        type : "Point",
                        coordinates : [match.lng, match.lat]
                    },
                    type : "Feature",
                    metadata : match
                }

                var trialId = feature.metadata.trialId;

                if(!(trialId in $scope.matchesByTrial)) {
                    $scope.matchesByTrial[trialId] = [];
                }
                $scope.matchesByTrial[trialId].push(feature);

                features.push(feature);
            }

            drawTimeline({
                type : "FeatureCollection",
                features : features
            });
        });
    };

    var drawMatches = function(matches) {
        $scope.markers = {};
        for(var i = 0; i < matches.length; ++i) {
            var match = matches[i];
            var lat = parseFloat(match.metadata.lat);
            var lng = parseFloat(match.metadata.lng);

            $scope.markers[(match.metadata.text || match.text).replace("-", " ") + " #" + (i+1)] = {
                lat : lat,
                lng : lng,
                message : "<ul>"+
                  "<li>Match: " + match.metadata.text + "</li>"+
                  "<li>Original: " + match.metadata.spanned + "</li>"+
                  "<li>Lat: " + match.metadata.lat + "</li>"+
                  "<li>Lng: " + match.metadata.lng + "</li>"+
                  "<li>Date: " + match.metadata.date + "</li>"+
                  "<li>Trial: " + match.metadata.trialId + "</li>"+
                "</ul>",
//                message : "<div ng-include=\"'marker'\"></div>",
                icon: {
                    iconUrl: 'node_modules/leaflet/dist/images/marker-icon.png',
                    iconSize:     [25, 41],
                    iconAnchor:   [12, 41],
                },
//                getMessageScope : function(match) {
//                    return function() {
//                        var scope = $scope.$new(true);
//                        scope.match = match;
//                        return scope;
//                    }
//                }(match)
            };
        }
    };

    $scope.selectTrial = function(id) {
        $scope.selectedTrialId = id;
    }

    var timeline = null;
    var timelineControl = null;

    function drawTimeline(data, from, to){
        if(timeline) {
            timeline.remove();
        }
        if(timelineControl) {
            timelineControl.remove();
        }
        var map = leafletData.getMap().then(function(map) {

            var getInterval = function(trial) {
                return {
                    start: moment(trial.metadata.date).add(1000, "y").toDate().getTime(),
                    end:   moment(trial.metadata.date).add(1000, "y").toDate().getTime() + (86400000 - 1000)
                };
            };

            var daysCovered = moment(to).diff(moment(from)) / (1000*60*60*24);

            timelineControl = L.timelineSliderControl({
                steps: daysCovered,
                duration : daysCovered * $scope.timelineDuration,
                enableKeyboardControls: true,
                formatOutput: function(date){
                    return moment(date).subtract(1000, "y").format(DATE_FORMAT);
                }
            });

            $scope.$watch("timelineDuration", function(val, old) {

                if(val && val != old) {

                    timelineControl.options.duration = val;
                }
            });


            timeline = L.timeline(data, {
                start : moment(from).add(1000, "y").toDate().getTime(),
                end: moment(to).add(1000, "y").toDate().getTime(),
                getInterval: getInterval,
                pointToLayer: function(data, latlng) {
                    return L.circleMarker(latlng,{radius:5, color:"green"}).bindPopup(function(l) {
                        $scope.selectedTrialId = data.metadata.trialId;
                        return "<ul>" +
                        "<li>Match: " + data.metadata.text + "</li>"+
                        "<li>Original: " + data.metadata.spanned + "</li>"+
                        "<li>Lat: " + data.metadata.lat + "</li>"+
                        "<li>Lng: " + data.metadata.lng + "</li>"+
                        "<li>Date: " + data.metadata.date + "</li>"+
                        "<li>Trial: " + data.metadata.trialId + "</li>"+
                        "</ul>";
                    });
                }
            });
            timelineControl.addTo(map);
            timelineControl.addTimelines(timeline);
            timeline.addTo(map);
            timeline.on('change', function(e){
                $scope.selectedDate = moment(e.target.time).subtract(1000, "y").toDate();
                $scope.selectedMarker = null;
                $scope.selectedTrialId = null;
                $scope.trials = [];
                $scope.markers = [];

                updateTrials($scope.selectedDate);
            });
        });
    };


    $scope.listTables = function() {

        $http.get("api/m52/list-tables", {
            params : {
                database : $scope.database
            }
        }).then(function(response) {
            $scope.tables = response.data;
        });
    };

    $scope.listKeys = function() {
        if(!$scope.config.table) {
            $window.alert("Please select a table first.");
        } else {
            $http.get("api/m52/list-keys", {
                params : {
                    table : $scope.config.table
                }
            }).then(function(response) {
                $scope.selectedKeys = [];
                $scope.keys = response.data;
            });
        }
    };
//        if(!$scope.config.table || !$scope.config.key) {
//            $window.alert("Please select a table, and a key first.");
//        } else {

//                    table : $scope.config.table,
//                    key : $scope.config.key
//        }

    $scope.loading = false;
    $scope.loadData = function()  {
        $scope.loading = true;
        var from = $scope.config.from.toISOString().split('T')[0];
        var to = $scope.config.to.toISOString().split('T')[0]
        $http.get("api/ob/load", {
            params : {
                from : from,
                to : to
            }
        }).then(function(response) {
            $scope.matchesByTrial = {};

            var features = [];

            for(var i = 0; i < response.data.length; ++i) {
                var match = response.data[i];
                var feature = {
                    geometry: {
                        type : "Point",
                        coordinates : [match.lng, match.lat]
                    },
                    type : "Feature",
                    metadata : match
                }

                var trialId = feature.metadata.trialId;

                if(!(trialId in $scope.matchesByTrial)) {
                    $scope.matchesByTrial[trialId] = [];
                }
                $scope.matchesByTrial[trialId].push(feature);

                var date = match.date;
                features.push(feature);
                if(!(date in $scope.matchLLByDate)) {
                    $scope.matchLLByDate[date] = [];
                }

                $scope.matchLLByDate[date].push({
                    trialId : trialId,
                    latlng : [match.lat, match.lng]
                });

            }

            getScores(Object.keys($scope.matchesByTrial));

            drawTimeline({
                type : "FeatureCollection",
                features : features
            }, from, to);
            $scope.loading = false;
        });
    };


    var getScores = function(ids) {
        var table = $scope.config.table;
        var key = $scope.config.key;

        $http.post("api/m52/get-scores", JSON.stringify(
            ids
        ),{
            params: {
                table : table,
                key : key
            }
        }).then(function(response) {

//            var scorePath = table+"/"+key;

            var scores = {};

            for(var i = 0; i < response.data.length; ++i) {
                var datum = response.data[i];
                var id = datum[TRIAL_ID_KEY];
                scores[id] = datum[key];
            }

            $scope.scoresByTrialId = scores;

//            console.log(response.status);
        });
    };


    $scope.listTables();
//    $scope.getAll();

});
