
package us.kbase.narrativemethodstore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * <p>Original spec-file type: TextOptions</p>
 * <pre>
 * valid_ws_types  - list of valid ws types that can be used for input
 * validate_as     - int | float | nonnumeric | none
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "valid_ws_types",
    "validate_as"
})
public class TextOptions {

    @JsonProperty("valid_ws_types")
    private List<String> validWsTypes;
    @JsonProperty("validate_as")
    private java.lang.String validateAs;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("valid_ws_types")
    public List<String> getValidWsTypes() {
        return validWsTypes;
    }

    @JsonProperty("valid_ws_types")
    public void setValidWsTypes(List<String> validWsTypes) {
        this.validWsTypes = validWsTypes;
    }

    public TextOptions withValidWsTypes(List<String> validWsTypes) {
        this.validWsTypes = validWsTypes;
        return this;
    }

    @JsonProperty("validate_as")
    public java.lang.String getValidateAs() {
        return validateAs;
    }

    @JsonProperty("validate_as")
    public void setValidateAs(java.lang.String validateAs) {
        this.validateAs = validateAs;
    }

    public TextOptions withValidateAs(java.lang.String validateAs) {
        this.validateAs = validateAs;
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
        return ((((((("TextOptions"+" [validWsTypes=")+ validWsTypes)+", validateAs=")+ validateAs)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
