package us.kbase.narrativemethodstore.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.JsonNode;

import us.kbase.narrativemethodstore.AppBriefInfo;
import us.kbase.narrativemethodstore.Category;
import us.kbase.narrativemethodstore.MethodBriefInfo;
import us.kbase.narrativemethodstore.TypeInfo;
import us.kbase.narrativemethodstore.db.github.MethodId;
import us.kbase.narrativemethodstore.db.github.RepoTag;

public class NarrativeCategoriesIndex {

	protected Map<String, Category> categories;
	protected Map<MethodId, MethodBriefInfo> methods;
	protected Map<String, AppBriefInfo> apps;
	protected Map<String, TypeInfo> types;
	protected Set<MethodId> dynamicRepoMethods;
    protected Map<String, Exception> dynamicRepoModuleNameToLoadingError;
    protected boolean invalid = false;
    protected final RepoTag defaultTagForGetters;
    
	public NarrativeCategoriesIndex(RepoTag defaultTagForGetters) {
		categories = new HashMap<String,Category>();
		methods = new TreeMap<MethodId,MethodBriefInfo>();
		apps = new HashMap<String, AppBriefInfo>();
		types = new HashMap<String, TypeInfo>();
	    dynamicRepoMethods = new TreeSet<MethodId>();
	    dynamicRepoModuleNameToLoadingError = new TreeMap<String, Exception>();
	    this.defaultTagForGetters = defaultTagForGetters;
	}
	
	public void updateAllCategories(Map<String,Category> categories) {
		this.categories = categories;
	}
	
	public void updateAllMethods(Map<MethodId,MethodBriefInfo> methods) {
		this.methods = methods;
	}

	public void updateAllApps(Map<String, AppBriefInfo> apps) {
		this.apps = apps;
	}

	public void updateAllTypes(Map<String, TypeInfo> types) {
	    this.types = types;
	}
	
	public void updateAllDynamicRepoMethods(Set<MethodId> dynamicRepoMethods,
	        Map<String, Exception> dynamicRepoModuleNameToLoadingError) {
	    this.dynamicRepoMethods = dynamicRepoMethods;
	    this.dynamicRepoModuleNameToLoadingError = dynamicRepoModuleNameToLoadingError;
	}
	
	public boolean isInvalid() {
        return invalid;
    }
	
	/**
	 * Invalidate this index which will lead to complete recreation of this index later.
	 */
	public void invalidate() {
	    this.invalid = true;
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
	
	public void addOrUpdateMethod(MethodId methodId, MethodBriefInfo briefInfo) {
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
	
	public Map<String,MethodBriefInfo> getMethods(String tagName) {
	    RepoTag tag = tagName == null ? defaultTagForGetters : RepoTag.valueOf(tagName);
	    Map<String, MethodBriefInfo> ret = new LinkedHashMap<String, MethodBriefInfo>();
	    for (Map.Entry<MethodId, MethodBriefInfo> entry : methods.entrySet())
	        if ((!entry.getKey().isDynamic()) || entry.getKey().getTag().equals(tag))
	            ret.put(entry.getKey().getExternalId(), entry.getValue());
		return ret;
	}

	public Map<MethodId,MethodBriefInfo> getAllMethods() {
	    return methods;
	}

	public Map<String, AppBriefInfo> getApps() {
		return apps;
	}
	
	public Map<String, TypeInfo> getTypes() {
		return types;
	}
	
	public Set<MethodId> getDynamicRepoMethods() {
        return dynamicRepoMethods;
    }
	
	public Map<String, Exception> getDynamicRepoModuleNameToLoadingError() {
        return dynamicRepoModuleNameToLoadingError;
    }
}
