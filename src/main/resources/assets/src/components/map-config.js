const mapConfig = {
        templateUrl : 'html/components/mapConfig.html',
        bindings : {
            map: '<?',
            queryList: '<',
            workspace: '<'
        },
        controller : function($scope, $state, $q, Tables, Queries, Maps, $stateParams) {

            var $ctrl = this;

            $scope.showNotes = true;

            
            $ctrl.tableKeyCache = {};
            $ctrl.queryTableCache = {};

            $ctrl.$onInit = () => {
                $ctrl.map || ($ctrl.map = {
                    queries : []
                });

                $ctrl.map.id = $stateParams.mapId;

                $ctrl.selectedQueries = {};

                $ctrl.map.queries.forEach(query=> {
                    $ctrl.selectedQueries[query] = true;
                });

                $ctrl.keyList = [];

                $scope.$watchCollection("$ctrl.selectedQueries", function(selected, old){
                    $ctrl.getKeys();
                });
            };


            var promiseTable = (query) => {
                if(query in $ctrl.queryTableCache) {
                    return $q( (r) => {
                        r($ctrl.queryTableCache[query]);
                    });
                } else {
                    return Queries.load($stateParams.workspaceId, query)
                        .then((data)=>{
                            $ctrl.queryTableCache[query] = data.table;
                            return data.table;
                        });
                }
            };

            var promiseSchema = (table) => {
                if(table in $ctrl.tableKeyCache) {
                    return $q( (r) => {
                        r($ctrl.tableKeyCache[table]);
                    });
                } else {
                    return Tables.schema(table)
                        .then((keys)=> {
                            $ctrl.tableKeyCache[table] = keys;
                            return keys;
                        });
                }
            };


            $ctrl.getKeys = () => {

                var dateKey = $ctrl.map.dateKey;
                var tablePromises = [];

                $ctrl.keyList = [];

                angular.forEach($ctrl.selectedQueries, (on, query) => {
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
                        $ctrl.map.keys = keys;
                        $ctrl.map.keyList = $ctrl.keyList;
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

            $ctrl.show = () => {

                $ctrl.map.queries = [];

                angular.forEach($ctrl.selectedQueries, (on, query) => {
                    on && $ctrl.map.queries.push(query);
                });

                Maps.save($stateParams.workspaceId, $ctrl.map.id, $ctrl.map).then(function(map){
                    var target = "workspace.maps.map.show";
                    $state.transitionTo(target,
                                        {mapId: $ctrl.map.id, map:$ctrl.map},
                                        {reload: true, inherit:true, relative: $state.$current}
                                       );
                });
            }
        }
};

export default mapConfig;
