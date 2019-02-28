//import { Array } from "immutable";
//// <reference path="./List.ts" />


/*export*/ class Type<T> {
    private readonly type:string;
    private readonly klass:string;
    private readonly typeParameters:Array<Type<any>>;

    constructor(klass:string, typeParameters?:Array<Type<any>>) {
        this.klass = klass;
        this.type = "class";
        this.typeParameters = typeParameters || new Array<Type<any>>();
        // this.typeParameters = typeParameters || Array.create();
    }

    public static of<T>(klass:string, typeParameters?:Array<Type<any>>):Type<T> {
        return new Type(klass, typeParameters);
    }

    public static from<T>(raw:Obj):Type<T> {

        const klass = raw['class'];

        const typeParameters:Array<Type<any>> = new Array();

        for(let i in raw['typeParameters']) {
            const rawTypeParam:Obj = raw['typeParameters'][i];
            typeParameters.push(Type.from(rawTypeParam));
        }

        return new Type(klass, typeParameters);
    }

    public getTypeParameters():Array<Type<any>> {
        return this.typeParameters;
    }

    public getKlass():string {
        return this.klass;
    }
    
    public equals(other:Type<any>):boolean {
        return other.getKlass() == this.klass;// && other.getTypeParameters() == this.typeParameters;
    }

}
