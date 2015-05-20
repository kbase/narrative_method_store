package us.kbase.narrativemethodstore.db.github;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

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
import us.kbase.narrativemethodstore.TypeInfo;
import us.kbase.narrativemethodstore.db.DynamicRepoDB;
import us.kbase.narrativemethodstore.db.FileLookup;
import us.kbase.narrativemethodstore.db.MethodSpecDB;
import us.kbase.narrativemethodstore.db.NarrativeAppData;
import us.kbase.narrativemethodstore.db.NarrativeCategoriesIndex;
import us.kbase.narrativemethodstore.db.NarrativeMethodData;
import us.kbase.narrativemethodstore.db.NarrativeTypeData;
import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreInitializationException;

public class LocalGitDB implements MethodSpecDB {

	
	protected final URL gitRepoUrl;
	protected final String gitBranch;
	protected final File gitLocalPath;
	protected final int refreshTimeInMinutes;
	protected final int cacheSize;
	
	protected final ObjectMapper mapper = new ObjectMapper();
	protected final Yaml yaml = new Yaml();
	
	protected long lastPullTime = -1;
	protected String lastCommit = null;
	
	protected NarrativeCategoriesIndex narCatIndex;
	protected final LoadingCache<String, MethodFullInfo> methodFullInfoCache;
	protected final LoadingCache<String, MethodSpec> methodSpecCache;
	protected final LoadingCache<String, AppFullInfo> appFullInfoCache;
	protected final LoadingCache<String, AppSpec> appSpecCache;
	
	protected final DynamicRepoDB dynamicRepos;
	protected final Map<String, String> dynamicMethodToRepoUrl = new TreeMap<String, String>();
	
