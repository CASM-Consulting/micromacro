package uk.ac.susx.shl.micromacro.db;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatumWrapper  {
    private final long id;
    private final String data;

    @JsonCreator
    public DatumWrapper(@JsonProperty("id") long id, @JsonProperty("data") String data) {
        this.id = id;
        this.data = data;
    }

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("data")
    public String getData() {
        return data;
    }
}
