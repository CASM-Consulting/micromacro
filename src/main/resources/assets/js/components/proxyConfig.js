MicroMacroApp.component("proxyConfig", function() {
    return {
        bindings: {
            tables : "=",
            config : "="
        },
        templateUrl: 'html/proxy-config.html',
        controller: function($scope) {

            $scope.config = $scope.config || {
                target:"",
                proxy:"",
                table:"",
                literals:[],
                proximity:0,
                limit:0,
                partitionKey:null,
                orderBy:null
            };

            scope.addLiteral = function() {
                scope.config.literals.push({
                    type:null,
                    key:null,
                    args:null
                });
            };

        }
    }
});
