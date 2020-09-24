
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
 * <p>Original spec-file type: DropdownOptions</p>
 * <pre>
 * Defines a parameter field that allows users to select from a list of options. It will
 * appear as a dropdown (a 'select' HTML element).
 * Parameters:
 *    options   - a list of maps with keys 'value' and 'display'; 'display' is the text
 *              presented to the user, and 'value' is what is passed from the element
 *              when it is submitted. See the DropDownOption type for the spec.
 *    multiselection - If true, multiple selections are allowed from a single field, and
 *              the parameter will return a list, rather than a single value.
 *              This parameter is optional.
 *              Default = false
 * </pre>
 *
 */


@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "multiselection",
    "options"
})
public class DropdownOptions {

    @JsonProperty("multiselection")
    private Long multiselection;
    @JsonProperty("options")
    private List<DropdownOption> options;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("multiselection")
    public Long getMultiselection() {
        return multiselection;
    }

    @JsonProperty("multiselection")
    public void setMultiselection(Long multiselection) {
        this.multiselection = multiselection;
    }

    public DropdownOptions withMultiselection(Long multiselection) {
        this.multiselection = multiselection;
        return this;
    }

    @JsonProperty("options")
    public List<DropdownOption> getOptions() {
        return options;
    }

    @JsonProperty("options")
    public void setOptions(List<DropdownOption> options) {
        this.options = options;
    }

    public DropdownOptions withOptions(List<DropdownOption> options) {
        this.options = options;
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
        return ((((((("DropdownOptions"+" [options=")+ options)+", multiselection=")+ multiselection)+
", additionalProperties=")+ additionalProperties)+"]");
    }
}
