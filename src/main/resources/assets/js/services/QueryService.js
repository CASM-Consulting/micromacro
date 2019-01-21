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
                var type = query.type;
                Server.post("api/query/"+type, query, {
                    success : resolve
                });
            });
        },

        addProxy : function(workspaceId, queryId, query) {
            return $q(function(resolve) {
                Server.post("api/workspace/addProxy", query,  {
                    params : {
                        workspaceName : workspaceId,
                        queryName : queryId
                    },
                    success : resolve
                });
            });
        },

        addSelect : function(workspaceId, queryId, query) {
            return $q(function(resolve) {
                Server.post("api/workspace/addSelect", query,  {
                    params : {
                        workspaceName : workspaceId,
                        queryName : queryId
                    },
                    success : resolve
                });
            });
        }
    }

});