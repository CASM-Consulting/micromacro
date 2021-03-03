const spanText = {
    bindings : {
        spanss: '<',
        updateSpan: '&'
    },
    templateUrl: 'html/components/spanText.html',
    controller : function($scope, Spans) {

        var $ctrl = this;

        $ctrl.activeSegment = false;

        $ctrl.$onInit = function() {

            $ctrl.segments = Spans.segments($ctrl.spanss);

            $scope.$watch("$ctrl.spanss", function() {
                $ctrl.segments = Spans.segments($ctrl.spanss);
            });

            $ctrl.changeEnd = (end) => {
                if(!$ctrl.activeEnd) {
                    $ctrl.activeEnd = end;
                }
            };

            $ctrl.selectSegment = (segment) => {
                if($ctrl.activeEnd) {
                    var span = $ctrl.activeEnd.span;
                    var i = $ctrl.activeEnd.i;
                    var j = $ctrl.activeEnd.j;
                    span.to = segment.i;
                    $ctrl.updateSpan({span: span, i: i, j: j});
                }
            };
        };
    }
};

export default spanText;
