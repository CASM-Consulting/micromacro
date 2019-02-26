MicroMacroApp.component('workspace', {
    templateUrl : 'html/workspace.html',
    bindings : {
        workspace : '<',
        queryList : '<'
    },
    controller: function($scope, $state, $stateParams, Queries, spinnerService) {
        var $ctrl = this;


        $ctrl.$onInit = () => {
            $ctrl.active = $ctrl.queryList.indexOf($stateParams.queryId);
            var firstLoad = true;
            $ctrl.loadQuery = (name) => {
                if(firstLoad) {
                    firstLoad = false;
                } else {
                    $state.go('workspace.query', {workspaceId:$stateParams.workspaceId, queryId: name});
                }
            }

            $ctrl.optimise = (query) => {
                Queries.optimise(query).then((resp)=>{
                    alert(resp);
                });
            }
        }
    }
});



