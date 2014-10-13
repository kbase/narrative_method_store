
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
 * <p>Original spec-file type: MethodSpec</p>
 * <pre>
 * The method specification which should provide enough information to render a default
 * input widget for the method.
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "info",
    "widgets",
    "parameters",
    "behavior",
    "job_id_output_field"
})
public class MethodSpec {

    /**
     * <p>Original spec-file type: MethodBriefInfo</p>
     * <pre>
     * Minimal information about a method suitable for displaying the method in a menu or navigator.
     * </pre>
     * 
     */
    @JsonProperty("info")
    private MethodBriefInfo info;
    /**
     * <p>Original spec-file type: WidgetSpec</p>
     * <pre>
     * specify the input / ouput widgets used for rendering
     * </pre>
     * 
     */
    @JsonProperty("widgets")
    private WidgetSpec widgets;
    @JsonProperty("parameters")
    private List<MethodParameter> parameters;
    /**
     * <p>Original spec-file type: MethodBehavior</p>
     * <pre>
     * Determines how the method is handled when run.
     * @optional python_function kb_service_name kb_service_method
     * </pre>
     * 
     */
    @JsonProperty("behavior")
    private MethodBehavior behavior;
    @JsonProperty("job_id_output_field")
    private String jobIdOutputField;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * <p>Original spec-file type: MethodBriefInfo</p>
     * <pre>
     * Minimal information about a method suitable for displaying the method in a menu or navigator.
     * </pre>
     * 
     */
    @JsonProperty("info")
    public MethodBriefInfo getInfo() {
        return info;
    }

    /**
     * <p>Original spec-file type: MethodBriefInfo</p>
     * <pre>
     * Minimal information about a method suitable for displaying the method in a menu or navigator.
     * </pre>
     * 
     */
    @JsonProperty("info")
    public void setInfo(MethodBriefInfo info) {
        this.info = info;
    }

    public MethodSpec withInfo(MethodBriefInfo info) {
        this.info = info;
        return this;
    }

    /**
     * <p>Original spec-file type: WidgetSpec</p>
     * <pre>
     * specify the input / ouput widgets used for rendering
     * </pre>
     * 
     */
    @JsonProperty("widgets")
    public WidgetSpec getWidgets() {
        return widgets;
    }

    /**
     * <p>Original spec-file type: WidgetSpec</p>
     * <pre>
     * specify the input / ouput widgets used for rendering
     * </pre>
     * 
     */
    @JsonProperty("widgets")
    public void setWidgets(WidgetSpec widgets) {
        this.widgets = widgets;
    }

    public MethodSpec withWidgets(WidgetSpec widgets) {
        this.widgets = widgets;
        return this;
    }

    @JsonProperty("parameters")
    public List<MethodParameter> getParameters() {
        return parameters;
    }

    @JsonProperty("parameters")
    public void setParameters(List<MethodParameter> parameters) {
        this.parameters = parameters;
    }

    public MethodSpec withParameters(List<MethodParameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * <p>Original spec-file type: MethodBehavior</p>
     * <pre>
     * Determines how the method is handled when run.
     * @optional python_function kb_service_name kb_service_method
     * </pre>
     * 
     */
    @JsonProperty("behavior")
    public MethodBehavior getBehavior() {
        return behavior;
    }

    /**
     * <p>Original spec-file type: MethodBehavior</p>
     * <pre>
     * Determines how the method is handled when run.
     * @optional python_function kb_service_name kb_service_method
     * </pre>
     * 
     */
    @JsonProperty("behavior")
    public void setBehavior(MethodBehavior behavior) {
        this.behavior = behavior;
    }

    public MethodSpec withBehavior(MethodBehavior behavior) {
        this.behavior = behavior;
        return this;
    }

    @JsonProperty("job_id_output_field")
    public String getJobIdOutputField() {
        return jobIdOutputField;
    }

    @JsonProperty("job_id_output_field")
    public void setJobIdOutputField(String jobIdOutputField) {
        this.jobIdOutputField = jobIdOutputField;
    }

    public MethodSpec withJobIdOutputField(String jobIdOutputField) {
        this.jobIdOutputField = jobIdOutputField;
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
        return ((((((((((((("MethodSpec"+" [info=")+ info)+", widgets=")+ widgets)+", parameters=")+ parameters)+", behavior=")+ behavior)+", jobIdOutputField=")+ jobIdOutputField)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
