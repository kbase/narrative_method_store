
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
 * <p>Original spec-file type: MethodFullInfo</p>
 * <pre>
 * Full information about a method suitable for displaying a method landing page.
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "name",
    "ver",
    "authors",
    "contact",
    "subtitle",
    "tooltip",
    "description",
    "technical_description",
    "categorizations",
    "screenshots"
})
public class MethodFullInfo {

    @JsonProperty("id")
    private java.lang.String id;
    @JsonProperty("name")
    private java.lang.String name;
    @JsonProperty("ver")
    private java.lang.String ver;
    @JsonProperty("authors")
    private List<String> authors;
    @JsonProperty("contact")
    private java.lang.String contact;
    @JsonProperty("subtitle")
    private java.lang.String subtitle;
    @JsonProperty("tooltip")
    private java.lang.String tooltip;
    @JsonProperty("description")
    private java.lang.String description;
    @JsonProperty("technical_description")
    private java.lang.String technicalDescription;
    @JsonProperty("categorizations")
    private List<List<String>> categorizations;
    @JsonProperty("screenshots")
    private List<ScreenShot> screenshots;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("id")
    public java.lang.String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(java.lang.String id) {
        this.id = id;
    }

    public MethodFullInfo withId(java.lang.String id) {
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

    public MethodFullInfo withName(java.lang.String name) {
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

    public MethodFullInfo withVer(java.lang.String ver) {
        this.ver = ver;
        return this;
    }

    @JsonProperty("authors")
    public List<String> getAuthors() {
        return authors;
    }

    @JsonProperty("authors")
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public MethodFullInfo withAuthors(List<String> authors) {
        this.authors = authors;
        return this;
    }

    @JsonProperty("contact")
    public java.lang.String getContact() {
        return contact;
    }

    @JsonProperty("contact")
    public void setContact(java.lang.String contact) {
        this.contact = contact;
    }

    public MethodFullInfo withContact(java.lang.String contact) {
        this.contact = contact;
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

    public MethodFullInfo withSubtitle(java.lang.String subtitle) {
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

    public MethodFullInfo withTooltip(java.lang.String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @JsonProperty("description")
    public java.lang.String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(java.lang.String description) {
        this.description = description;
    }

    public MethodFullInfo withDescription(java.lang.String description) {
        this.description = description;
        return this;
    }

    @JsonProperty("technical_description")
    public java.lang.String getTechnicalDescription() {
        return technicalDescription;
    }

    @JsonProperty("technical_description")
    public void setTechnicalDescription(java.lang.String technicalDescription) {
        this.technicalDescription = technicalDescription;
    }

    public MethodFullInfo withTechnicalDescription(java.lang.String technicalDescription) {
        this.technicalDescription = technicalDescription;
        return this;
    }

    @JsonProperty("categorizations")
    public List<List<String>> getCategorizations() {
        return categorizations;
    }

    @JsonProperty("categorizations")
    public void setCategorizations(List<List<String>> categorizations) {
        this.categorizations = categorizations;
    }

    public MethodFullInfo withCategorizations(List<List<String>> categorizations) {
        this.categorizations = categorizations;
        return this;
    }

    @JsonProperty("screenshots")
    public List<ScreenShot> getScreenshots() {
        return screenshots;
    }

    @JsonProperty("screenshots")
    public void setScreenshots(List<ScreenShot> screenshots) {
        this.screenshots = screenshots;
    }

    public MethodFullInfo withScreenshots(List<ScreenShot> screenshots) {
        this.screenshots = screenshots;
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
        return ((((((((((((((((((((((((("MethodFullInfo"+" [id=")+ id)+", name=")+ name)+", ver=")+ ver)+", authors=")+ authors)+", contact=")+ contact)+", subtitle=")+ subtitle)+", tooltip=")+ tooltip)+", description=")+ description)+", technicalDescription=")+ technicalDescription)+", categorizations=")+ categorizations)+", screenshots=")+ screenshots)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
