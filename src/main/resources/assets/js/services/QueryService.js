MicroMacroApp.factory("Queries", function($q, Server, $http) {

    return {

        binProximityResultByPartition : function (result, partitionKey) {


            partitionKey = DatumFactory.keyFromObj(partitionKey).key();
            var curPartition = null;
            var idx = -1;

            return result.reduce( (binned, row) => {
                var partition;
                if (row instanceof Datum) {
                    partition = row.get(partitionKey);
                } else {
                    partition = row[partitionKey];
                }
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
            ver || (ver = 0);
            return $q(function(resolve) {
                Server.get("api/workspace/loadQuery", {
                    params : {
                        workspaceId : workspaceId,
                        queryId : queryId,
                        ver : ver
                    },
                    success : resolve
                });
            });
        },

        save : function(workspaceId, queryId, query) {
            return $q(function(resolve) {
                var path;
                if(query._TYPE == "select") {
                    path = "addSelect";
                } else {
                    path = "addProximity";
                }

                Server.post("api/workspace/"+path, query,  {
                    params : {
                        workspaceId : workspaceId,
                        queryId : queryId
                    },
                    success : resolve
                });
            });
        },

        deleteQuery : function(workspaceId, queryId) {
            return $q(function(resolve) {
                Server.get("api/workspace/deleteQuery", {
                    params : {
                        workspaceId : workspaceId,
                        queryId : queryId
                    },
                    success : resolve
                });
            });
        },
        
        query : function(query, params) {
            return $q(function(resolve) {
                var type = query._TYPE;
                Server.post("api/query/"+type+"/query", query, {
                    params : params,
                    success : function(data) {
                        resolve(data.map( (raw) => {
                            return JSON.parse(raw)
                        }));
                    }
                });
            });
        },

        skipLimit : function(query, skip, limit) {
            return $q(function(resolve) {
                var type = query._TYPE;
                Server.post("api/query/"+type+"/skipLimit", query, {
                    params : {
                        skip: skip,
                        limit: limit
                    },
                    success : function(data) {
                        resolve(data.map( (raw) => {
                            return JSON.parse(raw)
                        }));
                    }
                });
            });
        },


        count : function(query) {
            return $q(function(resolve) {
                var type = query._TYPE;
                Server.post("api/query/"+type+"/cacheOnly", query, {
                    success : function(data) {
                        resolve(data);
                    }
                });
            });
        },

        page : function(query, page) {
            return $q(function(resolve) {
                var type = query._TYPE;
                Server.post("api/query/"+type+"/page", query, {
                    params : {page:page},
                    success : function(data) {
                        resolve(data.map( (raw) => {
                            return JSON.parse(raw)
                        }));
                    }
                });
            });
        },

        partition : function(query, partition) {
            return $q(function(resolve) {
                var type = query._TYPE;
                Server.post("api/query/"+type+"/partition", query, {
                    params : {partition:partition},
                    success : function(data) {
                        resolve(data.map( (raw) => {
                            return JSON.parse(raw)
                        }));
                    }
                });
            });
        },

        update : function(query) {
            return $q(function(resolve) {
                var type = query._TYPE;
                Server.post("api/query/"+type, query, {
                    success : function(data) {
                        resolve(data);
                    }
                });
            });
        },

        setMeta : function(workspaceId, queryId, metaKey, data, type) {
            return $q(function(resolve) {
                if(type == "json") {
                    data = JSON.stringify(data);
                }

                Server.post("api/workspace/setQueryMeta", data,  {
                    params : {
                        workspaceId : workspaceId,
                        queryId : queryId,
                        metaId : metaKey
                    },
                    success : resolve
                });
            });
        },
        getMeta : function(workspaceId, queryId, metaKey, type, defaultValue) {

            return $q(function(resolve) {

                if(type == "json") {
                    resolve = function(data) {
                        if(!data) {
                            return defaultValue;
                        } else {
                            return resolve(JSON.parse(data));
                        }
                    }
                }

                Server.get("api/workspace/getQueryMeta",  {
                    params : {
                        workspaceId : workspaceId,
                        queryId : queryId,
                        metaId : metaKey
                    },
                    success : resolve
                });
            });
        },

        getKeys : (workspaceId, queryId) => {

            return $q(function(resolve) {
                Server.get("api/workspace/getQueryKeys", {
                    params : {
                        workspaceId : workspaceId,
                        queryId : queryId
                    },
                    success : resolve
                });
            });
        },

        limitOffset : (query, limit, offset) => {

            query = angular.copy(query);

            if(query._TYPE == "select") {
                query.limit = limit;
                query.offset = offset;
            } else if(query._TYPE == "proximity") {
                query.innerLimit = limit;
                query.innerOffset = offset;
//                query.outerLimit = block;
            }

            return query;
        },

        optimise : (query) => {
            return $q(function(resolve) {
                return $q(function(resolve) {
                    var type = query._TYPE;
                    Server.post("api/query/"+type+"/optimise/", query, {
                        success : resolve
                    });
                });
            });
        },

        setTableLiterals : (workspaceId, table, literals) => {
            return $q(function(resolve) {
                Server.post("api/workspace/setTableLiterals", literals, {
                    params : {
                        workspaceId : workspaceId,
                        table : table
                    },
                    success : resolve
                });
            });
        },

        counts : (query, partitionIds) => {
            var type = query._TYPE;
            var fd = new FormData();
            fd.append('query', JSON.stringify(query));
            fd.append('partitionIds', JSON.stringify(partitionIds));
            return $q(resolve => {
                Server.post("api/query/"+type+"/counts/", fd, {
                    params : {
                    },
                    transformRequest: angular.identity,
                    headers: {'Content-Type': undefined} ,
//                    headers: {'Content-Type': "multipart/form-data"} ,
                    success : resolve
                });
            });
        }
    }

});