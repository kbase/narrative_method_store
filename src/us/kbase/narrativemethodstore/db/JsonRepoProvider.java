package us.kbase.narrativemethodstore.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRepoProvider implements RepoProvider {
    private final DynamicRepoDB db;
    private final RepoData data;
    
    public JsonRepoProvider(DynamicRepoDB db, String jsonString) throws NarrativeMethodStoreException {
        this(db, jsonStringToRepoData(jsonString));
    }

    public JsonRepoProvider(DynamicRepoDB db, RepoData data) throws NarrativeMethodStoreException {
        this.db = db;
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
    public List<String> listOwners() {
        return Collections.unmodifiableList(data.owners);
    }
    
    @Override
    public FilePointer getReadmeFile() throws NarrativeMethodStoreException {
        return fp(data.readmeFile);
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
    public FilePointer getUINarrativeMethodSpec(String methodId) throws NarrativeMethodStoreException {
        return fp(data.uiNarrativeMethods.get(methodId).specFile);
    }
    
    @Override
    public FilePointer getUINarrativeMethodDisplay(String methodId) throws NarrativeMethodStoreException {
        return fp(data.uiNarrativeMethods.get(methodId).displayFile);
    }

    @Override
    public List<String> listScreenshotIDs(String methodId)
            throws NarrativeMethodStoreException {
        return new ArrayList<String>(data.uiNarrativeMethods.get(methodId).screenshotIdToFile.keySet());
    }
    
    @Override
    public FilePointer getScreenshot(String methodId, String screenshotId)
            throws NarrativeMethodStoreException {
        return fp(data.uiNarrativeMethods.get(methodId).screenshotIdToFile.get(screenshotId));
    }

    @Override
    public List<String> listUIWidgetIds() throws NarrativeMethodStoreException {
        return new ArrayList<String>(data.uiWidgetIdToFile.keySet());
    }
    
    @Override
    public FilePointer getUIWidgetJS(String widgetId) throws NarrativeMethodStoreException {
        return fp(data.uiWidgetIdToFile.get(widgetId));
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
    
    private static String fId(DynamicRepoDB db, String moduleName, FilePointer fp) throws NarrativeMethodStoreException {
        if (fp.getFileId() != null)
            return fp.getFileId().getId();
        return db.saveFile(moduleName, fp.getFile()).getId();
    }
    
    /**
     * Transform RepoProvider data into simple structure with storing files into DB.
     * @param db
     * @param repo
     * @return
     * @throws NarrativeMethodStoreException
     */
    public static RepoData repoProviderToData(DynamicRepoDB db, RepoProvider repo) throws NarrativeMethodStoreException {
        String mn = repo.getModuleName();
        RepoData ret = new RepoData();
        ret.url = repo.getUrl();
        ret.gitCommitHash = repo.getGitCommitHash();
        ret.moduleName = mn;
        ret.moduleDescription = repo.getModuleDescription();
        ret.serviceLanguage = repo.getServiceLanguage();
        ret.owners = repo.listOwners();
        ret.readmeFile = fId(db, mn, repo.getReadmeFile());
        ret.uiNarrativeMethodIds = repo.listUINarrativeMethodIDs();
        ret.uiNarrativeMethods = new TreeMap<String, MethodData>();
        for (String methodId : ret.uiNarrativeMethodIds) {
            MethodData md = new MethodData();
            ret.uiNarrativeMethods.put(methodId, md);
            md.specFile = fId(db, mn, repo.getUINarrativeMethodSpec(methodId));
            md.displayFile = fId(db, mn, repo.getUINarrativeMethodDisplay(methodId));
            md.screenshotIdToFile = new TreeMap<String, String>();
            for (String screenshotId : repo.listScreenshotIDs(methodId)) {
                md.screenshotIdToFile.put(screenshotId, fId(db, mn, repo.getScreenshot(methodId, screenshotId)));
            }
        }
        ret.uiWidgetIdToFile = new TreeMap<String, String>();
        for (String widgetId : repo.listUIWidgetIds())
            ret.uiWidgetIdToFile.put(widgetId, fId(db, mn, repo.getUIWidgetJS(widgetId)));
        return ret;
    }

    /**
     * Transform data into JSON string with storing files into db.
     * @param db
     * @param repo
     * @return
     * @throws NarrativeMethodStoreException
     */
    public static String repoProviderToJsonString(DynamicRepoDB db, RepoProvider repo) throws NarrativeMethodStoreException {
        return repoDataToJsonString(repoProviderToData(db, repo));
    }
    
    private FilePointer fp(String fileId) throws NarrativeMethodStoreException {
        return db.loadFile(new FileId(fileId));
    }
    
    public static class RepoData {
        public String url;
        public String gitCommitHash;
        public String moduleName;
        public String moduleDescription;
        public String serviceLanguage;
        public List<String> owners;
        public String readmeFile;
        public List<String> uiNarrativeMethodIds;
        public Map<String, MethodData> uiNarrativeMethods;
        public Map<String, String> uiWidgetIdToFile;
    }
    
    public static class MethodData {
        public String specFile;
        public String displayFile;
        public Map<String, String> screenshotIdToFile;
    }
}
