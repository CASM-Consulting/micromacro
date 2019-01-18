MicroMacroApp.component("literals", {
    templateUrl : 'html/literals.html',
    bindings : {
        literals : '<',
        keys : '<'
    },
    controller : function($scope, Tables) {
        var $ctrl = this;

        $scope.addLiteral = function() {
            $ctrl.literals[$scope.letter] = {
                key: $scope.key,
                type: $scope.type,
                args: $scope.args
            };
        };

        $scope.setLiteral = function(letter, literal) {
            $scope.letter = letter;
            $scope.key = literal.key;
            $scope.type = literal.type;
            $scope.args = literal.args;
        };

    }
});

MicroMacroApp.component("literal", {
    templateUrl : 'html/literal.html',
    bindings : {
        literal : '<',
        keys : '<'
    },
    controller : function($scope) {


    }
});