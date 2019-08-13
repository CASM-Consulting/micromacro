const workspace = {
    templateUrl : 'html/components/workspace.html',
    bindings : {
        workspace : '<',
        queryList : '<',
        tables : '<'
    },
    controller: function($scope, $state, $stateParams, Queries, spinnerService, Workspaces, Tables) {
        var $ctrl = this;
        $ctrl.workspaceId = $stateParams.workspaceId;

        var first = true;

        $ctrl.loadQueries = () => {
            first || $state.go("workspace.queries")
            first = false;
        }

        $ctrl.loadMaps = () => {
            first || $state.go("workspace.maps")
            first = false;
        }

        $ctrl.$onInit = () => {
            if($state.$current.name.startsWith("workspace.queries")) {
                $ctrl.mainActive = 0;
            } else if($state.$current.name.startsWith("workspace.maps")) {
                $ctrl.mainActive = 1;
            }
        }
    }
}

export default workspace;
