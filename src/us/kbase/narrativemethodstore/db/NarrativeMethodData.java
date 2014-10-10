package us.kbase.narrativemethodstore.db;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import us.kbase.narrativemethodstore.Categorization;
import us.kbase.narrativemethodstore.MethodBriefInfo;
import us.kbase.narrativemethodstore.MethodFullInfo;
import us.kbase.narrativemethodstore.MethodSpec;

public class NarrativeMethodData {

	
	public NarrativeMethodData(String methodId, JsonNode spec, Map<String, Object> display) {
		update(methodId, spec, display);
	}
	
	protected String methodId;
	protected MethodBriefInfo briefInfo;
	protected MethodFullInfo fullInfo;
	protected MethodSpec methodSpec;
	
	
	
	public MethodBriefInfo getMethodBriefInfo() {
		return briefInfo;
	}
	
	public MethodFullInfo getMethodFullInfo() {
		return fullInfo;
	}
	
	public MethodSpec getMethodSpec() {
		return methodSpec;
	}
	
	
	public void update(String methodId, JsonNode spec, Map<String, Object> display) {
		this.methodId = methodId;
		
		List <String> categories = new ArrayList<String>(1);
		JsonNode cats = spec.get("categories");
		for(int k=0; k<cats.size(); k++) {
			categories.add(cats.get(k).asText());
		}
		
		briefInfo = new MethodBriefInfo()
							.withId(this.methodId)
							.withName((String)display.get("name"))
							.withVer(spec.get("ver").asText())
							.withSubtitle((String)display.get("subtitle"))
							.withTooltip((String)display.get("tooltip"))
							.withCategories(categories);
		
		List <String> authors = new ArrayList<String>(2);
		for(int a=0; a<spec.get("authors").size(); a++) {
			authors.add(spec.get("authors").get(a).asText());
		}
		
		fullInfo = new MethodFullInfo()
							.withId(this.methodId)
							.withName((String)display.get("name"))
							.withVer(spec.get("ver").asText())
							.withSubtitle((String)display.get("subtitle"))
							.withTooltip((String)display.get("tooltip"))
							.withCategories(categories)
							
							.withAuthors(null)
							.withContact(spec.get("contact").asText())
							
							.withDescription((String)display.get("description"))
							.withTechnicalDescription((String)display.get("technical-description"));
	}
	
	
	
	
}
