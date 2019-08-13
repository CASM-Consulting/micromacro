console.log("Routes.js");

function configureRoutes(MicroMacroApp) {
    MicroMacroApp.config(function($stateProvider){

        $stateProvider.state('workspace',{
            url: '/{workspaceId}',
            component:'workspace',
            resolve: {
                workspace: function(Workspaces, $stateParams) {
                    return Workspaces.load($stateParams.workspaceId);
                }
            }
        });

        /*

          MAPS

        */

        $stateProvider.state('workspace.maps',{
            url: '/maps',
            component:'maps',
            resolve: {
                queryList: function(workspace) {
                    var queryList = Object.keys(workspace.queries);
                    queryList.sort();
                    return queryList;
                },
                mapList: function(workspace) {
                    var mapList = Object.keys(workspace.maps);
                    mapList.sort();
                    return mapList;
                }
            }
        });

        $stateProvider.state('workspace.maps.newMap', {
            url: '/new-map',
            views : {
                config : {
                    component:'mapConfig'
                }
            }
        });

        $stateProvider.state('workspace.maps.map', {
            url: '/{mapId}',
            params : {
                map : null
            },
            resolve: {
                map : function(Maps, $stateParams){
                    return Maps.load($stateParams.workspaceId, $stateParams.mapId);
                }
            },
            views : {
                config : {
                    component:'mapConfig'
                }
            }
        });

        $stateProvider.state('workspace.maps.map.show', {
            url: '/show',
            views : {
                'map@workspace.maps' : {
                    component:'map'
                }
            }
        });

        /*

          QUERIES

        */

        $stateProvider.state('workspace.queries',{
            url: '/queries',
            component:'queries',
            resolve: {
                queryList: function(workspace) {
                    var queryList = Object.keys(workspace.queries);
                    queryList.sort();
                    return queryList;
                },
                tables: function(Tables) {
                    return Tables.list();
                }
            }
        });

        $stateProvider.state('workspace.queries.newQuery', {
            url: '/new-query/{type}',
            resolve: {
                query: function($stateParams) {
                    return {_TYPE:$stateParams.type};
                }
            },
            views : {
                config : {
                    component:'queryConfig'
                }
            }
        });

        $stateProvider.state('workspace.queries.query', {
            url: '/{queryId}?{ver:int}',
            params : {
                ver : 0
            },
            resolve: {
                query: function(Queries, $stateParams) {
                    return Queries.load($stateParams.workspaceId, $stateParams.queryId, $stateParams.ver);
                },
                tables: function(Tables) {
                    return Tables.list();
                },
                keys: function(query, Tables) {
                    return Tables.schema(query.table);
                },
                notes : function(Queries, $stateParams) {
                    return Queries.getMeta($stateParams.workspaceId, $stateParams.queryId, "notes");
                }
            },
            views : {
                config : {
                    component:'queryConfig'
                },
                queryNotes : {
                    component:'queryNotes'
                }
            }
        });

        $stateProvider.state('workspace.queries.query.execute', {
            url: '/execute/{page:int}?{selected:string}&{sampleSize:int}',
            params : {
                page : {
                    dynamic:true,
                    value:100
                },
                page : {
                    dynamic:true
                },
                selected : {
                    dynamic:true,
                    array:true
                }
            },
            views: {
                'result@workspace.queries' : {
                    component:'queryResult'
                }
            },
            resolve: {
                result: function(query, workspace, Queries, $stateParams) {
                    query.literals = workspace.tableLiterals[query.table] || query.literals;
                    return Queries.query(Queries.limitOffset(query, $stateParams.sampleSize));
                },
                defaultKeys : function(Queries, $stateParams) {
                    return Queries.getKeys($stateParams.workspaceId, $stateParams.queryId);
                }
            }
        });
    });
}



export {configureRoutes};
