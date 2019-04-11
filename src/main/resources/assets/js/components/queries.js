MicroMacroApp.component('queries', {
    templateUrl : 'html/queries.html',
    bindings : {
        workspace : '<',
        queryList : '<',
        tables : '<'
    },
    controller: function($scope, $state, $stateParams, Queries, spinnerService, Workspaces, Tables) {
        var $ctrl = this;
        $ctrl.workspaceId = $stateParams.workspaceId;

        var firstLoad = $state.$current.name.endsWith("execute");

        $ctrl.$onInit = () => {
            $ctrl.active = $ctrl.queryList.indexOf($stateParams.queryId);
            $ctrl.loadQuery = (name) => {
                firstLoad || $state.go('workspace.queries.query', {workspaceId:$stateParams.workspaceId, queryId: name});
                firstLoad = false;
            }

            $ctrl.optimise = (query) => {
                Queries.optimise(query).then((resp)=>{
                    alert(resp);
                });
            }

            $ctrl.clearCache = (queryId) => {
                Workspaces.clearCache($stateParams.workspaceId, queryId)
            }

            $ctrl.tableKeys = {};
            $ctrl.tableList = [];

            angular.forEach($ctrl.workspace.tableLiterals, (key, table) => {
                Tables.schema(table, true).then( (keys) => {
                    $ctrl.tableKeys[table] = keys;
                });
                $ctrl.tableList.push(table);
            });

            $ctrl.workspace.tableLiterals = $ctrl.workspace.tableLiterals || {};
        }


        $ctrl.addTable = (table) => {
            if(!(table in $ctrl.workspace.tableLiterals) ) {
                $ctrl.workspace.tableLiterals[table] = {};
                $ctrl.tableList.push(table);
                $ctrl.tableList.sort();
                Tables.schema(table, true).then( (keys) => {
                    $ctrl.tableKeys[table] = keys;
                });
            }
        }

        $ctrl.removeTable = (table, idx) => {
            delete $ctrl.workspace.tableLiterals[table];
            $ctrl.tableList.splice(idx,1);
        }

        $ctrl.setTableLiterals = (table) => {
            return (literals) => {
                Queries.setTableLiterals(table, literals).then((literals)=>{
                    $ctrl.workspace.tableLiterals[table] = literals;
                });
            }
        }
    }
});



