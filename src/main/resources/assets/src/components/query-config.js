const queryConfig = {
    templateUrl : 'html/components/queryConfig.html',
    bindings : {
        query: '<',
        tables: '<',
        keys: '<?',
        workspace: '<'
    },
    controller : function($scope, $state, Tables, Queries, $stateParams) {

        var $ctrl = this;
        $scope.showNotes = true;

        $ctrl.queryId = $stateParams.queryId;

        var keyList = (keys) => {
            $ctrl.keyList = [];
            angular.forEach(keys, (item, key) => {
                var listItem = angular.copy(item);
                listItem.id = listItem.key();
                $ctrl.keyList.push(listItem);
            });
            $ctrl.keyList.sort((a,b)=>{return a.id.localeCompare(b.id)});
        }

        $ctrl.reload = () => {
            Tables.schema($ctrl.query.table).then(function(keys) {
                $ctrl.keys = keys;
                keyList(keys);
            });

            $ctrl.query.literals = $ctrl.workspace.tableLiterals[$ctrl.query.table];
        };


        $ctrl.$onInit = () => {
            if(!$ctrl.keys && $ctrl.query.table) {
                $ctrl.reload();
            } else if($ctrl.keys) {
                keyList($ctrl.keys);
            }

            $ctrl.query.literals = $ctrl.query.literals || {};
            $scope.queryVer = $stateParams.ver;
            //            $ctrl.notes = JSON.parse($ctrl.notes || "[]");

            if(!$ctrl.query.orderBy) {
                $ctrl.query.orderBy = {};
            }

            if($ctrl.query._TYPE == "proximity" && !$ctrl.query.scope) {
                $ctrl.query.scope = {
                };
            }

            $ctrl.query.limit = $ctrl.query.limit || 0;
            $ctrl.sampleSize = $ctrl.sampleSize || 100;

            if($ctrl.query.isCached && $state.$current.name == "workspace.query") {
                $ctrl.execute($ctrl.sampleSize);
            }
        };

        $ctrl.partitionQuery = () => {
            if($ctrl.query.partition) {
                delete $ctrl.query.partition;
            } else {
                $ctrl.query.partition = {};
            }
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


        $ctrl.execute = (sampleSize) => {
            $ctrl.query.literals = $ctrl.workspace.tableLiterals[$ctrl.query.table];
            Queries.save($stateParams.workspaceId, $ctrl.queryId, $ctrl.query).then(function(query){
                //                alert("saved");
                //$state.go("^.query", {workspaceId:$stateParams.workspaceId, queryId:$ctrl.queryId, ver:0});
                $scope.queryVer = 0;
            }).then(()=>{
                var target = ".";
                if($state.$current.name == "workspace.queries.query") {
                    target += "execute";
                }
                $state.transitionTo(target,
                                    {queryId:$ctrl.queryId, page:$stateParams.page || 1, sampleSize:sampleSize},
                                    {reload: true, inherit:true, relative: $state.$current}
                                   );
            });
        }
    }
};

export default queryConfig;
