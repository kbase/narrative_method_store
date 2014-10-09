package us.kbase.narrativemethodstore.prepare;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OldMethodParam {
    @JsonProperty("default")
	public String default_;
	public String ui_name;
	public String type;
	public String description;
}
