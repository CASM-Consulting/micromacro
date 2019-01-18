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
                    success : resolve
                });
            });
        }
    }
});
