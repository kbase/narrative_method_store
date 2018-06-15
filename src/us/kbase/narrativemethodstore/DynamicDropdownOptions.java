
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
 * <p>Original spec-file type: DynamicDropdownOptions</p>
 * <pre>
 * Defines a parameter field that allows autocomplete based on a call to a dynamic service.
 * For instance, selection of files from the stageing_service or from kbase_search. It will
 * appear as a text field with dropdown similar to selection of other WS data objects.
 *     data_source - one of ftp_staging | search | custom. Provides sensible defaults to
 *                    for the following parameters for a common type of dropdown which can be
 *                    overwritten
 *     service_function - name of SDK method including prefix with SDK module started up as
 *                    dynamic service (it's fully qualified method name where module and
 *                    method are separated by '.').
 *     service_version - optional version of module used in service_function
 *                    (default value is 'release').
 *     service_params - The parameters that will be supplied to the dynamic service call as
 *                    JSON. The special text "{{dynamic_dropdown_input}}" will be replaced by
 *                    the value of user input at call time.
 *     selection_id - name of key result_aliases which will be sent as selected value
 *     description_template - Defines how the description of items is rendered using
 *                    Handlebar templates (use the keys in result_aliases as variable names)
 *     multiselection - if true, then multiple selections are allowed in a single input field.
 *                    This will override the allow_multiple option (which allows user addition)
 *                    of additional fields.  If true, then this parameter will return a list.
 *                    Default= false
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "data_source",
    "service_function",
    "service_version",
    "service_params",
    "selection_id",
    "description_template",
    "multiselection"
})
public class DynamicDropdownOptions {

    @JsonProperty("data_source")
    private String dataSource;
    @JsonProperty("service_function")
    private String serviceFunction;
    @JsonProperty("service_version")
    private String serviceVersion;
    @JsonProperty("service_params")
    private UObject serviceParams;
    @JsonProperty("selection_id")
    private String selectionId;
    @JsonProperty("description_template")
    private String descriptionTemplate;
    @JsonProperty("multiselection")
    private Long multiselection;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("data_source")
    public String getDataSource() {
        return dataSource;
    }

    @JsonProperty("data_source")
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public DynamicDropdownOptions withDataSource(String dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    @JsonProperty("service_function")
    public String getServiceFunction() {
        return serviceFunction;
    }

    @JsonProperty("service_function")
    public void setServiceFunction(String serviceFunction) {
        this.serviceFunction = serviceFunction;
    }

    public DynamicDropdownOptions withServiceFunction(String serviceFunction) {
        this.serviceFunction = serviceFunction;
        return this;
    }

    @JsonProperty("service_version")
    public String getServiceVersion() {
        return serviceVersion;
    }

    @JsonProperty("service_version")
    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public DynamicDropdownOptions withServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    @JsonProperty("service_params")
    public UObject getServiceParams() {
        return serviceParams;
    }

    @JsonProperty("service_params")
    public void setServiceParams(UObject serviceParams) {
        this.serviceParams = serviceParams;
    }

    public DynamicDropdownOptions withServiceParams(UObject serviceParams) {
        this.serviceParams = serviceParams;
        return this;
    }

    @JsonProperty("selection_id")
    public String getSelectionId() {
        return selectionId;
    }

    @JsonProperty("selection_id")
    public void setSelectionId(String selectionId) {
        this.selectionId = selectionId;
    }

    public DynamicDropdownOptions withSelectionId(String selectionId) {
        this.selectionId = selectionId;
        return this;
    }

    @JsonProperty("description_template")
    public String getDescriptionTemplate() {
        return descriptionTemplate;
    }

    @JsonProperty("description_template")
    public void setDescriptionTemplate(String descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }

    public DynamicDropdownOptions withDescriptionTemplate(String descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
        return this;
    }

    @JsonProperty("multiselection")
    public Long getMultiselection() {
        return multiselection;
    }

    @JsonProperty("multiselection")
    public void setMultiselection(Long multiselection) {
        this.multiselection = multiselection;
    }

    public DynamicDropdownOptions withMultiselection(Long multiselection) {
        this.multiselection = multiselection;
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
        return ((((((((((((((((("DynamicDropdownOptions"+" [dataSource=")+ dataSource)+", serviceFunction=")+ serviceFunction)+", serviceVersion=")+ serviceVersion)+", serviceParams=")+ serviceParams)+", selectionId=")+ selectionId)+", descriptionTemplate=")+ descriptionTemplate)+", multiselection=")+ multiselection)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
