MicroMacroApp.component("literals", {
    templateUrl : 'html/literals.html',
    bindings : {
        literals : '<',
        keys : '<'
    },
    controller : function(Tables) {
        var $ctrl = this;

        $ctrl.addLiteral = function() {
            $ctrl.literals[$ctrl.letter] = {
                key: $ctrl.key,
                type: $ctrl.type,
                args: $ctrl.args
            };
        };

        $ctrl.setLiteral = function(letter, literal) {
            $ctrl.letter = letter;
            $ctrl.key = literal.key;
            $ctrl.type = literal.type;
            $ctrl.args = literal.args;
        };

    }
});

MicroMacroApp.component("literal", {
    templateUrl : 'html/literal.html',
    bindings : {
        literal : '<',
        keys : '<'
    },
    controller : function() {
        var $ctrl = this;
        $ctrl.displayName = (key) => {
            var displayName = "";
            if(key.namespace) {
                displayName += key.namespace+"/";
            }
            displayName += key.name;
            return displayName;
        };
    }
});