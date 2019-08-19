function partition() {
    return {
        restrict: 'E',
        templateUrl : 'html/directives/partition.html',
        scope : {
            partition : '=ngModel',
            keys : '<',
            forId : '<?'
        },
        link :  {
            pre : function(scope, element, attrs) {

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
    }
}

export default partition;
