

class List<T> extends Array<T> {

    private constructor(items?: Array<T>) {
        super(...items)
    }

    public static create<T>(items?: Array<T>):List<T> {
        let list:List<T> = new List(Object.create(List.prototype).concat(items));
        return list;
    }

    public with(val:T):List<T>  {
        let list:List<T> = List.create(this);
        list.push(val);
        return list;
    } 
}