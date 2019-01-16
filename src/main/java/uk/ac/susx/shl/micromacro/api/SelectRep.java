package uk.ac.susx.shl.micromacro.api;

import java.util.List;
import java.util.Map;

public class SelectRep {

    public String table;
    public String filter;
    public Map<String, Map<String,String>> literals;
    public List<String> orderBy;
    public int limit;

}
