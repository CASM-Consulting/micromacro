package uk.ac.susx.shl.micromacro.api;

import java.util.Map;

public class ProxyRep {

    public String table;
    public String target;
    public String proxy;
    public String partitionKey;
    public String orderBy;
    public int proximity;
    public int limit;
    public Map<String, Map<String,String>> literals;

}
