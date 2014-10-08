
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
 * <p>Original spec-file type: DropdownOptions</p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "ids_to_options"
})
public class DropdownOptions {

    @JsonProperty("ids_to_options")
    private Map<String, String> idsToOptions;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("ids_to_options")
    public Map<String, String> getIdsToOptions() {
        return idsToOptions;
    }

    @JsonProperty("ids_to_options")
    public void setIdsToOptions(Map<String, String> idsToOptions) {
        this.idsToOptions = idsToOptions;
    }

    public DropdownOptions withIdsToOptions(Map<String, String> idsToOptions) {
        this.idsToOptions = idsToOptions;
        return this;
    }

    @JsonAnyGetter
    public Map<java.lang.String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(java.lang.String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public java.lang.String toString() {
        return ((((("DropdownOptions"+" [idsToOptions=")+ idsToOptions)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
