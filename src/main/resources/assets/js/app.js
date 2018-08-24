'use strict';


var MicroMacroApp = {};

angular.module('angular-toArrayFilter', []) .filter('toArray', function () {
  return function (obj, addKey) {
    if (!angular.isObject(obj)) return obj;
    if ( addKey === false ) {
      return Object.keys(obj).map(function(key) {
        return obj[key];
      });
    } else {
      return Object.keys(obj).map(function (key) {
        var value = obj[key];
        return angular.isObject(value) ?
          Object.defineProperty(value, '$key', { enumerable: false, value: key}) :
          { $key: key, $value: value };
      });
    }
  };
});

var app = angular.module('MicroMacroApp', ['ui.bootstrap', 'ngRoute', 'ui-leaflet', 'angular-toArrayFilter']);

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

app.directive('loading', ['$http' ,function ($http)
{
    return {
        restrict: 'A',
        link: function (scope, elm, attrs)
        {
            scope.isLoading = function () {
                return $http.pendingRequests.length > 0;
            };

            scope.$watch(scope.isLoading, function (v)
            {
                if(v){
                    elm.show();
                }else{
                    elm.hide();
                }
            });
        }
    };
}]);

app.directive('invertedCheckbox', function ()
{
    return {
        restrict: 'A',
        scope : {
            "key" : "=invertedCheckbox",
            "model" : "=invertedCheckboxModel",
            "change" : "=?invertedCheckboxChange"
        },
        link: function (scope, elm, attrs)
        {
            var onChange =
            elm.prop("checked",!scope.model[scope.key]);
            elm.change(function() {
                scope.model[scope.key] = !this.checked;
                if(attrs.invertedCheckboxChange) {
                    scope.change();
                }
            });
        }
    };
});