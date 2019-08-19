import { Key } from "./Key";
import { Types } from "./Types";
import { Type } from "./Type";


export class Util {

    public static getTargetType<T>(key: Key<T>): Type<T> {
        if (!key.type.equals(Types.SPANS)) {
            throw "Not an Spans Key";
        }

        // return key.type.getTypeParameters().get(0);
        return key.type.getTypeParameters()[0];
    }

    public static isTargetTypeSpans<T>(key: Key<T>): boolean {

        return Util.getTargetType(key).equals(Types.SPANS);
    }

}
