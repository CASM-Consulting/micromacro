//import { Array } from "immutable";
//// <reference path="./List.ts" />


/*export*/ class Type<T> {
    private readonly clazz:string;
    private readonly typeParameters:Array<Type<any>>;

    constructor(clazz:string, typeParameters?:Array<Type<any>>) {
        this.clazz = clazz;
        this.typeParameters = typeParameters || new Array<Type<any>>();
        // this.typeParameters = typeParameters || Array.create();
    }

    public static of<T>(clazz:string, typeParameters?:Array<Type<any>>):Type<T> {
        return new Type(clazz, typeParameters);
    }

    public static from<T>(raw:Obj):Type<T> {

        const clazz = raw['class'];

        const typeParameters:Array<Type<any>> = new Array();

        for(let i in raw['typeParameters']) {
            const rawTypeParam:Obj = raw['typeParameters'][i];
            typeParameters.push(Type.from(rawTypeParam));
        }

        return new Type(clazz, typeParameters);
    }

    public getTypeParameters():Array<Type<any>> {
        return this.typeParameters;
    }

    public getClazz():string {
        return this.clazz;
    }
    
    public equals(other:Type<any>):boolean {
        return other.getClazz() == this.clazz;// && other.getTypeParameters() == this.typeParameters;
    }

}
