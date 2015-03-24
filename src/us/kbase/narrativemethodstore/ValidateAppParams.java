
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
 * <p>Original spec-file type: ValidateAppParams</p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "spec_json",
    "display_yaml"
})
public class ValidateAppParams {

    @JsonProperty("id")
    private String id;
    @JsonProperty("spec_json")
    private String specJson;
    @JsonProperty("display_yaml")
    private String displayYaml;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public ValidateAppParams withId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("spec_json")
    public String getSpecJson() {
        return specJson;
    }

    @JsonProperty("spec_json")
    public void setSpecJson(String specJson) {
        this.specJson = specJson;
    }

    public ValidateAppParams withSpecJson(String specJson) {
        this.specJson = specJson;
        return this;
    }

    @JsonProperty("display_yaml")
    public String getDisplayYaml() {
        return displayYaml;
    }

    @JsonProperty("display_yaml")
    public void setDisplayYaml(String displayYaml) {
        this.displayYaml = displayYaml;
    }

    public ValidateAppParams withDisplayYaml(String displayYaml) {
        this.displayYaml = displayYaml;
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
        return ((((((((("ValidateAppParams"+" [id=")+ id)+", specJson=")+ specJson)+", displayYaml=")+ displayYaml)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
