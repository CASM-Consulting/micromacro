MicroMacroApp.component('queryResult', {
    templateUrl : 'html/queryResult.html',
    bindings : {
        query: '<',
        keys: '<',
        result: '<',
        defaultKeys: '<'
    },
    controller : function($scope, $state, $stateParams, Queries, Datums, Rows, ScrollEvent) {

        var LABEL = $scope.LABEL = 'uk.ac.susx.tag.method51.twitter.LabelDecision';
        var STRING = $scope.STRING = 'java.lang.String';
        var LIST = $scope.LIST = 'java.util.List';
        var SPAN = $scope.SPAN = 'uk.ac.susx.tag.method51.core.meta.span.Spans';

        var $ctrl = this;

        $scope.pageChange = function() {
            $state.go(".", {page:$scope.currentPage});
        };

        $scope.widths = {};

        $scope.page = [];
        $scope.currentPage = 1;
        $scope.numPerPage = 10;
        $scope.maxSize = 10;

        $ctrl.$onInit = function() {

            //alphabetical key list
            $scope.keyList = [];
            for(var key in $ctrl.keys) {
                $scope.keyList.push(key);
            }
            $scope.keyList.sort();

            //bind display keys to URL
            var bindSelectedKeys = function(){
                var findTarget = (key) => {
                    for(var i in $ctrl.result) {
                        var datum = Datums.datum($ctrl.result[i], $ctrl.keys);
                        if( datum.get(key) ) {
                            return datum.resolve(key).target.key();
                        }
                    }
                    return false;
                };

                $scope.selectedKeys = ($stateParams.display || $ctrl.defaultKeys).reduce((keys, key) => {
                    if($ctrl.keys[key].type.class == SPAN) {
                        var target = findTarget(key);
                        if(target) {
                            keys[target] = true;
                        }
                    }

                    keys[key] = true;
                    return keys;
                }, {});

                $scope.$watchCollection(angular.bind(this, function() {
                    return $state.params.selected;
                }), function(selected) {
                    if($scope.selectedKeys != selected && selected) {
                        $scope.selectedKeys = selected.reduce((keys, key)=> {
                            keys[key] = true;
                            return keys;
                        }, {});
                    }
                });

                $scope.$watchCollection("selectedKeys", function(selected, old){
                    var urlKeys = [];

                    angular.forEach($scope.selectedKeys, (selected, key)=> {
                        if(selected) {
                            urlKeys.push(key);
                        }
                    });
                    if(angular.equals(urlKeys, $state.params.selected)) return;
                    $state.go(".", {selected:urlKeys});

                    updateData();
                });
            }();

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
                resolveDisplayKeys();
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

        var resolveDisplayKeys = function() {

            $scope.widths = {};
            $scope.displayKeys = $scope.keyList.reduce((keys, key)=>{
                if($scope.selectedKeys[key] && $ctrl.keys[key].type.class != SPAN) {
                    keys.push(key);
                    $scope.widths[key] = 20;
                }
                return keys;
            }, []);

        };

        var updateData = function() {
            resolveDisplayKeys();
            if(isProxy()) {
                $scope.page = Rows.getRowsColumns(Datums.data($scope.pages[$scope.currentPage-1], $ctrl.keys), $scope.selectedKeys);
            } else {
                var begin = (($scope.currentPage - 1) * $scope.numPerPage);
                var end = begin + $scope.numPerPage;
                $scope.page = Rows.getRowsColumns(Datums.data($ctrl.result.slice(begin, end), $ctrl.keys), $scope.selectedKeys);
            }
        };

    }
});

