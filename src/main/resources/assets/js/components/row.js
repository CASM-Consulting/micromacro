MicroMacroApp.component('row', {
    bindings : {
        datum : '<',
        selectedKeys : '<',
        widths : '<',
        keyList : '<',
        keys : '<'
    },
    templateUrl : 'html/row.html',
    controller : function($scope, $state, Rows, Types) {
        var $ctrl = this;

        //TODO: use Types service
        var LABEL = $scope.LABEL = 'uk.ac.susx.tag.method51.twitter.LabelDecision';
        var STRING = $scope.STRING = 'java.lang.String';
        var LIST = $scope.LIST = 'java.util.List';
        var SPAN = $scope.SPAN = 'uk.ac.susx.tag.method51.core.meta.span.Spans';

        $ctrl.$onInit = function() {
            $scope.targets = {};

            $scope.columns = Rows.getColumns($ctrl.datum, $ctrl.selectedKeys);

            $scope.$watchCollection("$ctrl.selectedKeys", function() {
                $scope.columns = Rows.getColumns($ctrl.datum, $ctrl.selectedKeys);
            });
        };

        var getTarget = $scope.target = function(key) {
            if(!key) {
                return false;
            }
//            if(TOKEN_HACK) {
//                return key.target.replace("-token", '');
//            } else {
                return key.target;
//            }
        };
        var type = function(key) {
            return key.type.class;
        }
        $scope.type = function(keyName) {
            return type($ctrl.keys[keyName]);
        };
    }
});