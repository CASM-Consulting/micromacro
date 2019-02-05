/// <reference path="./Key.ts" />
/// <reference path="./Spans.ts" />
/// <reference path="./Types.ts" />
/// <reference path="./Util.ts" />
//// <reference path="../node_modules/immutable/dist/immutable.js" />


// import { Map } from "immutable";
// import { Key } from "./Key";
// import { Spans } from "./Spans";
// import { Types } from "./Types";
// import { Util } from "./Util";


/*export*/ class Datum {
    
    private readonly keys:Map<string,Key<any>>;
    private readonly data:Map<string,any>;

    public constructor(data?:Map<string,any>, keys?:Map<string,Key<any>>) {
        this.data = data || new Map();
        this.keys = keys || new Map();
    }

    public get<T>(key:Key<T>):T {
        return this.data.get(key.k());
    }

    public getKey<T>(key:string):Key<T> {
        return this.keys.get(key);
    }

    public with<T>(key:Key<T>, value:T):Datum {
        
        let datum:Datum = new Datum(this.data.set(key.k(),value), this.keys.set(key.k(), key));

        return datum;
    }

    public without(key:Key<any>) {

        const newData = new Map(this.data);
        newData.delete(key.k());

        const newKeys = new Map(this.keys);
        newKeys.delete(key.k());

        const datum:Datum = new Datum(newData, newKeys);

        return datum;
    }

    public resolve<T,V>(key:Key<Spans<any,any>>): Spans<T,V> {
        if(key.type != Types.SPANS) {
            throw "not a spans key!";
        }
        if(Util.isTargetTypeSpans(key)) {
            
            const targetKey:Key<Spans<T,V>> = this.get(key).target;
            const newKey:Key<Spans<T,V>> = Key.of(key.k(), targetKey.type);


            // key = key.type = targetType;
            let spans:Spans<T,V> = this.get(key);
            let newSpans = new Spans(targetKey);

            for(const span of spans.spans) {
                const from:number = this.get(targetKey).spans[span.from].from;
                const to:number = this.get(targetKey).spans[span.to-1].to;

                newSpans = newSpans.with(from, to, span.get());
            }

            return this.with(key, newSpans).resolve(key);
        } else {
            return this.get(key);
        }
    };

}
