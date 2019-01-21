MicroMacroApp.component('queryConfig', {
    templateUrl : 'html/queryConfig.html',
    bindings : {
        query: '<',
        tables: '<',
        keys: '<?'
    },
    controller : function($scope, $state, Tables, Queries, $stateParams) {

        var $ctrl = this;

        $ctrl.queryId = $stateParams.queryId;

        $scope.reload = function() {
//            $state.reload("workspace.query");
            Tables.schema($ctrl.query.table).then(function(keys) {
                $ctrl.keys = keys;
            })
        };

        $ctrl.$onInit = function(){
            if($ctrl.query.table) {
                Tables.schema($ctrl.query.table).then(function(keys) {
                    $scope.keys = keys;
                });
            }
            $ctrl.query.literals = $ctrl.query.literals || {};
        };

        $scope.execute = function () {
            Queries.execute($scope.$ctrl.query).then(function(data){
                $scope.results = data;
            });
        }

        $scope.addProxy = function() {
            Queries.addProxy($stateParams.workspaceId, $ctrl.queryId, $ctrl.query).then(function(query){
                alert("saved");
            });
        }

        $scope.addSelect = function() {
            Queries.addSelect($stateParams.workspaceId, $ctrl.queryId, $ctrl.query).then(function(query){
                alert("saved");
            });
        }

//        $scope.$watch("$ctrl.query", function(newVal, oldVal){
//            if(newVal) {
//            }
//        });
    }
});

