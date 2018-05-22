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
                    id : val.properties.trialId
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

            var features = [];

            for(var i = 0; i < response.data.length; ++i) {
                var match = response.data[i];
                features.push({
                    geometry: {
                        type : "Point",
                        coordinates : [match.lng, match.lat]
                    },
                    type : "Feature",
                    properties : match
                });
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
            var lat = parseFloat(match.lat);
            var lng = parseFloat(match.lng);
            $scope.markers[match.text.replace("-", " ") + " #" + (i+1)] = {
                lat : lat,
                lng : lng,
                message : "<div ng-include=\"'marker'\"></div>",
                getMessageScope : function(match) {
                    return function() {
                        var scope = $scope.$new(true);
                        scope.match = match;
                        return scope;
                    }
                }(match)
            };
        }
    };


    function updateList(timeline){
        var displayed = timeline.getLayers();
        var list = document.getElementById('displayed-list');
        list.innerHTML = "";
        displayed.forEach(function(quake){
          var li = document.createElement('li');
          li.innerHTML = quake.feature.properties.title;
          list.appendChild(li);
        });
      }

      function drawTimeline(data){
        var map = leafletData.getMap().then(function(map) {

            var getInterval = function(trial) {
              return {
                start: moment(trial.properties.date).add(1000, "y").toDate().getTime(),
                end:   moment(trial.properties.date).add(1000, "y").toDate().getTime()
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
                end: moment("1839-12-31").add(1000, "y").toDate().getTime(),
              getInterval: getInterval,
              pointToLayer: function(data, latlng){
                return L.circleMarker(latlng,{radius:5}).bindPopup(function(l) {
                    $scope.selectedMarker = data;
                    return "<ul>"+
                        "<li>Match: " + data.properties.text + "</li>"+
                        "<li>Original: " + data.properties.spanned + "</li>"+
                        "<li>Lat: " + data.properties.lat + "</li>"+
                        "<li>Lng: " + data.properties.lng + "</li>"+
                        "<li>Date: " + data.properties.date + "</li>"+
                        "<li>Trial: " + data.properties.trialId + "</li>"+
                    "</ul>";
                });
              }
            });
            timelineControl.addTo(map);
            timelineControl.addTimelines(timeline);
            timeline.addTo(map);
            timeline.on('change', function(e){
            //          updateList(e.target);
            });
        //        updateList(timeline);
        });
    };
    $scope.getAll();
});