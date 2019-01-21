MicroMacroApp.component('queryResult', {
    templateUrl : 'html/queryResult.html',
    bindings : {
        query: '<',
        keys: '<',
        result: '<'
    },
    controller : function($scope, $stateParams) {

        var $ctrl = this;

        $scope.page = [];
        $scope.currentPage = 1;
        $scope.numPerPage = 30;
        $scope.maxSize = 10;

        $ctrl.$onInit = function() {
            $scope.selectedKeys = $stateParams.displayKeys || {};
            $scope.keyList = [];

            for(var key in $ctrl.keys) {

                $scope.selectedKeys[key] = $scope.selectedKeys[key] || false;
                $scope.keyList.push(key);
            }


            $scope.keyList.sort();

            $scope.$watch('currentPage + numPerPage', function() {
                var begin = (($scope.currentPage - 1) * $scope.numPerPage);
                var end = begin + $scope.numPerPage;

                $scope.page = $ctrl.result.slice(begin, end);
            });
        };

        $scope.cols = function(max, num) {
            return Math.floor(max/num);
        }
    }
});

