package us.kbase.narrativemethodstore.db;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import us.kbase.narrativemethodstore.MethodBehavior;
import us.kbase.narrativemethodstore.MethodBriefInfo;
import us.kbase.narrativemethodstore.MethodFullInfo;
import us.kbase.narrativemethodstore.MethodParameter;
import us.kbase.narrativemethodstore.MethodSpec;
import us.kbase.narrativemethodstore.ScreenShot;
import us.kbase.narrativemethodstore.TextOptions;
import us.kbase.narrativemethodstore.WidgetSpec;

public class NarrativeMethodData {
	protected String methodId;
	protected MethodBriefInfo briefInfo;
	protected MethodFullInfo fullInfo;
	protected MethodSpec methodSpec;
	
	public NarrativeMethodData(String methodId, JsonNode spec, Map<String, Object> display,
			MethodFileLookup lookup) {
		update(methodId, spec, display, lookup);
	}
	
	public MethodBriefInfo getMethodBriefInfo() {
		return briefInfo;
	}
	
	public MethodFullInfo getMethodFullInfo() {
		return fullInfo;
	}
	
	public MethodSpec getMethodSpec() {
		return methodSpec;
	}
	
	
	public void update(String methodId, JsonNode spec, Map<String, Object> display,
			MethodFileLookup lookup) {
		this.methodId = methodId;
		
		List <String> categories = new ArrayList<String>(1);
		JsonNode cats = spec.get("categories");
		for(int k=0; k<cats.size(); k++) {
			categories.add(cats.get(k).asText());
		}
		
		String methodName = getDisplayProp(display, "name", lookup);
		String methodSubtitle = getDisplayProp(display, "subtitle", lookup);
		String methodTooltip = getDisplayProp(display, "tooltip", lookup);
		String methodDescription = getDisplayProp(display, "description", lookup);
		String methodTechnicalDescr = getDisplayProp(display, "technical-description", lookup);
		
		briefInfo = new MethodBriefInfo()
							.withId(this.methodId)
							.withName(methodName)
							.withVer(spec.get("ver").asText())
							.withSubtitle(methodSubtitle)
							.withTooltip(methodTooltip)
							.withCategories(categories);
		
		List <String> authors = new ArrayList<String>(2);
		for(int a=0; a<spec.get("authors").size(); a++) {
			authors.add(spec.get("authors").get(a).asText());
		}
		
		List<ScreenShot> screenshots = new ArrayList<ScreenShot>();
		List<String> imageNames = (List<String>)display.get("screenshots");
		if (imageNames != null) {
			for (String imageName : imageNames)
				screenshots.add(new ScreenShot().withUrl("img?method_id=" + this.methodId + "&image_name=" + imageName));
		}
		
		fullInfo = new MethodFullInfo()
							.withId(this.methodId)
							.withName(methodName)
							.withVer(spec.get("ver").asText())
							.withSubtitle(methodSubtitle)
							.withTooltip(methodTooltip)
							.withCategories(categories)
							
							.withAuthors(null)
							.withContact(spec.get("contact").asText())
							
							.withDescription(methodDescription)
							.withTechnicalDescription(methodTechnicalDescr)
							.withScreenshots(screenshots);
		
		JsonNode widgetsNode = spec.get("widgets");
		WidgetSpec widgets = new WidgetSpec()
							.withInput(getTextOrNull(widgetsNode.get("input")))
							.withOutput(getTextOrNull(widgetsNode.get("output")));
		JsonNode behaviorNode = spec.get("behavior");
		JsonNode serviceMappingNode = behaviorNode.get("service-mapping");
		MethodBehavior behavior = new MethodBehavior()
							.withPythonClass(getTextOrNull(behaviorNode.get("python_class")))
							.withPythonFunction(getTextOrNull(behaviorNode.get("python_function")));
		if (serviceMappingNode != null)
			behavior
				.withKbServiceName(getTextOrNull(behaviorNode.get("url")))
				.withKbServiceMethod(getTextOrNull(behaviorNode.get("method")));
		List<MethodParameter> parameters = new ArrayList<MethodParameter>();
		JsonNode parametersNode = spec.get("parameters");
		@SuppressWarnings("unchecked")
		Map<String, Map<String, String>> paramsDisplays = (Map<String, Map<String, String>>)display.get("parameters"); 
		for (int i = 0; i < parametersNode.size(); i++) {
			JsonNode paramNode = parametersNode.get(i);
			String paramId = paramNode.get("id").asText();
			Map<String, String> paramDisplay = paramsDisplays.get(paramId);
			TextOptions textOpt = null;
			if (paramNode.has("text_options")) {
				JsonNode optNode = paramNode.get("text_options");
				textOpt = new TextOptions()
							.withValidWsTypes(jsonListToStringList(optNode.get("valid_ws_types")))
							.withValidateAs(getTextOrNull(optNode.get("validate_as")));
			}
			MethodParameter param = new MethodParameter()
							.withId(paramId)
							.withUiName(paramDisplay.get("ui-name"))
							.withShortHint(paramDisplay.get("short-hint"))
							.withLongHint(paramDisplay.get("long-hint"))
							.withOptional(jsonBooleanToRPC(paramNode.get("optional")))
							.withAdvanced(jsonBooleanToRPC(paramNode.get("advanced")))
							.withAllowMultiple(jsonBooleanToRPC(paramNode.get("allow_multiple")))
							.withDefaultValues(jsonListToStringList(paramNode.get("default_values")))
							.withFieldType(paramNode.get("field_type").asText())
							.withTextOptions(textOpt);
			parameters.add(param);
		}
		methodSpec = new MethodSpec()
							.withInfo(briefInfo)
							.withWidgets(widgets)
							.withBehavior(behavior)
							.withParameters(parameters);
	}
	
	private static String getDisplayProp(Map<String, Object> display, String propName, 
			MethodFileLookup lookup) {
		return getDisplayProp(display, propName, lookup, propName);
	}

	private static String getDisplayProp(Map<String, Object> display, String propName, 
			MethodFileLookup lookup, String lookupName) {
		String ret = lookup.loadFileContent(lookupName + ".html");
		if (ret == null)
			ret = (String)display.get(propName);
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
