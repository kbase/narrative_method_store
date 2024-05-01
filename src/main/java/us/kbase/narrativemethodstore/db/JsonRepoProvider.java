package us.kbase.narrativemethodstore.db;

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
    public String getModuleVersion() {
        return data.moduleVersion;
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
        MethodData md = data.uiNarrativeMethods.get(methodId);
        checkMethod(methodId, md);
        return fp(md.specFile);
    }
    
    @Override
    public FilePointer getUINarrativeMethodDisplay(String methodId) throws NarrativeMethodStoreException {
        MethodData md = data.uiNarrativeMethods.get(methodId);
        checkMethod(methodId, md);
        return fp(md.displayFile);
    }

    public void checkMethod(String methodId, MethodData md)
            throws NarrativeMethodStoreException {
        if (md == null)
            throw new NarrativeMethodStoreException("Method is not found: " + getModuleName() + 
                    "/" + methodId + " (version: " + getGitCommitHash() + ")");
    }

    @Override
    public List<String> listScreenshotIDs(String methodId)
            throws NarrativeMethodStoreException {
        MethodData md = data.uiNarrativeMethods.get(methodId);
        checkMethod(methodId, md);
        List<String> ret = new ArrayList<String>();
        if (md.screenshotIdToFile != null) {
            ret.addAll(md.screenshotIdToFile.keySet());
        } else {
            for (FileRef fr : md.imageFileRefs)
                ret.add(fr.fileName);
        }
        return ret;
    }
    
    @Override
    public FilePointer getScreenshot(String methodId, String screenshotId)
            throws NarrativeMethodStoreException {
        MethodData md = data.uiNarrativeMethods.get(methodId);
        checkMethod(methodId, md);
        String fileId = null;
        if (md.screenshotIdToFile != null) {
            md.screenshotIdToFile.get(screenshotId);
        } else {
            for (FileRef fr : md.imageFileRefs)
                if (fr.fileName.equals(screenshotId))
                    fileId = fr.innerRef;
        }
        if (fileId == null)
            throw new NarrativeMethodStoreException("Image with id=" + screenshotId + " is not registered");
        return fp(fileId);
    }

    @Override
    public List<String> listUIWidgetIds() throws NarrativeMethodStoreException {
        List<String> ret = new ArrayList<String>();
        if (data.uiWidgetFileRefs != null)
            for (FileRef fr : data.uiWidgetFileRefs)
                ret.add(fr.fileName);
        return ret;
    }
    
    @Override
    public FilePointer getUIWidgetJS(String widgetId) throws NarrativeMethodStoreException {
        String fileId = null;
        if (data.uiWidgetFileRefs != null)
            for (FileRef fr : data.uiWidgetFileRefs)
                if (fr.fileName.equals(widgetId))
                    fileId = fr.innerRef;
        if (fileId == null)
            throw new NarrativeMethodStoreException("WidgetJS with id=" + widgetId + " is not registered");
        return fp(fileId);
    }
    /////////// [utils] ///////////
    
    @Override
    public FilePointer getRepoZip() throws NarrativeMethodStoreException {
        String fileId = data.repoZip;
        return fileId == null ? null : fp(fileId);
    }
    
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
        ret.moduleVersion = repo.getModuleVersion();
        ret.owners = repo.listOwners();
        ret.readmeFile = fId(db, mn, repo.getReadmeFile());
        ret.uiNarrativeMethodIds = repo.listUINarrativeMethodIDs();
        ret.uiNarrativeMethods = new TreeMap<String, MethodData>();
        for (String methodId : ret.uiNarrativeMethodIds) {
            MethodData md = new MethodData();
            ret.uiNarrativeMethods.put(methodId, md);
            md.specFile = fId(db, mn, repo.getUINarrativeMethodSpec(methodId));
            md.displayFile = fId(db, mn, repo.getUINarrativeMethodDisplay(methodId));
            md.imageFileRefs = new ArrayList<FileRef>();
            for (String screenshotId : repo.listScreenshotIDs(methodId)) {
                FileRef fr = new FileRef();
                fr.fileName = screenshotId;
                fr.innerRef = fId(db, mn, repo.getScreenshot(methodId, screenshotId));
                md.imageFileRefs.add(fr);
            }
        }
        ret.uiWidgetFileRefs = new ArrayList<FileRef>();
        for (String widgetId : repo.listUIWidgetIds()) {
            FileRef fr = new FileRef();
            fr.fileName = widgetId;
            fr.innerRef = fId(db, mn, repo.getUIWidgetJS(widgetId));
            ret.uiWidgetFileRefs.add(fr);
        }
        FilePointer zipFp = repo.getRepoZip();
        ret.repoZip = zipFp == null ? null : fId(db, mn, zipFp);
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
        public String moduleVersion;
        public List<String> owners;
        public String readmeFile;
        public List<String> uiNarrativeMethodIds;
        public Map<String, MethodData> uiNarrativeMethods;
        public Map<String, String> uiWidgetIdToFile;  // compatibility with older versions (not used)
        public List<FileRef> uiWidgetFileRefs;
        public String repoZip;
        
        /**
         * Should be solved every time before data structure is stored into MongoDB. 
         */
        public void repackForMongoDB() {
            for (String methodId : uiNarrativeMethods.keySet()) {
                uiNarrativeMethods.get(methodId).repackForMongoDB();
            }
        }
    }
    
    public static class MethodData {
        public String specFile;
        public String displayFile;
        public Map<String, String> screenshotIdToFile;
        public List<FileRef> imageFileRefs;    // Alternative to screenshotIdToFile solving
                                               // MongoDB problem with dots in map keys.
        
        public void repackForMongoDB() {
            if (screenshotIdToFile != null) {
                imageFileRefs = new ArrayList<FileRef>();
                for (String imageName : screenshotIdToFile.keySet()) {
                    FileRef fr = new FileRef();
                    fr.fileName = imageName;
                    fr.innerRef = screenshotIdToFile.get(imageName);
                    imageFileRefs.add(fr);
                }
                screenshotIdToFile = null;
            }
        }
    }
    
    public static class FileRef {
        public String fileName;
        public String innerRef;
    }
}
