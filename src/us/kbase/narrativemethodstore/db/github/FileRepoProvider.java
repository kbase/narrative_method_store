package us.kbase.narrativemethodstore.db.github;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class FileRepoProvider implements RepoProvider {
    protected File rootDir;
    
    public FileRepoProvider(File rootDir) {
        this.rootDir = rootDir;
    }
    
    /////////// [root]  ///////////
    
    @Override
    public String getNamespace() throws NarrativeMethodStoreException {
        return rootDir.getName();
    }
    
    public String loadKBaseConfig() throws NarrativeMethodStoreException {
        return get(new File(rootDir, "kbase.yml"));
    }
    public String loadReadmeFile() throws NarrativeMethodStoreException {
        return get(new File(rootDir, "README.md"), true);
    }
    
    /////////// data    ///////////
    
    /////////// docs    ///////////
    
    /////////// service ///////////
    
    /////////// test    ///////////
    
    /////////// ui      ///////////
    
    protected File getUIDir() {
        return new File(rootDir, "ui");
    }

    protected File getNarrativeDir() {
        return new File(getUIDir(), "narrative");
    }
    
    protected File getMethodsDir() {
        return new File(getNarrativeDir(), "methods");
    }

    protected File getWidgetsDir() {
        return new File(getUIDir(), "widgets");
    }

    public List<String> listUINarrativeMethodIDs() throws NarrativeMethodStoreException {
        List <String> methodList = new ArrayList<String>();
        if (!getMethodsDir().exists())
            return methodList;
        for (File sub : getMethodsDir().listFiles()) {
            if (sub.isDirectory())
                methodList.add(sub.getName());
        }
        return methodList;
    }

    protected File getMethodDir(String methodId) throws NarrativeMethodStoreException {
        File dir = new File(getMethodsDir(), methodId);
        if (!dir.exists())
            throw new NarrativeMethodStoreException("Method not found: " + methodId);
        return dir;
    }

    public String loadUINarrativeMethodSpec(String methodId) throws NarrativeMethodStoreException {
        File ret = new File(getMethodDir(methodId), "spec.json");
        return get(ret);
    }
    
    public String loadUINarrativeMethodDisplay(String methodId) throws NarrativeMethodStoreException {
        File ret = new File(getMethodDir(methodId), "display.yaml");
        return get(ret);
    }
    
    public List<String> listUIWidgetIds() throws NarrativeMethodStoreException {
        List <String> ret = new ArrayList<String>();
        if (!getWidgetsDir().exists())
            return ret;
        for (File sub : getWidgetsDir().listFiles()) {
            if (sub.isFile())
                ret.add(sub.getName());
        }
        return ret;
    }
    
    public String loadUIWidgetJS(String widgetId) throws NarrativeMethodStoreException {
        File ret = new File(getWidgetsDir(), widgetId);
        return get(ret);
    }
    
    /////////// [utils] ///////////
    
    public void dispose() throws NarrativeMethodStoreException {
        // Do nothing
    }

    protected String get(File f) throws NarrativeMethodStoreException {
        return get(f, false);
    }
    
    protected String get(File f, boolean optional) throws NarrativeMethodStoreException {
        if (!f.exists()) {
            if (optional)
                return null;
            throw new NarrativeMethodStoreException("File doesn't exist: " + f);
        }
        try {
            return get(new FileInputStream(f));
        } catch (IOException ex) {
            throw new NarrativeMethodStoreException("Error reading file [" + f + "] (" + ex.getMessage() + ")", ex);
        }
    }
    
    protected String get(InputStream is) throws IOException {
        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = in.readLine()) != null) 
            response.append(line+"\n");
        in.close();
        return response.toString();
    }

}
