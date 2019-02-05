

class List<T> extends Array<T> {

    private constructor(items?: Array<T>) {
        super(...items)
    }

    public static create<T>(items?: Array<T>):List<T> {
        return Object.call(List.prototype, items);
    }

    public with(val:T):List<T>  {
        let list:List<T> = List.create(this);
        list.push(val);
        return list;
    } 
}