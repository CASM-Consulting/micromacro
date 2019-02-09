package uk.ac.susx.shl.micromacro.api;

import com.google.common.base.Objects;

import java.util.Map;

public class ProxyRep extends AbstractDatumQueryRep {

    public String target;
    public String proxy;
    public String partitionKey;
    public String orderBy;
    public int proximity;
    public int limit;
    public Map<String, Map<String,String>> literals;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProxyRep proxyRep = (ProxyRep) o;
        return proximity == proxyRep.proximity &&
                limit == proxyRep.limit &&
                Objects.equal(target, proxyRep.target) &&
                Objects.equal(proxy, proxyRep.proxy) &&
                Objects.equal(partitionKey, proxyRep.partitionKey) &&
                Objects.equal(orderBy, proxyRep.orderBy) &&
                Objects.equal(literals, proxyRep.literals);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(target, proxy, partitionKey, orderBy, proximity, limit, literals);
    }
}
