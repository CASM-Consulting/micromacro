MicroMacroApp.directive("partition", function () {
    return {
        restrict: 'E',
        templateUrl : 'html/partition.html',
        scope : {
            partition : '=ngModel',
            keys : '<',
            forId : '<?'
        },
        link : function(scope, element, attrs) {

            scope.partition.orderBy = scope.partition.orderBy || {};
            scope.partition.function = scope.partition.function || "ROW_NUMBER";

//            scope.$watchCollection("keys", function(newVal, oldVal) {
//
//                scope.keyList = [];
//                angular.forEach(newVal, (item, key) => {
//                    var listItem = angular.copy(item);
//                    listItem.id = listItem.key();
//                    scope.keyList.push(listItem);
//                });
//            });

        }
    }
});
