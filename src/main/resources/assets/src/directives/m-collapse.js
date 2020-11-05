function mCollapse() {
    return {
        restrict: "A",
        scope: false,
        link: function (scope, elem, attrs) {
            if (!scope._collapseZone) throw new Error("collapse directive used outside of collapse zone");
            var targetId = scope.$eval(attrs.mCollapse);
            scope.$watch(attrs.mCollapse, function (newVal, oldVal) {
                if (oldVal && newVal) {
                    targetId = newVal;
                }
            });
            elem.click(function () {
                scope._collapseZone.toggle(targetId);
                scope.$root.$$phase || scope.$apply();
                return false;
            });
        }
    }
}

export default mCollapse;