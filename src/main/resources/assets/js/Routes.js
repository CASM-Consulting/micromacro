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

    $stateProvider.state('workspace.query', {
        url: '/{queryId}',
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
        url: '/execute',
        views: {
            'result@workspace' :  {
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