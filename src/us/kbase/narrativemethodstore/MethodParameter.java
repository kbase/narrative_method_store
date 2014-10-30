
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
 * <p>Original spec-file type: MethodParameter</p>
 * <pre>
 * Description of a method parameter.
 * @optional text_options textarea_options intslider_options checkbox_options
 * @optional dropdown_options radio_options
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "ui_name",
    "short_hint",
    "long_hint",
    "field_type",
    "allow_multiple",
    "optional",
    "advanced",
    "default_values",
    "text_options",
    "textarea_options",
    "intslider_options",
    "floatslider_options",
    "checkbox_options",
    "dropdown_options",
    "radio_options"
})
public class MethodParameter {

    @JsonProperty("id")
    private java.lang.String id;
    @JsonProperty("ui_name")
    private java.lang.String uiName;
    @JsonProperty("short_hint")
    private java.lang.String shortHint;
    @JsonProperty("long_hint")
    private java.lang.String longHint;
    @JsonProperty("field_type")
    private java.lang.String fieldType;
    @JsonProperty("allow_multiple")
    private Long allowMultiple;
    @JsonProperty("optional")
    private Long optional;
    @JsonProperty("advanced")
    private Long advanced;
    @JsonProperty("default_values")
    private List<String> defaultValues;
    /**
     * <p>Original spec-file type: TextOptions</p>
     * <pre>
     * valid_ws_types  - list of valid ws types that can be used for input
     * validate_as     - int | float | nonnumeric | none
     * is_output_name  - true if the user is specifying an output name, false otherwise, default is false
     * </pre>
     * 
     */
    @JsonProperty("text_options")
    private TextOptions textOptions;
    /**
     * <p>Original spec-file type: TextAreaOptions</p>
     * 
     * 
     */
    @JsonProperty("textarea_options")
    private TextAreaOptions textareaOptions;
    /**
     * <p>Original spec-file type: IntSliderOptions</p>
     * 
     * 
     */
    @JsonProperty("intslider_options")
    private IntSliderOptions intsliderOptions;
    /**
     * <p>Original spec-file type: FloatSliderOptions</p>
     * 
     * 
     */
    @JsonProperty("floatslider_options")
    private FloatSliderOptions floatsliderOptions;
    /**
     * <p>Original spec-file type: CheckboxOptions</p>
     * 
     * 
     */
    @JsonProperty("checkbox_options")
    private CheckboxOptions checkboxOptions;
    /**
     * <p>Original spec-file type: DropdownOptions</p>
     * 
     * 
     */
    @JsonProperty("dropdown_options")
    private DropdownOptions dropdownOptions;
    /**
     * <p>Original spec-file type: RadioOptions</p>
     * 
     * 
     */
    @JsonProperty("radio_options")
    private RadioOptions radioOptions;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("id")
    public java.lang.String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(java.lang.String id) {
        this.id = id;
    }

