const spanText = {
    bindings : {
        spanss: '<'
    },
    templateUrl: 'html/components/spanText.html',
    controller : function($scope, Spans) {

        var $ctrl = this;

        $ctrl.$onInit = function() {

            $ctrl.segments = Spans.segments($ctrl.spanss);
            $scope.$watch("$ctrl.spanss", function() {
                $ctrl.segments = Spans.segments($ctrl.spanss);
            });
        };
    }
};

export default spanText;
