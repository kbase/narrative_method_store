
package us.kbase.narrativemethodstore;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import us.kbase.common.service.UObject;


/**
 * <p>Original spec-file type: ServiceMethodInputMapping</p>
 * <pre>
 * input_parameter - parameter_id, if not specified then one of 'constant_value' or 
 *     'narrative_system_variable' should be set.
 * constant_value - constant value, could be even map/array, if not specified then 'input_parameter' or
 *     'narrative_system_variable' should be set.
 * narrative_system_variable - name of internal narrative framework property, currently only these names are
 *     supported: 'workspace', 'token', 'user_id'; if not specified then one of 'input_parameter' or
 *     'constant_value' should be set.
 * target_argument_position - position of argument in RPC-method call, optional field, default value is 0.
 * target_property - name of field inside structure that will be send as arguement. Optional field,
 *     in case this field is not defined (or null) whole object will be sent as method argument instead of
 *     wrapping it by structure with inner property defined by 'target_property'.
 * target_type_transform - none/string/int/float/list<type>/mapping<type>/ref, optional field, default is 
 *     no transformation.
 * @optional input_parameter constant_value narrative_system_variable target_argument_position target_property target_type_transform
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "input_parameter",
    "constant_value",
    "narrative_system_variable",
    "target_argument_position",
    "target_property",
    "target_type_transform"
})
public class ServiceMethodInputMapping {

    @JsonProperty("input_parameter")
    private String inputParameter;
    @JsonProperty("constant_value")
    private UObject constantValue;
    @JsonProperty("narrative_system_variable")
    private String narrativeSystemVariable;
    @JsonProperty("target_argument_position")
    private Long targetArgumentPosition;
    @JsonProperty("target_property")
    private String targetProperty;
    @JsonProperty("target_type_transform")
    private String targetTypeTransform;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("input_parameter")
    public String getInputParameter() {
        return inputParameter;
    }

    @JsonProperty("input_parameter")
    public void setInputParameter(String inputParameter) {
        this.inputParameter = inputParameter;
    }

    public ServiceMethodInputMapping withInputParameter(String inputParameter) {
        this.inputParameter = inputParameter;
        return this;
    }

    @JsonProperty("constant_value")
    public UObject getConstantValue() {
        return constantValue;
    }

    @JsonProperty("constant_value")
    public void setConstantValue(UObject constantValue) {
        this.constantValue = constantValue;
    }

    public ServiceMethodInputMapping withConstantValue(UObject constantValue) {
        this.constantValue = constantValue;
        return this;
    }

    @JsonProperty("narrative_system_variable")
    public String getNarrativeSystemVariable() {
        return narrativeSystemVariable;
    }

    @JsonProperty("narrative_system_variable")
    public void setNarrativeSystemVariable(String narrativeSystemVariable) {
        this.narrativeSystemVariable = narrativeSystemVariable;
    }

    public ServiceMethodInputMapping withNarrativeSystemVariable(String narrativeSystemVariable) {
        this.narrativeSystemVariable = narrativeSystemVariable;
        return this;
    }

    @JsonProperty("target_argument_position")
    public Long getTargetArgumentPosition() {
        return targetArgumentPosition;
    }

    @JsonProperty("target_argument_position")
    public void setTargetArgumentPosition(Long targetArgumentPosition) {
        this.targetArgumentPosition = targetArgumentPosition;
    }

    public ServiceMethodInputMapping withTargetArgumentPosition(Long targetArgumentPosition) {
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

    public ServiceMethodInputMapping withTargetProperty(String targetProperty) {
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

    public ServiceMethodInputMapping withTargetTypeTransform(String targetTypeTransform) {
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
        return ((((((((((((((("ServiceMethodInputMapping"+" [inputParameter=")+ inputParameter)+", constantValue=")+ constantValue)+", narrativeSystemVariable=")+ narrativeSystemVariable)+", targetArgumentPosition=")+ targetArgumentPosition)+", targetProperty=")+ targetProperty)+", targetTypeTransform=")+ targetTypeTransform)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
