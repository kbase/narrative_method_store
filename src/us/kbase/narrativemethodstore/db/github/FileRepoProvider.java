package us.kbase.narrativemethodstore.db.github;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class FileRepoProvider implements RepoProvider {
    protected final File rootDir;
    protected final String moduleName;
    protected String moduleDescription = null;
    protected String serviceLanguage = null;
    
    public FileRepoProvider(File rootDir) throws NarrativeMethodStoreException {
        this.rootDir = rootDir;
        String kbaseConfig = get(new File(rootDir, "kbase.yml"));
        try {
            Map<String,Object> map = YamlUtils.getDocumentAsYamlMap(kbaseConfig);
            moduleName = (String)map.get("module-name");
            if (moduleName == null)
                throw new NarrativeMethodStoreException("module-name is not set in repository configuration");
            moduleDescription = (String)map.get("module-description");
            serviceLanguage = (String)map.get("service-language");
        } catch (IOException ex) {
            throw new NarrativeMethodStoreException(ex);
        }
    }
    
    @Override
    public String getUrl() {
        return rootDir.getAbsolutePath();
    }
    
    @Override
    public String getGitCommitHash() {
        return "";
    }
    
    /////////// [root]  ///////////
    
    @Override
    public String getModuleName() {
        return moduleName;
    }
    
    @Override
    public String getModuleDescription() {
        return moduleDescription;
    }
    
    @Override
    public String getServiceLanguage() {
        return serviceLanguage;
    }
    
    @Override
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

    @Override
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

    @Override
    public String loadUINarrativeMethodSpec(String methodId) throws NarrativeMethodStoreException {
        File ret = new File(getMethodDir(methodId), "spec.json");
        return get(ret);
    }
    
    @Override
    public String loadUINarrativeMethodDisplay(String methodId) throws NarrativeMethodStoreException {
        File ret = new File(getMethodDir(methodId), "display.yaml");
        return get(ret);
    }
    
    @Override
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
    
    @Override
    public String loadUIWidgetJS(String widgetId) throws NarrativeMethodStoreException {
        File ret = new File(getWidgetsDir(), widgetId);
        return get(ret);
    }
    
    /////////// [utils] ///////////
    
    @Override
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
