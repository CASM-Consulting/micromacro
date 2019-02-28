MicroMacroApp.factory("Rows", function(Types, Datums) {

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
            }
        });


        //process spans next
        angular.forEach(displayKeys, function(display, keyName) {

            var key = datum.getKey(keyName);

            if(!datum.get(key) || !display) return;

            if( key.type.equals(Types.SPANS) ) {

                var spans = datum.resolve(key);

                var target = datum.get(keyName).target.key();

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
});