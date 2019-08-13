import { Type } from "./Type";
import { Spans } from "./Spans";

export class Types {
    public static readonly SPANS: Type<Spans<any, any>> = Type.of('uk.ac.susx.tag.method51.core.meta.span.Spans');
    public static readonly LABEL: Type<string> = Type.of('uk.ac.susx.tag.method51.twitter.LabelDecision');
    public static readonly STRING: Type<string> = Type.of('java.lang.String');
    public static readonly LIST: Type<Array<any>> = Type.of('java.util.List');
    public static readonly LONG: Type<number> = Type.of('java.lang.Long');
    public static readonly DOUBLE: Type<number> = Type.of('java.lang.Double');
    public static readonly BOOLEAN: Type<boolean> = Type.of('java.lang.Boolean');
    public static readonly DATE: Type<Date> = Type.of('org.joda.time.Instant');
    public static readonly DATE2: Type<Date> = Type.of('java.time.LocalDate');
}
