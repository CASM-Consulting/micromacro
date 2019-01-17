MicroMacroApp.factory("Tables", function($q, Server) {

    return {
        list : function() {
            return $q(function(resolve) {
                Server.get("api/tables/list", {
                    success : resolve
                });
            });
        }
    }
});
