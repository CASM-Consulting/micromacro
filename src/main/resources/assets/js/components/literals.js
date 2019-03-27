MicroMacroApp.component("literals", {
    templateUrl : 'html/literals.html',
    bindings : {
        literals : '<',
        keys : '<',
        table : '<?',
        workspace : '<?'
    },
    controller : function(Queries) {
        var $ctrl = this;

        $ctrl.addLiteral = function() {
            $ctrl.literals[$ctrl.letter] = {
                key: $ctrl.key,
                type: $ctrl.type,
                args: $ctrl.args
            };
            if($ctrl.workspace && $ctrl.table) {
                Queries.setTableLiterals($ctrl.workspace, $ctrl.table, $ctrl.literals);
            }
        };

        $ctrl.setLiteral = function(letter, literal) {
            $ctrl.letter = letter;
            $ctrl.key = literal.key;
            $ctrl.type = literal.type;
            $ctrl.args = literal.args;
            $ctrl.isUncollapsed = true;
        };

        $ctrl.$onInit = () => {
            console.log($ctrl.literals);
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