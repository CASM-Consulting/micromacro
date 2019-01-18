MicroMacroApp.component('queryConfig', {
    templateUrl : 'html/queryConfig.html',
    bindings : {
        query: '<',
        tables: '<',
        keys: '<'
    },
    controller : function($scope, $state, Tables, Queries) {

        var $ctrl = this;

        $scope.reload = function() {
            $state.reload("workspace.query");
        };

        $ctrl.$onInit = function(){
            Tables.schema($scope.$ctrl.query.table).then(function(keys) {
                $scope.keys = keys;
            });
        };

        $scope.execute = function () {
            Queries.execute($scope.$ctrl.query).then(function(data){
                $scope.results = data;
            });
        }
    }
});

