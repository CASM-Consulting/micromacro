'use strict';

app.controller('OBMapController', function($scope, $rootScope, $http, $compile, leafletData) {

    var tilesDict = {
        openstreetmap: {
            url: "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        },
        oldlondon: {
            url: "https://nls-2.tileserver.com/fpsUZba7ERPD/{z}/{x}/{y}.png"
        }
    };

    $scope.$watch("selectedMarker", function(val, old) {
        if(val) {
            $http.get("api/ob/trials-by-id", {
                params : {
                    id : val.metadata.trialId
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
                $scope.currentTrial = response.data;
                $scope.currentTrial.matchedMap = matchedMap;
                $scope.currentTrial.unmatchedMap = unmatchedMap;
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
            scrollWheelZoom: true
        },
        tiles: tilesDict.oldlondon

    });

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
            $scope.markers[match.metadata.text.replace("-", " ") + " #" + (i+1)] = {
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


    function drawTimeline(data){
        var map = leafletData.getMap().then(function(map) {

            var getInterval = function(trial) {
                return {
                    start: moment(trial.metadata.date).add(1000, "y").toDate().getTime(),
                    end:   moment(trial.metadata.date).add(1000, "y").toDate().getTime()
                };
            };
            var timelineControl = L.timelineSliderControl({
                formatOutput: function(date){
                    return moment(date).subtract(1000, "y").format("YYYY-MM-DD");
                }
            });
            var timeline = L.timeline(data, {
//                start : moment("1674-04-29").add(1000, "y").toDate().getTime(),
                start : moment("1830-01-01").add(1000, "y").toDate().getTime(),
//                end: moment("1913-04-01").add(1000, "y").toDate().getTime(),
                end: moment("1830-12-31").add(1000, "y").toDate().getTime(),
                getInterval: getInterval,
                pointToLayer: function(data, latlng) {
                    return L.circleMarker(latlng,{radius:5}).bindPopup(function(l) {
                        $scope.selectedMarker = data;
                        drawMatches($scope.matchesByTrial[data.metadata.trialId]);
                        return "<ul>"+
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
//            timeline.on('change', function(e){
//
//            });
        });
    };
    $scope.getAll();
});