MicroMacroApp.component('queryResult', {
    templateUrl : 'html/queryResult.html',
    bindings : {
        query: '<',
        keys: '<',
        result: '<',
        defaultKeys: '<'
    },
    controller : function($scope, $state, $stateParams, Queries) {

        var LABEL = $scope.LABEL = 'uk.ac.susx.tag.method51.twitter.LabelDecision';
        var STRING = $scope.STRING = 'java.lang.String';
        var LIST = $scope.LIST = 'java.util.List';
        var SPAN = $scope.SPAN = 'uk.ac.susx.tag.method51.core.meta.span.Spans';

        var $ctrl = this;

        $scope.pageChange = function() {
            $state.go(".", {page:$scope.currentPage});
        };

        $scope.page = [];
        $scope.currentPage = 1;
        $scope.numPerPage = 30;
        $scope.maxSize = 10;

        $ctrl.$onInit = function() {

            var findTarget = (key) => {
                for(var i in $ctrl.result) {
                    var row = $ctrl.result[i].data;
                    if(row[key]) {
                        return row[key].target;
                    }
                }
            };

            //bind display keys to URL
            $scope.selectedKeys = $stateParams.displayKeys || $ctrl.defaultKeys.reduce((keys, key) => {
                if($ctrl.keys[key].type.class == SPAN) {
                    keys[findTarget(key)] = true;
                }
                keys[key] = true;
                return keys;
            }, {});

            $scope.$watchCollection(angular.bind(this, function() {
                return $state.params.displayKeys;
            }), function(displayKeys){
                if($scope.selectedKeys != displayKeys && displayKeys) {
                    $scope.selectedKeys = displayKeys;
                }
            });

            $scope.$watchCollection("selectedKeys", function(displayKeys){
                $state.go(".", {displayKeys:$scope.selectedKeys});
            });

            //bind page number to URL
            $scope.currentPage = $stateParams.page;

            $scope.$watch(angular.bind(this, function() {
                return $state.params.page;
            }), function(page){
                if($scope.currentPage != page) {
                    $scope.currentPage = page;
                }
            });

            //alphabetical key list
            $scope.keyList = [];
            for(var key in $ctrl.keys) {
                $scope.selectedKeys[key] = $scope.selectedKeys[key] || false;
                $scope.keyList.push(key);
            }
            $scope.keyList.sort();


            $scope.$watch('currentPage + numPerPage', function() {
                if(isProxy()) {
                    $scope.page = $scope.pages[$scope.currentPage-1];
                } else {
                    var begin = (($scope.currentPage - 1) * $scope.numPerPage);
                    var end = begin + $scope.numPerPage;
                    $scope.page = $ctrl.result.slice(begin, end);
                }
            });

            if(isProxy()) {
                $scope.pages = Queries.binProxyResultByPartition($ctrl.result, $ctrl.query.partitionKey);
            }

        };

        var isProxy = () => $ctrl.query.type == "proxy";

        $scope.cols = function(max, num) {
            return Math.floor(max/num);
        }

        $scope.totalPages = () => {
            if(isProxy()) {
                return $scope.pages.length * $scope.numPerPage -1;
            } else {
                return $ctrl.result.length;
            }
        };
    }
});

