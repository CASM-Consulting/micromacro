MicroMacroApp.factory("Datums", function() {

    var data = function(rawData) {

        var data = [];

        for(var i in rawData) {
            var rawDatum = rawData[i];
            var datum = new Datum();


            data.push(datum);
        }

        return data;
    };

    return {
        data : data
    };
});