	public LocalGitDB(URL gitRepoUrl, String branch, File localPath, 
			int refreshTimeInMinutes, int cacheSize, DynamicRepoDB dynamicRepos) 
			        throws NarrativeMethodStoreInitializationException {
		this.gitRepoUrl = gitRepoUrl;
		this.gitBranch = branch;
		this.gitLocalPath = localPath;
		this.refreshTimeInMinutes = refreshTimeInMinutes;
		this.cacheSize = cacheSize;
		this.methodFullInfoCache = CacheBuilder.newBuilder().maximumSize(cacheSize).build(
				new CacheLoader<String, MethodFullInfo>() {
					@Override
					public MethodFullInfo load(String methodId) throws NarrativeMethodStoreException {
						return loadMethodDataUncached(methodId).getMethodFullInfo();
					}
				});
		this.methodSpecCache = CacheBuilder.newBuilder().maximumSize(cacheSize).build(
				new CacheLoader<String, MethodSpec>() {
					@Override
					public MethodSpec load(String methodId) throws NarrativeMethodStoreException {
						return loadMethodDataUncached(methodId).getMethodSpec();
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
        this.dynamicRepos = dynamicRepos;
        if (dynamicRepos != null) {
            try {
                for (String repoUrl : dynamicRepos.listRepoURLs()) {
                    RepoProvider repo = dynamicRepos.getRepoDetails(repoUrl);
                    String namespace = repo.getNamespace();
                    for (String methodId : repo.listUINarrativeMethodIDs()) {
                        dynamicMethodToRepoUrl.put(namespace + methodId, repoUrl);
                    }
                }
            } catch (NarrativeMethodStoreInitializationException ex) {
                throw ex;
            } catch (NarrativeMethodStoreException ex) {
                throw new NarrativeMethodStoreInitializationException(ex);
            }
        }
        try {
            loadCategoriesIndex();
        } catch(NarrativeMethodStoreException e) {
            throw new NarrativeMethodStoreInitializationException(e.getMessage(), e);
        }
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
		if (System.currentTimeMillis() < lastPullTime + refreshTimeInMinutes * 60000)
			return;
		lastPullTime = System.currentTimeMillis();
		try {
			String ret = GitUtils.gitPull(gitLocalPath, gitRepoUrl);
			if (ret != null && ret.startsWith("Already up-to-date."))
				return;
			String commit = GitUtils.getCommitInfo(gitLocalPath, gitRepoUrl);
			if (!commit.equals(lastCommit)) {
				lastCommit = commit;
				System.out.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: refreshing caches");
				// recreate the categories index
				loadCategoriesIndex();
				methodFullInfoCache.invalidateAll();
				methodSpecCache.invalidateAll();
				appFullInfoCache.invalidateAll();
				appSpecCache.invalidateAll();
			}
		} catch (Exception ex) {
			System.err.println("Error doing git pull: " + ex.getMessage());
		}
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

	protected List<String> listMethodIdsUncached() {
		List <String> methodList = new ArrayList<String>();
		if (!getMethodsDir().exists())
			return methodList;
		for (File sub : getMethodsDir().listFiles()) {
			if (sub.isDirectory())
				methodList.add(sub.getName());
		}
		methodList.addAll(dynamicMethodToRepoUrl.keySet());
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

	protected NarrativeMethodData loadMethodDataUncached(final String methodId) throws NarrativeMethodStoreException {
		try {
			// Fetch the resources needed
			JsonNode spec = null;
			Map<String,Object> display = null;
			if (dynamicMethodToRepoUrl.containsKey(methodId)) {
			    String repoUrl = dynamicMethodToRepoUrl.get(methodId);
			    RepoProvider repo = dynamicRepos.getRepoDetails(repoUrl);
			    String namespace = repo.getNamespace();
			    for (String mId : repo.listUINarrativeMethodIDs()) {
			        if (methodId.equals(namespace + mId)) {
		                spec = mapper.readTree(repo.loadUINarrativeMethodSpec(mId));
		                display = getDocumentAsYamlMap(repo.loadUINarrativeMethodDisplay(mId));
			        }
			    }
			    if (spec == null || display == null)
			        throw new NarrativeMethodStoreException("Method " + methodId + " not found in repository: " + repoUrl);
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
	
	/**
	 * Reloads from files the entire categories index
	 */
	protected synchronized void loadCategoriesIndex() throws NarrativeMethodStoreException {
		
		narCatIndex = new NarrativeCategoriesIndex();  // create a new index
		try {
			List<String> catIds = listCategoryIds(); // iterate over each category
			for(String catId : catIds) {
				JsonNode spec = getResourceAsJson("categories/"+catId+"/spec.json");
				//Map<String,Object> display = getResourceAsYamlMap("categories/"+catId+"/display.yaml");
				Map<String,Object> display = null;
				narCatIndex.addOrUpdateCategory(catId, spec, display);
			}
			
			List<String> methIds = listMethodIdsUncached(); // iterate over each category
			for(String mId : methIds) {
				// TODO: check cache for data instead of loading it all directly; Roman: I doubt it's a good 
				// idea to check cache first cause narrative engine more likely loads list of all categories 
				// before any full infos and specs.
				MethodBriefInfo mbi;
				try {
					NarrativeMethodData data = loadMethodDataUncached(mId);
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
		} catch (IOException e) {
			throw new NarrativeMethodStoreException("Cannot load category index : "+e.getMessage(),e);
		}
		
		return;
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
		return getDocumentAsYamlMap(document);
	}

	protected Map<String,Object> getDocumentAsYamlMap(String document) throws IOException {
	    StringBuilder sb = new StringBuilder();
		for (int i = 0; i < document.length(); i++) {
			char ch = document.charAt(i);
			if ((ch < 32 && ch != 10 && ch != 13) || ch >= 127)
				ch = ' ';
			sb.append(ch);
		}
		document = sb.toString();
		@SuppressWarnings("unchecked")
		Map<String,Object> data = (Map<String, Object>) yaml.load(document);
		//System.out.println("fetched yaml ("+url+"):\n"+yaml.dump(data));
		return data;
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
	
}
