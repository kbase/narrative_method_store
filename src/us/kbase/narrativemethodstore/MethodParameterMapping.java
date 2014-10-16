
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
 * <p>Original spec-file type: MethodParameterMapping</p>
 * <pre>
 * target_argument_position - position of argument in RPC-method call, optional field, default value is 0.
 * target_property - name of field inside structure that will be send as arguement. Optional field,
 *     in case this field is not defined (or null) whole object will be sent as method argument instead of
 *     wrapping it by structure with inner property defined by 'target_property'.
 * target_type_transform - none/string/int/float/list<type>/mapping<type>/ref, optional field, default is 
 *     no transformation.
 * @optional target_argument_position target_property target_type_transform
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "target_argument_position",
    "target_property",
    "target_type_transform"
})
public class MethodParameterMapping {

    @JsonProperty("target_argument_position")
    private Long targetArgumentPosition;
    @JsonProperty("target_property")
    private String targetProperty;
    @JsonProperty("target_type_transform")
    private String targetTypeTransform;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("target_argument_position")
    public Long getTargetArgumentPosition() {
        return targetArgumentPosition;
    }

    @JsonProperty("target_argument_position")
    public void setTargetArgumentPosition(Long targetArgumentPosition) {
        this.targetArgumentPosition = targetArgumentPosition;
    }

    public MethodParameterMapping withTargetArgumentPosition(Long targetArgumentPosition) {
        this.targetArgumentPosition = targetArgumentPosition;
        return this;
    }

    @JsonProperty("target_property")
    public String getTargetProperty() {
        return targetProperty;
    }

    @JsonProperty("target_property")
    public void setTargetProperty(String targetProperty) {
        this.targetProperty = targetProperty;
    }

    public MethodParameterMapping withTargetProperty(String targetProperty) {
        this.targetProperty = targetProperty;
        return this;
    }

    @JsonProperty("target_type_transform")
    public String getTargetTypeTransform() {
        return targetTypeTransform;
    }

    @JsonProperty("target_type_transform")
    public void setTargetTypeTransform(String targetTypeTransform) {
        this.targetTypeTransform = targetTypeTransform;
    }

    public MethodParameterMapping withTargetTypeTransform(String targetTypeTransform) {
        this.targetTypeTransform = targetTypeTransform;
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
        return ((((((((("MethodParameterMapping"+" [targetArgumentPosition=")+ targetArgumentPosition)+", targetProperty=")+ targetProperty)+", targetTypeTransform=")+ targetTypeTransform)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
