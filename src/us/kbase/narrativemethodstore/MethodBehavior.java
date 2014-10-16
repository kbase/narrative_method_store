
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
 * <p>Original spec-file type: MethodBehavior</p>
 * <pre>
 * Determines how the method is handled when run.
 * kb_service_name - name of service which will be part of fully qualified method name, optional field (in
 *     case it's not defined developer should enter fully qualified name with dot into 'kb_service_method'.
 * kb_service_parameters_mapping - mapping from parameter_id to service method arguments (in case
 *     mapping is not described for some parameter it will be mapped into structure with target_property
 *     equal to parameter id.
 * @optional python_function kb_service_name kb_service_method kb_service_parameters_mapping kb_service_workspace_name_mapping
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "python_class",
    "python_function",
    "kb_service_url",
    "kb_service_name",
    "kb_service_method",
    "kb_service_parameters_mapping",
    "kb_service_workspace_name_mapping"
})
public class MethodBehavior {

    @JsonProperty("python_class")
    private java.lang.String pythonClass;
    @JsonProperty("python_function")
    private java.lang.String pythonFunction;
    @JsonProperty("kb_service_url")
    private java.lang.String kbServiceUrl;
    @JsonProperty("kb_service_name")
    private java.lang.String kbServiceName;
    @JsonProperty("kb_service_method")
    private java.lang.String kbServiceMethod;
    @JsonProperty("kb_service_parameters_mapping")
    private Map<String, us.kbase.narrativemethodstore.MethodParameterMapping> kbServiceParametersMapping;
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
    @JsonProperty("kb_service_workspace_name_mapping")
    private us.kbase.narrativemethodstore.MethodParameterMapping kbServiceWorkspaceNameMapping;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("python_class")
    public java.lang.String getPythonClass() {
        return pythonClass;
    }

    @JsonProperty("python_class")
    public void setPythonClass(java.lang.String pythonClass) {
        this.pythonClass = pythonClass;
    }

    public MethodBehavior withPythonClass(java.lang.String pythonClass) {
        this.pythonClass = pythonClass;
        return this;
    }

    @JsonProperty("python_function")
    public java.lang.String getPythonFunction() {
        return pythonFunction;
    }

    @JsonProperty("python_function")
    public void setPythonFunction(java.lang.String pythonFunction) {
        this.pythonFunction = pythonFunction;
    }

    public MethodBehavior withPythonFunction(java.lang.String pythonFunction) {
        this.pythonFunction = pythonFunction;
        return this;
    }

    @JsonProperty("kb_service_url")
    public java.lang.String getKbServiceUrl() {
        return kbServiceUrl;
    }

    @JsonProperty("kb_service_url")
    public void setKbServiceUrl(java.lang.String kbServiceUrl) {
        this.kbServiceUrl = kbServiceUrl;
    }

    public MethodBehavior withKbServiceUrl(java.lang.String kbServiceUrl) {
        this.kbServiceUrl = kbServiceUrl;
        return this;
    }

    @JsonProperty("kb_service_name")
    public java.lang.String getKbServiceName() {
        return kbServiceName;
    }

    @JsonProperty("kb_service_name")
    public void setKbServiceName(java.lang.String kbServiceName) {
        this.kbServiceName = kbServiceName;
    }

    public MethodBehavior withKbServiceName(java.lang.String kbServiceName) {
        this.kbServiceName = kbServiceName;
        return this;
    }

    @JsonProperty("kb_service_method")
    public java.lang.String getKbServiceMethod() {
        return kbServiceMethod;
    }

    @JsonProperty("kb_service_method")
    public void setKbServiceMethod(java.lang.String kbServiceMethod) {
        this.kbServiceMethod = kbServiceMethod;
    }

    public MethodBehavior withKbServiceMethod(java.lang.String kbServiceMethod) {
        this.kbServiceMethod = kbServiceMethod;
        return this;
    }

    @JsonProperty("kb_service_parameters_mapping")
    public Map<String, us.kbase.narrativemethodstore.MethodParameterMapping> getKbServiceParametersMapping() {
        return kbServiceParametersMapping;
    }

    @JsonProperty("kb_service_parameters_mapping")
    public void setKbServiceParametersMapping(Map<String, us.kbase.narrativemethodstore.MethodParameterMapping> kbServiceParametersMapping) {
        this.kbServiceParametersMapping = kbServiceParametersMapping;
    }

    public MethodBehavior withKbServiceParametersMapping(Map<String, us.kbase.narrativemethodstore.MethodParameterMapping> kbServiceParametersMapping) {
        this.kbServiceParametersMapping = kbServiceParametersMapping;
        return this;
    }

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
    @JsonProperty("kb_service_workspace_name_mapping")
    public us.kbase.narrativemethodstore.MethodParameterMapping getKbServiceWorkspaceNameMapping() {
        return kbServiceWorkspaceNameMapping;
    }

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
    @JsonProperty("kb_service_workspace_name_mapping")
    public void setKbServiceWorkspaceNameMapping(us.kbase.narrativemethodstore.MethodParameterMapping kbServiceWorkspaceNameMapping) {
        this.kbServiceWorkspaceNameMapping = kbServiceWorkspaceNameMapping;
    }

    public MethodBehavior withKbServiceWorkspaceNameMapping(us.kbase.narrativemethodstore.MethodParameterMapping kbServiceWorkspaceNameMapping) {
        this.kbServiceWorkspaceNameMapping = kbServiceWorkspaceNameMapping;
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
        return ((((((((((((((((("MethodBehavior"+" [pythonClass=")+ pythonClass)+", pythonFunction=")+ pythonFunction)+", kbServiceUrl=")+ kbServiceUrl)+", kbServiceName=")+ kbServiceName)+", kbServiceMethod=")+ kbServiceMethod)+", kbServiceParametersMapping=")+ kbServiceParametersMapping)+", kbServiceWorkspaceNameMapping=")+ kbServiceWorkspaceNameMapping)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
