//import { List } from "immutable";


/*export*/ class Type<T> {
    private readonly clazz:string;
    private readonly typeParamters:List<Type<any>>;

    constructor(clazz:string, typeParameters?:List<Type<any>>) {
        this.clazz = clazz;
        // this.typeParamters = typeParameters || new List<Type<any>>();
        this.typeParamters = typeParameters || List.create();
    }

    public static of<T>(clazz:string, typeParameters?:List<Type<any>>):Type<T> {
        return new Type(clazz, typeParameters);
    }

    public getTypeParameters():List<Type<any>> {
        return this.typeParamters;
    }

    public getClazz():string {
        return this.clazz;
    }

}
