// import { Type } from "./Type";
// import { Spans } from "./Spans";

/*export*/ class Types {
    public static readonly SPANS:Type<Spans<any,any>> = Type.of('uk.ac.susx.tag.method51.core.meta.span.Spans');
    public static readonly LABEL:Type<string> = Type.of('uk.ac.susx.tag.method51.twitter.LabelDecision');
    public static readonly STRING:Type<string> = Type.of('java.lang.String');
    public static readonly LIST:Type<Array<any>> = Type.of('java.util.List');
    public static readonly LONG:Type<number> = Type.of('java.lang.Long');
    public static readonly BOOLEAN:Type<number> = Type.of('java.lang.Boolean');
}
