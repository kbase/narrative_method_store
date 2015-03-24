
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
 * <p>Original spec-file type: ValidationResults</p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "is_valid",
    "errors",
    "warnings",
    "app_full_info",
    "method_full_info",
    "type_info"
})
public class ValidationResults {

    @JsonProperty("is_valid")
    private Long isValid;
    @JsonProperty("errors")
    private List<String> errors;
    @JsonProperty("warnings")
    private List<String> warnings;
    /**
     * <p>Original spec-file type: AppFullInfo</p>
     * 
     * 
     */
    @JsonProperty("app_full_info")
    private AppFullInfo appFullInfo;
    /**
     * <p>Original spec-file type: MethodFullInfo</p>
     * <pre>
     * Full information about a method suitable for displaying a method landing page.
     * </pre>
     * 
     */
    @JsonProperty("method_full_info")
    private MethodFullInfo methodFullInfo;
    /**
     * <p>Original spec-file type: TypeInfo</p>
     * <pre>
     * @optional icon landing_page_url_prefix loading_error
     * </pre>
     * 
     */
    @JsonProperty("type_info")
    private TypeInfo typeInfo;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("is_valid")
    public Long getIsValid() {
        return isValid;
    }

    @JsonProperty("is_valid")
    public void setIsValid(Long isValid) {
        this.isValid = isValid;
    }

    public ValidationResults withIsValid(Long isValid) {
        this.isValid = isValid;
        return this;
    }

    @JsonProperty("errors")
    public List<String> getErrors() {
        return errors;
    }

    @JsonProperty("errors")
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public ValidationResults withErrors(List<String> errors) {
        this.errors = errors;
        return this;
    }

    @JsonProperty("warnings")
    public List<String> getWarnings() {
        return warnings;
    }

    @JsonProperty("warnings")
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public ValidationResults withWarnings(List<String> warnings) {
        this.warnings = warnings;
        return this;
    }

    /**
     * <p>Original spec-file type: AppFullInfo</p>
     * 
     * 
     */
    @JsonProperty("app_full_info")
    public AppFullInfo getAppFullInfo() {
        return appFullInfo;
    }

    /**
     * <p>Original spec-file type: AppFullInfo</p>
     * 
     * 
     */
    @JsonProperty("app_full_info")
    public void setAppFullInfo(AppFullInfo appFullInfo) {
        this.appFullInfo = appFullInfo;
    }

    public ValidationResults withAppFullInfo(AppFullInfo appFullInfo) {
        this.appFullInfo = appFullInfo;
        return this;
    }

    /**
     * <p>Original spec-file type: MethodFullInfo</p>
     * <pre>
     * Full information about a method suitable for displaying a method landing page.
     * </pre>
     * 
     */
    @JsonProperty("method_full_info")
    public MethodFullInfo getMethodFullInfo() {
        return methodFullInfo;
    }

    /**
     * <p>Original spec-file type: MethodFullInfo</p>
     * <pre>
     * Full information about a method suitable for displaying a method landing page.
     * </pre>
     * 
     */
    @JsonProperty("method_full_info")
    public void setMethodFullInfo(MethodFullInfo methodFullInfo) {
        this.methodFullInfo = methodFullInfo;
    }

    public ValidationResults withMethodFullInfo(MethodFullInfo methodFullInfo) {
        this.methodFullInfo = methodFullInfo;
        return this;
    }

    /**
     * <p>Original spec-file type: TypeInfo</p>
     * <pre>
     * @optional icon landing_page_url_prefix loading_error
     * </pre>
     * 
     */
    @JsonProperty("type_info")
    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    /**
     * <p>Original spec-file type: TypeInfo</p>
     * <pre>
     * @optional icon landing_page_url_prefix loading_error
     * </pre>
     * 
     */
    @JsonProperty("type_info")
    public void setTypeInfo(TypeInfo typeInfo) {
        this.typeInfo = typeInfo;
    }

    public ValidationResults withTypeInfo(TypeInfo typeInfo) {
        this.typeInfo = typeInfo;
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
        return ((((((((((((((("ValidationResults"+" [isValid=")+ isValid)+", errors=")+ errors)+", warnings=")+ warnings)+", appFullInfo=")+ appFullInfo)+", methodFullInfo=")+ methodFullInfo)+", typeInfo=")+ typeInfo)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
