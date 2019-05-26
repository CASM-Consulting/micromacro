MicroMacroApp.component('workspaces', {
    templateUrl: 'html/components/workspaces.html',
    controller: function ($scope, Workspaces) {

        $scope.list = function() {
            Workspaces.list().then(function(workspaces){
                $scope.workspaces = workspaces;
            });
        };

        $scope.createWorkspace = function(name) {
            Workspaces.create(name).then(function(workspace) {
                $scope.list();
            });
        };

        $scope.list();
    }
});






