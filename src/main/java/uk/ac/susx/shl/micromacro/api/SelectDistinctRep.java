package uk.ac.susx.shl.micromacro.api;

import java.util.Map;

public class SelectDistinctRep extends AbstractQueryRep{

    public String filter;
    public String distinctKey;
    public Map<String, Map<String,String>> literals;

}
