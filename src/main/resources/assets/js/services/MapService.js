MicroMacroApp.factory("Maps", function($q, Queries, Datums, Server) {


    var MAX_DATE = new Date( 8640000000000000);
    var MIN_DATE = new Date(-8640000000000000);

    return {

        load : function(workspaceId, mapId) {
            return $q(function(resolve) {
                Server.get("api/workspace/loadMap", {
                    params : {
                        workspaceId : workspaceId,
                        mapId : mapId
                    },
                    success : resolve
                });
            });
        },

        save : function(workspaceId, mapId, map) {
            return $q(function(resolve) {
                
                Server.post("api/workspace/saveMap", map,  {
                    params : {
                        workspaceId : workspaceId,
                        mapId : mapId
                    },
                    success : resolve
                });
            });
        },

        getQueries : (workspaceId, config) => {

            return $q.all(config.queries.map( id =>{

                return Queries.load(workspaceId, id)
                    .then(query => {
                        query._id = id;
                        return query;
                    });
            }));
        },

        getDateLimits : (queries, config) => {

            var endPromises = queries.map( query => {
                return Queries.count(query).then( count => {
                    return Queries.page(query, count-1).then( data => {
                        var key = Datums.key(query.partition.key);
                        var end = data[0][key.key()];
                        return new Date(end);
                    });
                });
            });


            var beginPromises = queries.map( query  => {
                return Queries.page(query, 0).then( data => {
                    var key = Datums.key(query.partition.key);
                    var begin = data[0][key.key()];
                    return new Date(begin);
                });
            });

            return $q.all(
                [$q.all(beginPromises).then((ends) => {
                    return ends.reduce( (min, date) =>{
                        return date < min ? date : min;
                    }, MAX_DATE);
                }),
                $q.all(endPromises).then(ends => {
                    return ends.reduce( (max, date) => {
                        return date >= max ? date : max;
                    }, MIN_DATE);
                })]
            ).then( (bounds) => {
                return bounds;
            });
        },

        getData : (query, config, time) => {

            var date = moment(time).format("YYYY-MM-DD");

            return Queries.partition(query, date).then(data => {

                var key = Datums.key(config.geoKey).key();

                var features = data.filter(datum => key in datum).flatMap(datum => {
                    return datum[key].spans.map(span => {
                        var match = span.with[0];
                        var feature = {
                            geometry: {
                                type : "Point",
                                coordinates : [match.resource.lng, match.resource.lat]
                            },
                            type : "Feature",
                            metadata : span
                        }
                        return feature;
                    });
                    
                });

                var featureCollcetion = {
                    type : "FeatureCollection",
                    features : features
                };

                return featureCollcetion;
            });
        }
        
    }

});