package uk.ac.susx.shl.micromacro.api;

import com.google.common.base.Objects;

import java.util.List;
import java.util.Map;

public class SelectRep extends AbstractDatumQueryRep {

    public String filter;
    public Map<String, Map<String,String>> literals;
    public List<String> orderBy;
    public int limit;
    public int offset;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectRep selectRep = (SelectRep) o;
        return limit == selectRep.limit &&
                offset == selectRep.offset &&
                Objects.equal(filter, selectRep.filter) &&
                Objects.equal(literals, selectRep.literals) &&
                Objects.equal(orderBy, selectRep.orderBy);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(filter, literals, orderBy, limit, offset);
    }
}