    public MethodParameter withId(java.lang.String id) {
        this.id = id;
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

    public MethodParameter withUiName(java.lang.String uiName) {
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

    public MethodParameter withShortHint(java.lang.String shortHint) {
        this.shortHint = shortHint;
        return this;
    }

    @JsonProperty("long_hint")
    public java.lang.String getLongHint() {
        return longHint;
    }

    @JsonProperty("long_hint")
    public void setLongHint(java.lang.String longHint) {
        this.longHint = longHint;
    }

    public MethodParameter withLongHint(java.lang.String longHint) {
        this.longHint = longHint;
        return this;
    }

    @JsonProperty("field_type")
    public java.lang.String getFieldType() {
        return fieldType;
    }

    @JsonProperty("field_type")
    public void setFieldType(java.lang.String fieldType) {
        this.fieldType = fieldType;
    }

    public MethodParameter withFieldType(java.lang.String fieldType) {
        this.fieldType = fieldType;
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

    public MethodParameter withAllowMultiple(Long allowMultiple) {
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

    public MethodParameter withOptional(Long optional) {
        this.optional = optional;
        return this;
    }

    @JsonProperty("advanced")
    public Long getAdvanced() {
        return advanced;
    }

    @JsonProperty("advanced")
    public void setAdvanced(Long advanced) {
        this.advanced = advanced;
    }

    public MethodParameter withAdvanced(Long advanced) {
        this.advanced = advanced;
        return this;
    }

    @JsonProperty("default_values")
    public List<String> getDefaultValues() {
        return defaultValues;
    }

    @JsonProperty("default_values")
    public void setDefaultValues(List<String> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public MethodParameter withDefaultValues(List<String> defaultValues) {
        this.defaultValues = defaultValues;
        return this;
    }

    /**
     * <p>Original spec-file type: TextOptions</p>
     * <pre>
     * valid_ws_types  - list of valid ws types that can be used for input
     * validate_as     - int | float | nonnumeric | none
     * is_output_name  - true if the user is specifying an output name, false otherwise, default is false
     * </pre>
     * 
     */
    @JsonProperty("text_options")
    public TextOptions getTextOptions() {
        return textOptions;
    }

    /**
     * <p>Original spec-file type: TextOptions</p>
     * <pre>
     * valid_ws_types  - list of valid ws types that can be used for input
     * validate_as     - int | float | nonnumeric | none
     * is_output_name  - true if the user is specifying an output name, false otherwise, default is false
     * </pre>
     * 
     */
    @JsonProperty("text_options")
    public void setTextOptions(TextOptions textOptions) {
        this.textOptions = textOptions;
    }

    public MethodParameter withTextOptions(TextOptions textOptions) {
        this.textOptions = textOptions;
        return this;
    }

    /**
     * <p>Original spec-file type: TextAreaOptions</p>
     * 
     * 
     */
    @JsonProperty("textarea_options")
    public TextAreaOptions getTextareaOptions() {
        return textareaOptions;
    }

    /**
     * <p>Original spec-file type: TextAreaOptions</p>
     * 
     * 
     */
    @JsonProperty("textarea_options")
    public void setTextareaOptions(TextAreaOptions textareaOptions) {
        this.textareaOptions = textareaOptions;
    }

    public MethodParameter withTextareaOptions(TextAreaOptions textareaOptions) {
        this.textareaOptions = textareaOptions;
        return this;
    }

    /**
     * <p>Original spec-file type: IntSliderOptions</p>
     * 
     * 
     */
    @JsonProperty("intslider_options")
    public IntSliderOptions getIntsliderOptions() {
        return intsliderOptions;
    }

    /**
     * <p>Original spec-file type: IntSliderOptions</p>
     * 
     * 
     */
    @JsonProperty("intslider_options")
    public void setIntsliderOptions(IntSliderOptions intsliderOptions) {
        this.intsliderOptions = intsliderOptions;
    }

    public MethodParameter withIntsliderOptions(IntSliderOptions intsliderOptions) {
        this.intsliderOptions = intsliderOptions;
        return this;
    }

    /**
     * <p>Original spec-file type: FloatSliderOptions</p>
     * 
     * 
     */
    @JsonProperty("floatslider_options")
    public FloatSliderOptions getFloatsliderOptions() {
        return floatsliderOptions;
    }

    /**
     * <p>Original spec-file type: FloatSliderOptions</p>
     * 
     * 
     */
    @JsonProperty("floatslider_options")
    public void setFloatsliderOptions(FloatSliderOptions floatsliderOptions) {
        this.floatsliderOptions = floatsliderOptions;
    }

    public MethodParameter withFloatsliderOptions(FloatSliderOptions floatsliderOptions) {
        this.floatsliderOptions = floatsliderOptions;
        return this;
    }

    /**
     * <p>Original spec-file type: CheckboxOptions</p>
     * 
     * 
     */
    @JsonProperty("checkbox_options")
    public CheckboxOptions getCheckboxOptions() {
        return checkboxOptions;
    }

    /**
     * <p>Original spec-file type: CheckboxOptions</p>
     * 
     * 
     */
    @JsonProperty("checkbox_options")
    public void setCheckboxOptions(CheckboxOptions checkboxOptions) {
        this.checkboxOptions = checkboxOptions;
    }

    public MethodParameter withCheckboxOptions(CheckboxOptions checkboxOptions) {
        this.checkboxOptions = checkboxOptions;
        return this;
    }

    /**
     * <p>Original spec-file type: DropdownOptions</p>
     * 
     * 
     */
    @JsonProperty("dropdown_options")
    public DropdownOptions getDropdownOptions() {
        return dropdownOptions;
    }

    /**
     * <p>Original spec-file type: DropdownOptions</p>
     * 
     * 
     */
    @JsonProperty("dropdown_options")
    public void setDropdownOptions(DropdownOptions dropdownOptions) {
        this.dropdownOptions = dropdownOptions;
    }

    public MethodParameter withDropdownOptions(DropdownOptions dropdownOptions) {
        this.dropdownOptions = dropdownOptions;
        return this;
    }

    /**
     * <p>Original spec-file type: RadioOptions</p>
     * 
     * 
     */
    @JsonProperty("radio_options")
    public RadioOptions getRadioOptions() {
        return radioOptions;
    }

    /**
     * <p>Original spec-file type: RadioOptions</p>
     * 
     * 
     */
    @JsonProperty("radio_options")
    public void setRadioOptions(RadioOptions radioOptions) {
        this.radioOptions = radioOptions;
    }

    public MethodParameter withRadioOptions(RadioOptions radioOptions) {
        this.radioOptions = radioOptions;
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
        return ((((((((((((((((((((((((((((((((((("MethodParameter"+" [id=")+ id)+", uiName=")+ uiName)+", shortHint=")+ shortHint)+", longHint=")+ longHint)+", fieldType=")+ fieldType)+", allowMultiple=")+ allowMultiple)+", optional=")+ optional)+", advanced=")+ advanced)+", defaultValues=")+ defaultValues)+", textOptions=")+ textOptions)+", textareaOptions=")+ textareaOptions)+", intsliderOptions=")+ intsliderOptions)+", floatsliderOptions=")+ floatsliderOptions)+", checkboxOptions=")+ checkboxOptions)+", dropdownOptions=")+ dropdownOptions)+", radioOptions=")+ radioOptions)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
