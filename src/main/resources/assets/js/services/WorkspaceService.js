MicroMacroApp.factory("Workspaces", function($q, Server) {
    return {
        list : function() {
            return $q(function(resolve){
                Server.get("api/workspaces/list", {
                    success : resolve
                });
            });
        },
        create : function(name) {
            return $q(function(resolve){
                Server.get("api/workspaces/create", {
                    params : {name : name},
                    success : resolve
                });
            });
        },
        load : function(id) {
            return $q(function(resolve) {
                Server.get("api/workspace/load", {
                    params : {name : id},
                    success : resolve
                });
            });
        }
    };

});