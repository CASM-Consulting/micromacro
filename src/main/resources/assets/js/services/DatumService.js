MicroMacroApp.factory("Datums", function() {

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

    return {
        data : data,
        datum : datum
    };
});