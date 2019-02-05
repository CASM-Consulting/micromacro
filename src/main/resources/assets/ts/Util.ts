/// <reference path="./Key.ts" />
/// <reference path="./Types.ts" />
/// <reference path="./Type.ts" />

// import { Key } from "./Key";
// import { Types } from "./Types";
// import { Type } from "./Type";


/*export*/ class Util {

    public static getTargetType<T>(key:Key<T>):Type<T> {
        if(key.type != Types.SPANS) {
            throw "Not an Spans Key";
        }
        
        // return key.type.getTypeParameters().get(0);
        return key.type.getTypeParameters()[0];
    }

    public static isTargetTypeSpans<T>(key:Key<T>):boolean {

        return Util.getTargetType(key) == Types.SPANS;
    }

}
