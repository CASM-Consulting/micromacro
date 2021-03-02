import moment from 'moment';

function maps($q, Queries, Datums, Server) {
    var MAX_DATE = new Date( 8640000000000000);
    var MIN_DATE = new Date(-8640000000000000);

    var data2geoJson = (data, key) => {

        var features = data.filter(datum => key in datum).flatMap(datum => {
            return datum[key].spans.map( (span, idx) => {
                var match = span.with[0];
                var feature = {
                    geometry: {
                        type : "Point",
                        coordinates : [match.resource.lng, match.resource.lat]
                    },
                    type : "Feature",
                    metadata : span,
                    datum: datum,
                    idx : idx
                }
                return feature;
            });

        });

        var featureCollection = {
            type : "FeatureCollection",
            features : features
        };

        return featureCollection;
    };

    /**
     * Collects keys from tables across all relevant map queries.
     */
    var getKeys = (map) => {

        var dateKey = $ctrl.map.dateKey;
        var keysPromises = [];

        $ctrl.keyList = [];

        angular.forEach(map.queries, (on, query) => {
            on && keysPromises.push(promiseTable(query));
        });

        $q.all(tablePromises).then((tables) => {

            var schemaPromises = tables.map( (table) => {
                return promiseSchema(table);
            });

            $q.all(schemaPromises).then( (keyss) => {
                var keys = keyss[0];
                var rest = keyss.slice(1);
                if(rest.length > 0) {
                    rest.reduce((keys, moreKeys) => {
                        angular.forEach(keys, (key, name) => {
                            if(!(name in moreKeys)) {
                                delete keys[name];
                            }
                        });
                        return keys;
                    }, keys);
                }
                $ctrl.keyList = keyList(keys);
                $ctrl.map.dateKey = dateKey;
            })
        });
    }

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

        data2geoJson : data2geoJson,

        getData : (query, config, time) => {

            var date = moment(time).format("YYYY-MM-DD");

            return Queries.partition(query, date).then(data => {

                var key = Datums.key(config.geoKey).key();

                var featureCollection = data2geoJson(data, key);

                return featureCollection;
            });
        }
    }
}

export default maps;
