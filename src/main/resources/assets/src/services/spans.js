function spans(Datums, Types) {
    var colours = ['grey',  'orange', 'green', 'blue', 'red']


    var segments = function(spanColumn) {
        var text = spanColumn.text;
        var spanss = spanColumn.spans;
        var spanIndices = [];

        for(var i = 0; i < text.length+1; ++i) {
            spanIndices.push({begins:[],ends:[]});
        }

        var idx = 0;

        var n = text.length;

        for(var i in spanss) {
            var spans = spanss[i].spans.spans;
            angular.forEach(spans, (span, j) => {
                span._text = text.substring(span.from, span.to);
                spanIndices[span.from].begins.push({
                        color: colours[idx],
                        span : span,
                        i : i,
                        j : j
                    });

                spanIndices[span.to].ends.push({
                        color: colours[idx],
                        span : span,
                        i : i,
                        j : j
                    });

            });
            ++idx;
        }

        var segments = [];

        var prev = 0;

        for(var i in spanIndices) {
            var spansAtIdx = spanIndices[i];

            if(spansAtIdx.begins.length || spansAtIdx.ends.length) {
                var segmentText = text.substring(prev, i);

                segments.push({
                    text: segmentText,
                    begins : spansAtIdx.begins,
                    ends : spansAtIdx.ends,
                    from : prev*1,
                    to : i*1
                });
                prev = i;
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
}

export default spans;
