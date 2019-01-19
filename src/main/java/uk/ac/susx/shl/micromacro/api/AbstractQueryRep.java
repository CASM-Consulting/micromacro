package uk.ac.susx.shl.micromacro.api;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbstractQueryRep implements Serializable {

    public transient String type;
    public String table;


    @JsonGetter
    public String getType() {
        return type;
    }

    @JsonSetter
    public void setType(String type) {
        this.type = type;
    }
}
