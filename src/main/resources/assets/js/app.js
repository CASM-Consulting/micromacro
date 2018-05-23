'use strict';


var MicroMacroApp = {};

var app = angular.module('MicroMacroApp', ['ui.bootstrap', 'ngRoute', 'ui-leaflet']);



app.factory("OBTrials", function($scope, $http){


    $http.get("/api/places/ob", {
            params : {
                name : $scope.query
            }
        }).then(function(response){

            drawMatches(response.data);

        });

    return trials;
});

app.factory('debounce', function($timeout) {
    return function(callback, interval) {
        var timeout = null;
        return function() {
            $timeout.cancel(timeout);
            var args = arguments;
            timeout = $timeout(function () {
                callback.apply(this, args);
            }, interval);
        };
    };
});