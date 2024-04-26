
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
 * <p>Original spec-file type: ValidateMethodParams</p>
 * <pre>
 * verbose - flag for adding more details into error messages (like stack traces).
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "spec_json",
    "display_yaml",
    "extra_files",
    "verbose"
})
public class ValidateMethodParams {

    @JsonProperty("id")
    private java.lang.String id;
    @JsonProperty("spec_json")
    private java.lang.String specJson;
    @JsonProperty("display_yaml")
    private java.lang.String displayYaml;
    @JsonProperty("extra_files")
    private Map<String, String> extraFiles;
    @JsonProperty("verbose")
    private Long verbose;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("id")
    public java.lang.String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(java.lang.String id) {
        this.id = id;
    }

    public ValidateMethodParams withId(java.lang.String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("spec_json")
    public java.lang.String getSpecJson() {
        return specJson;
    }

    @JsonProperty("spec_json")
    public void setSpecJson(java.lang.String specJson) {
        this.specJson = specJson;
    }

    public ValidateMethodParams withSpecJson(java.lang.String specJson) {
        this.specJson = specJson;
        return this;
    }

    @JsonProperty("display_yaml")
    public java.lang.String getDisplayYaml() {
        return displayYaml;
    }

    @JsonProperty("display_yaml")
    public void setDisplayYaml(java.lang.String displayYaml) {
        this.displayYaml = displayYaml;
    }

    public ValidateMethodParams withDisplayYaml(java.lang.String displayYaml) {
        this.displayYaml = displayYaml;
        return this;
    }

    @JsonProperty("extra_files")
    public Map<String, String> getExtraFiles() {
        return extraFiles;
    }

    @JsonProperty("extra_files")
    public void setExtraFiles(Map<String, String> extraFiles) {
        this.extraFiles = extraFiles;
    }

    public ValidateMethodParams withExtraFiles(Map<String, String> extraFiles) {
        this.extraFiles = extraFiles;
        return this;
    }

    @JsonProperty("verbose")
    public Long getVerbose() {
        return verbose;
    }

    @JsonProperty("verbose")
    public void setVerbose(Long verbose) {
        this.verbose = verbose;
    }

    public ValidateMethodParams withVerbose(Long verbose) {
        this.verbose = verbose;
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
        return ((((((((((((("ValidateMethodParams"+" [id=")+ id)+", specJson=")+ specJson)+", displayYaml=")+ displayYaml)+", extraFiles=")+ extraFiles)+", verbose=")+ verbose)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
