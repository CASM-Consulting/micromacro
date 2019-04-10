MicroMacroApp.factory("Tables", function($q, Server) {

    return {
        list : function() {
            return $q(function(resolve) {
                Server.get("api/tables/list", {
                    success : resolve
                });
            });
        },
        schema : function(table, asList) {
            return $q(function(resolve) {
                Server.get("api/tables/schema", {
                    params : {
                        table : table
                    },
                    success : function(keySet) {
                        var keys = {};
                        angular.forEach(keySet, (value, key) => {
                            var type = DatumFactory.type(value.type);
                            keys[key] = DatumFactory.key(key, type);
                        });

                        if(asList) {
                            var keyList = [];
                            angular.forEach(keys, (item, key) => {
                                var listItem = angular.copy(item);
                                listItem.id = listItem.key();
                                keyList.push(listItem);
                            });
                            keyList.sort((a,b)=>{return a.id.localeCompare(b.id)});
                            resolve(keyList);
                        } else {
                            resolve(keys);
                        }
                    }
                });
            });
        }
    }
});
