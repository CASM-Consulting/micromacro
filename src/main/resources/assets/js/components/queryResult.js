MicroMacroApp.component('queryResult', {
    templateUrl : 'html/queryResult.html',
    bindings : {
        query: '<',
        keys: '<',
        result: '<'
    },
    controller : function($scope, $state, $stateParams) {

        var $ctrl = this;

        $scope.pageChange = function() {
            var params = $stateParams;
            params.page = $scope.currentPage;
            $state.go(".", {page:$scope.currentPage});
        };

        $scope.page = [];
        $scope.currentPage = 1;
        $scope.numPerPage = 30;
        $scope.maxSize = 10;

        $ctrl.$onInit = function() {
            $scope.selectedKeys = $stateParams.displayKeys || {};
            $scope.keyList = [];

            $scope.$watch(angular.bind(this, function() {
                return $state.params.page;
            }), function(page){
                if($scope.currentPage != page) {
                    $scope.currentPage = page;
                }
            });

            $scope.currentPage = $stateParams.page;


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

