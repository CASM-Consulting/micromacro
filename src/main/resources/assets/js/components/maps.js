MicroMacroApp.component('maps', {
    templateUrl : 'html/maps.html',
    bindings : {
        workspace : '<',
        mapList : '<',
        queryList : '<'
    },
    controller: function($scope, $state, $stateParams, Queries, spinnerService, Workspaces, Tables) {
        var $ctrl = this;
        $ctrl.workspaceId = $stateParams.workspaceId;

        $ctrl.$onInit = () => {
            var firstLoad = true;
            $ctrl.active = $ctrl.mapList.indexOf($stateParams.mapId);
            $ctrl.loadMap = (name) => {
                firstLoad || $state.go('workspace.maps.map', {workspaceId:$stateParams.workspaceId, mapId: name});
                firstLoad = false;
            }
        }


    }
});



