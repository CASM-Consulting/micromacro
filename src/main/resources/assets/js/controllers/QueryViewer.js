'use strict';


app.controller('QueryViewer', function($scope, Tables, ) {

    var tables = Tables(function(tables){
        $scope.tables = tables;
    });

    var configs = {};




});

app.service("Tables", function($http) {

    return function(success, error) {
        error = error || function(){};
        $http.get("api/tables/list")
        .then(function (data, status) {
            success && success(data.data);
        }, function (data, status) {
            error(data, status);
        });
    }
});


app.service("Configs", function($http) {

    return function(success, error) {
        error = error || function(){};
        $http.get("api/tables/list")
        .then(function (data, status) {
            success && success(data.data);
        }, function (data, status) {
            error(data, status);
        });
    }
});


app.factory("Workspace", function() {




    return {

    };
});