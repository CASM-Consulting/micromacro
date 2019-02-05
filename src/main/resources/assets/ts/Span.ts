/// <reference path="./Key.ts" />

// import { Key } from "./Key";

/*export*/ class Span<T,V> {

    public readonly from:number;
    public readonly to:number;
    public readonly target:Key<T>;
    public readonly with:V;


    constructor(target:Key<T>, from:number, to:number, wiith:V) {
        this.target = target;
        this.from = from;
        this.to = to;
        this.with = wiith;
    }

    public get():V {
        return this.with;
    }

}
