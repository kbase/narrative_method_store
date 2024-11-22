
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
import us.kbase.common.service.UObject;


/**
 * <p>Original spec-file type: DynamicDropdownOptions</p>
 * <pre>
 * Defines a parameter field that allows autocomplete based on a call to a dynamic service.
 * For instance, selection of files from the staging_service or from kbase_search. It will
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
 *     selection_id - The value of this key will be extracted from the item selected by the
 *                    user. The item is expected to be represented as a map.
 *     exact_match_on - if exactly matching the user's input to the results from the dynamic
 *                    service is required, this field contains the name of the key in
 *                    the results document that contains the value to which the user's
 *                    input should be matched. May or may not be the same key as
 *                    'selection_id'.
 *     description_template - Defines how the description of items is rendered using
 *                    Handlebar templates (use the keys in the items as variable names)
 *     multiselection - If true, then multiple selections are allowed in a single input field.
 *                    This will override the allow_multiple option (which allows user addition)
 *                    of additional fields.  If true, then this parameter will return a list.
 *                    Default= false
 *     query_on_empty_input - true, the default, to send a request to the dynamic service even
 *                    if there is no input.
 *     result_array_index - The index of the result array returned from the dynamic service
 *                    from where the selection items will be extracted. Default 0.
 *     path_to_selection_items - The path into the result data object to the list of
 *                    selection items. If missing, the data at the specified result array
 *                    index is used (defaulting to the first returned value in the list).
 *     The selection items data structure must be a list of mappings or structures.
 *     As an example of correctly specifying where the selection items are within the
 *     data structure returned from the dynamic service, if the data structure is:
 *     [
 *         "foo",                # return array position 0
 *         {                     # return array position 1
 *          "interesting_data":
 *              [
 *                  "baz",
 *                  "boo",
 *                  [
 *                      {"id": 1,
 *                       "name": "foo"
 *                       },
 *                       ...
 *                      {"id": 42,
 *                       "name": "wowbagger"
 *                       }
 *                  ],
 *                  "bat"
 *              ]
 *          },
 *          "bar"                # return array position 2
 *      ]
 *     Note that KBase dynamic services all return an array of values, even for single-value
 *     returns, as the KIDL spec allows specifying multiple return values per function.
 *     In this case:
 *         result_array_index would be 1
 *         path_to_selection_items would be ["interesting_data", "2"]
 *         selection_id would be "name"
 *     The selection items would be the 42 items represented by
 *     {"id": 1,
 *      "name": "foo"
 *      },
 *      ...
 *     {"id": 42,
 *      "name": "wowbagger"
 *      }
 *     Selection items must always be a list of maps.
 *     The final value returned when the user selects a value would be the "name" field -
 *     "foo" if the first item is selected, and "wowbagger" if the last item is selected.
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
    "exact_match_on",
    "description_template",
    "multiselection",
    "query_on_empty_input",
    "result_array_index",
    "path_to_selection_items"
})
public class DynamicDropdownOptions {

    @JsonProperty("data_source")
    private java.lang.String dataSource;
    @JsonProperty("service_function")
    private java.lang.String serviceFunction;
    @JsonProperty("service_version")
    private java.lang.String serviceVersion;
    @JsonProperty("service_params")
    private UObject serviceParams;
    @JsonProperty("selection_id")
    private java.lang.String selectionId;
    @JsonProperty("exact_match_on")
    private java.lang.String exactMatchOn;
    @JsonProperty("description_template")
    private java.lang.String descriptionTemplate;
    @JsonProperty("multiselection")
    private Long multiselection;
    @JsonProperty("query_on_empty_input")
    private Long queryOnEmptyInput;
    @JsonProperty("result_array_index")
    private Long resultArrayIndex;
    @JsonProperty("path_to_selection_items")
    private List<String> pathToSelectionItems;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("data_source")
    public java.lang.String getDataSource() {
        return dataSource;
    }

    @JsonProperty("data_source")
    public void setDataSource(java.lang.String dataSource) {
        this.dataSource = dataSource;
    }

    public DynamicDropdownOptions withDataSource(java.lang.String dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    @JsonProperty("service_function")
    public java.lang.String getServiceFunction() {
        return serviceFunction;
    }

    @JsonProperty("service_function")
    public void setServiceFunction(java.lang.String serviceFunction) {
        this.serviceFunction = serviceFunction;
    }

    public DynamicDropdownOptions withServiceFunction(java.lang.String serviceFunction) {
        this.serviceFunction = serviceFunction;
        return this;
    }

    @JsonProperty("service_version")
    public java.lang.String getServiceVersion() {
        return serviceVersion;
    }

    @JsonProperty("service_version")
    public void setServiceVersion(java.lang.String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public DynamicDropdownOptions withServiceVersion(java.lang.String serviceVersion) {
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
    public java.lang.String getSelectionId() {
        return selectionId;
    }

    @JsonProperty("selection_id")
    public void setSelectionId(java.lang.String selectionId) {
        this.selectionId = selectionId;
    }

    public DynamicDropdownOptions withSelectionId(java.lang.String selectionId) {
        this.selectionId = selectionId;
        return this;
    }

    @JsonProperty("exact_match_on")
    public java.lang.String getExactMatchOn() {
        return exactMatchOn;
    }

    @JsonProperty("exact_match_on")
    public void setExactMatchOn(java.lang.String exactMatchOn) {
        this.exactMatchOn = exactMatchOn;
    }

    public DynamicDropdownOptions withExactMatchOn(java.lang.String exactMatchOn) {
        this.exactMatchOn = exactMatchOn;
        return this;
    }

    @JsonProperty("description_template")
    public java.lang.String getDescriptionTemplate() {
        return descriptionTemplate;
    }

    @JsonProperty("description_template")
    public void setDescriptionTemplate(java.lang.String descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }

    public DynamicDropdownOptions withDescriptionTemplate(java.lang.String descriptionTemplate) {
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

    @JsonProperty("query_on_empty_input")
    public Long getQueryOnEmptyInput() {
        return queryOnEmptyInput;
    }

    @JsonProperty("query_on_empty_input")
    public void setQueryOnEmptyInput(Long queryOnEmptyInput) {
        this.queryOnEmptyInput = queryOnEmptyInput;
    }

    public DynamicDropdownOptions withQueryOnEmptyInput(Long queryOnEmptyInput) {
        this.queryOnEmptyInput = queryOnEmptyInput;
        return this;
    }

    @JsonProperty("result_array_index")
    public Long getResultArrayIndex() {
        return resultArrayIndex;
    }

    @JsonProperty("result_array_index")
    public void setResultArrayIndex(Long resultArrayIndex) {
        this.resultArrayIndex = resultArrayIndex;
    }

    public DynamicDropdownOptions withResultArrayIndex(Long resultArrayIndex) {
        this.resultArrayIndex = resultArrayIndex;
        return this;
    }

    @JsonProperty("path_to_selection_items")
    public List<String> getPathToSelectionItems() {
        return pathToSelectionItems;
    }

    @JsonProperty("path_to_selection_items")
    public void setPathToSelectionItems(List<String> pathToSelectionItems) {
        this.pathToSelectionItems = pathToSelectionItems;
    }

    public DynamicDropdownOptions withPathToSelectionItems(List<String> pathToSelectionItems) {
        this.pathToSelectionItems = pathToSelectionItems;
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
        return ((((((((((((((((((((((((("DynamicDropdownOptions"+" [dataSource=")+ dataSource)+", serviceFunction=")+ serviceFunction)+", serviceVersion=")+ serviceVersion)+", serviceParams=")+ serviceParams)+", selectionId=")+ selectionId)+", exactMatchOn=")+ exactMatchOn)+", descriptionTemplate=")+ descriptionTemplate)+", multiselection=")+ multiselection)+", queryOnEmptyInput=")+ queryOnEmptyInput)+", resultArrayIndex=")+ resultArrayIndex)+", pathToSelectionItems=")+ pathToSelectionItems)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
