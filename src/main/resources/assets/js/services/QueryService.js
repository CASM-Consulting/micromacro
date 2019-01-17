MicroMacroApp.factory("Queries", function($q, Server) {

    return {
        proxy : function() {

            var config = {
                target:"",
                proxy:"",
                table:"",
                literals:[],
                proximity:0,
                limit:0,
                partitionKey:null,
                orderBy:null
            };
        },

        load : function(workspaceId, queryId) {
            return $q(function(resolve) {
                Server.get("api/workspace/loadQuery", {
                    params : {
                        workspace : workspaceId,
                        queryName : queryId
                    },
                    success : resolve
                });
            });
        },

        execute : function(query) {
            return $q(function(resolve) {
                Server.post("api/query/proxy", query, {
                    success : resolve
                });
            });
        }

    }

});