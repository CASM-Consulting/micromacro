MicroMacroApp.factory("Queries", function($q, Server) {

    return {

        binProxyResultByPartition : function (result, partitionKey) {

            var curPartition = null;
            var idx = -1;

            return result.reduce( (binned, row) => {
                var partition = row.data[partitionKey];
                if(partition != curPartition) {
                    binned.push([row]);
                    curPartition = partition;
                    ++idx;
                } else {
                    binned[idx].push(row);
                }
                return binned;
            }, []);
        },

        load : function(workspaceId, queryId, ver) {
            return $q(function(resolve) {
                Server.get("api/workspace/loadQuery", {
                    params : {
                        workspace : workspaceId,
                        queryName : queryId,
                        ver : ver
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