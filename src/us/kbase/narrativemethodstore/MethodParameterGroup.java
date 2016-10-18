
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
 * <p>Original spec-file type: MethodParameterGroup</p>
 * <pre>
 * Description of a method parameter.
 * id - id of the parameter group, must be unique within the method among all parameters 
 *            and groups
 * parameter_ids - IDs of parameters included in this group
 * ui_name - short name that is displayed to the user
 * short_hint - short phrase or sentence describing the parameter group
 * description - longer and more technical description of the parameter group (long-hint)
 * allow_mutiple - allows entry of a list instead of a single structure, default is 0
 *                 if set, the number of starting boxes will be either 1 or the
 *                 number of elements in the default_values list
 * optional - set to true to make the group optional, default is 0
 * id_mapping - optional mapping for parameter IDs used to pack group into resulting
 *                 value structure (not used for non-multiple groups)
 * with_border - flag for one-copy groups saying to show these group with border
 * @optional id_mapping
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "parameter_ids",
    "ui_name",
    "short_hint",
    "description",
    "allow_multiple",
    "optional",
    "id_mapping",
    "with_border"
})
public class MethodParameterGroup {

    @JsonProperty("id")
    private java.lang.String id;
    @JsonProperty("parameter_ids")
    private List<String> parameterIds;
    @JsonProperty("ui_name")
    private java.lang.String uiName;
    @JsonProperty("short_hint")
    private java.lang.String shortHint;
    @JsonProperty("description")
    private java.lang.String description;
    @JsonProperty("allow_multiple")
    private Long allowMultiple;
    @JsonProperty("optional")
    private Long optional;
    @JsonProperty("id_mapping")
    private Map<String, String> idMapping;
    @JsonProperty("with_border")
    private Long withBorder;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("id")
    public java.lang.String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(java.lang.String id) {
        this.id = id;
    }

    public MethodParameterGroup withId(java.lang.String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("parameter_ids")
    public List<String> getParameterIds() {
        return parameterIds;
    }

    @JsonProperty("parameter_ids")
    public void setParameterIds(List<String> parameterIds) {
        this.parameterIds = parameterIds;
    }

    public MethodParameterGroup withParameterIds(List<String> parameterIds) {
        this.parameterIds = parameterIds;
        return this;
    }

    @JsonProperty("ui_name")
    public java.lang.String getUiName() {
        return uiName;
    }

    @JsonProperty("ui_name")
    public void setUiName(java.lang.String uiName) {
        this.uiName = uiName;
    }

    public MethodParameterGroup withUiName(java.lang.String uiName) {
        this.uiName = uiName;
        return this;
    }

    @JsonProperty("short_hint")
    public java.lang.String getShortHint() {
        return shortHint;
    }

    @JsonProperty("short_hint")
    public void setShortHint(java.lang.String shortHint) {
        this.shortHint = shortHint;
    }

    public MethodParameterGroup withShortHint(java.lang.String shortHint) {
        this.shortHint = shortHint;
        return this;
    }

    @JsonProperty("description")
    public java.lang.String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(java.lang.String description) {
        this.description = description;
    }

    public MethodParameterGroup withDescription(java.lang.String description) {
        this.description = description;
        return this;
    }

    @JsonProperty("allow_multiple")
    public Long getAllowMultiple() {
        return allowMultiple;
    }

    @JsonProperty("allow_multiple")
    public void setAllowMultiple(Long allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public MethodParameterGroup withAllowMultiple(Long allowMultiple) {
        this.allowMultiple = allowMultiple;
        return this;
    }

    @JsonProperty("optional")
    public Long getOptional() {
        return optional;
    }

    @JsonProperty("optional")
    public void setOptional(Long optional) {
        this.optional = optional;
    }

    public MethodParameterGroup withOptional(Long optional) {
        this.optional = optional;
        return this;
    }

    @JsonProperty("id_mapping")
    public Map<String, String> getIdMapping() {
        return idMapping;
    }

    @JsonProperty("id_mapping")
    public void setIdMapping(Map<String, String> idMapping) {
        this.idMapping = idMapping;
    }

    public MethodParameterGroup withIdMapping(Map<String, String> idMapping) {
        this.idMapping = idMapping;
        return this;
    }

    @JsonProperty("with_border")
    public Long getWithBorder() {
        return withBorder;
    }

    @JsonProperty("with_border")
    public void setWithBorder(Long withBorder) {
        this.withBorder = withBorder;
    }

    public MethodParameterGroup withWithBorder(Long withBorder) {
        this.withBorder = withBorder;
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
        return ((((((((((((((((((((("MethodParameterGroup"+" [id=")+ id)+", parameterIds=")+ parameterIds)+", uiName=")+ uiName)+", shortHint=")+ shortHint)+", description=")+ description)+", allowMultiple=")+ allowMultiple)+", optional=")+ optional)+", idMapping=")+ idMapping)+", withBorder=")+ withBorder)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
