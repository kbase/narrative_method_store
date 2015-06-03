package us.kbase.narrativemethodstore.db.github;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import us.kbase.narrativemethodstore.AppBriefInfo;
import us.kbase.narrativemethodstore.AppFullInfo;
import us.kbase.narrativemethodstore.AppSpec;
import us.kbase.narrativemethodstore.MethodBriefInfo;
import us.kbase.narrativemethodstore.MethodFullInfo;
import us.kbase.narrativemethodstore.MethodSpec;
import us.kbase.narrativemethodstore.RepoDetails;
import us.kbase.narrativemethodstore.TypeInfo;
import us.kbase.narrativemethodstore.db.DynamicRepoDB;
import us.kbase.narrativemethodstore.db.FileLookup;
import us.kbase.narrativemethodstore.db.FilePointer;
import us.kbase.narrativemethodstore.db.MethodSpecDB;
import us.kbase.narrativemethodstore.db.NarrativeAppData;
import us.kbase.narrativemethodstore.db.NarrativeCategoriesIndex;
import us.kbase.narrativemethodstore.db.NarrativeMethodData;
import us.kbase.narrativemethodstore.db.NarrativeTypeData;
import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.db.DynamicRepoDB.RepoState;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreInitializationException;

public class LocalGitDB implements MethodSpecDB {

	
	protected final URL gitRepoUrl;
	protected final String gitBranch;
	protected final File gitLocalPath;
	protected final int refreshTimeInMinutes;
	protected final int cacheSize;
	
	protected final ObjectMapper mapper = new ObjectMapper();
	
	protected long lastPullTime = -1;
	protected String lastCommit = null;
	
	protected NarrativeCategoriesIndex narCatIndex;
	protected final LoadingCache<String, MethodFullInfo> methodFullInfoCache;
	protected final LoadingCache<String, MethodSpec> methodSpecCache;
	protected final LoadingCache<String, AppFullInfo> appFullInfoCache;
	protected final LoadingCache<String, AppSpec> appSpecCache;
	
	protected final File tempDir;
	protected final DynamicRepoDB dynamicRepos;
	
	public LocalGitDB(URL gitRepoUrl, String branch, File localPath, int refreshTimeInMinutes, 
	        int cacheSize, DynamicRepoDB dynamicRepos, File tempDir) throws NarrativeMethodStoreInitializationException {
		this.gitRepoUrl = gitRepoUrl;
		this.gitBranch = branch;
		this.gitLocalPath = localPath;
		this.refreshTimeInMinutes = refreshTimeInMinutes;
		this.cacheSize = cacheSize;
		this.methodFullInfoCache = CacheBuilder.newBuilder().maximumSize(cacheSize).build(
				new CacheLoader<String, MethodFullInfo>() {
					@Override
					public MethodFullInfo load(String methodId) throws NarrativeMethodStoreException {
						return loadMethodDataUncached(methodId, narCatIndex).getMethodFullInfo();
					}
				});
		this.methodSpecCache = CacheBuilder.newBuilder().maximumSize(cacheSize).build(
				new CacheLoader<String, MethodSpec>() {
					@Override
					public MethodSpec load(String methodId) throws NarrativeMethodStoreException {
						return loadMethodDataUncached(methodId, narCatIndex).getMethodSpec();
					}
				});
		this.appFullInfoCache = CacheBuilder.newBuilder().maximumSize(cacheSize).build(
				new CacheLoader<String, AppFullInfo>() {
					@Override
					public AppFullInfo load(String methodId) throws NarrativeMethodStoreException {
						return loadAppDataUncached(methodId).getAppFullInfo();
					}
				});
		this.appSpecCache = CacheBuilder.newBuilder().maximumSize(cacheSize).build(
				new CacheLoader<String, AppSpec>() {
					@Override
					public AppSpec load(String methodId) throws NarrativeMethodStoreException {
						return loadAppDataUncached(methodId).getAppSpec();
					}
				});
		if (!localPath.exists())
			localPath.mkdirs();
		initializeLocalRepo();
		this.tempDir = tempDir;
        this.dynamicRepos = dynamicRepos;
        try {
            loadCategoriesIndex();
        } catch (NarrativeMethodStoreInitializationException ex) {
            throw ex;
        } catch(NarrativeMethodStoreException e) {
            throw new NarrativeMethodStoreInitializationException(e.getMessage(), e);
        }
	}
		
