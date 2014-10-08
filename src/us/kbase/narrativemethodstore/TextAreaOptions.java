
package us.kbase.narrativemethodstore;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * <p>Original spec-file type: TextAreaOptions</p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "n_rows"
})
public class TextAreaOptions {

    @JsonProperty("n_rows")
    private Long nRows;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("n_rows")
    public Long getNRows() {
        return nRows;
    }

    @JsonProperty("n_rows")
    public void setNRows(Long nRows) {
        this.nRows = nRows;
    }

    public TextAreaOptions withNRows(Long nRows) {
        this.nRows = nRows;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return ((((("TextAreaOptions"+" [nRows=")+ nRows)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
