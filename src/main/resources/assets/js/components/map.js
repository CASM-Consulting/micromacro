MicroMacroApp.component('map', {
    templateUrl : 'html/map.html',
    bindings : {
        map : '<'
    },
    controller : function(Datums, $q, $stateParams, $http, $compile, leafletData, debounce, $window, Queries, Maps) {

        var colours = ['purple', 'green', 'red', 'blue', 'orange', 'black'];

        var $ctrl = this;

        var DATE_FORMAT = 'YYYY-MM-DD';

        var tilesDict = {
            openstreetmap: {
                url: "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            },
            oldlondon: {
                url: "https://nls-2.tileserver.com/fpsUZba7ERPD/{z}/{x}/{y}.png"
            }
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
            
            $ctrl.map = $ctrl.map || {id:'123', queries:['select1']};

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

                var times = [];

                var date = moment($ctrl.minDate);

                while(date <= $ctrl.maxDate) {
                    times.push(moment(date).add(1000, "y").toDate().getTime())
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
                
                timelines = $ctrl.queries.map( (query, idx) => {
                    var timeline = L.timeline(null, {
                        start : $ctrl.minDate.add(1000, "y").toDate().getTime(),
                        end: $ctrl.maxDate.add(1000, "y").toDate().getTime(),
                        // getInterval: getInterval,
                        drawOnSetTime: false,
                        pointToLayer: function(data, latlng) {
                            var colour = colours[idx];

                            return L.circleMarker(latlng,{radius:5, color:colour}).bindPopup(function(l) {
                                $ctrl.selectedTrialId = data.metadata.trialId;
                                return "<ul>" +
                                "<li>Match: " + data.metadata.text + "</li>"+
                                "<li>Original: " + data.metadata.spanned + "</li>"+
                                "<li>Lat: " + data.metadata.lat + "</li>"+
                                "<li>Lng: " + data.metadata.lng + "</li>"+
                                "<li>Date: " + data.metadata.date + "</li>"+
                                "<li>Trial: " + data.metadata.trialId + "</li>"+
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
