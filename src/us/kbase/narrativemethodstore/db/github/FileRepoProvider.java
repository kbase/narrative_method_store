package us.kbase.narrativemethodstore.db.github;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import us.kbase.narrativemethodstore.db.FileId;
import us.kbase.narrativemethodstore.db.FilePointer;
import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;
import us.kbase.narrativemethodstore.util.FileUtils;

public class FileRepoProvider implements RepoProvider {
    protected final File rootDir;
    protected URL url;
    protected final String moduleName;
    protected String moduleDescription = null;
    protected String serviceLanguage = null;
    protected String moduleVersion = null;
    protected List<String> owners = null;
    private File repoZipFile = null;

    public FileRepoProvider(File rootDir) throws NarrativeMethodStoreException {
        this(rootDir, null);
    }
    
    public FileRepoProvider(File rootDir, URL url) throws NarrativeMethodStoreException {
        this.rootDir = rootDir;
        this.url = url;
        String source = "kbase.yml";
        File configFile = new File(rootDir, source);
        if (!configFile.exists()) {
            source = "kbase.yaml";
            configFile = new File(rootDir, source);
        }
        String kbaseConfig = get(configFile);
        try {
            Map<String,Object> map = YamlUtils.getDocumentAsYamlMap(kbaseConfig);
            moduleName = YamlUtils.getPropertyNotNull(source, map, "module-name", String.class);
            moduleDescription = YamlUtils.getPropertyOrNull(source, map, "module-description", String.class);
            serviceLanguage = YamlUtils.getPropertyOrNull(source, map, "service-language", String.class);
            moduleVersion = YamlUtils.getPropertyOrNull(source, map, "module-version", String.class);
            owners = Collections.unmodifiableList(YamlUtils.getPropertyNotNull(source, map, "owners", 
                    new TypeReference<List<String>>() {}));
        } catch (NarrativeMethodStoreException ex) {
            dispose();
            throw ex;
        } catch (IOException ex) {
            dispose();
            throw new NarrativeMethodStoreException(ex);
        }
    }
    
    @Override
    public String getUrl() {
        return url == null ? null : ("" + url);
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
    public String getModuleVersion() {
        return moduleVersion;
    }
    
    @Override
    public FilePointer getReadmeFile() throws NarrativeMethodStoreException {
        //return get(new File(rootDir, "README.md"), true);
        return fp(new File(rootDir, "README.md"));
    }

    @Override
    public List<String> listOwners() {
        return owners;
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

    protected File getImgDir(String methodId) throws NarrativeMethodStoreException {
        return new File(getMethodDir(methodId), "img");
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
    public FilePointer getUINarrativeMethodSpec(String methodId) throws NarrativeMethodStoreException {
        return fp(new File(getMethodDir(methodId), "spec.json"));
    }
    
    @Override
    public FilePointer getUINarrativeMethodDisplay(String methodId) throws NarrativeMethodStoreException {
        return fp(new File(getMethodDir(methodId), "display.yaml"));
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
    public FilePointer getUIWidgetJS(String widgetId) throws NarrativeMethodStoreException {
        return fp(new File(getWidgetsDir(), widgetId));
    }
    
    
    @Override
    public List<String> listScreenshotIDs(String methodId)
            throws NarrativeMethodStoreException {
        List <String> ret = new ArrayList<String>();
        if (!getImgDir(methodId).exists())
            return ret;
        for (File sub : getImgDir(methodId).listFiles()) {
            if (sub.isFile() && isScreenshot(sub.getName()))
                ret.add(sub.getName());
        }
        return ret;
    }
    
    private static boolean isScreenshot(String fileName) {
        int dotPos = fileName.lastIndexOf('.');
        if (dotPos <= 0)
            return false;
        String ext = fileName.substring(dotPos + 1).toLowerCase();
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") ||
                ext.equals("bmp") || ext.equals("gif") || ext.equals("tiff") ||
                ext.equals("svg");
    }
    
    @Override
    public FilePointer getScreenshot(String methodId, String screenshotId)
            throws NarrativeMethodStoreException {
        return fp(new File(getImgDir(methodId), screenshotId));
    }
    /////////// [utils] ///////////
    
    @Override
    public void dispose() throws NarrativeMethodStoreException {
        if (repoZipFile != null && repoZipFile.exists())
            repoZipFile.delete();
    }

    @Override
    public FilePointer getRepoZip() throws NarrativeMethodStoreException {
        if (repoZipFile == null) {
            try {
                repoZipFile = File.createTempFile("repo_", ".zip", rootDir);
                FileUtils.zip(rootDir, new FileOutputStream(repoZipFile), false, 
                        new HashSet<String>(Arrays.asList(repoZipFile.getName())));
            } catch (IOException ex) {
                throw new NarrativeMethodStoreException(ex);
            }
        }
        return fp(repoZipFile);
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
    
    private FilePointer fp(File f) {
        return new DiskFilePointer(f);
    }
    
    public static class DiskFilePointer implements FilePointer {
        private final File file;
        
        public DiskFilePointer(File f) {
            this.file = f;
        }
        
        @Override
        public FileId getFileId() {
            return null;
        }
        
        @Override
        public File getFile() {
            return file;
        }
        
        @Override
        public String getName() {
            return file.getName();
        }
        
        @Override
        public long length() {
            return file.length();
        }
        
        @Override
        public void saveToStream(OutputStream os)
                throws NarrativeMethodStoreException {
            try {
                InputStream input = new FileInputStream(file);
                IOUtils.copy(input, os);
                input.close();
            } catch (IOException ex) {
                throw new NarrativeMethodStoreException(ex);
            }
        }
    }
}
