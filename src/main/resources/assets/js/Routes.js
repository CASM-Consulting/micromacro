MicroMacroApp.config(function($stateProvider){

    $stateProvider.state('workspace',{
        name: 'workspace',
        url: '/workspace/{workspaceId}',
        component:'workspace',
        resolve: {
            workspace: function(Workspaces, $stateParams) {
                return Workspaces.load($stateParams.workspaceId);
            }
        }
    });

    $stateProvider.state('workspace.newQuery', {
        url: '/new-query/{type}',
        resolve: {
            query: function($stateParams) {
                return {type:$stateParams.type};
            },
            tables: function(Tables) {
                return Tables.list();
            }
        },
        views : {
            config : {
                component:'queryConfig'
            }
        }
    });

    $stateProvider.state('workspace.query', {
        url: '/query/{queryId}',
        resolve: {
            query: function(Queries, $stateParams) {
                return Queries.load($stateParams.workspaceId, $stateParams.queryId);
            },
            tables: function(Tables) {
                return Tables.list();
            },
            keys: function(query, Tables) {
                return Tables.schema(query.table);
            }
        },
        views : {
            config : {
                component:'queryConfig'
            }
        }
    });

    $stateProvider.state('workspace.query.execute', {
        url: '/execute/{page:int}?{displayKeys:json}',
        params : {
            page : {dynamic:true}
        },
        views: {
            'result@workspace' : {
                component:'queryResult'
            }
        },
        resolve: {
            result: function(query, Queries) {
                return Queries.execute(query);
            }
        }
    });


});