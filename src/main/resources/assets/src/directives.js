function initializeDirectives(MicroMacroApp) {
    console.log("initializing directives");

    MicroMacroApp.directive("collapseZone", function () {
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
    });MicroMacroApp.directive("key", function () {
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
    });
    MicroMacroApp.directive('keySelector', function(Collapse) {
        return {
            restrict: 'A',
            scope : {
                "selectedKeys" : "=",
                "keys" : "=",
                "invert" : "=",
                "msg": "=",
                "useField": "=?",
                "onSelect": "&?"
            },
            templateUrl : 'html/directives/key-selector.html',
            link: function($scope, element, attr) {
                $scope.kz = Collapse.newCollapseZone();
                $scope.selectedKeys = $scope.selectedKeys || [];
                $scope.showing = false;

                var useField = 'useField' in attr? $scope.useField : false;
                var onChange = 'onSelect' in attr? $scope.onSelect : function(){};

                $scope.$watch(useField? 'keys.'+useField: 'keys.incoming', function (val) {
                    if (val) {
                        $scope.selectedKeys = $scope.selectedKeys || [];
                        $scope.namespace2keys = {}
                        val = Object.keys(val);
                        $scope.showing = val.length > 0;
                        val.sort();
                        angular.forEach(val, function (keyName) {
                            var key = {str: keyName};
                            if(!$scope.invert) {
                                if ($scope.selectedKeys.indexOf(keyName) > -1) {
                                    key.selected = true;
                                } else {
                                    key.selected = false;
                                }
                            } else {
                                if ($scope.selectedKeys.indexOf(keyName) > -1) {
                                    key.selected = false;
                                } else {
                                    key.selected = true;
                                }
                            }

                            var idx = keyName.indexOf("/"),
                                ns,
                                name,
                                ks;

                            if (idx > -1) {
                                ns = keyName.substring(0, idx);
                                name = keyName.substring(idx+1);
                            } else {
                                ns = "ε";
                                name = keyName;
                            }

                            key.name = name;

                            if ((ks = $scope.namespace2keys[ns]) == null) {
                                ks = $scope.namespace2keys[ns] = [];
                            }

                            ks.push(key);
                        });
                    }
                });

                $scope.selectKey = function (key, noSave) {
                    if(!$scope.invert) {
                        if(!key.selected) {
                            $scope.selectedKeys.push(key.str);
                            key.selected = true;
                            if (!noSave) onChange();
                        }
                    } else {
                        if(!key.selected) {
                            $scope.selectedKeys.splice($scope.selectedKeys.indexOf(key.str), 1);
                            key.selected = true;
                            if (!noSave) onChange();
                        }
                    }
                };

                $scope.deselectKey = function (key, noSave) {
                    if(!$scope.invert) {
                        if (key.selected) {
                            $scope.selectedKeys.splice($scope.selectedKeys.indexOf(key.str), 1);
                            key.selected = false;
                            if (!noSave) onChange();
                        }
                    } else {
                        if (key.selected) {
                            $scope.selectedKeys.push(key.str);
                            key.selected = false;
                            if (!noSave) onChange();
                        }
                    }
                }

                $scope.toggleKey = function (key) {
                    if (!key.selected) {
                        $scope.selectKey(key);
                    } else {
                        $scope.deselectKey(key);
                    }
                    onChange();
                };


                $scope.selectAll = function (ns, noSave) {
                    if (ns){
                        $scope.namespace2keys[ns].forEach(function(element){
                            $scope.selectKey(element, true);
                        });
                    } else {
                        angular.forEach($scope.namespace2keys, function(value, key) {
                            $scope.selectAll(key, true);
                        });
                    }
                    if (!noSave) {
                        onChange();
                    }
                };

                $scope.deselectAll = function (ns, noSave) {
                    if (ns){
                        $scope.namespace2keys[ns].forEach(function(element){
                            $scope.deselectKey(element, true);
                        });
                    } else {
                        angular.forEach($scope.namespace2keys, function(value, key) {
                            $scope.deselectAll(key, true);
                        });
                    }
                    if (!noSave) {
                        onChange();
                    }
                };

                $scope.nsMixed = function (ns) {
                    var isSelecting = false;
                    var isDeselecting = false;
                    for (var i in $scope.namespace2keys[ns]) {
                        var k = $scope.namespace2keys[ns][i];
                        if (k.selected) {
                            isSelecting = true;
                        } else {
                            isDeselecting = true;
                        }
                        if (isDeselecting && isSelecting) return true;
                    }
                };

                $scope.nsDisabled = function (ns) {
                    var isSelecting = false;
                    var isDeselecting = false;
                    for (var i in $scope.namespace2keys[ns]) {
                        var k = $scope.namespace2keys[ns][i];
                        if (k.selected) {
                            isSelecting = true;
                        } else {
                            isDeselecting = true;
                        }
                    }

                    return isDeselecting && !isSelecting;
                };
            }
        };
    });MicroMacroApp.directive("lineChart", function () {
        return {
            restrict: 'E',
            templateUrl : 'html/directives/lineChart.html',
            scope : {
                ngModel : '=',
                options : '='
            },
            link : function(scope, element, attrs) {

                var defaultOptions = {
                    chart: {
                        type: 'lineChart',
                        height: 450,
                        width: 800,
                        margin : {
                            top: 20,
                            right: 20,
                            bottom: 40,
                            left: 55
                        },
                        x: function(d){ return d.x; },
                        y: function(d){ return d.y; },
                        useInteractiveGuideline: true,
                        dispatch: {
                            stateChange: function(e){ console.log("stateChange"); },
                            changeState: function(e){ console.log("changeState"); },
                            tooltipShow: function(e){ console.log("tooltipShow"); },
                            tooltipHide: function(e){ console.log("tooltipHide"); }
                        },
                        xAxis: {
                            axisLabel: 'Time (ms)'
                        },
                        yAxis: {
                            //                        axisLabel: 'Voltage (v)',
                            tickFormat: function(d){
                                return d3.format('.0f')(d);
                            },
                            axisLabelDistance: -10
                        },
                        callback: function(chart){
                            console.log("!!! lineChart callback !!!");
                        }
                    },
                    title: {
                        enable: false
                        //                    text: 'Title for Line Chart'
                    },
                    subtitle: {
                        enable: false,
                        //                    text: 'Subtitle for simple line chart. Lorem ipsum dolor sit amet, at eam blandit sadipscing, vim adhuc sanctus disputando ex, cu usu affert alienum urbanitas.',
                        css: {
                            'text-align': 'center',
                            'margin': '10px 13px 0px 7px'
                        }
                    },
                    caption: {
                        enable: false,
                        //                    html: '<b>Figure 1.</b> Lorem ipsum dolor sit amet, at eam blandit sadipscing, <span style="text-decoration: underline;">vim adhuc sanctus disputando ex</span>, cu usu affert alienum urbanitas. <i>Cum in purto erat, mea ne nominavi persecuti reformidans.</i> Docendi blandit abhorreant ea has, minim tantas alterum pro eu. <span style="color: darkred;">Exerci graeci ad vix, elit tacimates ea duo</span>. Id mel eruditi fuisset. Stet vidit patrioque in pro, eum ex veri verterem abhorreant, id unum oportere intellegam nec<sup>[1, <a href="https://github.com/krispo/angular-nvd3" target="_blank">2</a>, 3]</sup>.',
                        css: {
                            'text-align': 'justify',
                            'margin': '10px 13px 0px 7px'
                        }
                    }
                };

                scope.options = angular.merge(defaultOptions, scope.options);

                scope.$watchCollection('ngModel', (d)=>{
                    console.log(d);
                });

            }
        }
    });
    MicroMacroApp.directive("orderBy", function () {
        return {
            restrict: 'E',
            templateUrl : 'html/directives/orderBy.html',
            scope : {
                orderBy : '=ngModel',
                keys : '<',
                forId : '<?'
            },
            link : function(scope, element, attrs) {

                scope.orderBy.clauses = scope.orderBy.clauses || [];

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
    MicroMacroApp.directive("partition", function () {
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
    });
}

export {initializeDirectives};
