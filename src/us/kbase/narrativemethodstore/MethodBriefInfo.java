
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
 * <p>Original spec-file type: MethodBriefInfo</p>
 * <pre>
 * Minimal information about a method suitable for displaying the method in a menu or navigator.
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "name",
    "ver",
    "subtitle",
    "tooltip",
    "icon",
    "categories",
    "loading_error"
})
public class MethodBriefInfo {

    @JsonProperty("id")
    private java.lang.String id;
    @JsonProperty("name")
    private java.lang.String name;
    @JsonProperty("ver")
    private java.lang.String ver;
    @JsonProperty("subtitle")
    private java.lang.String subtitle;
    @JsonProperty("tooltip")
    private java.lang.String tooltip;
    /**
     * <p>Original spec-file type: Icon</p>
     * 
     * 
     */
    @JsonProperty("icon")
    private Icon icon;
    @JsonProperty("categories")
    private List<String> categories;
    @JsonProperty("loading_error")
    private java.lang.String loadingError;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("id")
    public java.lang.String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(java.lang.String id) {
        this.id = id;
    }

    public MethodBriefInfo withId(java.lang.String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("name")
    public java.lang.String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(java.lang.String name) {
        this.name = name;
    }

    public MethodBriefInfo withName(java.lang.String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("ver")
    public java.lang.String getVer() {
        return ver;
    }

    @JsonProperty("ver")
    public void setVer(java.lang.String ver) {
        this.ver = ver;
    }

    public MethodBriefInfo withVer(java.lang.String ver) {
        this.ver = ver;
        return this;
    }

    @JsonProperty("subtitle")
    public java.lang.String getSubtitle() {
        return subtitle;
    }

    @JsonProperty("subtitle")
    public void setSubtitle(java.lang.String subtitle) {
        this.subtitle = subtitle;
    }

    public MethodBriefInfo withSubtitle(java.lang.String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    @JsonProperty("tooltip")
    public java.lang.String getTooltip() {
        return tooltip;
    }

    @JsonProperty("tooltip")
    public void setTooltip(java.lang.String tooltip) {
        this.tooltip = tooltip;
    }

    public MethodBriefInfo withTooltip(java.lang.String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    /**
     * <p>Original spec-file type: Icon</p>
     * 
     * 
     */
    @JsonProperty("icon")
    public Icon getIcon() {
        return icon;
    }

    /**
     * <p>Original spec-file type: Icon</p>
     * 
     * 
     */
    @JsonProperty("icon")
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public MethodBriefInfo withIcon(Icon icon) {
        this.icon = icon;
        return this;
    }

    @JsonProperty("categories")
    public List<String> getCategories() {
        return categories;
    }

    @JsonProperty("categories")
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public MethodBriefInfo withCategories(List<String> categories) {
        this.categories = categories;
        return this;
    }

    @JsonProperty("loading_error")
    public java.lang.String getLoadingError() {
        return loadingError;
    }

    @JsonProperty("loading_error")
    public void setLoadingError(java.lang.String loadingError) {
        this.loadingError = loadingError;
    }

    public MethodBriefInfo withLoadingError(java.lang.String loadingError) {
        this.loadingError = loadingError;
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
        return ((((((((((((((((((("MethodBriefInfo"+" [id=")+ id)+", name=")+ name)+", ver=")+ ver)+", subtitle=")+ subtitle)+", tooltip=")+ tooltip)+", icon=")+ icon)+", categories=")+ categories)+", loadingError=")+ loadingError)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
