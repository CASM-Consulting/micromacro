function lineChart() {
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
}

export default lineChart;
