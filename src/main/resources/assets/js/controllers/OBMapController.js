'use strict';

app.controller('OBMapController', function($scope, $http, $compile) {

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

            drawMatches(response.data);
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

      function eqfeed_callback(data){
        var getInterval = function(t) {
          // earthquake data only has a time, so we'll use that as a "start"
          // and the "end" will be that + some value based on magnitude
          // 18000000 = 30 minutes, so a quake of magnitude 5 would show on the
          // map for 150 minutes or 2.5 hours
          return {
            start: quake.properties.time,
            end:   quake.properties.time + quake.properties.mag * 1800000
          };
        };
        var timelineControl = L.timelineSliderControl({
          formatOutput: function(date){
            return moment(date).format("YYYY-MM-DD");
          }
        });
        var timeline = L.timeline(data, {
          getInterval: getInterval,
          pointToLayer: function(data, latlng){
            var hue_min = 120;
            var hue_max = 0;
            var hue = data.properties.mag / 10 * (hue_max - hue_min) + hue_min;
            return L.circleMarker(latlng, {
              radius: data.properties.mag * 3,
              color: "hsl("+hue+", 100%, 50%)",
              fillColor: "hsl("+hue+", 100%, 50%)"
            }).bindPopup('<a href="' + data.properties.url + '">click for more info</a>');
          }
        });
        timelineControl.addTo(map);
        timelineControl.addTimelines(timeline);
        timeline.addTo(map);
        timeline.on('change', function(e){
          updateList(e.target);
        });
        updateList(timeline);
        }
//
//    $http.get("geo/LL_PL_PA_WA_POINTS_FeaturesT.json").then(function(response) {
//            angular.extend($scope, {
//                geojson: {
//                    data: response.data,
//                    style: {
//                        fillColor: "green",
//                        weight: 2,
//                        opacity: 1,
//                        color: 'white',
//                        dashArray: '3',
//                        fillOpacity: 0.7
//                    }
//                }
//            });
//        });
//    var map = new L.Map("map", {center: [37.8, -96.9], zoom: 4})
//            .addLayer(new L.TileLayer("https://nls-2.tileserver.com/fpsUZba7ERPD/{z}/{x}/{y}.png"));
//    //    .addLayer(new L.TileLayer("http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"));
//
//
//
//
//    var svg = d3.select(map.getPanes().overlayPane).append("svg"),
//        g = svg.append("g").attr("class", "leaflet-zoom-hide");




});