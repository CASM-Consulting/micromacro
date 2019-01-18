MicroMacroApp.config(function($stateProvider){

    var workspace = {
        name: 'workspace',
        url: '/workspace/{workspaceId}',
        component:'workspace',
        resolve: {
            workspace: function(Workspaces, $stateParams) {
                return Workspaces.load($stateParams.workspaceId);
            }
        }
    };
    $stateProvider.state(workspace);


    var queryResults = {

    };

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
            results : {
                component:'queryResult'
            },
            config : {
                component:'queryConfig'
            }
        }
    });

});