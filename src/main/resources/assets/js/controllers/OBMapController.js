'use strict';

app.controller('OBMapController', function($scope, $http) {

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
            scrollWheelZoom: false
        },
        tiles: tilesDict.oldlondon

    });

    $scope.changeTiles = function(tiles) {
        $scope.tiles = tilesDict[tiles];
    };


    var getPoints = function() {
        $http.get("/api/points", {}).then(function(response){
            $scope.points = response.data;

        });
    };



    $http.get("geo/LL_PL_PA_WA_POINTS_FeaturesT.json").then(function(response) {
            angular.extend($scope, {
                geojson: {
                    data: response.data,
                    style: {
                        fillColor: "green",
                        weight: 2,
                        opacity: 1,
                        color: 'white',
                        dashArray: '3',
                        fillOpacity: 0.7
                    }
                }
            });
        });
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