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

        $scope.reload = () => {
//            $state.reload("workspace.query");
            Tables.schema($ctrl.query.table).then(function(keys) {
                $ctrl.keys = keys;
            })
        };

        $ctrl.$onInit = () => {
            if(!$ctrl.keys && $ctrl.query.table) {
                Tables.schema($ctrl.query.table).then(function(keys) {
                    $scope.keys = keys;
                });
            }
            $ctrl.query.literals = $ctrl.query.literals || {};
            $scope.queryVer = $stateParams.ver;
        };

        $scope.execute = () => {
            Queries.execute($scope.$ctrl.query).then(function(data){
                $scope.results = data;
            });
        };

        $scope.addProxy = () => {
            Queries.addProxy($stateParams.workspaceId, $ctrl.queryId, $ctrl.query).then(function(query){
                alert("saved");
                $state.go(".", {workspaceId:$stateParams.workspaceId, queryId:$ctrl.queryId, ver:0});
                $scope.queryVer = 0;
            });
        };

        $scope.addSelect = () => {
            Queries.addSelect($stateParams.workspaceId, $ctrl.queryId, $ctrl.query).then(function(query){
                alert("saved");
                $state.go(".", {workspaceId:$stateParams.workspaceId, queryId:$ctrl.queryId, ver:0});
                $scope.queryVer = 0;
            });
        };

        $scope.changeVer = () => $state.go(".", {ver:$scope.queryVer});

        $scope.undo = () => {
            --$scope.queryVer;
            $scope.changeVer();
        };

        $scope.redo = () => {
            if($scope.queryVer < 0) {
                ++$scope.queryVer;
                $scope.changeVer();
            }
        };

    }
});

