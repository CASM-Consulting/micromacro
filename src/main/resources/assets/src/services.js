import {DatumFactory} from './ts/DatumFactory';

function initializeServices(MicroMacroApp) {
    console.log("initializing services");

    MicroMacroApp.factory("Collapse", function () {
        return {
            newCollapseZone: function (oneAtATime) {
                var _collapseFlags = {};
                var _zoneId = null;
                var _elems = {};

                var _toggle = function (elemID, toggle) {
                    var elem = _elems[elemID];
                    if (elem) {
                        var tog;
                        if (toggle === true || toggle === false) {
                            tog = !!toggle;
                        } else {
                            tog = !_collapseFlags[elemID];
                        }
                        if (tog) {
                            if (elem.hasClass("sg-collapse")) {
                                elem.removeClass("out");
                                var h = elem.height();
                                elem.css({height: 0});
                                elem.addClass("transitioning");
                                elem[0].offsetHeight;
                                elem.css({height: h});
                                setTimeout(function () {
                                    elem.removeClass("transitioning");
                                    elem.css({height: "auto"});
                                }, 340);
                            } else {
                                elem.addClass("sg-collapse");
                            }
                        } else {
                            if (elem.hasClass("sg-collapse")) {
                                elem.css({height: elem.height()});
                                elem.addClass("transitioning");
                                elem[0].offsetHeight;
                                elem.css({height: 0});
                                setTimeout(function () {
                                    elem.removeClass("transitioning");
                                    elem.addClass("out");
                                    elem.css({height: "auto"});
                                }, 340);
                            } else {
                                elem.addClass("sg-collapse")
                                elem.addClass("out");
                            }
                        }
                        _collapseFlags[elemID] = tog;
                        localStorage["collapse:" + _zoneId] = JSON.stringify(_collapseFlags);
                    }
                };

                return {
                    _setZoneId: function (id) {
                        if (_zoneId) {
                            localStorage["collapse:" + id] = JSON.stringify(_collapseFlags);
                            delete localStorage["collapse:" + +_zoneId];
                            _zoneId = id;
                        } else {
                            _zoneId = id;
                            var existingData = localStorage["collapse:" + _zoneId];
                            if (existingData) {
                                _collapseFlags = JSON.parse(existingData);
                            }
                        }
                    },
                    _registerElem: function (id, elem) {
                        _elems[id] = elem;
                        this.toggle(id, this.isOpen(id));
                    },
                    _renameElem: function (oldId, newId) {
                        _elems[newId] = _elems[oldId];
                        delete _elems[oldId];
                        _collapseFlags[newId] = _collapseFlags[oldId];
                        delete _collapseFlags[oldId];
                    },
                    isCollapsed: function (elemID) {
                        return !_collapseFlags[elemID];
                    },
                    isOpen: function (elemID) {
                        return !!_collapseFlags[elemID];
                    },
                    toggle: function (elemID, toggle) {
                        if(oneAtATime) {
                            _toggle(elemID, toggle);
                            for(var id in _elems) {
                                if(id != elemID && ( elemID.indexOf(id) != 0 && id.indexOf('/') - 1 != elemID.length ) ) {
                                    _toggle(id, false);
                                }
                            }
                        } else {
                            _toggle(elemID, toggle);
                        }
                    }
                }
            }
        }
    });MicroMacroApp.factory("Datums", function() {

        var data = function(rawData, keys) {

            var data = [];

            for(var i in rawData) {

                var rawDatum = rawData[i];

                var datum = DatumFactory.datum(rawDatum, keys);

                data.push(datum);
            }

            return data;
        };

        var datum = function(rawDatum, rawKeys) {
            return DatumFactory.datum(rawDatum, rawKeys);
        }

        var key = function(rawKey) {
            return DatumFactory.keyFromObj(rawKey);
        }

        return {
            data : data,
            datum : datum,
            key : key
        };
    });MicroMacroApp.factory("Filters", function(Types) {

        var filters = {};

        filters[Types.SPANS.getKlass()] = ["present"];
        filters[Types.LABEL.getKlass()] = ["present", "equals"];
        filters[Types.STRING.getKlass()] = ["present", "equals", "regex"];
        filters[Types.BOOLEAN.getKlass()] = ["present", "equals"];
        filters[Types.LONG.getKlass()] = ["present", "equals", "range"];
        filters[Types.DOUBLE.getKlass()] = ["present", "equals", "range"];
        filters[Types.DATE.getKlass()] = ["present", "equals", "date_range"];
        filters[ypes.DATE2.getKlass()] = ["present", "equals", "date_range"];

        var forType = (type) => {
            if(!type) {
                return [];
            }
            if(typeof type == "string")  {
                return filters[type];
            } else {
                return filters[type.getKlass()];
            }
        }

        return {
            forType : forType
        }
    });
    MicroMacroApp.factory("Maps", function($q, Queries, Datums, Server) {


        var MAX_DATE = new Date( 8640000000000000);
        var MIN_DATE = new Date(-8640000000000000);

        var data2geoJson = (data, key) => {

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

            var featureCollection = {
                type : "FeatureCollection",
                features : features
            };

            return featureCollection;
        };

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

    });MicroMacroApp.factory("Queries", function($q, Server, $http) {

        return {

            binProximityResultByPartition : function (result, partitionKey) {


                partitionKey = DatumFactory.keyFromObj(partitionKey).key();
                var curPartition = null;
                var idx = -1;

                return result.reduce( (binned, row) => {
                    var partition;
                    if (row instanceof Datum) {
                        partition = row.get(partitionKey);
                    } else {
                        partition = row[partitionKey];
                    }
                    if(partition != curPartition) {
                        binned.push([row]);
                        curPartition = partition;
                        ++idx;
                    } else {
                        binned[idx].push(row);
                    }
                    return binned;
                }, []);
            },

            load : function(workspaceId, queryId, ver) {
                ver || (ver = 0);
                return $q(function(resolve) {
                    Server.get("api/workspace/loadQuery", {
                        params : {
                            workspaceId : workspaceId,
                            queryId : queryId,
                            ver : ver
                        },
                        success : resolve
                    });
                });
            },

            save : function(workspaceId, queryId, query) {
                return $q(function(resolve) {
                    var path;
                    if(query._TYPE == "select") {
                        path = "addSelect";
                    } else {
                        path = "addProximity";
                    }

                    Server.post("api/workspace/"+path, query,  {
                        params : {
                            workspaceId : workspaceId,
                            queryId : queryId
                        },
                        success : resolve
                    });
                });
            },

            deleteQuery : function(workspaceId, queryId) {
                return $q(function(resolve) {
                    Server.get("api/workspace/deleteQuery", {
                        params : {
                            workspaceId : workspaceId,
                            queryId : queryId
                        },
                        success : resolve
                    });
                });
            },

            query : function(query, params) {
                return $q(function(resolve) {
                    var type = query._TYPE;
                    Server.post("api/query/"+type+"/query", query, {
                        params : params,
                        success : function(data) {
                            resolve(data.map( (raw) => {
                                return JSON.parse(raw)
                            }));
                        }
                    });
                });
            },

            skipLimit : function(query, skip, limit) {
                return $q(function(resolve) {
                    var type = query._TYPE;
                    Server.post("api/query/"+type+"/skipLimit", query, {
                        params : {
                            skip: skip,
                            limit: limit
                        },
                        success : function(data) {
                            resolve(data.map( (raw) => {
                                return JSON.parse(raw)
                            }));
                        }
                    });
                });
            },


            count : function(query) {
                return $q(function(resolve) {
                    var type = query._TYPE;
                    Server.post("api/query/"+type+"/cacheOnly", query, {
                        success : function(data) {
                            resolve(data);
                        }
                    });
                });
            },

            page : function(query, page) {
                return $q(function(resolve) {
                    var type = query._TYPE;
                    Server.post("api/query/"+type+"/page", query, {
                        params : {page:page},
                        success : function(data) {
                            resolve(data.map( (raw) => {
                                return JSON.parse(raw)
                            }));
                        }
                    });
                });
            },

            partition : function(query, partition) {
                return $q(function(resolve) {
                    var type = query._TYPE;
                    Server.post("api/query/"+type+"/partition", query, {
                        params : {partition:partition},
                        success : function(data) {
                            resolve(data.map( (raw) => {
                                return JSON.parse(raw)
                            }));
                        }
                    });
                });
            },

            partitions : function(query, partitions) {
                return $q(function(resolve) {
                    var type = query._TYPE;
                    var fd = new FormData();
                    fd.append('query', JSON.stringify(query));
                    fd.append('partitionIds', JSON.stringify(partitions));
                    Server.post("api/query/"+type+"/partitions", fd, {
                        params : {},
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined} ,
                        success : function(data) {
                            var parsed = {};
                            angular.forEach(data, (value, key) => {
                                parsed[key] = value.map( (raw) => {return JSON.parse(raw)} );
                            });
                            resolve(parsed);
                        }
                    });
                });
            },

            update : function(query) {
                return $q(function(resolve) {
                    var type = query._TYPE;
                    Server.post("api/query/"+type+"/update", query, {
                        success : function(data) {
                            resolve(data);
                        }
                    });
                });
            },

            setMeta : function(workspaceId, queryId, metaKey, data, type) {
                return $q(function(resolve) {
                    if(type == "json") {
                        data = JSON.stringify(data);
                    }

                    Server.post("api/workspace/setQueryMeta", data,  {
                        params : {
                            workspaceId : workspaceId,
                            queryId : queryId,
                            metaId : metaKey
                        },
                        success : resolve
                    });
                });
            },
            getMeta : function(workspaceId, queryId, metaKey, type, defaultValue) {

                return $q(function(resolve) {

                    if(type == "json") {
                        resolve = function(data) {
                            if(!data) {
                                return defaultValue;
                            } else {
                                return resolve(JSON.parse(data));
                            }
                        }
                    }

                    Server.get("api/workspace/getQueryMeta",  {
                        params : {
                            workspaceId : workspaceId,
                            queryId : queryId,
                            metaId : metaKey
                        },
                        success : resolve
                    });
                });
            },

            getKeys : (workspaceId, queryId) => {

                return $q(function(resolve) {
                    Server.get("api/workspace/getQueryKeys", {
                        params : {
                            workspaceId : workspaceId,
                            queryId : queryId
                        },
                        success : resolve
                    });
                });
            },

            limitOffset : (query, limit, offset) => {

                query = angular.copy(query);

                if(query._TYPE == "select") {
                    query.limit = limit;
                    query.offset = offset;
                } else if(query._TYPE == "proximity") {
                    query.innerLimit = limit;
                    query.innerOffset = offset;
    //                query.outerLimit = block;
                }

                return query;
            },

            optimise : (query) => {
                return $q(function(resolve) {
                    return $q(function(resolve) {
                        var type = query._TYPE;
                        Server.post("api/query/"+type+"/optimise/", query, {
                            success : resolve
                        });
                    });
                });
            },

            setTableLiterals : (workspaceId, table, literals) => {
                return $q(function(resolve) {
                    Server.post("api/workspace/setTableLiterals", literals, {
                        params : {
                            workspaceId : workspaceId,
                            table : table
                        },
                        success : resolve
                    });
                });
            },

            counts : (query, partitionIds) => {
                var type = query._TYPE;
                var fd = new FormData();
                fd.append('query', JSON.stringify(query));
                fd.append('partitionIds', JSON.stringify(partitionIds));
                return $q(resolve => {
                    Server.post("api/query/"+type+"/counts/", fd, {
                        params : {
                        },
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined} ,
    //                    headers: {'Content-Type': "multipart/form-data"} ,
                        success : resolve
                    });
                });
            },

            chunkCounts : (query) => {
                var type = query._TYPE;
                return $q(resolve => {
                    Server.post("api/query/"+type+"/chunkCounts/", query, {
                        success : resolve
                    });
                });
            },

            partitionPage : function(query, partitionId) {
                return $q(function(resolve) {
                    var type = query._TYPE;
                    Server.post("api/query/"+type+"/partitionPage", query, {
                        params : {partitionId:partitionId},
                        success : function(page) {
                            resolve(page);
                        }
                    });
                });
            },
        }

    });MicroMacroApp.factory("Rows", function(Types, Datums) {

        var getRowColumns = function(datum, displayKeys) {

            var columns = {};


            //process 'normal' fields first
            angular.forEach(displayKeys, function(display, keyName) {
                var key = datum.getKey(keyName);

                if(!datum.get(key) || !display) return;

                if (key.type.equals(Types.LABEL)) {
                    columns[keyName] = {
                        type : 'label',
                        text : datum.get(key).label
                    };
                } else if (key.type.equals(Types.STRING)) {
                    columns[keyName] = {
                        type : 'text',
                        text : datum.get(key),
                        spans : []
                    };
                } else if (key.type.equals(Types.LIST)) {
                    columns[keyName] = {
                        type : 'list',
                        text : datum.get(key)
                    };
                } else if (key.type.equals(Types.LONG)) {
                    columns[keyName] = {
                        type : 'long',
                        text : datum.get(key)
                    };
                } else {
                    columns[keyName] = {
                        type : 'text',
                        text : datum.get(key)
                    };
                }
            });


            //process spans next
            angular.forEach(displayKeys, function(display, keyName) {

                var key = datum.getKey(keyName);

                if(!datum.get(key) || !display) return;

                if( key.type.equals(Types.SPANS) ) {

                    var spans = datum.resolve(key);

                    var target = spans.target.key();

                    if(! (target in columns) ) {
                        console.log("span target not present!");
                        return;
                    }


                    columns[target].type = 'spans';
                    columns[target].spans.push( {
                        key : keyName,
                        spans : spans
                    });
                }
            });

            return columns;
        }

        var getRowsColumns = function(rawData, keys, displayKeys) {
            var keyMap = new Map(Object.entries(keys));
            var data = Datums.data(rawData, keyMap);
            var rows = data.reduce( (datums, datum) => {
                datums.push(getRowColumns(datum, displayKeys));
                return datums;
            },[]);
            return rows;
        }

        return {
            getRowColumns : getRowColumns,
            getRowsColumns : getRowsColumns

        };
    });MicroMacroApp.factory("Spans", function(Datums, Types) {
        var colours = ['red', 'grey', 'blue', 'green', 'orange']


        var segments = function(spanColumn) {
            var text = spanColumn.text;
            var spanss = spanColumn.spans;
            var spanIndices = [];

            for(var i = 0; i < text.length; ++i) {
                spanIndices.push({begins:[],ends:[]});
            }

            var idx = 0;

            var n = text.length-1;

            for(var i in spanss) {
                var spans = spanss[i].spans.spans;
                angular.forEach(spans, (span) => {
                    spanIndices[span.from].begins.push({color: colours[idx]});
                    spanIndices[Math.min(n,span.to)].ends.push({color: colours[idx]});
                });
                ++idx;
            }

            var segments = [];

            var prev = 0;

            for(var i in spanIndices) {
                var spansAtIdx = spanIndices[i];

                if(spansAtIdx.begins.length || spansAtIdx.ends.length) {
                    var segmentText = text.substring(prev, i);
                    prev = i;

                    segments.push({
                        text: segmentText,
                        begins : spansAtIdx.begins,
                        ends : spansAtIdx.ends
                    });
                }

            }

            if(prev < text.length) {
                segments.push({
                    text: text.substring(prev, text.length),
                    begins : [],
                    ends : []
                });
            }

            return segments;
        };



        return {
            segments : segments
        };
    });
    MicroMacroApp.factory("Tables", function($q, Server) {

        return {
            list : function() {
                return $q(function(resolve) {
                    Server.get("api/tables/list", {
                        success : resolve
                    });
                });
            },
            schema : function(table, asList) {
                return $q(function(resolve) {
                    Server.get("api/tables/schema", {
                        params : {
                            table : table
                        },
                        success : function(keySet) {
                            var keys = {};
                            angular.forEach(keySet, (value, key) => {
                                var type = DatumFactory.type(value.type);
                                keys[key] = DatumFactory.key(key, type);
                            });

                            if(asList) {
                                var keyList = [];
                                angular.forEach(keys, (item, key) => {
                                    var listItem = angular.copy(item);
                                    listItem.id = listItem.key();
                                    keyList.push(listItem);
                                });
                                keyList.sort((a,b)=>{return a.id.localeCompare(b.id)});
                                resolve(keyList);
                            } else {
                                resolve(keys);
                            }
                        }
                    });
                });
            }
        }
    });
    MicroMacroApp.factory("Types", function() {
        return {
            SPANS : Types.SPANS,
            LABEL : Types.LABEL,
            STRING : Types.STRING,
            LIST : Types.LIST,
            LONG : Types.LONG,
            DOUBLE : Types.DOUBLE,
            BOOLEAN : Types.BOOLEAN,
            DATE : Types.DATE,
            DATE2 : Types.DATE2
        }
    });
    MicroMacroApp.factory("Server", function($http){

        return {
            get : function(url, options) {

                var error = options.error || function(err){
                    alert(err.data.message);
                };
                var success = options.success || function(){};
                options.params = options.params || {};

                $http.get(url, options).then(function (data, status) {
                    success(data.data);
                }, function (data, status) {
                    error(data, status);
                });

            },
            post : function(url, data, options) {
                options = options || {};
                var error = options.error ||function(err){
                    alert(err.data.message);
                }; 
                var success = options.success || function(){};
                options.params = options.params || {};

                $http.post(url, data, options).then(function (data, status) {
                    success(data.data);
                }, function (data, status) {
                    error(data, status);
                });
            }

        }

    });MicroMacroApp.factory("Workspaces", function($q, Server) {
        return {
            list : function() {
                return $q(function(resolve){
                    Server.get("api/workspaces/list", {
                        success : resolve
                    });
                });
            },
            create : function(name) {
                return $q(function(resolve){
                    Server.get("api/workspaces/create", {
                        params : {name : name},
                        success : resolve
                    });
                });
            },
            load : function(id) {
                return $q(function(resolve) {
                    Server.get("api/workspaces/load", {
                        params : {name : id},
                        success : resolve
                    });
                });
            },
            clearCache : function(workspaceId, queryId) {
                return $q(function(resolve) {
                    Server.get("api/workspace/clearCache", {
                        params : {
                            workspaceId : workspaceId,
                            queryId : queryId
                        },
                        success : resolve
                    });
                });
            }
        };

    });
}

export {initializeServices};
