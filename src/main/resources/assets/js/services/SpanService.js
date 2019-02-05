MicroMacroApp.factory("Spans", function(Datums, Types) {
    var colours = ['red', 'blue', 'green', 'orange', 'grey']


    var segments = function(text, spanss, displayKeys) {
        var spanIndices = [];

        for(var i = 0; i < text.length; ++i) {
            spanIndices.push({begins:[],ends:[]});
        }

        var idx = 0;

        var n = text.length-1;

        for(var keyName in spanss) {
            if(!displayKeys[keyName]) {
                continue;
            }
            var spans = spanss[keyName];
            for(var jdx in spans) {
                var span = spans[jdx];
                spanIndices[span.from].begins.push({color: colours[idx]});
                spanIndices[Math.min(n,span.to)].ends.push({color: colours[idx]});
            }
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
