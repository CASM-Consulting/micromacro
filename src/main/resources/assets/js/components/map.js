MicroMacroApp.component('map', {
    templateUrl : 'html/components/map.html',
    bindings : {
        map : '<'
    },
    controller : function(Datums, $q, $stateParams, $http, $compile, leafletData, debounce, $window, Queries, Maps) {
        var $ctrl = this;

        var colours = ['purple', 'green', 'red', 'blue', 'orange', 'black'];

        var heatmapDecay = 20;
        var heatmapIntensity = 1;

        var DATE_FORMAT = 'YYYY-MM-DD';

        var tilesDict = {
            openstreetmap: {
                url: "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            },
            oldlondon: {
                url: "https://nls-2.tileserver.com/fpsUZba7ERPD/{z}/{x}/{y}.png"
            }
        };
        $ctrl.changeTiles = function(tiles) {
            $ctrl.leafletMap.tiles = tilesDict[tiles];
        };

        $ctrl.leafletMap = {
            center: {
                lat: 51.51,
                lng: -0.11,
                zoom: 12
            },

            defaults: {
//                scrollWheelZoom: true,
                minZoom: 10,
                maxZoom: 14,
                wheelDebounceTime: 100,
//                scrollWheelZoom:false,
                zoomSnap: 0,
                zoomDelta: 0.1,
                wheelPxPerZoomLevel : 10
            },

            tiles: tilesDict.oldlondon,

            layers: {
                baselayers : {},
                overlays: {}
            }

        };
     
        $ctrl.timelineDuration = 100;

        var timelines = [];
        var timelineControl = null;

        $ctrl.queries = [];

        $ctrl.$onInit = () => {

//            $ctrl.map = $ctrl.map || {id:'123', queries:['select1']};

            $ctrl.geoKey = Datums.key($ctrl.map.geoKey).key();

            Maps.getQueries($stateParams.workspaceId, $ctrl.map).then((queries) => {
                
                $ctrl.queries = queries;
                
                return Maps.getDateLimits(queries, $ctrl.map).then((bounds) => {
                    $ctrl.minDate = moment(bounds[0]);
                    $ctrl.maxDate = moment(bounds[1]);
                });

            }).then(() => {
                
                drawTimelines();
            });


        }

        var drawTimelines = () => {

            timelines.forEach( timeline => {
                if(timeline) {
                    timeline.remove();        
                }
            });

            if(timelineControl) {
                timelineControl.remove();
            }

            leafletData.getMap().then(function(map) {

                var dates = [];
                var times = [];

                var date = moment($ctrl.minDate);

                while(date <= $ctrl.maxDate) {
                    var tmp = moment(date);
                    dates.push(tmp.format(DATE_FORMAT));
                    times.push(tmp.add(1000, "y").toDate().getTime())
                    date.add(1, 'days');
                }

                var daysCovered = $ctrl.maxDate.diff($ctrl.minDate, 'days');

                timelineControl = L.timelineSliderControl({
                    steps: daysCovered,
                    start : $ctrl.minDate.add(1000, "y").toDate().getTime(),
                    end: $ctrl.maxDate.add(1000, "y").toDate().getTime(),
                    // duration : daysCovered * $ctrl.timelineDuration,
    //                enableKeyboardControls: true,
                    formatOutput: function(date){
                        return moment(date).subtract(1000, "y").format(DATE_FORMAT);
                    }
                });
                
                $ctrl.lineChartData = [];
                $ctrl.lineChartOptions = {
                    chart : {
                        height: 200,
                        x: (d) => {
                            return moment(d[0]).toDate();
                        },
                        y: (d) => {
                            return d[1];
                        },
                        xAxis: {
                            tickFormat: (d) => {
                                return d3.time.format('%Y-%m-%d')(new Date(d));
                            },
                            axisLabel: 'Date'
                        },
                    }
                };

                var lineChartPromises = $ctrl.queries.map( (query, idx) => {

                    return Queries.counts(query, dates).then(counts => {
                        $ctrl.lineChartData.push({
                            key : query._id,
                            values : counts
                        });
                    });
                });

                $q.all(lineChartPromises).then( () => {
                    $ctrl.lineChartDataReady = true;
                });

                var drawHeat = (date) => {

                    var from = moment(date).subtract(heatmapDecay, 'days');//.format(DATE_FORMAT);
                    var to = moment(date);//.format(DATE_FORMAT);

                    var dates = [];
                    var data = [];

                    var date = moment(from);

                    while(date <= to) {
                        var tmp = moment(date);
                        dates.push(tmp.format(DATE_FORMAT));
                        date.add(1, 'days');
                    }

                    var promises = $ctrl.queries.map((query) => {
                        return Queries.partitions(query, dates);
                    });

                    $q.all(promises).then(( queryResults )=>{
                        var merged = {};
                        dates.forEach( ( date ) => {
                            merged[date] = [];
                            queryResults.forEach( (queryResult) => {
                                merged[date] = merged[date].concat(queryResult[date]);
                            });

                            merged[date] = Maps.data2geoJson(merged[date], $ctrl.geoKey);
                        });

                        var i = 0;

                        for(var d = moment(from); d.diff(to, 'days') <= 0; d.add(1, 'days') ) {
                            var dayDate = moment(d).format(DATE_FORMAT);
                            var dayData = merged[dayDate].features;

                            var intensity = (i / heatmapDecay) * heatmapIntensity;

                            for(var j = 0; j < dayData.length; ++j) {
                                var point = [dayData[j].geometry.coordinates[1],dayData[j].geometry.coordinates[0]];
                                data.push(point.concat(intensity));
                            }

                            ++i;
                        }
                        if(!data) return;

                        leafletData.getLayers().then(function(layers) {
                            if(layers.overlays.heat) {
                                layers.overlays.heat.setLatLngs(data);
                            } else {
                                $ctrl.leafletMap.layers.overlays = {
                                    heat : {
                                        name: 'Heat Map',
                                        type: 'heat',
                                        data: data,
                                        layerOptions: {
                                            radius: 10,
                                            blur: 5
                                        },
                                        visible: true
                                    }
                                };
                            }
                        });
                    });
                }

                timelines = $ctrl.queries.map( (query, idx) => {


                    var timeline = L.timeline(null, {
                        start : $ctrl.minDate.add(1000, "y").toDate().getTime(),
                        end: $ctrl.maxDate.add(1000, "y").toDate().getTime(),
                        // getInterval: getInterval,
                        drawOnSetTime: false,
                        pointToLayer: function(data, latlng) {
                            var colour = colours[idx];

                            return L.circleMarker(latlng, {radius:5, color:colour}).bindPopup(function(l) {
                                return "<ul>" +
                                "<li>Match: " + data.metadata.with[0].match + "</li>"+
                                // "<li>Original: " + data.metadata.spanned + "</li>"+
                                "<li>Lat: " + latlng.lat + "</li>"+
                                "<li>Lng: " + latlng.lng+ "</li>"+
                                // "<li>Date: " + data.metadata.date + "</li>"+
                                // "<li>Trial: " + data.metadata.trialId + "</li>"+
                                "</ul>";
                            });
                        }
                    });
                    timeline.times = times;
                    timelineControl.addTo(map);
                    timelineControl.addTimelines(timeline);
                    timeline.addTo(map);
                    timeline.on('change', function(e){
                        $ctrl.selectedDate = moment(e.target.time).subtract(1000, "y").toDate();
                        Maps.getData(query, $ctrl.map, $ctrl.selectedDate)
                        .then(featureCollection => {
                            timeline.addData(featureCollection);
                        });
                        drawHeat($ctrl.selectedDate);
                    });
                    return timeline;
                });

                var overlays = {};
                
                for(var i = 0; i < $ctrl.queries.length; ++i) {
                    overlays[$ctrl.queries[i]._id] = timelines[i];
                }

                L.control.layers(null, overlays).addTo(map);
            });
        };
    }
});
