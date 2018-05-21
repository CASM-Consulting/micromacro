'use strict';

app.controller('OBMapController', function($scope, $http, $compile, leafletData) {

    var tilesDict = {
        openstreetmap: {
            url: "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        },
        oldlondon: {
            url: "https://nls-2.tileserver.com/fpsUZba7ERPD/{z}/{x}/{y}.png"
        }
    };

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
                start : moment("1674-04-29").add(1000, "y").toDate().getTime(),
                end: moment("1913-04-01").add(1000, "y").toDate().getTime(),
              getInterval: getInterval,
              pointToLayer: function(data, latlng){
                return L.marker(latlng);
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

});