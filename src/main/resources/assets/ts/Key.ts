/// <reference path="./Type.ts" />

//import { Type } from "./Type";


/*export*/ class Key<T> {

    // private readonly isMapped:boolean;
    public readonly name:string;
    public readonly namespace:string;
    public readonly type:Type<T>;

    constructor(namespace:string, name:string, type:Type<T>) {
        this.type = type;
        this.namespace = namespace;
        this.name = name;
    }

    public key():string {
        return (this.namespace?this.namespace+"/":"")+this.name;
    }

}
