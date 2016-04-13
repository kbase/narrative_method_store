package us.kbase.narrativemethodstore.db.github;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
import us.kbase.narrativemethodstore.db.NarrativeAppData;
import us.kbase.narrativemethodstore.db.NarrativeCategoriesIndex;
import us.kbase.narrativemethodstore.db.NarrativeMethodData;
import us.kbase.narrativemethodstore.db.NarrativeTypeData;
import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.db.ServiceUrlTemplateEvaluater;
import us.kbase.narrativemethodstore.db.DynamicRepoDB.RepoState;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreInitializationException;
import us.kbase.narrativemethodstore.util.TextUtils;

public class LocalGitDB {
	
	protected final URL gitRepoUrl;
	protected final String gitBranch;
	protected final File gitLocalPath;
	protected final int refreshTimeInMinutes;
	protected final int cacheSize;
	
	protected final ObjectMapper mapper = new ObjectMapper();
	
	protected long lastPullTime = -1;
	protected String lastCommit = null;
	
	protected NarrativeCategoriesIndex narCatIndex;
	protected final LoadingCache<MethodId, NarrativeMethodData> methodDataCache;
	protected final LoadingCache<String, AppFullInfo> appFullInfoCache;
	protected final LoadingCache<String, AppSpec> appSpecCache;
	protected static Thread refreshingThread = null;
    protected boolean inGitFetch = false;
    protected boolean gitMergeWasDoneAfterFetch = false;
	protected boolean needToStopRefreshingThread = false;
	
	protected final File tempDir;
	protected final DynamicRepoDB dynamicRepos;
	protected final ServiceUrlTemplateEvaluater srvUrlTemplEval;
	protected final RepoTag defaultTagForGetters;
	
