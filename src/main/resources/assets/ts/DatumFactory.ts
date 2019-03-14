type Obj = { [key: string]: any };

class DatumFactory {


    public static datum(rawData:Obj, keys:Map<string,Key<any>>):Datum {
      
        const data:Map<string,any> = new Map();

        for(let i in rawData.data) {
            
            const value:any = DatumFactory.get(keys.get(i), rawData.data[i], keys);

            data.set(i, value);
        }

        return new Datum(data, keys);
    }

    public static key<T>(name:string, type:Type<T>):Key<T> {

        let bits:string[] = name.split('/');

        if(bits.length == 2) {
            return new Key(bits[0], bits[1], type);
        } else if(bits.length == 1) {
            return new Key(null, bits[0], type);
        } else {
            throw "invalid key name: '"+name+"'";
        }        
    }

    public static keyFromObj<T>(key:Obj):Key<T> {
        if(key.namespace) {
            return new Key(key.namespace, key.name, DatumFactory.type(key.type));            
        } else {
            return new Key(null, key.name, DatumFactory.type(key.type));        
        }
        
    }

    public static type<T>(raw:Obj):Type<T> {
    
        const clazz = raw['class'];

        const typeParameters:Array<Type<any>> = new Array();

        for(let i in raw['typeParameters']) {
            const rawTypeParam:Obj = raw['typeParameters'][i];
            typeParameters.push(Type.from(rawTypeParam));
        }

        return new Type(clazz, typeParameters);
    }

    public static spans<T,V>(obj:Obj, keys:Map<string,Key<any>>):Spans<T,V> {
        
        const target:Key<T> = keys.get(obj.target);

        let spans:Spans<T,V> = new Spans(target);

        for(let i in obj.spans) {
            const span:Span<T,V> = DatumFactory.span(obj.spans[i], keys);
            spans = spans.with(span);
        }

        return spans;
    }

    public static span<T,V>(obj:Obj, keys:Map<string,Key<any>>):Span<T,V> {

        // const wiith:V = DatumFactory.get(keys.get(obj.target), obj.with, keys);

        const span:Span<T,V> = new Span<T,V>(keys.get(obj.target), obj.from, obj.to, obj.with);

        return span;
    }

    public static get(key:Key<any>, obj:Obj, keys:Map<string,Key<any>>):any {
        
        if(key.type.equals(Types.SPANS)) {
            return DatumFactory.spans(obj, keys);
        } else if(key.type.equals(Types.LABEL)) {
            return obj;
        } else if(key.type.equals(Types.STRING)) {
            return obj;
        } else if(key.type.equals(Types.LIST)) {
            return obj;
        } else if(key.type.equals(Types.LONG)) {
            return obj;
        }  else if(key.type.equals(Types.BOOLEAN)){
            return obj;
        } else {
            // console.log("WARN: Unknown type - " + key.type.getKlass());
            return obj;   
        }
    }
}