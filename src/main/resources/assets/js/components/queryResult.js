MicroMacroApp.component('queryResult', {
    templateUrl : 'html/queryResult.html',
    bindings : {
        query: '<',
        keys: '<',
        result: '<',
        defaultKeys: '<',
        literals : '<'
    },
    controller : function($scope, $state, $stateParams, Queries, Datums, Rows, Types) {
        var $ctrl = this;

        $ctrl.typeList = [];
        for(var key in Types) {
            $ctrl.typeList.push(key);
        }
        $ctrl.typeList.sort();

        $ctrl.pageChange = function() {
            $state.go(".", {page:$ctrl.currentPage});
        };

        $ctrl.widths = {};

        $ctrl.page = [];
//        $ctrl.currentPage = 1;
        $ctrl.numPerPage = 10;

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
                var keyMap = new Map(Object.entries($ctrl.keys));

                var findTarget = (key) => {
                    for(var i in $ctrl.result) {
                        var datum = Datums.datum($ctrl.result[i], keyMap);
                        if( datum.get(key) ) {
                            return datum.resolve(key).target.key();
                        }
                    }
                    return false;
                };

                $ctrl.selectedKeys = ($stateParams.display || $ctrl.defaultKeys).reduce((keys, keyName) => {
                    if($ctrl.keys[keyName].type.equals(Types.SPANS)) {
                        var target = findTarget(keyName);
                        if(target) {
                            keys[target] = true;
                        }
                    }

                    keys[keyName] = true;
                    return keys;
                }, {});

                var syncKeys = (selected) => {
                    if($ctrl.selectedKeys != selected && selected) {
                        $ctrl.selectedKeys = selected.reduce((keys, key)=> {
                            keys[key] = true;
                            return keys;
                        }, {});
                    }
                };

                syncKeys($state.params.selected);

                $scope.$watchCollection(angular.bind(this, function() {
                    return $state.params.selected;
                }), function(selected) {
                    syncKeys(selected);
                });

                $scope.$watchCollection("$ctrl.selectedKeys", function(selected, old){
                    var urlKeys = [];

                    angular.forEach($ctrl.selectedKeys, (selected, key) => {
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

            if(isPartitioned()) {
                $ctrl.pages = Queries.binProximityResultByPartition($ctrl.result, $ctrl.query.partition.key);
                $ctrl.page = Rows.getRowsColumns($ctrl.pages[$ctrl.currentPage-1], $ctrl.keys, $ctrl.selectedKeys);
            }

            if($ctrl.query.isCached) {
                $ctrl.cacheResults().then(updateData);
            } else {
                updateData();
            }

            $scope.$watch('$ctrl.currentPage', function(newVal, oldVal) {
                if(newVal != oldVal) {
                    updateData();
                }
            });

            $ctrl.annotate = {};

        };

        $ctrl.cacheResults  = () => {
            $ctrl.loading = true;
            return Queries.count($ctrl.query).then( (count) => {
                if(isPartitioned()) {
                    $ctrl.totalItems = count * $ctrl.numPerPage;
                } else {
                    $ctrl.totalItems = count;
                }

                $ctrl.loading = false;
            });
        }

        $ctrl.$postLink = () => {
//            spinnerService.show('booksSpinner');
        }

        var isPartitioned = () => $ctrl.query.partition &&  $ctrl.query.partition.key;

        $ctrl.cols = function(max, num) {
            return Math.floor(max/num);
        }

//        $ctrl.totalItems = npm install spin.js() => {
//            if(isProxy()) {
//                return $ctrl.pages.length * $ctrl.numPerPage -1;
//            } else {
//                return $ctrl.result.length;
//            }
//        };

        var resolveDisplayKeys = function() {

            $ctrl.widths = {};
            $ctrl.displayKeys = $ctrl.keyList.reduce((keys, keyName)=>{
                if($ctrl.selectedKeys[keyName] && !$ctrl.keys[keyName].type.equals(Types.SPANS)) {
                    keys.push(keyName);
                    $ctrl.widths[keyName] = 20;
                }
                return keys;
            }, []);

        };

        var updateData = function() {
            resolveDisplayKeys();
            if(isPartitioned()) {
                var page = $ctrl.currentPage - 1;
                if($ctrl.pages[$ctrl.currentPage-1]) {
                    $ctrl.page = Rows.getRowsColumns($ctrl.pages[$ctrl.currentPage-1], $ctrl.keys, $ctrl.selectedKeys);
                } else {
                    Queries.page($ctrl.query, page).then( (data)=> {
                        $ctrl.page = Rows.getRowsColumns(data, $ctrl.keys, $ctrl.selectedKeys);
                    });
                }
            } else {

                var skip = ($ctrl.currentPage - 1) * $ctrl.numPerPage;
                var limit = $ctrl.numPerPage;
                if($ctrl.result[skip] && $ctrl.result[skip+limit-1]) {
                    $ctrl.page = Rows.getRowsColumns($ctrl.result.slice(skip, skip+limit), $ctrl.keys, $ctrl.selectedKeys);
                } else {
                    Queries.skipLimit($ctrl.query, skip, limit).then( (data)=> {
                        $ctrl.page = Rows.getRowsColumns(data, $ctrl.keys, $ctrl.selectedKeys);
                    });
                }
            }
        };

        $ctrl.annotateQuery = function() {

            var updateQuery = angular.copy($ctrl.query);
            var type = Types[$ctrl.annotate.type];

            updateQuery.key = DatumFactory.key($ctrl.annotate.key, type);
            updateQuery.value = $ctrl.annotate.value;

            updateQuery._TYPE += "Update";

            Queries.update(updateQuery);
        };

    }
});

