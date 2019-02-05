/// <reference path="./Type.ts" />

//import { Type } from "./Type";


/*export*/ class Key<T> {

    // private readonly isMapped:boolean;
    public readonly name:string;
    public readonly namespace:string;
    public readonly type:Type<T>;

    constructor(name:string, namespace:string, type:Type<T>) {
        this.type = type;
        this.name = name;
        this.namespace = namespace;
    }

    public k():string {
        return this.namespace+"/"+this.name;
    }

    public static of<T>(name:string, type:Type<T>):Key<T> {
        let bits:string[] = name.split('/');

        return new Key(bits[0], bits[1], type);
    }

}
