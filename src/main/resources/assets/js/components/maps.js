MicroMacroApp.component('maps', {
    templateUrl : 'html/maps.html',
    bindings : {
        workspace : '<',
        queryList : '<'
    },
    controller: function($scope, $state, $stateParams, Queries, spinnerService, Workspaces, Tables) {
        var $ctrl = this;
        $ctrl.workspaceId = $stateParams.workspaceId;


        $ctrl.$onInit = () => {

        }


    }
});



