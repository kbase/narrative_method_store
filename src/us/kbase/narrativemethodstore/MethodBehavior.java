
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
 * @optional python_function kb_service_name kb_service_method
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "python_class",
    "python_function",
    "kb_service_name",
    "kb_service_method"
})
public class MethodBehavior {

    @JsonProperty("python_class")
    private String pythonClass;
    @JsonProperty("python_function")
    private String pythonFunction;
    @JsonProperty("kb_service_name")
    private String kbServiceName;
    @JsonProperty("kb_service_method")
    private String kbServiceMethod;
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
        return ((((((((((("MethodBehavior"+" [pythonClass=")+ pythonClass)+", pythonFunction=")+ pythonFunction)+", kbServiceName=")+ kbServiceName)+", kbServiceMethod=")+ kbServiceMethod)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
