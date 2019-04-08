MicroMacroApp.component('mapConfig', {
    templateUrl : 'html/mapConfig.html',
    bindings : {
        map: '<?',
        queryList: '<',
        workspace: '<'
    },
    controller : function($scope, $state, $q, Tables, Queries, $stateParams) {

        var $ctrl = this;

        $scope.showNotes = true;

        $ctrl.mapId = $stateParams.mapId;

        $ctrl.tableKeyCache = {};
        $ctrl.queryTableCache = {};

        $ctrl.$onInit = () => {
            $ctrl.map || ($ctrl.map = {
                selectedQueries : {}
            });

            $ctrl.keyList = [];

            $scope.$watchCollection("$ctrl.map.selectedQueries", function(selected, old){
                $ctrl.getKeys();
            });
        };


        var promiseTable = (query) => {
            if(query in $ctrl.queryTableCache) {
                return $q( (r) => {
                    r($ctrl.queryTableCache[query]);
                });
            } else {
                return $q( (r) => {
                    Queries.load($stateParams.workspaceId, query)
                    .then((data)=>{
                        $ctrl.queryTableCache[query] = data.table;
                        r(data.table);
                    });
                });
            }
        };

        var promiseSchema = (table) => {
            if(table in $ctrl.tableKeyCache) {
                return $q( (r) => {
                    r($ctrl.tableKeyCache[table]);
                });
            } else {
                return $q( (r) => {
                    Tables.schema(table)
                    .then((keys)=> {
                        $ctrl.tableKeyCache[table] = keys;
                        r(keys);
                    });
                });
            }
        };


        $ctrl.getKeys = () => {

            var dateKey = $ctrl.map.dateKey;
            var tablePromises = [];

            $ctrl.keyList = [];

            angular.forEach($ctrl.map.selectedQueries, (on, query) => {
                on && tablePromises.push(promiseTable(query));
            });

            $q.all(tablePromises).then((tables) => {

                var schemaPromises = tables.map( (table) => {
                    return promiseSchema(table);
                });

                $q.all(schemaPromises).then( (keyss) => {
                    var keys = keyss[0];
                    var rest = keyss.slice(1);
                    if(rest.length > 0) {
                        rest.reduce((keys, moreKeys) => {
                            angular.forEach(keys, (key, name) => {
                                if(!(name in moreKeys)) {
                                    delete keys[name];
                                }
                            });
                            return keys;
                        }, keys);
                    }
                    $ctrl.keyList = keyList(keys);
                    $ctrl.map.dateKey = dateKey;
                })
            });
        }


        var keyList = (keys) => {
            var keyList = [];
            angular.forEach(keys, (item, key) => {
                var listItem = angular.copy(item);
                listItem.id = listItem.key();
                keyList.push(listItem);
            });
            keyList.sort((a,b)=>{return a.id.localeCompare(b.id)});
            return keyList;
        }
    }
});

