function collapseZone() {
    return {
        restrict: "A",
        scope: true,
        link: function (scope, elem, attrs) {
            scope._collapseZone = scope.$eval(attrs.collapseZone) || true;

            scope.$watch(attrs.collapseZone, function (val) {
                if (val) {
                    scope._collapseZone = val;
                }
            });

            if (attrs.collapseZoneId) {
                scope.$watch(attrs.collapseZoneId, function (val) {
                    if (val && typeof val === 'string') {
                        scope._collapseZone._setZoneId(val);
                    }
                });
            }
        }
    }
}

export default collapseZone;
