MicroMacroApp.component("literals", {
    templateUrl : 'html/components/literals.html',
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

        $ctrl.deleteLiteral = (letter) => {
            delete $ctrl.literals[letter];
            if($ctrl.workspace && $ctrl.table) {
                Queries.setTableLiterals($ctrl.workspace, $ctrl.table, $ctrl.literals);
            }
        };
    }
});
