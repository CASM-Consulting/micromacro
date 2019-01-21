MicroMacroApp.component('queryResult', {
    templateUrl : 'html/queryResult.html',
    bindings : {
        query: '<',
        keys: '<',
        result: '<'
    },
    controller : function($scope, $stateParams) {

        var TOKEN_HACK = $scope.TOKEN_HACK = false;

        var $ctrl = this;

        $ctrl.$onInit = function() {
            $scope.selectedKeys = $stateParams.displayKeys || {};
            $scope.keyList = [];

            for(var key in $ctrl.keys) {

                $scope.selectedKeys[key] = $scope.selectedKeys[key] || false;
                $scope.keyList.push(key);
            }


            $scope.keyList.sort();
        };

        $scope.cols = function(max, num) {
            return Math.floor(max/num);
        }
    }
});

