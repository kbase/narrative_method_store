package us.kbase.narrativemethodstore.db;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import us.kbase.narrativemethodstore.AppBriefInfo;
import us.kbase.narrativemethodstore.AppFullInfo;
import us.kbase.narrativemethodstore.AppSpec;
import us.kbase.narrativemethodstore.AppStepInputMapping;
import us.kbase.narrativemethodstore.AppSteps;
import us.kbase.narrativemethodstore.Icon;
import us.kbase.narrativemethodstore.ScreenShot;
import us.kbase.narrativemethodstore.Suggestions;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class NarrativeAppData {
	protected String appId;
	protected AppBriefInfo briefInfo;
	protected AppFullInfo fullInfo;
	protected AppSpec appSpec;
	
	public NarrativeAppData(String appId, JsonNode spec, Map<String, Object> display,
			FileLookup lookup) throws NarrativeMethodStoreException {
		try {
			update(appId, spec, display, lookup);
		} catch (Throwable ex) {
			if (briefInfo.getName() == null)
				briefInfo.withName(briefInfo.getId());
			if (briefInfo.getCategories() == null)
				briefInfo.withCategories(Arrays.asList("error"));
			NarrativeMethodStoreException ret = new NarrativeMethodStoreException(ex.getMessage(), ex);
			ret.setErrorApp(briefInfo);
			throw ret;
		}
	}
	
	public AppBriefInfo getAppBriefInfo() {
		return briefInfo;
	}
	
	public AppFullInfo getAppFullInfo() {
		return fullInfo;
	}
	
	public AppSpec getAppSpec() {
		return appSpec;
	}
	
	
	public void update(String appId, JsonNode spec, Map<String, Object> display,
			FileLookup lookup) throws NarrativeMethodStoreException {
		this.appId = appId;

		briefInfo = new AppBriefInfo()
							.withId(this.appId);

		List <String> categories = new ArrayList<String>(1);
		JsonNode cats = get(spec, "categories");
		for(int k=0; k<cats.size(); k++) {
			categories.add(cats.get(k).asText());
		}
		briefInfo.withCategories(categories);
		
		String appName = getDisplayProp(display, "name", lookup);
		briefInfo.withName(appName);
		String appSubtitle = getDisplayProp(display, "subtitle", lookup);
		briefInfo.withSubtitle(appSubtitle);
		String appTooltip = getDisplayProp(display, "tooltip", lookup);
		briefInfo.withTooltip(appTooltip);
		String appDescription = getDisplayProp(display, "description", lookup);
		String appTechnicalDescr = "";
		try { appTechnicalDescr = getDisplayProp(display, "technical-description", lookup); }
		catch (IllegalStateException e) { /* tech description is optional, do nothing */ }
		
		String appHeader = getDisplayProp(display, "header", lookup);
		
		briefInfo.withVer(get(spec, "ver").asText()).withHeader(appHeader);
		
		@SuppressWarnings("unchecked")
		Icon icon = null;
		try {
			String iconName = getDisplayProp(display,"icon",lookup);
			if(iconName.trim().length()>0) {
				icon = new Icon().withUrl("img?method_id=" + this.appId + "&image_name=" + iconName);
			}
			briefInfo.withIcon(icon);
		} catch (IllegalStateException e) { /* icon is optional, do nothing */ }
		
		List<ScreenShot> screenshots = new ArrayList<ScreenShot>();
		@SuppressWarnings("unchecked")
		List<String> imageNames = (List<String>)getDisplayProp("/", display, "screenshots");
		if (imageNames != null) {
			for (String imageName : imageNames)
				screenshots.add(new ScreenShot().withUrl("img?app_id=" + this.appId + "&image_name=" + imageName));
		}
		
		List<String> relatedApps = new ArrayList<String>();
		List<String> nextApps = new ArrayList<String>();
		List<String> relatedMethods = new ArrayList<String>();
		List<String> nextMethods = new ArrayList<String>();
		try {
			@SuppressWarnings("unchecked")
			Map<String,Object> sugg = (Map<String,Object>)getDisplayProp("/", display, "suggestions");
			if(sugg.get("apps")!=null) {
				@SuppressWarnings("unchecked")
				Map<String,List<String>> suggApps = (Map<String, List<String>>) sugg.get("apps");
				if(suggApps.get("related")!=null) {
					relatedApps = suggApps.get("related");
				}
				if(suggApps.get("next")!=null) {
					nextApps = suggApps.get("next");
				}
			}
			if(sugg.get("methods")!=null) {
				@SuppressWarnings("unchecked")
				Map<String,List<String>> suggMethods = (Map<String, List<String>>) sugg.get("methods");
				if(suggMethods.get("related")!=null) {
					relatedMethods = suggMethods.get("related");
				}
				if(suggMethods.get("next")!=null) {
					nextMethods = suggMethods.get("next");
				}
			}
		} catch(IllegalStateException e) {}
		Suggestions suggestions = new Suggestions()
									.withRelatedApps(relatedApps)
									.withNextApps(nextApps)
									.withRelatedMethods(relatedMethods)
									.withNextMethods(nextMethods);
		
		fullInfo = new AppFullInfo()
							.withId(this.appId)
							.withName(appName)
							.withVer(briefInfo.getVer())
							.withSubtitle(appSubtitle)
							.withTooltip(appTooltip)
							.withCategories(categories)
							
							.withAuthors(jsonListToStringList(spec.get("authors")))
							.withContact(get(spec, "contact").asText())
							
							.withDescription(appDescription)
							.withTechnicalDescription(appTechnicalDescr)
							.withScreenshots(screenshots)
							
							.withIcon(icon)
							.withSuggestions(suggestions)
							
							.withHeader(appHeader);
		
		List<AppSteps> steps = new ArrayList<AppSteps>();
		JsonNode stepsNode = get(spec, "steps");
		@SuppressWarnings("unchecked")
		Map<String, Object> stepsDisplay = (Map<String, Object>)getDisplayProp("/", display, "step-descriptions");
		for (int i = 0; i < stepsNode.size(); i++) {
			JsonNode stepNode = stepsNode.get(i);
			String stepPath = "steps/" + i;
			String stepId = get(stepPath, stepNode, "step-id").asText();
			String methodId = get(stepPath, stepNode, "method-id").asText();
			AppSteps step = new AppSteps()
							.withStepId(stepId)
							.withMethodId(methodId)
							.withDescription((String)getDisplayProp("step-descriptions", stepsDisplay, stepId));
			JsonNode mappingNode = stepNode.get("input_mapping");
			List<AppStepInputMapping> inputMappings = new ArrayList<AppStepInputMapping>();
			if (mappingNode != null) {
				for (int j = 0; j < mappingNode.size(); j++) {
					JsonNode linkNode = mappingNode.get(j);
					String path = stepPath + "/input_mapping/" + j;
					AppStepInputMapping mapping = new AppStepInputMapping();
					for (Iterator<String> it = linkNode.fieldNames(); it.hasNext(); ) {
						String field = it.next();
						if (field.equals("step")) {
							mapping.withStepSource(linkNode.get(field).asText());
						} else if (field.equals("is_from_input")) {
							mapping.withIsFromInput(jsonBooleanToRPC(linkNode.get(field)));
						} else if (field.equals("from")) {
							mapping.withFrom(linkNode.get(field).asText());
						} else if (field.equals("to")) {
							mapping.withTo(linkNode.get(field).asText());
						} else {
							throw new IllegalStateException("Unknown field [" + field + "] in step input " +
									"mapping structure within path " + path);
						}
					}
					inputMappings.add(mapping);
				}
			}
			step.setInputMapping(inputMappings);
			steps.add(step);
		}
		appSpec = new AppSpec()
							.withInfo(briefInfo)
							.withSteps(steps);
	}

	private static JsonNode get(JsonNode node, String childName) {
		return get(null, node, childName);
	}
	
	private static JsonNode get(String nodePath, JsonNode node, String childName) {
		JsonNode ret = node.get(childName);
		if (ret == null)
			throw new IllegalStateException("Can't find sub-node [" + childName + "] within " +
					"path [" + (nodePath == null ? "/" : nodePath) + "] in spec.json");
		return ret;
	}
	
	private static String getDisplayProp(Map<String, Object> display, String propName, 
			FileLookup lookup) {
		return getDisplayProp(display, propName, lookup, propName);
	}

	private static String getDisplayProp(Map<String, Object> display, String propName, 
			FileLookup lookup, String lookupName) {
		String ret = lookup.loadFileContent(lookupName + ".html");
		if (ret == null)
			ret = (String)getDisplayProp("/", display, propName);
		return ret;
	}
	
	private static Object getDisplayProp(String path, Map<String, Object> display, String propName) {
		Object ret = display.get(propName);
		if (ret == null)
			throw new IllegalStateException("Can't find property [" + propName + "] within path [" + 
					path + "] in display.yaml");
		return ret;
	}

	private static String getTextOrNull(JsonNode node) {
		return node == null ? null : node.asText();
	}

	private static Long jsonBooleanToRPC(JsonNode node) {
		return node.asBoolean() ? 1L : 0L;
	}
	
	private static List<String> jsonListToStringList(JsonNode node) {
		if (node == null)
			return null;
		List<String> ret = new ArrayList<String>();
		for (int i = 0; i < node.size(); i++)
			ret.add(node.get(i).asText());
		return ret;
	}
}
