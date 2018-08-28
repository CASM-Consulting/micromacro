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

//
//    $scope.geojson = {
//        pointToLayer: function(feature, latlng) {
//          return L.circleMarker(latlng, {
//            fillColor: '#f00',
//            color: '#000',
//          });
//        }
//    };

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
                from: new Date(1803,0, 1),
                to: new Date(1803, 5, 30),
                scoreThresh : 0,
                filter: {
                    pubs : true,
                    places : true,
                    cat: {},
                    subCat: {},
                    annotations: {}
                },
                heatmap : {
                    intensity : 1,
                    decay : 20,
                    radius : 10,
                    blur : 15
                }
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
        if(!angular.equals(val, old)) {
            $scope.persistConfig()
        }
    }, true);


    $scope.persistConfig = function() {
        $window.localStorage.setItem( "config", JSON.stringify($scope.config) );
    }

    $scope.resetConfig = function() {
        $window.localStorage.clear();
        restoreConfig();
    }


    $scope.fromDateOptions = angular.extend({}, dateOptions, {
        initDate: $scope.config.from
    });

    $scope.toDateOptions = angular.extend({}, dateOptions, {
        initDate: $scope.config.to
    });


    $scope.timelineDuration = 100;



    $scope.$watch("config.filter", function(val, old ) {
        var date = moment($scope.selectedDate).format(DATE_FORMAT);
        drawHeat(date);
    }, true);

    $scope.$watch("config.heatmap", function(val, old ) {
        var date = moment($scope.selectedDate).format(DATE_FORMAT);
        drawHeat(date);
    }, true);


    $scope.$watch("config.heatmap.radius", function(val, old ) {
        if(val && val != old) {
            leafletData.getLayers().then(function(layers) {
                layers.overlays.heat.setOptions({radius:val})
            });
        }
    });

    $scope.$watch("config.heatmap.blur", function(val, old ) {
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
                var matchedPlaceMap = {};
                var unmatchedPlaceMap = {};

                var matchedPubMap = {};
                var unmatchedPubMap = {};
                for(var i = 0; i < response.data.data.length; ++i) {
                    matchedPlaceMap[i] = {};
                    for(var j = 0; j < response.data.data[i].spans.placeNameMatch.length; ++j) {
                        var span = response.data.data[i].spans.placeNameMatch[j];
                        for(var k = span.from; k < span.to; ++k) {
                            matchedPlaceMap[i][k] = span.value;
                        }
                    }

                    unmatchedPlaceMap[i] = {};
                    for(var j = 0; j < response.data.data[i].spans.placeName.length; ++j) {
                        var span = response.data.data[i].spans.placeName[j];
                        for(var k = span.from; k < span.to; ++k) {
                            if(!matchedPlaceMap[i][k]) {
                                unmatchedPlaceMap[i][k] = true;
                            }
                        }
                    }

                    matchedPubMap[i] = {};
                    for(var j = 0; j < response.data.data[i].spans.pubMatch.length; ++j) {
                        var span = response.data.data[i].spans.pubMatch[j];
                        for(var k = span.from; k < span.to; ++k) {
                            matchedPubMap[i][k] = span.value;
                        }
                    }

                    unmatchedPubMap[i] = {};
                    for(var j = 0; j < response.data.data[i].spans.pub.length; ++j) {
                        var span = response.data.data[i].spans.pub[j];
                        for(var k = span.from; k < span.to; ++k) {
                            if(!matchedPubMap[i][k]) {
                                unmatchedPubMap[i][k] = true;
                            }
                        }
                    }

                }
                $scope.selectedTrial = response.data;
                $scope.selectedTrial.matchedPlaceMap = matchedPlaceMap;
                $scope.selectedTrial.unmatchedPlaceMap = unmatchedPlaceMap;

                $scope.selectedTrial.matchedPubMap = matchedPubMap;
                $scope.selectedTrial.unmatchedPubMap = unmatchedPubMap;
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

        layers: {
            baselayers : {}
        }

    });

    var drawHeat = function(date) {

        function scoreThresh(trialId) {
            var pass = false;
            if($scope.scores) {
                var trialId = dayData[j].trialId;
                var score = $scope.scoresByTrialId[trialId];
                if(score > $scope.config.scoreThresh) {
                    pass = true;
                } else {
                    console.log(trialId + " under thresh with " + score);
                }
            } else {
                pass = true;
            }
            return pass;
        }

        function pointType(trialId, idx) {
            return $scope.config.filter.pubs && $scope.matchesByTrial[trialId][idx].metadata.type == "pub" ||
                $scope.config.filter.places && $scope.matchesByTrial[trialId][idx].metadata.type == "place";
        }

        function crimeType(trialId, idx) {
            var cat = $scope.matchesByTrial[trialId][idx].metadata.offCat;
//            var subCat = $scope.matchesByTrial[trialId][idx].metadata.offSubCat;

            return !($scope.config.filter.cat[cat]
//            || $scope.config.filter.subCat[subCat]
            );
        }

        function annotations(trialId, idx) {
            var match = $scope.matchesByTrial[trialId][idx];
            var sentenceId = match.metadata['sentenceId'];
            var pass = false;
            var overridePass = false;

            for(var i in $scope.config.annotationKeys) {
                var annotationKey = $scope.config.annotationKeys[i];

                if($scope.keys[annotationKey].type.class == 'java.lang.String') {

                    if(!$scope.annotationsByTrialId  ||
                    (!$scope.config.filter.annotations[annotationKey] &&
                    $scope.annotationsByTrialId[trialId] &&
                    $scope.annotationsByTrialId[trialId][sentenceId] &&
                    $scope.annotationsByTrialId[trialId][sentenceId][annotationKey])
                    )  {
                        pass = pass || true;
                    }

                } else if("java.lang.Double") {

                    if(!$scope.annotationsByTrialId  ||
                    ($scope.annotationsByTrialId[trialId] &&
                    $scope.annotationsByTrialId[trialId][sentenceId] &&
                    $scope.annotationsByTrialId[trialId][sentenceId][annotationKey] < $scope.config.filter.annotations[annotationKey])
                    )  {
                        overridePass = true;
                    }
                }

            }

            if(overridePass) {
                pass = false;
            }

            return pass;

        }

        var from = moment(date).subtract($scope.config.heatmap.decay, 'days').format(DATE_FORMAT);
        var to = moment(date).format(DATE_FORMAT);

        var data = [];
        var i = 1;
        for(var d = moment(from); d.diff(to, 'days') <= 0; d.add(1, 'days') ) {
            var dayDate = moment(d).format(DATE_FORMAT);
            var dayData = $scope.matchLLByDate[dayDate] || [];

            var intensity = (i / $scope.config.heatmap.decay) * $scope.config.heatmap.intensity;

            for(var j = 0; j < dayData.length; ++j) {
                var trialId = dayData[j].trialId;
                var idx = dayData[j].idx;
                var point = dayData[j].latlng;
                if(
                    scoreThresh(trialId) &&
                    pointType(trialId, idx) &&
                    crimeType(trialId, idx) &&
                    annotations(trialId, idx)
                ) {
                    data.push(point.concat(intensity));
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
                            radius: $scope.config.heatmap.radius,
                            blur: $scope.config.heatmap.blur
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

            drawMatches2(response.data);
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
                icon: {
                    iconUrl: 'node_modules/leaflet/dist/images/marker-icon.png',
                    iconSize:     [25, 41],
                    iconAnchor:   [12, 41],
                },

            };
        }
    };

    var drawMatches2 = function(matches) {
        $scope.markers = {};
        for(var i = 0; i < matches.length; ++i) {
            var match = matches[i];
            var lat = parseFloat(match.metadata.lat);
            var lng = parseFloat(match.metadata.lng);

            $scope.markers[(match.metadata.text || match.text).replace("-", " ") + " #" + (i+1)] = {
                lat : lat,
                lng : lng,
                message : "<ul>"+
                  "<li>Match: " + match.text + "</li>"+
                  "<li>Original: " + match.candidate.text + "</li>"+
                  "<li>Lat: " + match.metadata.lat + "</li>"+
                  "<li>Lng: " + match.metadata.lng + "</li>"+
                "</ul>",
                icon: {
                    iconUrl: 'node_modules/leaflet/dist/images/marker-icon.png',
                    iconSize:     [25, 41],
                    iconAnchor:   [12, 41],
                },

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
//                enableKeyboardControls: true,
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
                    var colour = data.metadata.type == 'pub' ? 'purple' : 'green';

                    return L.circleMarker(latlng,{radius:5, color:colour}).bindPopup(function(l) {
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
        $scope.loadingData = true;
        var from = $scope.config.from.toISOString().split('T')[0];
        var to = $scope.config.to.toISOString().split('T')[0]

        $scope.pubs = {};

        $http.get("api/ob/load", {
            params : {
                from : from,
                to : to
            }
        }).then(function(response) {
            $scope.matchesByTrial = {};
            $scope.offences = {
                cats : {},
                subCats : {}
            };

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

                var offCat =  match.offCat;
                var offSubCat =  match.offSubCat;

                $scope.offences.cats[offCat] = {};
                $scope.offences.subCats[offSubCat] = {cat : offCat};
//                $scope.offences.cats[offCat] = $scope.offences.cats[offCat] || {};
//                $scope.offences.cats[offCat][trialId] = true;
//                $scope.offences.subCats[offSubCat] = $scope.offences.subCats[offSubCat] || {};
//                $scope.offences.subCats[offSubCat][trialId] = offCat;


                if(!(trialId in $scope.matchesByTrial)) {
                    $scope.matchesByTrial[trialId] = [];
                }
                $scope.matchesByTrial[trialId].push(feature);

                var date = match.date;
                features.push(feature);
                if(!(date in $scope.matchLLByDate)) {
                    $scope.matchLLByDate[date] = [];
                }

                if(match.type == 'pub') {
                    $scope.pubs[match.pubId] = true;
                }


                $scope.matchLLByDate[date].push({
                    trialId : trialId,
                    idx : $scope.matchesByTrial[trialId].length - 1,
                    latlng : [match.lat, match.lng]
                });

            }

            $scope.loadPubs();

            getAnnotations(Object.keys($scope.matchesByTrial));

            drawTimeline({
                type : "FeatureCollection",
                features : features
            }, from, to);
            $scope.loadingData = false;
        });
    };

    $scope.saveSents2Table = function()  {
        $scope.loading = true;
        var from = $scope.config.from.toISOString().split('T')[0];
        var to = $scope.config.to.toISOString().split('T')[0]
        $http.get("api/ob/saveSents2Table", {
            params : {
                from : from,
                to : to,
                table : $scope.config.newTable
            }
        }).then(function(response) {
            alert("saved");
            $scope.loading = false;
        }, function(response){
            alert("failed " + response.data);
            $scope.loading = false;
        });
    };

    $scope.saveStatements2Table = function()  {
        $scope.loadingData = true;
        var from = $scope.config.from.toISOString().split('T')[0];
        var to = $scope.config.to.toISOString().split('T')[0]
        $http.get("api/ob/saveStatements2Table", {
            params : {
                from : from,
                to : to,
                table : $scope.config.newTable
            }
        }).then(function(response) {
            alert("saved");
            $scope.loadingData = false;
        }, function(response){
            alert("failed " + response.data);
            $scope.loadingData = false;
        });
    }

    var getAnnotations = function(ids) {
        $scope.loadingAnnotations = true;
        var table = $scope.config.table;
        var trialIdKey = $scope.config.trialIdKey;
        var sentenceIdKey = $scope.config.sentenceIdKey;
        var annotationKeys = $scope.config.annotationKeys;


        if(table && trialIdKey  && sentenceIdKey) {
            var annotations = {};

            $http.post("api/m52/get-annotations", JSON.stringify(
                ids
            ),{
                params: {
                    table : table,
                    trialIdKey : trialIdKey,
                    sentenceIdKey : sentenceIdKey,
                    annotationKeys : annotationKeys
                }
            }).then(function(response) {

                for(var i = 0; i < response.data.length; ++i) {
                    var datum = response.data[i];
                    var trialId = datum[trialIdKey];

                    annotations[trialId] = annotations[trialId] || {};

                    annotations[trialId][datum[sentenceIdKey]] = datum;
                }

                $scope.annotationsByTrialId = annotations;

                $scope.loadingAnnotations = false;
    //            console.log(response.status);
            });
        } else {
            $scope.scores = false;
        }
    };


    $scope.loadPubs = function() {

        $scope.loadingPubs = true;


        $http.get("api/places/pubs").then(function(response) {

            var pubs = response.data;

            var pubMarkers = [];

            for(var i in response.data) {

                var pub = response.data[i];

                if(!(pub.id in $scope.pubs) )  {
                    continue;
                }

                var metadata = pub.match.metadata;

                var lat = parseFloat(metadata.lat);
                var lng = parseFloat(metadata.lng);

                var marker = L.circleMarker([lat, lng],{radius:3, color:'purple', pub:pub}).bindPopup(function(l) {
                    return "<ul>" +
                    "<li>Name: " + l.options.pub.name + "</li>"+
                    "<li>Addr1: " + l.options.pub.addr[0] + "</li>"+
                    "<li>Addr2: " + l.options.pub.addr[1] + "</li>"+
                    "<li>Addr3: " + l.options.pub.addr[2] + "</li>"+
                    "<li>Lat: " + l.options.pub.match.metadata.lat + "</li>"+
                    "<li>Lng: " + l.options.pub.match.metadata.lng + "</li>"+
                    "</ul>";
                });

                pubMarkers.push(marker);

            }


            var pubsLayer = L.layerGroup(pubMarkers);

//            $scope.layers.baselayers['pubs'] = pubsLayer;

            leafletData.getMap().then(function(map) {
                pubsLayer.addTo(map);
            });

            $scope.loadingPubs = false;
        }, function(response){
            alert("failed " + response.data);
            $scope.loadingPubs = false;
        });
    }



    $scope.listTables();
//    $scope.getAll();

});
