package uk.ac.susx.shl.micromacro.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

public class DatumRep implements Serializable {
    public long id;
    public Map data;

    public DatumRep(long id, Map data) {
        this.id = id;
        this.data = data;
    }
}
