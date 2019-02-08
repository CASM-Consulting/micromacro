MicroMacroApp.component('spanText', {
    bindings : {
        spanss: '<'
    },
    templateUrl: 'html/spanText.html',
    controller : function($scope, Spans) {

        var $ctrl = this;

        $ctrl.$onInit = function() {

            $ctrl.segments = Spans.segments($ctrl.spanss);
            $scope.$watch("$ctrl.spanss", function() {
                $ctrl.segments = Spans.segments($ctrl.spanss);
            });
        };
    }
});