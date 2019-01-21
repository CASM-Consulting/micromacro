MicroMacroApp.component('queryConfig', {
    templateUrl : 'html/queryConfig.html',
    bindings : {
        query: '<',
        tables: '<',
        keys: '<'
    },
    controller : function($scope, $state, Tables, Queries, $stateParams) {

        var $ctrl = this;

        $scope.reload = function() {
//            $state.reload("workspace.query");
            Tables.schema($ctrl.query.table).then(function(keys) {
                $ctrl.keys = keys;
            })
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

        $scope.addProxy = function() {
            Queries.addProxy($stateParams.workspaceId, $stateParams.queryId, $ctrl.query).then(function(query){
                alert("saved");
            });
        }

        $scope.addSelect = function() {
            Queries.addSelect($stateParams.workspaceId, $stateParams.queryId, $ctrl.query).then(function(query){
                alert("saved");
            });
        }

//        $scope.$watch("$ctrl.query", function(newVal, oldVal){
//            if(newVal) {
//            }
//        });
    }
});

