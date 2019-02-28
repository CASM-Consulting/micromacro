MicroMacroApp.factory("Tables", function($q, Server) {

    return {
        list : function() {
            return $q(function(resolve) {
                Server.get("api/tables/list", {
                    success : resolve
                });
            });
        },
        schema : function(table) {
            return $q(function(resolve) {
                Server.get("api/tables/schema", {
                    params : {
                        table : table
                    },
                    success : function(keySet) {
                        var keys = new Map();
                        angular.forEach(keySet, (value, key) => {
                            var type = DatumFactory.type(value.type);
                            keys.set(key, DatumFactory.key(key, type));
                        });

                        resolve(keys);
                    }
                });
            });
        }
    }
});
