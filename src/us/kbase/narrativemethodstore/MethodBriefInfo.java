
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
    "categorizations"
})
public class MethodBriefInfo {

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("ver")
    private String ver;
    @JsonProperty("subtitle")
    private String subtitle;
    @JsonProperty("tooltip")
    private String tooltip;
    @JsonProperty("categorizations")
    private List<Categorization> categorizations;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public MethodBriefInfo withId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public MethodBriefInfo withName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("ver")
    public String getVer() {
        return ver;
    }

    @JsonProperty("ver")
    public void setVer(String ver) {
        this.ver = ver;
    }

    public MethodBriefInfo withVer(String ver) {
        this.ver = ver;
        return this;
    }

    @JsonProperty("subtitle")
    public String getSubtitle() {
        return subtitle;
    }

    @JsonProperty("subtitle")
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public MethodBriefInfo withSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    @JsonProperty("tooltip")
    public String getTooltip() {
        return tooltip;
    }

    @JsonProperty("tooltip")
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public MethodBriefInfo withTooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @JsonProperty("categorizations")
    public List<Categorization> getCategorizations() {
        return categorizations;
    }

    @JsonProperty("categorizations")
    public void setCategorizations(List<Categorization> categorizations) {
        this.categorizations = categorizations;
    }

    public MethodBriefInfo withCategorizations(List<Categorization> categorizations) {
        this.categorizations = categorizations;
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
        return ((((((((((((((("MethodBriefInfo"+" [id=")+ id)+", name=")+ name)+", ver=")+ ver)+", subtitle=")+ subtitle)+", tooltip=")+ tooltip)+", categorizations=")+ categorizations)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
