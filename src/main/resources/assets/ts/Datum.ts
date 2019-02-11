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

    // public get<T>(key:Key<T>):T {
    //     return this.data.get(key.k());
    // }

    public get<T>(key:Key<T> | string):T {
        if(typeof(key) === 'string') {
            return this.data.get(key);
        } else {
            return this.data.get(key.key());
        }
    }

    public getKey<T>(key:string):Key<T> {
        return this.keys.get(key);
    }

    public with<T>(key:Key<T>, value:T):Datum {
        
        let datum:Datum = new Datum(this.data.set(key.key(),value), this.keys.set(key.key(), key));

        return datum;
    }

    public without(key:Key<any>) {

        const newData = new Map(this.data);
        newData.delete(key.key());

        const newKeys = new Map(this.keys);
        newKeys.delete(key.key());

        const datum:Datum = new Datum(newData, newKeys);

        return datum;
    }

    public resolve<T,V>(key:Key<Spans<any,any>> | string): Spans<T,V> {
        if(typeof key === 'string') {
            key = this.getKey(key);
        }
        if(!key.type.equals(Types.SPANS)) {
            throw "not a spans key!";
        }
        if(Util.isTargetTypeSpans(key)) {
            
            const targetKey:Key<Spans<T,V>> = this.get(key).target;
            const newKey:Key<Spans<T,V>> = DatumFactory.key(key.key(), targetKey.type);

            // key = key.type = targetType;
            let spans:Spans<T,V> = this.get(key);
            let newSpans = new Spans(this.get(targetKey).target);

            for(const span of spans.spans) {
                const from:number = this.get(targetKey).spans[span.from].from;
                const to:number = this.get(targetKey).spans[span.to-1].to;

                newSpans = newSpans.with(new Span(targetKey, from, to, span.get()));
            }

            return this.with(newKey, newSpans).resolve(newKey);
        } else {
            return this.get(key);
        }
    };

}
