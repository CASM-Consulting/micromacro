MicroMacroApp.controller('QueryViewer', function($scope, Tables) {

    var tables = Tables(function(tables){
        $scope.tables = tables;
    });

    var configs = {};




});

MicroMacroApp.service("Tables", function($http) {

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


MicroMacroApp.service("Configs", function($http) {

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


MicroMacroApp.factory("Workspace", function() {




    return {

    };
});