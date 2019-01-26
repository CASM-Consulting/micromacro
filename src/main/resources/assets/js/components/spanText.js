MicroMacroApp.component('spanText', {
    bindings : {
        spanss: '<',
        text : '<',
        displayKeys: '<'
    },
    templateUrl: 'html/spanText.html',
    controller : function(Spans) {

        var $ctrl = this;

        $ctrl.$onInit = function() {

            $ctrl.segments = Spans.segments($ctrl.text, $ctrl.spanss, $ctrl.displayKeys);
        };
    }
});