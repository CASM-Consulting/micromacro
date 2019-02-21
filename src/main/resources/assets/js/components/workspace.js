MicroMacroApp.component('workspace', {
    templateUrl : 'html/workspace.html',
    bindings : {
        workspace : '<',
        queryList : '<'
    },
    controller: function($scope, $state, $stateParams) {
        var $ctrl = this;


        $ctrl.$onInit = () => {
            $ctrl.active = $ctrl.queryList.indexOf($stateParams.queryId);
            $ctrl.loadQuery = (name) => {
                $state.go('workspace.query', {workspaceId:$stateParams.workspaceId, queryId: name});
            }
        }
    }
});



