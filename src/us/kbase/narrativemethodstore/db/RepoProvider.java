package us.kbase.narrativemethodstore.db;

import java.util.List;

import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public interface RepoProvider {
    /////////// [root]  ///////////
    public String getNamespace() throws NarrativeMethodStoreException;
    public String loadKBaseConfig() throws NarrativeMethodStoreException;
    public String loadReadmeFile() throws NarrativeMethodStoreException;
    /////////// data    ///////////
    /////////// docs    ///////////
    /////////// service ///////////
    /////////// test    ///////////
    /////////// ui      ///////////
    public List<String> listUINarrativeMethodIDs() throws NarrativeMethodStoreException;
    public String loadUINarrativeMethodSpec(String methodId) throws NarrativeMethodStoreException;
    public String loadUINarrativeMethodDisplay(String methodId) throws NarrativeMethodStoreException;
    public List<String> listUIWidgetIds() throws NarrativeMethodStoreException;
    public String loadUIWidgetJS(String widgetId) throws NarrativeMethodStoreException;
    /////////// [utils] ///////////
    public void dispose() throws NarrativeMethodStoreException;
}
