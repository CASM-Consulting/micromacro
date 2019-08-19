function key() {
    return {
        restrict: 'E',
        templateUrl : 'html/directives/key.html',
        scope : {
            ngModel : '=',
            keys : '<',
            forId : '<?'
        },
        link : function(scope, element, attrs) {

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

export default key;