	public LocalGitDB(URL gitRepoUrl, String branch, File localPath, int refreshTimeInMinutes, 
	        int cacheSize, DynamicRepoDB dynamicRepos, File tempDir,
	        ServiceUrlTemplateEvaluater srvUrlTemplEval, RepoTag defaultTagForGetters) throws NarrativeMethodStoreInitializationException {
		this.gitRepoUrl = gitRepoUrl;
		this.gitBranch = branch;
		this.gitLocalPath = localPath;
		this.refreshTimeInMinutes = refreshTimeInMinutes;
		this.cacheSize = cacheSize;
        this.methodDataCache = CacheBuilder.newBuilder().maximumSize(cacheSize).build(
                new CacheLoader<MethodId, NarrativeMethodData>() {
                    @Override
                    public NarrativeMethodData load(MethodId methodId) throws NarrativeMethodStoreException {
                        return loadMethodDataUncached(methodId, narCatIndex);
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
        this.srvUrlTemplEval = srvUrlTemplEval;
        this.defaultTagForGetters = defaultTagForGetters;
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
                reloadAll();
			}
		} catch (Exception ex) {
			System.err.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: error doing git merge FETCH_HEAD: " + ex.getMessage());
		}
	}

	public synchronized void hardRefresh() throws NarrativeMethodStoreException {
	    reloadAll();
	}
	
    public void reloadAll() throws NarrativeMethodStoreException {
        System.out.println("[" + new Date() + "] NarrativeMethodStore.LocalGitDB: refreshing caches");
        // recreate the categories index
        loadCategoriesIndex();
        methodDataCache.invalidateAll();
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

	/*protected File getRepositoriesFile() {
	    return new File(gitLocalPath, "repositories");
	}*/

	protected List<MethodId> listMethodIdsUncached(NarrativeCategoriesIndex narCatIndex) {
		List<MethodId> methodList = new ArrayList<MethodId>();
		if (getMethodsDir().exists())
		    for (File sub : getMethodsDir().listFiles()) {
		        if (sub.isDirectory())
		            methodList.add(new MethodId(sub.getName()));
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
	
	public List<String> listMethodIds(boolean withErrors, String tag) {
		checkForChanges();
		List<String> ret = new ArrayList<String>();
		for (Map.Entry<String, MethodBriefInfo> entry : narCatIndex.getMethods(tag).entrySet()) {
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
	
	protected NarrativeMethodData loadMethodDataUncached(final MethodId methodId,
	        NarrativeCategoriesIndex narCatIndex) throws NarrativeMethodStoreException {
		try {
			// Fetch the resources needed
			JsonNode spec = null;
			Map<String,Object> display = null;
			String serviceVersion = null;
			RepoTag tag = null;
			FileLookup fl = null;
			String version = null;
			if (methodId.isDynamic()) {
			    final RepoProvider repo = dynamicRepos.getRepoDetails(methodId.getRepoModuleName(), methodId.getTag());
			    if (repo == null)
			        throw new NarrativeMethodStoreException("Repository " + methodId.getRepoModuleName() + 
			                " wasn't tagged with " + methodId.getTag() + " tag");
			    serviceVersion = repo.getGitCommitHash();
			    spec = mapper.readTree(asText(repo.getUINarrativeMethodSpec(methodId.getMethodId())));
			    display = YamlUtils.getDocumentAsYamlMap(asText(repo.getUINarrativeMethodDisplay(methodId.getMethodId())));
			    tag = methodId.getTag();
			    fl = new FileLookup() {
                    @Override
                    public String loadFileContent(String fileName) {
                        return null;
                    }
                    @Override
                    public boolean fileExists(String fileName) {
                        if (fileName.startsWith("img/")) {
                            fileName = fileName.split("/")[1];
                            try {
                                return repo.getScreenshot(methodId.getMethodId(), fileName) != null;
                            } catch (Exception ignore) {}
                        }
                        return false;
                    }
                };
                version = repo.getModuleVersion();
			} else {
			    spec = getResourceAsJson("methods/"+methodId+"/spec.json");
			    display = getResourceAsYamlMap("methods/"+methodId+"/display.yaml");
			    fl = createFileLookup(new File(getMethodsDir(), methodId.getMethodId()));
			}

			// Initialize the actual data
			NarrativeMethodData data = new NarrativeMethodData(methodId.getExternalId(), spec, display,
					fl, methodId.getRepoModuleName(), serviceVersion, srvUrlTemplEval, tag, version);
			return data;
		} catch (NarrativeMethodStoreException ex) {
			throw ex;
		} catch (Exception ex) {
			NarrativeMethodStoreException ret = new NarrativeMethodStoreException(ex);
			ret.setErrorMethod(new MethodBriefInfo().withCategories(Arrays.asList("error"))
					.withId(methodId.getExternalId()).withName(methodId.getExternalId()));
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
						return TextUtils.text(f);
					} catch (IOException ignore) {}
				return null;
			}
			@Override
			public boolean fileExists(String fileName) {
			    return new File(dir, fileName).exists();
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

	public MethodBriefInfo getMethodBriefInfo(String methodId, String tag)
			throws NarrativeMethodStoreException {
		checkForChanges();
		MethodId mId = new MethodId(methodId, notNull(tag));
		MethodBriefInfo ret = narCatIndex.getAllMethods().get(mId);
		if (ret == null && mId.isDynamic()) {
	        try {
	            ret = methodDataCache.get(mId).getMethodBriefInfo();
	        } catch (ExecutionException e) {
	            if (e.getCause() != null && e.getCause() instanceof NarrativeMethodStoreException)
	                throw (NarrativeMethodStoreException)e.getCause();
	            throw new NarrativeMethodStoreException("Error loading brief info for method id=" + mId + " (" + e.getMessage() + ")", e);
	        }
		}
		return ret;
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
	
	public MethodFullInfo getMethodFullInfo(String methodId, String tag)
			throws NarrativeMethodStoreException {
		checkForChanges();
        MethodId mId = new MethodId(methodId, notNull(tag));
		try {
			return methodDataCache.get(mId).getMethodFullInfo();
		} catch (ExecutionException e) {
			if (e.getCause() != null && e.getCause() instanceof NarrativeMethodStoreException)
				throw (NarrativeMethodStoreException)e.getCause();
			throw new NarrativeMethodStoreException("Error loading full info for method id=" + mId + " (" + e.getMessage() + ")", e);
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

	public MethodSpec getMethodSpec(String methodId, String tag)
			throws NarrativeMethodStoreException {
		checkForChanges();
		try {
			return methodDataCache.get(new MethodId(methodId, notNull(tag))).getMethodSpec();
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
	    Set<MethodId> dynamicRepoMethods = new TreeSet<MethodId>();
	    Map<String, Exception> dynamicRepoModuleNameToLoadingError = new TreeMap<String, Exception>();
        if (dynamicRepos != null) {
            for (String repoMN : dynamicRepos.listRepoModuleNames(false, null)) {
                for (RepoTag tag : RepoTag.values()) {
                    try {
                        RepoProvider repo = dynamicRepos.getRepoDetails(repoMN, tag);
                        if (repo == null)
                            continue;
                        for (String methodId : repo.listUINarrativeMethodIDs()) {
                            dynamicRepoMethods.add(new MethodId(repoMN, methodId, tag));
                        }
                    } catch (Exception ex) {
                        if (tag.equals(RepoTag.dev))
                            dynamicRepoModuleNameToLoadingError.put(repoMN, ex);
                    }
                }
            }
        }

        NarrativeCategoriesIndex narCatIndex = new NarrativeCategoriesIndex(defaultTagForGetters);  // create a new index
        narCatIndex.updateAllDynamicRepoMethods(dynamicRepoMethods, dynamicRepoModuleNameToLoadingError);
		try {
			List<String> catIds = listCategoryIdsUncached(); // iterate over each category
			for(String catId : catIds) {
				JsonNode spec = getResourceAsJson("categories/"+catId+"/spec.json");
				//Map<String,Object> display = getResourceAsYamlMap("categories/"+catId+"/display.yaml");
				Map<String,Object> display = null;
				narCatIndex.addOrUpdateCategory(catId, spec, display);
			}
			
			List<MethodId> methIds = listMethodIdsUncached(narCatIndex); // iterate over each category
			for(MethodId mId : methIds) {
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
		return TextUtils.text(f);
	}
	
	protected Map<String,Object> getResourceAsYamlMap(String path) throws IOException {
		File f = new File(gitLocalPath, path);
		String document = TextUtils.text(f);
		return YamlUtils.getDocumentAsYamlMap(document);
	}

	protected JsonNode getAsJson(File f) throws JsonProcessingException, IOException {
		return mapper.readTree(TextUtils.text(f));
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

	public long registerRepo(String userId, String url, String commitHash) throws NarrativeMethodStoreException {
	    RepoProvider pvd = null;
	    try {
	        pvd = new GitHubRepoProvider(new URL(url), commitHash, getTempDir());
            String serviceVersion = pvd.getGitCommitHash();
            StringBuilder errors = new StringBuilder();
            for (String methodId : pvd.listUINarrativeMethodIDs()) {
                try {
                    JsonNode spec = mapper.readTree(asText(pvd.getUINarrativeMethodSpec(methodId)));
                    Map<String, Object> display = YamlUtils.getDocumentAsYamlMap(asText(
                            pvd.getUINarrativeMethodDisplay(methodId)));
                    // Initialize the actual data
                    new NarrativeMethodData(pvd.getModuleName() + "/" + methodId, 
                            spec, display, createFileLookup(new File(getMethodsDir(), methodId)), 
                            pvd.getModuleName(), serviceVersion, srvUrlTemplEval, RepoTag.dev, pvd.getModuleVersion());
                } catch (Exception ex) {
                    if (errors.length() > 0)
                        errors.append("; ");
                    errors.append("Error parsing method [" + methodId + "]: " + ex.getMessage());
                }
            }
            if (errors.length() > 0)
                throw new NarrativeMethodStoreException(errors.toString());
	        dynamicRepos.registerRepo(userId, pvd);
	        // TODO: Index invalidation is temp solution, we need to substitute it by small 
	        // corrections related to this particular repo.
	        hardRefresh();
	        return dynamicRepos.getRepoLastVersion(pvd.getModuleName(), null);
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
	
	/*public long getRepoLastVersion(String moduleName, Long withDisabled)
	        throws NarrativeMethodStoreException {
	    checkIfRepoDisabled(moduleName, withDisabled);
	    return dynamicRepos.getRepoLastVersion(moduleName);
	}*/

	/*public List<String> listRepoModuleNames(Long withDisabled) 
	        throws NarrativeMethodStoreException {
	    return dynamicRepos.listRepoModuleNames(bool(withDisabled));
	}*/

	public RepoProvider getRepoProvider(String moduleName, Long version, 
	        Long withDisabled, RepoTag tag) throws NarrativeMethodStoreException {
        checkIfRepoDisabled(moduleName, withDisabled);
        if (version == null) {
            return dynamicRepos.getRepoDetails(moduleName, tag);
        } else {
            return dynamicRepos.getRepoDetailsHistory(moduleName, version);
        }
	}

	public RepoDetails getRepoDetails(String moduleName, Long version, 
	        Long withDisabled, RepoTag tag) throws NarrativeMethodStoreException {
	    RepoProvider repo = getRepoProvider(moduleName, version, withDisabled, tag);
        String repoModuleName = repo.getModuleName();
        List<String> methodIds = new ArrayList<String>();
        for (String shortId : repo.listUINarrativeMethodIDs())
            methodIds.add(getFullMethodName(repoModuleName, shortId));
        return new RepoDetails().withModuleName(repoModuleName)
                .withModuleDescription(repo.getModuleDescription())
                .withServiceLanguage(repo.getServiceLanguage())
                .withModuleVersion(repo.getModuleVersion())
                .withGitUrl(repo.getUrl())
                .withGitCommitHash(repo.getGitCommitHash())
                .withOwners(repo.listOwners())
                .withReadme(asText(repo.getReadmeFile()))
                .withMethodIds(methodIds)
                .withWidgetIds(repo.listUIWidgetIds());

	}
	
	/*public List<Long> listRepoVersions(String moduleName, Long withDisabled) 
	        throws NarrativeMethodStoreException {
	    checkIfRepoDisabled(moduleName, withDisabled);
	    return dynamicRepos.listRepoVersions(moduleName);
	}*/
	
	public String loadWidgetJavaScript(String moduleName, Long version, 
	        String widgetId, String tag) throws NarrativeMethodStoreException {
	    RepoProvider repo = getRepoProvider(moduleName, version, null, notNull(tag));
	    return asText(repo.getUIWidgetJS(widgetId));
	}
	
	public void setRepoState(String userId, String moduleName, String repoState)
	        throws NarrativeMethodStoreException {
	    dynamicRepos.setRepoState(userId, moduleName, RepoState.valueOf(repoState));
	    hardRefresh();
	}
	
	public String getRepoState(String moduleName) throws NarrativeMethodStoreException {
	    return dynamicRepos.getRepoState(moduleName).name();
	}
	
	private RepoTag notNull(String tagName) {
	    return tagName == null ? defaultTagForGetters : RepoTag.valueOf(tagName);
	}
	
	public void saveScreenshotIntoStream(String moduleName, String methodId, 
	        String screenshotId, String tag, OutputStream os) throws NarrativeMethodStoreException {
	    dynamicRepos.getRepoDetails(moduleName, notNull(tag)).getScreenshot(methodId, 
	            screenshotId).saveToStream(os);
	}
	
	/*public long registerRepo(String userId, String moduleName, MethodSpec methodSpec, 
	        String pyhtonCode, String dockerCommands) throws NarrativeMethodStoreException {
	    File repoDir = null;
	    try {
	        repoDir = us.kbase.narrativemethodstore.util.FileUtils.generateTempDir(
	                getTempDir(), "local_", ".temp");
	        PySrvRepoPreparator.prepare(userId, moduleName, methodSpec, pyhtonCode, 
	                dockerCommands, repoDir);
	    } catch (Exception ex) {
	        throw new NarrativeMethodStoreException(ex);
	    } finally {
	        try {
	            if (repoDir != null && repoDir.exists())
	                FileUtils.deleteDirectory(repoDir);
	        } catch (Exception ignore) {}
	    }
	    return -1;
	}*/
	
    public void pushRepoToTag(String repoModuleName, String tagName, String userId) 
            throws NarrativeMethodStoreException {
        dynamicRepos.pushRepoToTag(repoModuleName, RepoTag.valueOf(tagName), userId);
        hardRefresh();
    }
}
