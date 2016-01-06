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
import us.kbase.narrativemethodstore.db.FileLookup;
import us.kbase.narrativemethodstore.db.MethodSpecDB;
import us.kbase.narrativemethodstore.db.NarrativeAppData;
import us.kbase.narrativemethodstore.db.NarrativeCategoriesIndex;
import us.kbase.narrativemethodstore.db.NarrativeMethodData;
import us.kbase.narrativemethodstore.db.NarrativeTypeData;
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
	
	protected String lastCommit = null;
	
	protected NarrativeCategoriesIndex narCatIndex;
	protected final LoadingCache<String, MethodFullInfo> methodFullInfoCache;
	protected final LoadingCache<String, MethodSpec> methodSpecCache;
	protected final LoadingCache<String, AppFullInfo> appFullInfoCache;
	protected final LoadingCache<String, AppSpec> appSpecCache;
	protected static Thread refreshingThread = null;
    protected boolean inGitFetch = false;
    protected boolean gitMergeWasDoneAfterFetch = false;
	protected boolean needToStopRefreshingThread = false;
	
	public LocalGitDB(URL gitRepoUrl, String branch, File localPath, 
			int refreshTimeInMinutes, int cacheSize) throws NarrativeMethodStoreInitializationException {
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
		String cloneStatus = gitClone();
		this.lastCommit = getCommitInfo();
		System.out.println(cloneStatus);
		try {
		    gitPull();
		} catch (Exception ex) {
            System.err.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: " + ex.getMessage());
		}
		startRefreshingThread();
	}

	/**
	 * Clones the configured git repo to the target local file location, returns standard output of the command
	 * if successful, otherwise throws an exception.
	 */
	protected String gitClone() throws NarrativeMethodStoreInitializationException {
		try {
			return gitCommand("git clone --branch "+gitBranch+" "+gitRepoUrl+" "+gitLocalPath.getAbsolutePath(), 
					"clone", gitLocalPath.getCanonicalFile().getParentFile());
		} catch (IOException e) {
			throw new NarrativeMethodStoreInitializationException("Cannot clone "+gitRepoUrl+": " + e.getMessage(), e);
		}
	}
	
	/**
	 * Runs a git pull on the local git spec repo.
	 */
	protected String gitPull() throws NarrativeMethodStoreInitializationException {
		return gitCommand("git pull", "pull", gitLocalPath);
	}

	/**
     * Runs a git fetch on the local git spec repo.
     */
    protected String gitFetch() throws NarrativeMethodStoreInitializationException {
        return gitCommand("git fetch origin " + gitBranch, "fetch", gitLocalPath);
    }

    /**
     * Runs a git merge FETCH_HEAD on the local git spec repo.
     */
    protected String gitMergeFetchHead() throws NarrativeMethodStoreInitializationException {
        return gitCommand("git merge FETCH_HEAD", "merge FETCH_HEAD", gitLocalPath);
    }

	protected String gitCommand(String fullCmd, String nameOfCmd, File curDir) throws NarrativeMethodStoreInitializationException {
		try {
			Process p = Runtime.getRuntime().exec(fullCmd, null, curDir);
			
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			StringBuilder out = new StringBuilder();
			StringBuilder error = new StringBuilder();
			Thread outT = readInThread(stdOut, out);
			Thread errT = readInThread(stdError, error);
			
			outT.join();
			errT.join();
			int exitcode = p.waitFor();
			
			if(exitcode!=0) {
				throw new NarrativeMethodStoreInitializationException("Cannot " + nameOfCmd + " "+gitRepoUrl+": " + error);
			}
			return out.toString();
		} catch (Exception e) {
			throw new NarrativeMethodStoreInitializationException("Cannot " + nameOfCmd + " "+gitRepoUrl+": " + e.getMessage(), e);
		}
	}

	private Thread readInThread(final BufferedReader stdOut, final StringBuilder out) {
		Thread ret = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String s1 = null;
					while ((s1 = stdOut.readLine()) != null) { out.append(s1+"\n"); }
				} catch (IOException ex) {
					throw new IllegalStateException(ex);
				}
			}
		});
		ret.start();
		return ret;
	}
	
	public void stopRefreshingThread() {
	    needToStopRefreshingThread = true;
        System.out.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: refreshing thread was requested to stop");
	    try {
	        if (refreshingThread != null)
	            refreshingThread.interrupt();
	    } catch (Exception ex) {
            System.out.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: error interrupting refreshing thread (" + ex.getMessage() + ")");
	    }
	}
	
	private void startRefreshingThread() {
	    if (refreshingThread != null) {
            System.out.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: refreshing thread was already started earlier");
	        return;
	    }
	    refreshingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: refreshing thread is starting");
                while (true) {
                    inGitFetch = true;
                    gitMergeWasDoneAfterFetch = false;
                    try {
                        gitFetch();
                        inGitFetch = false;
                        checkForChanges();
                    } catch (Throwable ex) {
                        inGitFetch = false;
                        System.err.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: error doing git fetch: " + ex.getMessage());
                    }
                    if (needToStopRefreshingThread)
                        break;
                    try {
                        Thread.sleep(refreshTimeInMinutes * 60000);
                    } catch (Throwable ex) {
                        System.err.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: error waiting: " + ex.getMessage());
                    }
                    if (needToStopRefreshingThread)
                        break;
                }
                System.out.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: refreshing thread is stoping");
                refreshingThread = null;
            }
        });
	    refreshingThread.start();
	}
	
	/**
	 * We need to call this method at the beginning of every public access method.
	 * This method refreshes file copy of specs-repo if it's necessary and clear
	 * caches in case something was changed.
	 */
	public synchronized void checkForChanges() {
	    if (refreshingThread == null) {
            System.out.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: refreshing thread wasn't started for some reason");
	        startRefreshingThread();
	        return;
	    }
		if (inGitFetch || gitMergeWasDoneAfterFetch)
			return;
		gitMergeWasDoneAfterFetch = true;
		try {
			String ret = gitMergeFetchHead();
			if (ret != null && ret.startsWith("Already up-to-date."))
				return;
			String commit = getCommitInfo();
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
			System.err.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: error doing git merge FETCH_HEAD: " + ex.getMessage());
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
		return gitCommand("git log -n 1", "log -n 1", gitLocalPath);
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
			JsonNode spec = getResourceAsJson("methods/"+methodId+"/spec.json");
			Map<String,Object> display = getResourceAsYamlMap("methods/"+methodId+"/display.yaml");

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
