import {DatumFactory} from '../ts/DatumFactory';

function datums() {
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
}

export default datums;