	public DynamicRepoDB getDynamicRepos() {
        return dynamicRepos;
    }
	
	protected void initializeLocalRepo() throws NarrativeMethodStoreInitializationException {
		try {
			FileUtils.deleteDirectory(gitLocalPath);
		} catch (IOException e) {
			throw new NarrativeMethodStoreInitializationException("Cannot clone "+gitRepoUrl+", error deleting old directory: " + e.getMessage(), e);
		}
		String cloneStatus = GitUtils.gitClone(gitRepoUrl, gitBranch, gitLocalPath);
		this.lastPullTime = System.currentTimeMillis();
		this.lastCommit = GitUtils.getCommitInfo(gitLocalPath, gitRepoUrl);
		System.out.println(cloneStatus);
	}
	
	/**
	 * We need to call this method at the beginning of every public access method.
	 * This method refreshes file copy of specs-repo if it's necessary and clear
	 * caches in case something was changed.
	 */
	protected synchronized void checkForChanges() {
		if ((!narCatIndex.isInvalid()) && 
		        System.currentTimeMillis() < lastPullTime + refreshTimeInMinutes * 60000)
			return;
		lastPullTime = System.currentTimeMillis();
		try {
		    boolean needReload = narCatIndex.isInvalid();
		    String ret = GitUtils.gitPull(gitLocalPath, gitRepoUrl);
		    if (ret == null || !ret.startsWith("Already up-to-date.")) {
		        String commit = GitUtils.getCommitInfo(gitLocalPath, gitRepoUrl);
		        if (!commit.equals(lastCommit))
		            needReload = true;
	            lastCommit = commit;
		    }
		    if (needReload)
		        reloadAll();
		} catch (Exception ex) {
			System.err.println("Error checking for changed:");
			ex.printStackTrace();
		}
	}

    public void reloadAll() throws NarrativeMethodStoreException {
        System.out.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: refreshing caches");
        // recreate the categories index
        loadCategoriesIndex();
        methodFullInfoCache.invalidateAll();
        methodSpecCache.invalidateAll();
        appFullInfoCache.invalidateAll();
        appSpecCache.invalidateAll();
    }	
	
	protected File getMethodsDir() {
		return new File(gitLocalPath, "methods");
	}

	protected File getCategoriesDir() {
		return new File(gitLocalPath, "categories");
	}

	protected File getAppsDir() {
		return new File(gitLocalPath, "apps");
	}

	protected File getTypesDir() {
		return new File(gitLocalPath, "types");
	}

	protected File getRepositoriesFile() {
	    return new File(gitLocalPath, "repositories");
	}

	protected List<String> listMethodIdsUncached(NarrativeCategoriesIndex narCatIndex) {
		List <String> methodList = new ArrayList<String>();
		if (!getMethodsDir().exists())
			return methodList;
		for (File sub : getMethodsDir().listFiles()) {
			if (sub.isDirectory())
				methodList.add(sub.getName());
		}
		methodList.addAll(narCatIndex.getDynamicRepoMethods());
		return methodList;
	}

	protected List<String> listAppIdsUncached() {
		List <String> appList = new ArrayList<String>();
		if (!getAppsDir().exists())
			return appList;
		for (File sub : getAppsDir().listFiles()) {
			if (sub.isDirectory())
				appList.add(sub.getName());
		}
		return appList;
	}

	protected List<String> listTypeNamesUncached() {
		List<String> ret = new ArrayList<String>();
		if (!getTypesDir().exists())
			return ret;
		for (File sub : getTypesDir().listFiles()) {
			if (sub.isDirectory())
				ret.add(sub.getName());
		}
		return ret;
	}

	public String getCommitInfo() throws NarrativeMethodStoreInitializationException {
	    return GitUtils.getCommitInfo(gitLocalPath, gitRepoUrl);
	}
	
	@Override
	public List<String> listMethodIds(boolean withErrors) {
		checkForChanges();
		List<String> ret = new ArrayList<String>();
		for (Map.Entry<String, MethodBriefInfo> entry : narCatIndex.getMethods().entrySet()) {
			if (entry.getValue().getLoadingError() != null && !withErrors)
				continue;
			ret.add(entry.getKey());
		}
		return ret;
	}

