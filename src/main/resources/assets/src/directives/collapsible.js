function collapsible() {
    return {
        restrict: "A",
        scope: false,
        link: function (scope, elem, attrs) {
            if (!scope._collapseZone) throw new Error("collapsible directive used outside of collapse zone");
            var id = scope.$eval(attrs.collapsible);
            scope.$watch(function () {return scope._collapseZone;}, function (val) {
                if (val !== true) {
                    scope._collapseZone._registerElem(id, elem);
                }
            });

            scope.$watch(attrs.collapsible, function (newVal, oldVal) {
                if (newVal && oldVal && newVal !== oldVal)
                    scope._collapseZone._renameElem(oldVal, newVal);
            });
        }
    }
}

export default collapsible;