
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
 * <p>Original spec-file type: MethodBehavior</p>
 * <pre>
 * Determines how the method is handled when run.
 * kb_service_name - name of service which will be part of fully qualified method name, optional field (in
 *     case it's not defined developer should enter fully qualified name with dot into 'kb_service_method'.
 * kb_service_input_mapping - mapping from input parameters to input service method arguments.
 * kb_service_output_mapping - mapping from output of service method to final output of narrative method.
 * output_mapping - mapping from input to final output of narrative method to support steps without operations.
 * @optional python_function kb_service_name kb_service_method kb_service_input_mapping kb_service_output_mapping
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
    "kb_service_input_mapping",
    "kb_service_output_mapping",
    "output_mapping"
})
public class MethodBehavior {

    @JsonProperty("python_class")
    private String pythonClass;
    @JsonProperty("python_function")
    private String pythonFunction;
    @JsonProperty("kb_service_url")
    private String kbServiceUrl;
    @JsonProperty("kb_service_name")
    private String kbServiceName;
    @JsonProperty("kb_service_method")
    private String kbServiceMethod;
    @JsonProperty("kb_service_input_mapping")
    private List<ServiceMethodInputMapping> kbServiceInputMapping;
    @JsonProperty("kb_service_output_mapping")
    private List<ServiceMethodOutputMapping> kbServiceOutputMapping;
    @JsonProperty("output_mapping")
    private List<OutputMapping> outputMapping;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("python_class")
    public String getPythonClass() {
        return pythonClass;
    }

    @JsonProperty("python_class")
    public void setPythonClass(String pythonClass) {
        this.pythonClass = pythonClass;
    }

    public MethodBehavior withPythonClass(String pythonClass) {
        this.pythonClass = pythonClass;
        return this;
    }

    @JsonProperty("python_function")
    public String getPythonFunction() {
        return pythonFunction;
    }

    @JsonProperty("python_function")
    public void setPythonFunction(String pythonFunction) {
        this.pythonFunction = pythonFunction;
    }

    public MethodBehavior withPythonFunction(String pythonFunction) {
        this.pythonFunction = pythonFunction;
        return this;
    }

    @JsonProperty("kb_service_url")
    public String getKbServiceUrl() {
        return kbServiceUrl;
    }

    @JsonProperty("kb_service_url")
    public void setKbServiceUrl(String kbServiceUrl) {
        this.kbServiceUrl = kbServiceUrl;
    }

    public MethodBehavior withKbServiceUrl(String kbServiceUrl) {
        this.kbServiceUrl = kbServiceUrl;
        return this;
    }

    @JsonProperty("kb_service_name")
    public String getKbServiceName() {
        return kbServiceName;
    }

    @JsonProperty("kb_service_name")
    public void setKbServiceName(String kbServiceName) {
        this.kbServiceName = kbServiceName;
    }

    public MethodBehavior withKbServiceName(String kbServiceName) {
        this.kbServiceName = kbServiceName;
        return this;
    }

    @JsonProperty("kb_service_method")
    public String getKbServiceMethod() {
        return kbServiceMethod;
    }

    @JsonProperty("kb_service_method")
    public void setKbServiceMethod(String kbServiceMethod) {
        this.kbServiceMethod = kbServiceMethod;
    }

    public MethodBehavior withKbServiceMethod(String kbServiceMethod) {
        this.kbServiceMethod = kbServiceMethod;
        return this;
    }

    @JsonProperty("kb_service_input_mapping")
    public List<ServiceMethodInputMapping> getKbServiceInputMapping() {
        return kbServiceInputMapping;
    }

    @JsonProperty("kb_service_input_mapping")
    public void setKbServiceInputMapping(List<ServiceMethodInputMapping> kbServiceInputMapping) {
        this.kbServiceInputMapping = kbServiceInputMapping;
    }

    public MethodBehavior withKbServiceInputMapping(List<ServiceMethodInputMapping> kbServiceInputMapping) {
        this.kbServiceInputMapping = kbServiceInputMapping;
        return this;
    }

    @JsonProperty("kb_service_output_mapping")
    public List<ServiceMethodOutputMapping> getKbServiceOutputMapping() {
        return kbServiceOutputMapping;
    }

    @JsonProperty("kb_service_output_mapping")
    public void setKbServiceOutputMapping(List<ServiceMethodOutputMapping> kbServiceOutputMapping) {
        this.kbServiceOutputMapping = kbServiceOutputMapping;
    }

    public MethodBehavior withKbServiceOutputMapping(List<ServiceMethodOutputMapping> kbServiceOutputMapping) {
        this.kbServiceOutputMapping = kbServiceOutputMapping;
        return this;
    }

    @JsonProperty("output_mapping")
    public List<OutputMapping> getOutputMapping() {
        return outputMapping;
    }

    @JsonProperty("output_mapping")
    public void setOutputMapping(List<OutputMapping> outputMapping) {
        this.outputMapping = outputMapping;
    }

    public MethodBehavior withOutputMapping(List<OutputMapping> outputMapping) {
        this.outputMapping = outputMapping;
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
        return ((((((((((((((((((("MethodBehavior"+" [pythonClass=")+ pythonClass)+", pythonFunction=")+ pythonFunction)+", kbServiceUrl=")+ kbServiceUrl)+", kbServiceName=")+ kbServiceName)+", kbServiceMethod=")+ kbServiceMethod)+", kbServiceInputMapping=")+ kbServiceInputMapping)+", kbServiceOutputMapping=")+ kbServiceOutputMapping)+", outputMapping=")+ outputMapping)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
