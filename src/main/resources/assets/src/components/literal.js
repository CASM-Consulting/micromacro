const literal = {
    templateUrl : 'html/components/literal.html',
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
};

export default literal;
