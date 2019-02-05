MicroMacroApp.component('row', {
    bindings : {
        datum : '<',
        selectedKeys : '<',
        widths : '<',
        keyList : '<',
        keys : '<'
    },
    templateUrl : 'html/row.html',
    controller : function($scope, $state, Types) {
        var $ctrl = this;

        //TODO: use Types service
        var LABEL = $scope.LABEL = 'uk.ac.susx.tag.method51.twitter.LabelDecision';
        var STRING = $scope.STRING = 'java.lang.String';
        var LIST = $scope.LIST = 'java.util.List';
        var SPAN = $scope.SPAN = 'uk.ac.susx.tag.method51.core.meta.span.Spans';

        $ctrl.$onInit = function() {
            $scope.targets = {};

            angular.forEach($ctrl.keys, function(key, keyName) {

                if( type(key) == SPAN && $ctrl.datum.get(keyName) ) {

                    var target = getTarget($ctrl.datum.get(keyName)).key();

                    if( !(target in $scope.targets) ) {

                        $scope.targets[target] = {};
                    }
                    $scope.targets[target][keyName] = $ctrl.datum.get(keyName).spans;
                }

            });

//
//
//            $scope.$watchCollection("$ctrl.selectedKeys", function() {
//                $scope.targets = {};
//                $scope.display = {};
//                angular.forEach($ctrl.selectedKeys, function(selected, keyName) {
//                    if(selected && $ctrl.data[keyName]) {
//
//                        var key = $ctrl.keys[keyName];
//
//                        if(type(key) == STRING || type(key) == LIST) {
//
//                            if(keyName in $ctrl.selectedKeys) {
//
//                                $scope.display[keyName]=key;
//                            }
//                        } else if(type(key) == SPAN) {
//
//                            var target = getTarget($ctrl.data[keyName]);
//                            if( !(target in $scope.targets) ) {
//
//                                $scope.targets[target] = {};
//                            }
//                            $scope.targets[target][keyName] = $ctrl.data[keyName].spans;
//                        }
//                    }
//                });
//            });
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