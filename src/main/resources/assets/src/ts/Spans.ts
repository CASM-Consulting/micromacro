import { Key } from "./Key";
import { Span } from "./Span";

export class Spans<T, V> {

    public readonly target: Key<T>;
    public readonly spans: Array<Span<T, V>>;

    public constructor(target: Key<T>, spans?: Array<Span<T, V>>) {

        this.target = target;
        // this.spans = spans || new Array<Span<T,V>>();

        this.spans = spans || new Array();
    }

    // public with(from:number, to:number, wiith:V):Spans<T,V> {

    //     // return new Spans(this.target, this.spans.push(new Span(this.target, from, to, wiith)))

    //     const newSpans:Array<Span<T,V>> = Array.create(this.spans);
    //     newSpans.push(new Span(this.target, from, to, wiith));

    //     return new Spans(this.target, newSpans);
    // }

    public with(span: Span<T, V>): Spans<T, V> {

        // return new Spans(this.target, this.spans.push(new Span(this.target, from, to, wiith)))

        let newSpans: Array<Span<T, V>> = new Array<Span<T, V>>();
        newSpans = newSpans.concat(this.spans);
        newSpans.push(span);

        return new Spans(this.target, newSpans);
    }
}
