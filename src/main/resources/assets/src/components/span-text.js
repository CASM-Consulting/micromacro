const spanText = {
    bindings : {
        spanss: '<',
        updateSpan: '&'
    },
    templateUrl: 'html/components/spanText.html',
    controller : function($scope, Spans) {

        var $ctrl = this;

        $ctrl.activeEnd = false;
        $ctrl.activeBegin = false;

        $ctrl.isSelected = ( segment ) => {
            var span = false;
            if($ctrl.activeEnd) {
                span = $ctrl.activeEnd.span;
            } else if($ctrl.activeBegin) {
                span = $ctrl.activeBegin.span;
            }
            if(span) {
                return segment.from >= span.from && segment.to <= span.to;
            } else {
                false;
            }
        };

        $ctrl.$onInit = function() {

            $ctrl.segments = Spans.segments($ctrl.spanss);

            $scope.$watch("$ctrl.spanss", function() {
                $ctrl.segments = Spans.segments($ctrl.spanss);
            });

            $ctrl.changeEnd = (end) => {
                $ctrl.activeEnd = end;
                $ctrl.activeBegin = false;
            };

            $ctrl.changeBegin = (begin) => {
                $ctrl.activeBegin = begin;
                $ctrl.activeEnd = false;
            };

            $ctrl.selectSegment = (segment) => {
                if($ctrl.activeEnd) {
                    var span = $ctrl.activeEnd.span;
                    if(segment.to >= span.from) {
                        var i = $ctrl.activeEnd.i;
                        var j = $ctrl.activeEnd.j;
                        span.to = segment.to;
                        $ctrl.updateSpan({span: span, i: i, j: j});
                        $ctrl.activeEnd = false;
                    }
                } else if( $ctrl.activeBegin ) {
                    var span = $ctrl.activeBegin.span;
                    if(segment.from < span.to) {
                        var i = $ctrl.activeBegin.i;
                        var j = $ctrl.activeBegin.j;
                        span.from = segment.from;
                        $ctrl.updateSpan({span: span, i: i, j: j});
                        $ctrl.activeBegin = false;
                    }
                } else if( $ctrl.activeSegment ) {

                }
            };
        };
    }
};

export default spanText;
