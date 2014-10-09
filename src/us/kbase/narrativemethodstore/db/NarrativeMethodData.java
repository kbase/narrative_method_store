package us.kbase.narrativemethodstore.db;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;

import us.kbase.narrativemethodstore.Categorization;
import us.kbase.narrativemethodstore.MethodBriefInfo;
import us.kbase.narrativemethodstore.MethodFullInfo;
import us.kbase.narrativemethodstore.MethodSpec;

public class NarrativeMethodData {

	
	public NarrativeMethodData(String methodId, JsonNode spec, String descriptionHtml, String technicalDescriptionHtml) {
		update(methodId, spec,descriptionHtml,technicalDescriptionHtml);
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
	
	
	public void update(String methodId, JsonNode spec, String descriptionHtml, String technicalDescriptionHtml) {
		this.methodId = methodId;
		
		List <Categorization> categorizations = new ArrayList<Categorization>(1);
		JsonNode cats = spec.get("categorizations");
		for(int k=0; k<cats.size(); k++) {
			List<String> catPath = new ArrayList<String>(1);
			for(int i=0; i<cats.get(k).size(); i++) {
				catPath.add(cats.get(k).get(i).asText());
			}
			categorizations.add(new Categorization().withNamedPath(catPath));
		}
		
		briefInfo = new MethodBriefInfo()
							.withId(this.methodId)
							.withName(spec.get("name").asText())
							.withVer(spec.get("ver").asText())
							.withSubtitle(spec.get("subtitle").asText())
							.withTooltip(spec.get("tooltip").asText())
							.withCategorizations(categorizations);
		
		List <String> authors = new ArrayList<String>(2);
		for(int a=0; a<spec.get("authors").size(); a++) {
			authors.add(spec.get("authors").get(a).asText());
		}
		
		fullInfo = new MethodFullInfo()
							.withId(this.methodId)
							.withName(spec.get("name").asText())
							.withVer(spec.get("ver").asText())
							.withSubtitle(spec.get("subtitle").asText())
							.withTooltip(spec.get("tooltip").asText())
							.withCategorizations(categorizations)
							
							.withAuthors(null)
							.withContact(spec.get("contact").asText())
							
							.withDescription(descriptionHtml)
							.withTechnicalDescription(technicalDescriptionHtml);
		
		
		
	}
	
	
	
	
}
