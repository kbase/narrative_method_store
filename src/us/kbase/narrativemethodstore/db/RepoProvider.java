package us.kbase.narrativemethodstore.db;

import java.util.List;

import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public interface RepoProvider {
    public String getUrl();
    public String getGitCommitHash();
    /////////// [root]  ///////////
    public String getModuleName();
    public String getModuleDescription();
    public String getServiceLanguage();
    public List<String> listOwners();
    public FilePointer getReadmeFile() throws NarrativeMethodStoreException;
    /////////// data    ///////////
    /////////// docs    ///////////
    /////////// service ///////////
    /////////// test    ///////////
    /////////// ui      ///////////
    public List<String> listUINarrativeMethodIDs() throws NarrativeMethodStoreException;
    public FilePointer getUINarrativeMethodSpec(String methodId) throws NarrativeMethodStoreException;
    public FilePointer getUINarrativeMethodDisplay(String methodId) throws NarrativeMethodStoreException;
    public List<String> listUIWidgetIds() throws NarrativeMethodStoreException;
    public FilePointer getUIWidgetJS(String widgetId) throws NarrativeMethodStoreException;
    public List<String> listScreenshotIDs(String methodId) throws NarrativeMethodStoreException;
    public FilePointer getScreenshot(String methodId, String screenshotId) throws NarrativeMethodStoreException;
    /////////// [utils] ///////////
    public void dispose() throws NarrativeMethodStoreException;
}