	public List<String> listAppIds(boolean withErrors) {
		checkForChanges();
		List<String> ret = new ArrayList<String>();
		for (Map.Entry<String, AppBriefInfo> entry : narCatIndex.getApps().entrySet()) {
			if (entry.getValue().getLoadingError() != null && !withErrors)
				continue;
			ret.add(entry.getKey());
		}
		return ret;
	}

	private String asText(FilePointer fp) throws NarrativeMethodStoreException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    fp.saveToStream(baos);
	    return new String(baos.toByteArray(), Charset.forName("utf-8"));
	}
	
	protected NarrativeMethodData loadMethodDataUncached(final String methodId,
	        NarrativeCategoriesIndex narCatIndex) throws NarrativeMethodStoreException {
		try {
			// Fetch the resources needed
			JsonNode spec = null;
			Map<String,Object> display = null;
			if (narCatIndex.getDynamicRepoMethods().contains(methodId)) {
			    String[] moduleNameAndMethodId = methodId.split("/");
			    RepoProvider repo = dynamicRepos.getRepoDetails(moduleNameAndMethodId[0]);
			    
			    spec = mapper.readTree(asText(repo.getUINarrativeMethodSpec(moduleNameAndMethodId[1])));
			    display = YamlUtils.getDocumentAsYamlMap(asText(repo.getUINarrativeMethodDisplay(moduleNameAndMethodId[1])));
			} else {
			    spec = getResourceAsJson("methods/"+methodId+"/spec.json");
			    display = getResourceAsYamlMap("methods/"+methodId+"/display.yaml");
			}

			// Initialize the actual data
			NarrativeMethodData data = new NarrativeMethodData(methodId, spec, display,
					createFileLookup(new File(getMethodsDir(), methodId)));
			return data;
		} catch (NarrativeMethodStoreException ex) {
			throw ex;
		} catch (Exception ex) {
			NarrativeMethodStoreException ret = new NarrativeMethodStoreException(ex);
			ret.setErrorMethod(new MethodBriefInfo().withCategories(Arrays.asList("error"))
					.withId(methodId).withName(methodId));
			throw ret;
		}
	}

	protected FileLookup createFileLookup(final File dir) {
		return new FileLookup() {
			@Override
			public String loadFileContent(String fileName) {
				File f = new File(dir, fileName);
				if (f.exists())
					try {
						return get(f);
					} catch (IOException ignore) {}
				return null;
			}
		};
	}

	protected NarrativeAppData loadAppDataUncached(final String appId) throws NarrativeMethodStoreException {
		try {
			// Fetch the resources needed
			JsonNode spec = getResourceAsJson("apps/"+appId+"/spec.json");
			Map<String,Object> display = getResourceAsYamlMap("apps/"+appId+"/display.yaml");

			// Initialize the actual data
			NarrativeAppData data = new NarrativeAppData(appId, spec, display,
					createFileLookup(new File(getAppsDir(), appId)));
			return data;
		} catch (NarrativeMethodStoreException ex) {
			throw ex;
		} catch (Exception ex) {
			NarrativeMethodStoreException ret = new NarrativeMethodStoreException(ex);
			ret.setErrorApp(new AppBriefInfo().withCategories(Arrays.asList("error"))
					.withId(appId).withName(appId));
			throw ret;
		}
	}

	protected NarrativeTypeData loadTypeDataUncached(final String typeName) throws NarrativeMethodStoreException {
		try {
			// Fetch the resources needed
			JsonNode spec = getResourceAsJson("types/"+typeName+"/spec.json");
			Map<String,Object> display = getResourceAsYamlMap("types/"+typeName+"/display.yaml");

			// Initialize the actual data
			NarrativeTypeData data = new NarrativeTypeData(typeName, spec, display,
					createFileLookup(new File(getTypesDir(), typeName)));
			return data;
		} catch (NarrativeMethodStoreException ex) {
			throw ex;
		} catch (Exception ex) {
			NarrativeMethodStoreException ret = new NarrativeMethodStoreException(ex);
			ret.setErrorType(new TypeInfo().withTypeName(typeName).withName(typeName));
			throw ret;
		}
	}

	@Override
	public MethodBriefInfo getMethodBriefInfo(String methodId)
			throws NarrativeMethodStoreException {
		checkForChanges();
		return narCatIndex.getMethods().get(methodId);
	}

	public AppBriefInfo getAppBriefInfo(String appId)
			throws NarrativeMethodStoreException {
		checkForChanges();
		return narCatIndex.getApps().get(appId);
	}

	public TypeInfo getTypeInfo(String typeName)
			throws NarrativeMethodStoreException {
		checkForChanges();
		return narCatIndex.getTypes().get(typeName);
	}

	@Override
	public MethodFullInfo getMethodFullInfo(String methodId)
			throws NarrativeMethodStoreException {
		checkForChanges();
		try {
			return methodFullInfoCache.get(methodId);
		} catch (ExecutionException e) {
			if (e.getCause() != null && e.getCause() instanceof NarrativeMethodStoreException)
				throw (NarrativeMethodStoreException)e.getCause();
			throw new NarrativeMethodStoreException("Error loading full info for method id=" + methodId + " (" + e.getMessage() + ")", e);
		}
	}

	public AppFullInfo getAppFullInfo(String appId)
			throws NarrativeMethodStoreException {
		checkForChanges();
		try {
			return appFullInfoCache.get(appId);
		} catch (ExecutionException e) {
			if (e.getCause() != null && e.getCause() instanceof NarrativeMethodStoreException)
				throw (NarrativeMethodStoreException)e.getCause();
			throw new NarrativeMethodStoreException("Error loading full info for app id=" + appId + " (" + e.getMessage() + ")", e);
		}
	}

	@Override
	public MethodSpec getMethodSpec(String methodId)
			throws NarrativeMethodStoreException {
		checkForChanges();
		try {
			return methodSpecCache.get(methodId);
		} catch (ExecutionException e) {
			if (e.getCause() != null && e.getCause() instanceof NarrativeMethodStoreException)
				throw (NarrativeMethodStoreException)e.getCause();
			throw new NarrativeMethodStoreException("Error loading full info for method id=" + methodId + " (" + e.getMessage() + ")", e);
		}
	}

	public AppSpec getAppSpec(String appId)
			throws NarrativeMethodStoreException {
		checkForChanges();
		try {
			return appSpecCache.get(appId);
		} catch (ExecutionException e) {
			if (e.getCause() != null && e.getCause() instanceof NarrativeMethodStoreException)
				throw (NarrativeMethodStoreException)e.getCause();
			throw new NarrativeMethodStoreException("Error loading full info for app id=" + appId + " (" + e.getMessage() + ")", e);
		}
	}

	public List<String> listCategoryIds() throws NarrativeMethodStoreException {
		checkForChanges();
		return listCategoryIdsUncached();
	}
	
	protected List<String> listCategoryIdsUncached() throws NarrativeMethodStoreException {
		List <String> catList = new ArrayList<String>();
		for (File sub : getCategoriesDir().listFiles()) {
			if (sub.isDirectory())
				catList.add(sub.getName());
		}
		return catList;
	}
	
	
	public NarrativeCategoriesIndex getCategoriesIndex() {
		checkForChanges();
		return narCatIndex;
	}
	
	private File getTempDir() {
	    return tempDir == null ? new File(".") : tempDir;
	}
	
	/**
	 * Reloads from files the entire categories index
	 */
	protected synchronized void loadCategoriesIndex() throws NarrativeMethodStoreException {
	    Set<String> dynamicRepoMethods = new TreeSet<String>();
	    Map<String, Exception> dynamicRepoModuleNameToLoadingError = new TreeMap<String, Exception>();
        if (dynamicRepos != null) {
            File f = getRepositoriesFile();
            if (f.exists()) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    try {
                        while (true) {
                            String l = br.readLine();
                            if (l == null)
                                break;
                            String[] parts = l.trim().split("\\s+");
                            if (parts.length < 1)
                                continue;
                            String url = parts[0];
                            RepoProvider pvd = null;
                            try {
                                pvd = new GitHubRepoProvider(new URL(url), getTempDir());
                                String repoModuleName = pvd.getModuleName();
                                boolean newReg = !dynamicRepos.isRepoRegistered(repoModuleName, true);
                                List<String> owners = pvd.listOwners();
                                if (owners.isEmpty())
                                    throw new NarrativeMethodStoreException("Lists of owners is empty for " +
                                            "repository " + repoModuleName);
                                String owner = owners.get(0);
                                if (newReg) {
                                    dynamicRepos.registerRepo(owner, pvd);
                                } else {
                                    String oldCommitHash = dynamicRepos.getRepoDetails(repoModuleName).getGitCommitHash();
                                    if (!oldCommitHash.equals(pvd.getGitCommitHash()))
                                        dynamicRepos.registerRepo(owner, pvd);
                                }
                            } finally {
                                if (pvd != null)
                                    pvd.dispose();
                            }
                        }
                    } finally {
                        br.close();
                    }
                } catch (IOException ex) {
                    throw new NarrativeMethodStoreException(ex);
                }
            }
            for (String repoMN : dynamicRepos.listRepoModuleNames(false)) {
                try {
                    RepoProvider repo = dynamicRepos.getRepoDetails(repoMN);
                    for (String methodId : repo.listUINarrativeMethodIDs()) {
                        dynamicRepoMethods.add(getFullMethodName(repoMN, methodId));
                    }
                } catch (Exception ex) {
                    dynamicRepoModuleNameToLoadingError.put(repoMN, ex);
                }
            }
        }

        NarrativeCategoriesIndex narCatIndex = new NarrativeCategoriesIndex();  // create a new index
        narCatIndex.updateAllDynamicRepoMethods(dynamicRepoMethods, dynamicRepoModuleNameToLoadingError);
		try {
			List<String> catIds = listCategoryIdsUncached(); // iterate over each category
			for(String catId : catIds) {
				JsonNode spec = getResourceAsJson("categories/"+catId+"/spec.json");
				//Map<String,Object> display = getResourceAsYamlMap("categories/"+catId+"/display.yaml");
				Map<String,Object> display = null;
				narCatIndex.addOrUpdateCategory(catId, spec, display);
			}
			
			List<String> methIds = listMethodIdsUncached(narCatIndex); // iterate over each category
			for(String mId : methIds) {
				// TODO: check cache for data instead of loading it all directly; Roman: I doubt it's a good 
				// idea to check cache first cause narrative engine more likely loads list of all categories 
				// before any full infos and specs.
				MethodBriefInfo mbi;
				try {
					NarrativeMethodData data = loadMethodDataUncached(mId, narCatIndex);
					mbi = data.getMethodBriefInfo();
				} catch (NarrativeMethodStoreException ex) {
					mbi = ex.getErrorMethod();
				}
				narCatIndex.addOrUpdateMethod(mId, mbi);
			}

			List<String> appIds = listAppIdsUncached(); // iterate over each category
			for(String appId : appIds) {
				AppBriefInfo abi;
				try {
					NarrativeAppData data = loadAppDataUncached(appId);
					abi = data.getAppBriefInfo();
				} catch (NarrativeMethodStoreException ex) {
					abi = ex.getErrorApp();
				}
				narCatIndex.addOrUpdateApp(appId, abi);
			}

			List<String> typeNames = listTypeNamesUncached(); // iterate over each category
			for(String typeName : typeNames) {
				TypeInfo ti;
				try {
					NarrativeTypeData data = loadTypeDataUncached(typeName);
					ti = data.getTypeInfo();
				} catch (NarrativeMethodStoreException ex) {
					ti = ex.getErrorType();
				}
				narCatIndex.addOrUpdateType(typeName, ti);
			}
			this.narCatIndex = narCatIndex;
		} catch (IOException e) {
			throw new NarrativeMethodStoreException("Cannot load category index : "+e.getMessage(),e);
		}
		
		return;
	}

    public String getFullMethodName(String repoModuleName, String shortMethodId) {
        return repoModuleName + "/" + shortMethodId;
    }
	
	protected JsonNode getResourceAsJson(String path) throws JsonProcessingException, IOException {
		File f = new File(gitLocalPath, path);
		return getAsJson(f);
	}
	
	protected String getResource(String path) throws IOException {
		File f = new File(gitLocalPath, path);
		return get(f);
	}
	
	protected Map<String,Object> getResourceAsYamlMap(String path) throws IOException {
		File f = new File(gitLocalPath, path);
		String document = get(f);
		return YamlUtils.getDocumentAsYamlMap(document);
	}

	protected JsonNode getAsJson(File f) throws JsonProcessingException, IOException {
		return mapper.readTree(get(f));
	}
	
	protected String get(URL url) throws IOException {
		return get(url.openStream());
	}
	
	protected String get(File f) throws IOException {
		return get(new FileInputStream(f));
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

	private boolean bool(Long value) {
	    return bool(value, false);
	}
	
	private boolean bool(Long value, boolean defaultValue) {
	    return value == null ? defaultValue : (((long)value) != 0L);
	}
	
	public long isRepoRegistered(String moduleName, Long withDisabled) 
	        throws NarrativeMethodStoreException {
        boolean found = dynamicRepos.isRepoRegistered(moduleName, bool(withDisabled));
        return found ? 1L : 0L;
	}

	public long registerRepo(String userId, String url) throws NarrativeMethodStoreException {
	    RepoProvider pvd = null;
	    try {
	        pvd = new GitHubRepoProvider(new URL(url), getTempDir());
	        dynamicRepos.registerRepo(userId, pvd);
	        return dynamicRepos.getRepoLastVersion(pvd.getModuleName());
	    } catch (MalformedURLException ex) {
	        throw new NarrativeMethodStoreException("Error parsing repository url: " + 
	                url + " (" + ex.getMessage() + ")", ex);
	    } finally {
	        if (pvd != null)
	            pvd.dispose();
	    }
	}
	
	private void checkIfRepoDisabled(String moduleName, Long withDisabled) 
	        throws NarrativeMethodStoreException {
	    if (bool(withDisabled))
	        return;
	    if (dynamicRepos.getRepoState(moduleName) == RepoState.disabled)
	        throw new NarrativeMethodStoreException("Repository " + moduleName + " is disabled");
	}
	
	public long getRepoLastVersion(String moduleName, Long withDisabled)
	        throws NarrativeMethodStoreException {
	    checkIfRepoDisabled(moduleName, withDisabled);
	    return dynamicRepos.getRepoLastVersion(moduleName);
	}

	public List<String> listRepoModuleNames(Long withDisabled) 
	        throws NarrativeMethodStoreException {
	    return dynamicRepos.listRepoModuleNames(bool(withDisabled));
	}

	public RepoProvider getRepoProvider(String moduleName, Long version, Long withDisabled)
	        throws NarrativeMethodStoreException {
        checkIfRepoDisabled(moduleName, withDisabled);
        if (version == null) {
            return dynamicRepos.getRepoDetails(moduleName);
        } else {
            return dynamicRepos.getRepoDetailsHistory(moduleName, version);
        }
	}

	public RepoDetails getRepoDetails(String moduleName, Long version, Long withDisabled)
	        throws NarrativeMethodStoreException {
	    RepoProvider repo = getRepoProvider(moduleName, version, withDisabled);
        String repoModuleName = repo.getModuleName();
        List<String> methodIds = new ArrayList<String>();
        for (String shortId : repo.listUINarrativeMethodIDs())
            methodIds.add(getFullMethodName(repoModuleName, shortId));
        return new RepoDetails().withModuleName(repoModuleName)
                .withModuleDescription(repo.getModuleDescription())
                .withServiceLanguage(repo.getServiceLanguage())
                .withGitUrl(repo.getUrl())
                .withGitCommitHash(repo.getGitCommitHash())
                .withOwners(repo.listOwners())
                .withReadme(asText(repo.getReadmeFile()))
                .withMethodIds(methodIds)
                .withWidgetIds(repo.listUIWidgetIds());

	}
	
	public List<Long> listRepoVersions(String moduleName, Long withDisabled) 
	        throws NarrativeMethodStoreException {
	    checkIfRepoDisabled(moduleName, withDisabled);
	    return dynamicRepos.listRepoVersions(moduleName);
	}
	
	public String loadWidgetJavaScript(String moduleName, Long version, String widgetId)
	        throws NarrativeMethodStoreException {
	    RepoProvider repo = getRepoProvider(moduleName, version, null);
	    return asText(repo.getUIWidgetJS(widgetId));
	}
	
	public void setRepoState(String userId, String moduleName, String repoState)
	        throws NarrativeMethodStoreException {
	    dynamicRepos.setRepoState(userId, moduleName, RepoState.valueOf(repoState));
	}
	
	public String getRepoState(String moduleName) throws NarrativeMethodStoreException {
	    return dynamicRepos.getRepoState(moduleName).name();
	}
	
	public void saveScreenshotIntoStream(String moduleName, String methodId, 
	        String screenshotId, OutputStream os) throws NarrativeMethodStoreException {
	    dynamicRepos.getRepoDetails(moduleName).getScreenshot(methodId, 
	            screenshotId).saveToStream(os);
	}
}
