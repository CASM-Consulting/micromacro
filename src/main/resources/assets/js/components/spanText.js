MicroMacroApp.component('spanText', {
    bindings : {
        spanss: '<',
        text : '<',
        tokenHack : '<'
    },
    templateUrl: 'html/spanText.html',
    controller : function() {

        var $ctrl = this;

        $ctrl.$onInit = function() {
            $ctrl.tokens = [];
            var tokens;
//            if($ctrl.tokenHack) {
//                tokens = $ctrl.text.split(' ');
//            } else {
                tokens = $ctrl.text;
//            }
            for(var i in tokens) {
                $ctrl.tokens.push({
                    text:tokens[i]
                });
            }

            for(var keyName in $ctrl.spanss) {
                var spans = $ctrl.spanss[keyName];
                for(var idx in spans) {
                    var span = spans[idx];
                    for(var i = span.from; i < span.to; ++i) {
                        $ctrl.tokens[i].color = 'red';
                    }
                }
            }
        };
    }
});