MicroMacroApp.component('queryResult', {
    templateUrl : 'html/queryResult.html',
    bindings : {
        query: '<',
        keys: '<',
        result: '<',
        defaultKeys: '<'
    },
    controller : function($scope, $state, $stateParams, Queries, Datums, Rows) {

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
        $scope.numPerPage = 10;
        $scope.maxSize = 10;

        $ctrl.$onInit = function() {

            $ctrl.gridOptions = {
                data : $scope.page,
                useExternalPagination: true,
                enableColumnResizing: true
//                useExternalSorting: true,
            };

            var resolveSelectedKeys = function() {
                var findTarget = (key) => {
                    for(var i in $ctrl.result) {
                        var datum = Datums.datum($ctrl.result[i], $ctrl.keys);
                        if( datum.get(key) ) {
                            return datum.resolve(key).target.key();
                        }
                    }
                };

                //bind display keys to URL
                $scope.selectedKeys = ($stateParams.display || $ctrl.defaultKeys).reduce((keys, key) => {
                    if($ctrl.keys[key].type.class == SPAN) {
                        keys[findTarget(key)] = true;
                    }
                    keys[key] = true;
                    return keys;
                }, {});

                //alphabetical key list
                $scope.keyList = [];
                for(var key in $ctrl.keys) {
                    $scope.selectedKeys[key] = $scope.selectedKeys[key] || false;
                    $scope.keyList.push(key);
                }
                $scope.keyList.sort();
            };

            $scope.$watchCollection(angular.bind(this, function() {
                return $state.params.displayKeys;
            }), function(displayKeys){
                if($scope.selectedKeys != displayKeys && displayKeys) {
                    $scope.selectedKeys = displayKeys;
                }
            });

            $scope.$watchCollection("selectedKeys", function(displayKeys){
                var urlKeys = [];

                angular.forEach($scope.selectedKeys, (selected, key)=> {
                    if(selected) {
                        urlKeys.push(key);
                    }
                });

                var columnDefs = [];
                angular.forEach($scope.keyList, (key) => {
                    if($scope.selectedKeys[key] && $ctrl.keys[key].type.class != SPAN) {

                        columnDefs.push({
                            name : key,
                            cellTemplate : 'cell.html'
                        });
                    }
                });

                $ctrl.gridOptions.columnDefs = columnDefs;

                updateData();
                $state.go(".", {display:urlKeys});
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



            if(isProxy()) {
                $scope.pages = Queries.binProxyResultByPartition($ctrl.result, $ctrl.query.partitionKey);
            }

            $scope.$watch('currentPage + numPerPage', function() {
                resolveSelectedKeys();

                updateData();
            });


            Queries.execute(Queries.limitOffset($ctrl.query, 1000, 1000)).then( (moreData)=> {
                $ctrl.result = $ctrl.result.concat(moreData);
                if(isProxy()) {
                    $scope.pages = Queries.binProxyResultByPartition($ctrl.result, $ctrl.query.partitionKey);
                }
            });
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


        var updateData = function() {
            if(isProxy()) {
                $ctrl.gridOptions.data = $scope.page = Rows.getRowsColumns(Datums.data($scope.pages[$scope.currentPage-1], $ctrl.keys), $scope.selectedKeys);
            } else {
                var begin = (($scope.currentPage - 1) * $scope.numPerPage);
                var end = begin + $scope.numPerPage;
                $ctrl.gridOptions.data = $scope.page = Rows.getRowsColumns(Datums.data($ctrl.result.slice(begin, end), $ctrl.keys), $scope.selectedKeys);
            }
        };

    }
});

