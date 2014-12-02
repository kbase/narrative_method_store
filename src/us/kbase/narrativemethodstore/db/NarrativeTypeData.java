package us.kbase.narrativemethodstore.db;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import us.kbase.narrativemethodstore.ScreenShot;
import us.kbase.narrativemethodstore.TypeInfo;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class NarrativeTypeData {
	protected String typeName;
	protected TypeInfo typeInfo;
	
	public NarrativeTypeData(String typeName, JsonNode spec, Map<String, Object> display,
			FileLookup lookup) throws NarrativeMethodStoreException {
		try {
			update(typeName, spec, display, lookup);
		} catch (Throwable ex) {
			if (typeInfo.getName() == null)
				typeInfo.withName(typeName);
			NarrativeMethodStoreException ret = new NarrativeMethodStoreException(ex.getMessage(), ex);
			ret.setErrorType(typeInfo);
			throw ret;
		}
	}
	
	public TypeInfo getTypeInfo() {
		return typeInfo;
	}

	public void update(String typeName, JsonNode spec, Map<String, Object> display,
			FileLookup lookup) throws NarrativeMethodStoreException {
		this.typeName = typeName;

		typeInfo = new TypeInfo().withTypeName(typeName);

		String name = getDisplayProp(display, "name", lookup);
		typeInfo.withName(name);
		String subtitle = getDisplayProp(display, "subtitle", lookup);
		typeInfo.withSubtitle(subtitle);
		String tooltip = getDisplayProp(display, "tooltip", lookup);
		typeInfo.withTooltip(tooltip);
		String description = getDisplayProp(display, "description", lookup);
		typeInfo.withDescription(description);
		
		String imageName = (String)getDisplayProp("/", display, "icon");
		if (imageName != null)
			typeInfo.withIcon(new ScreenShot().withUrl("img?type_name=" + this.typeName + "&image_name=" + imageName));
		
		typeInfo.withViewMethodIds(jsonListToStringListOrEmpty(spec.get("view_method_ids")));
		typeInfo.withImportMethodIds(jsonListToStringListOrEmpty(spec.get("import_method_ids")));
		typeInfo.withLandingPageUrlPrefix(getTextOrNull(spec.get("landing_page_url_prefix")));
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

	private static List<String> jsonListToStringListOrEmpty(JsonNode node) {
		if (node == null)
			return Collections.<String>emptyList();
		List<String> ret = new ArrayList<String>();
		for (int i = 0; i < node.size(); i++)
			ret.add(node.get(i).asText());
		return ret;
	}
}
