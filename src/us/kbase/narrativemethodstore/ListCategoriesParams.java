
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
 * <p>Original spec-file type: ListCategoriesParams</p>
 * <pre>
 * load_methods - optional field (default value is 1)
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "load_methods"
})
public class ListCategoriesParams {

    @JsonProperty("load_methods")
    private Long loadMethods;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("load_methods")
    public Long getLoadMethods() {
        return loadMethods;
    }

    @JsonProperty("load_methods")
    public void setLoadMethods(Long loadMethods) {
        this.loadMethods = loadMethods;
    }

    public ListCategoriesParams withLoadMethods(Long loadMethods) {
        this.loadMethods = loadMethods;
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
        return ((((("ListCategoriesParams"+" [loadMethods=")+ loadMethods)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
