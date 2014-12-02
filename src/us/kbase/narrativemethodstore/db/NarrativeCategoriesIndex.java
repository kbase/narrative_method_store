package us.kbase.narrativemethodstore.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import us.kbase.narrativemethodstore.AppBriefInfo;
import us.kbase.narrativemethodstore.Category;
import us.kbase.narrativemethodstore.MethodBriefInfo;
import us.kbase.narrativemethodstore.TypeInfo;

public class NarrativeCategoriesIndex {

	protected Map<String, Category> categories;
	protected Map<String, MethodBriefInfo> methods;
	protected Map<String, AppBriefInfo> apps;
	protected Map<String, TypeInfo> types;
	
	public NarrativeCategoriesIndex() {
		clearIndex();
	}

	public void clearIndex() {
		categories = new HashMap<String,Category>();
		methods = new HashMap<String,MethodBriefInfo>();
		apps = new HashMap<String, AppBriefInfo>();
		types = new HashMap<String, TypeInfo>();
	}
	
	public void updateAllCategories(Map<String,Category> categories) {
		this.categories = categories;
	}
	
	public void updateAllMethods(Map<String,MethodBriefInfo> methods) {
		this.methods = methods;
	}

	public void updateAllApps(Map<String, AppBriefInfo> apps) {
		this.apps = apps;
	}

	public void addOrUpdateCategory(String catId, JsonNode spec, Map<String,Object> display) {
		
		List<String> parentIds = new ArrayList<String>();
		for(int p=0; p<spec.get("parent").size(); p++) {
			parentIds.add(spec.get("parent").get(p).asText());
		}
		
		Category c = new Category()
						.withId(catId)
						.withName(spec.get("name").asText())
						.withVer(spec.get("ver").asText())
						.withTooltip(spec.get("tooltip").asText())
						.withParentIds(parentIds)
						.withDescription("");
		
		categories.put(catId, c);
	}
	
	public void addOrUpdateMethod(String methodId, MethodBriefInfo briefInfo) {
		methods.put(methodId, briefInfo);
	}

	public void addOrUpdateApp(String appId, AppBriefInfo briefInfo) {
		apps.put(appId, briefInfo);
	}

	public void addOrUpdateType(String typeName, TypeInfo typeInfo) {
		types.put(typeName, typeInfo);
	}

	public Map<String,Category> getCategories() {
		return categories;
	}
	
	public Map<String,MethodBriefInfo> getMethods() {
		return methods;
	}
	
	public Map<String, AppBriefInfo> getApps() {
		return apps;
	}
	
	public Map<String, TypeInfo> getTypes() {
		return types;
	}
}
