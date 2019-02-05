/// <reference path="./Util.ts" />
/// <reference path="./List.ts" />


// import { Key } from "./Key";
// import { List } from "immutable";
// import { Span } from "./Span";

/*export*/ class Spans<T,V> { 

    public readonly target:Key<T>;
    public readonly spans:List<Span<T,V>>;
    
    public constructor (target:Key<T>, spans?:List<Span<T,V>>) {

        this.target = target;
        // this.spans = spans || new List<Span<T,V>>();

        this.spans = spans || List.create();
    }

    public with(from:number, to:number, wiith:V):Spans<T,V> {

        // return new Spans(this.target, this.spans.push(new Span(this.target, from, to, wiith)))

        const newSpans:List<Span<T,V>> = List.create(this.spans);
        newSpans.push(new Span(this.target, from, to, wiith));

        return new Spans(this.target, newSpans);
    }

}
