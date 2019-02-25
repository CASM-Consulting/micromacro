MicroMacroApp.component('queryResult', {
    templateUrl : 'html/queryResult.html',
    bindings : {
        query: '<',
        keys: '<',
        result: '<',
        defaultKeys: '<'
    },
    controller : function($scope, $state, $stateParams, Queries, Datums, Rows) {
        var $ctrl = this;

        var LABEL = $ctrl.LABEL = 'uk.ac.susx.tag.method51.twitter.LabelDecision';
        var STRING = $ctrl.STRING = 'java.lang.String';
        var LIST = $ctrl.LIST = 'java.util.List';
        var SPAN = $ctrl.SPAN = 'uk.ac.susx.tag.method51.core.meta.span.Spans';


        $ctrl.pageChange = function() {
            $state.go(".", {page:$ctrl.currentPage});
        };

        $ctrl.widths = {};

        $ctrl.page = [];
        $ctrl.currentPage = 1;
        $ctrl.numPerPage = 10;
        $ctrl.maxSize = 10;

        $ctrl.$onInit = function() {

            $ctrl.totalItems = $ctrl.result.length;
            //alphabetical key list
            $ctrl.keyList = [];
            for(var key in $ctrl.keys) {
                $ctrl.keyList.push(key);
            }
            $ctrl.keyList.sort();

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

                $ctrl.selectedKeys = ($stateParams.display || $ctrl.defaultKeys).reduce((keys, key) => {
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
                    if($ctrl.selectedKeys != selected && selected) {
                        $ctrl.selectedKeys = selected.reduce((keys, key)=> {
                            keys[key] = true;
                            return keys;
                        }, {});
                    }
                });

                $scope.$watchCollection("$ctrl.selectedKeys", function(selected, old){
                    var urlKeys = [];

                    angular.forEach($ctrl.selectedKeys, (selected, key)=> {
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
            $ctrl.currentPage = $stateParams.page;

            $scope.$watch(angular.bind(this, function() {
                return $state.params.page;
            }), function(page){
                if($ctrl.currentPage != page) {
                    $ctrl.currentPage = page;
                }
            });

            if(isProxy()) {
                $ctrl.pages = Queries.binProxyResultByPartition($ctrl.result, $ctrl.query.partitionKey);
                $ctrl.page = Rows.getRowsColumns(Datums.data($ctrl.pages[$ctrl.currentPage-1], $ctrl.keys), $ctrl.selectedKeys);
            }

            $scope.$watch('$ctrl.currentPage + $ctrl.numPerPage', function() {
                resolveDisplayKeys();
                updateData();
            });


            Queries.execute($ctrl.query, true).then( (count)=> {
                $ctrl.totalItems = count;
//                $ctrl.result = $ctrl.result.concat(moreData);
//                if(isProxy()) {
//                    $ctrl.pages = Queries.binProxyResultByPartition($ctrl.result, $ctrl.query.partitionKey);
//                }
            });
        };

        var isProxy = () => $ctrl.query.type == "proxy";

        $ctrl.cols = function(max, num) {
            return Math.floor(max/num);
        }

//        $ctrl.totalItems = () => {
//            if(isProxy()) {
//                return $ctrl.pages.length * $ctrl.numPerPage -1;
//            } else {
//                return $ctrl.result.length;
//            }
//        };

        var resolveDisplayKeys = function() {

            $ctrl.widths = {};
            $ctrl.displayKeys = $ctrl.keyList.reduce((keys, key)=>{
                if($ctrl.selectedKeys[key] && $ctrl.keys[key].type.class != SPAN) {
                    keys.push(key);
                    $ctrl.widths[key] = 20;
                }
                return keys;
            }, []);

        };

        var updateData = function() {
            resolveDisplayKeys();
            if(isProxy()) {
                var page = $ctrl.currentPage - 1;
                if($ctrl.pages[$ctrl.currentPage-1]) {
                    $ctrl.page = Rows.getRowsColumns(Datums.data($ctrl.pages[$ctrl.currentPage-1], $ctrl.keys), $ctrl.selectedKeys);
                } else {
                    Queries.execute($ctrl.query, false, page).then( (data)=> {
                        $ctrl.page = Rows.getRowsColumns(Datums.data(data, $ctrl.keys), $ctrl.selectedKeys);
                    });
                }
            } else {

                var skip = ($ctrl.currentPage - 1) * $ctrl.numPerPage;
                var limit = $ctrl.numPerPage;
                if($ctrl.result[skip] && $ctrl.result[skip+limit]) {
                    $ctrl.page = Rows.getRowsColumns(Datums.data($ctrl.result.slice(skip, skip+limit), $ctrl.keys), $ctrl.selectedKeys);
                } else {
                    Queries.execute($ctrl.query, false, skip, limit).then( (data)=> {
                        $ctrl.page = Rows.getRowsColumns(Datums.data(data, $ctrl.keys), $ctrl.selectedKeys);
                    });
                }
            }
        };

    }
});

