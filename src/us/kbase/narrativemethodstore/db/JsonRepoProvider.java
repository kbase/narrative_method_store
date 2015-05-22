package us.kbase.narrativemethodstore.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRepoProvider implements RepoProvider {
    private final RepoData data;
    
    public JsonRepoProvider(String jsonString) throws NarrativeMethodStoreException {
        this(jsonStringToRepoData(jsonString));
    }

    public JsonRepoProvider(RepoData data) throws NarrativeMethodStoreException {
        this.data = data;
    }
    
    @Override
    public String getUrl() {
        return data.url;
    }
    
    @Override
    public String getGitCommitHash() {
        return data.gitCommitHash;
    }
    
    /////////// [root]  ///////////
    
    @Override
    public String getModuleName() {
        return data.moduleName;
    }
    
    @Override
    public String getModuleDescription() {
        return data.moduleDescription;
    }
    
    @Override
    public String getServiceLanguage() {
        return data.serviceLanguage;
    }
    
    @Override
    public String loadReadmeFile() throws NarrativeMethodStoreException {
        return data.readmeFile;
    }
    
    /////////// data    ///////////
    
    /////////// docs    ///////////
    
    /////////// service ///////////
    
    /////////// test    ///////////
    
    /////////// ui      ///////////

    @Override
    public List<String> listUINarrativeMethodIDs() throws NarrativeMethodStoreException {
        return Collections.unmodifiableList(data.uiNarrativeMethodIds);
    }
    
    @Override
    public String loadUINarrativeMethodSpec(String methodId) throws NarrativeMethodStoreException {
        return data.uiNarrativeMethodIdToSpec.get(methodId);
    }
    
    @Override
    public String loadUINarrativeMethodDisplay(String methodId) throws NarrativeMethodStoreException {
        return data.uiNarrativeMethodIdToDisplay.get(methodId);
    }
    
    @Override
    public List<String> listUIWidgetIds() throws NarrativeMethodStoreException {
        return new ArrayList<String>(data.uiWidgetIdToJavaScript.keySet());
    }
    
    @Override
    public String loadUIWidgetJS(String widgetId) throws NarrativeMethodStoreException {
        return data.uiWidgetIdToJavaScript.get(widgetId);
    }
    
    /////////// [utils] ///////////
    
    @Override
    public void dispose() throws NarrativeMethodStoreException {
        // Do nothing
    }

    public static RepoData jsonStringToRepoData(String jsonString) throws NarrativeMethodStoreException {
        try {
            return new ObjectMapper().readValue(jsonString, RepoData.class);
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException(ex);
        }
    }

    public static String repoDataToJsonString(RepoData data) throws NarrativeMethodStoreException {
        try {
            return new ObjectMapper().writeValueAsString(data);
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException(ex);
        }
    }
    
    public static RepoData repoProviderToData(RepoProvider repo) throws NarrativeMethodStoreException {
        RepoData ret = new RepoData();
        ret.url = repo.getUrl();
        ret.gitCommitHash = repo.getGitCommitHash();
        ret.moduleName = repo.getModuleName();
        ret.moduleDescription = repo.getModuleDescription();
        ret.serviceLanguage = repo.getServiceLanguage();
        ret.uiNarrativeMethodIds = repo.listUINarrativeMethodIDs();
        ret.uiNarrativeMethodIdToSpec = new TreeMap<String, String>();
        ret.uiNarrativeMethodIdToDisplay = new TreeMap<String, String>();
        for (String methodId : ret.uiNarrativeMethodIds) {
            ret.uiNarrativeMethodIdToSpec.put(methodId, repo.loadUINarrativeMethodSpec(methodId));
            ret.uiNarrativeMethodIdToDisplay.put(methodId, repo.loadUINarrativeMethodDisplay(methodId));
        }
        ret.uiWidgetIdToJavaScript = new TreeMap<String, String>();
        for (String widgetId : repo.listUIWidgetIds())
            ret.uiWidgetIdToJavaScript.put(widgetId, repo.loadUIWidgetJS(widgetId));
        return ret;
    }

    public static String repoProviderToJsonString(RepoProvider repo) throws NarrativeMethodStoreException {
        return repoDataToJsonString(repoProviderToData(repo));
    }
    
    public static class RepoData {
        public String url;
        public String gitCommitHash;
        public String moduleName;
        public String moduleDescription;
        public String serviceLanguage;
        public String readmeFile;
        public List<String> uiNarrativeMethodIds;
        public Map<String, String> uiNarrativeMethodIdToSpec;
        public Map<String, String> uiNarrativeMethodIdToDisplay;
        public Map<String, String> uiWidgetIdToJavaScript;
    }
}
