MicroMacroApp.factory("Datums", function() {

    var data = function(rawData, rawKeys) {

        var data = [];

        for(var i in rawData) {

            var rawDatum = rawData[i];

            var datum = DatumFactory.datum(rawDatum, rawKeys);

            data.push(datum);
        }

        return data;
    };

    return {
        data : data
    };
});