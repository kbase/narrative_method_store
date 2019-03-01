package us.kbase.narrativemethodstore;

import java.io.File;
import java.util.List;
import java.util.Map;

import us.kbase.auth.AuthConfig;
import us.kbase.auth.AuthToken;
import us.kbase.auth.ConfigurableAuthService;
import us.kbase.common.service.JsonServerMethod;
import us.kbase.common.service.JsonServerServlet;
import us.kbase.common.service.JsonServerSyslog;
import us.kbase.common.service.RpcContext;
import us.kbase.common.service.Tuple4;

//BEGIN_HEADER
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.ini4j.Ini;

import us.kbase.auth.AuthService;
import us.kbase.narrativemethodstore.db.NarrativeCategoriesIndex;
import us.kbase.narrativemethodstore.db.ServiceUrlTemplateEvaluater;
import us.kbase.narrativemethodstore.db.Validator;
import us.kbase.narrativemethodstore.db.github.LocalGitDB;
import us.kbase.narrativemethodstore.db.github.RepoTag;
import us.kbase.narrativemethodstore.db.mongo.MongoDynamicRepoDB;
//END_HEADER

/**
 * <p>Original spec-file module name: NarrativeMethodStore</p>
 * <pre>
 * </pre>
 */
public class NarrativeMethodStoreServer extends JsonServerServlet {
    private static final long serialVersionUID = 1L;
    private static final String version = "0.1.0";
    private static final String gitUrl = "https://github.com/kbase/narrative_method_store.git";
    private static final String gitCommitHash = "b805a9d11eb5a2e9a84ab44945f5bcb2c4d4fae3";

    //BEGIN_CLASS_HEADER
    public static final String SYS_PROP_KB_DEPLOYMENT_CONFIG = "KB_DEPLOYMENT_CONFIG";
    public static final String SERVICE_DEPLOYMENT_NAME = "NarrativeMethodStore";
    
    public static final String         CFG_PROP_GIT_REPO = "method-spec-git-repo";
    public static final String       CFG_PROP_GIT_BRANCH = "method-spec-git-repo-branch";
    public static final String    CFG_PROP_GIT_LOCAL_DIR = "method-spec-git-repo-local-dir";
    public static final String CFG_PROP_GIT_REFRESH_RATE = "method-spec-git-repo-refresh-rate";
    public static final String       CFG_PROP_CACHE_SIZE = "method-spec-cache-size";
    public static final String         CFG_PROP_TEMP_DIR = "method-spec-temp-dir";
    public static final String       CFG_PROP_MONGO_HOST = "method-spec-mongo-host";
    public static final String     CFG_PROP_MONGO_DBNAME = "method-spec-mongo-dbname";
    public static final String       CFG_PROP_MONGO_USER = "method-spec-mongo-user";
    public static final String   CFG_PROP_MONGO_PASSWORD = "method-spec-mongo-password";
    public static final String   CFG_PROP_MONGO_READONLY = "method-spec-mongo-readonly";
    public static final String      CFG_PROP_ADMIN_USERS = "method-spec-admin-users";
    public static final String        CFG_PROP_SHOCK_URL = "method-spec-shock-url";
    public static final String       CFG_PROP_SHOCK_USER = "method-spec-shock-user";
    public static final String   CFG_PROP_SHOCK_PASSWORD = "method-spec-shock-password";
    public static final String      CFG_PROP_SHOCK_TOKEN = "method-spec-shock-token";
    public static final String    CFG_PROP_ENDPOINT_BASE = "endpoint-base";
    public static final String    CFG_PROP_ENDPOINT_HOST = "endpoint-host";
    public static final String      CFG_PROP_DEFAULT_TAG = "method-spec-default-tag";
    public static final String CFG_PROP_AUTH_SERVICE_URL = "auth-service-url";
    public static final String    CFG_PROP_AUTH_INSECURE = "auth-service-url-allow-insecure";
    
    public static final String VERSION = "0.3.9";
    
    private static Throwable configError = null;
    private static Map<String, String> config = null;

    private static LocalGitDB localGitDB;

    public static Map<String, String> config() {
    	if (config != null)
    		return config;
        if (configError != null)
        	throw new IllegalStateException("There was an error while loading configuration", configError);
		String configPath = System.getProperty(SYS_PROP_KB_DEPLOYMENT_CONFIG);
		if (configPath == null)
			configPath = System.getenv(SYS_PROP_KB_DEPLOYMENT_CONFIG);
		if (configPath == null) {
			configError = new IllegalStateException("Configuration file was not defined");
		} else {
			System.out.println(NarrativeMethodStoreServer.class.getName() + ": Deployment config path was defined: " + configPath);
			try {
				config = new Ini(new File(configPath)).get(SERVICE_DEPLOYMENT_NAME);
			} catch (Throwable ex) {
				System.out.println(NarrativeMethodStoreServer.class.getName() + ": Error loading deployment config-file: " + ex.getMessage());
				configError = ex;
			}
		}
		if (config == null)
			throw new IllegalStateException("There was unknown error in service initialization when checking"
					+ "the configuration: is the ["+SERVICE_DEPLOYMENT_NAME+"] config group defined?");
		return config;
    }
    
    private static String getGitRepo() {
    	String ret = config().get(CFG_PROP_GIT_REPO);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_GIT_REPO + " is not defined in configuration");
    	return ret;
    }
    private static String getGitBranch() {
    	String ret = config().get(CFG_PROP_GIT_BRANCH);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_GIT_BRANCH + " is not defined in configuration");
    	return ret;
    }
    private static String getGitLocalDir() {
    	String ret = config().get(CFG_PROP_GIT_LOCAL_DIR);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_GIT_LOCAL_DIR + " is not defined in configuration");
    	return ret;
    }
    private static int getGitRefreshRate() {
    	String ret = config().get(CFG_PROP_GIT_REFRESH_RATE);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_GIT_REFRESH_RATE + " is not defined in configuration");
    	try {
    		return Integer.parseInt(ret);
    	} catch (NumberFormatException ex) {
    		throw new IllegalStateException("Parameter " + CFG_PROP_GIT_REFRESH_RATE + " is not defined in configuration as integer: " + ret);
    	}
    }
    private static int getCacheSize() {
    	String ret = config().get(CFG_PROP_CACHE_SIZE);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_CACHE_SIZE + " is not defined in configuration");
    	try {
    		return Integer.parseInt(ret);
    	} catch (NumberFormatException ex) {
    		throw new IllegalStateException("Parameter " + CFG_PROP_CACHE_SIZE + " is not defined in configuration as integer: " + ret);
    	}
    }
    private static String getTempDir() {
        String ret = config().get(CFG_PROP_TEMP_DIR);
        if (ret == null)
            throw new IllegalStateException("Parameter " + CFG_PROP_TEMP_DIR + " is not defined in configuration");
        return ret;
    }
    private static String getMongoHost() {
        String ret = config().get(CFG_PROP_MONGO_HOST);
        if (ret == null)
            throw new IllegalStateException("Parameter " + CFG_PROP_MONGO_HOST + " is not defined in configuration");
        return ret;
    }
    private static String getMongoDbname() {
        String ret = config().get(CFG_PROP_MONGO_DBNAME);
        if (ret == null)
            throw new IllegalStateException("Parameter " + CFG_PROP_MONGO_DBNAME + " is not defined in configuration");
        return ret;
    }
    private static String getAdminUsers() {
        String ret = config().get(CFG_PROP_ADMIN_USERS);
        if (ret == null)
            throw new IllegalStateException("Parameter " + CFG_PROP_ADMIN_USERS + " is not defined in configuration");
        return ret;
    }
    private static String getDefaultTag() {
        return config().get(CFG_PROP_DEFAULT_TAG);
    }
    
    private static <T> List<T> trim(List<T> data, ListParams params) {
    	if (params.getOffset() == null && params.getLimit() == null)
    		return data;
    	int from = params.getOffset() != null && params.getOffset() > 0 ? (int)(long)params.getOffset() : 0;
    	int to = data.size();
    	if (params.getLimit() != null && params.getLimit() > 0 && from + params.getLimit() < to)
    		to = from + (int)(long)params.getLimit();
    	return data.subList(from, to);
    }
    
    public static synchronized LocalGitDB getLocalGitDB() throws Exception {
        if (localGitDB == null) {
            // TODO: Make sure LocalGitDB doesn't require synchronization for when shared between servlet threads (including ImageServlet).
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_GIT_REPO +" = " + getGitRepo());
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_GIT_BRANCH +" = " + getGitBranch());
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_GIT_LOCAL_DIR +" = " + getGitLocalDir());
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_GIT_REFRESH_RATE +" = " + getGitRefreshRate());
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_CACHE_SIZE +" = " + getCacheSize());
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_TEMP_DIR +" = " + getTempDir());
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_MONGO_HOST +" = " + getMongoHost());
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_MONGO_DBNAME +" = " + getMongoDbname());
            String dbUser = config().get(CFG_PROP_MONGO_USER);
            String dbPwd = config().get(CFG_PROP_MONGO_PASSWORD);
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_MONGO_USER +" = " + (dbUser == null ? "<not-set>" : dbUser));
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_MONGO_PASSWORD +" = " + (dbPwd == null ? "<not-set>" : "[*****]"));
            String mongoReadOnlyText = config().get(CFG_PROP_MONGO_READONLY);
            boolean mongoRO = mongoReadOnlyText != null && (mongoReadOnlyText.equals("1") || mongoReadOnlyText.equals("true") || 
                    mongoReadOnlyText.equals("y") || mongoReadOnlyText.equals("yes"));
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_MONGO_READONLY +" = " + mongoRO);
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_ADMIN_USERS +" = " + getAdminUsers());
            List<String> adminUsers = Arrays.asList(getAdminUsers().trim().split(Pattern.quote(",")));
            String shockUrl = config().get(CFG_PROP_SHOCK_URL);
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_SHOCK_URL +" = " + (shockUrl == null ? "<not-set>" : shockUrl));
            String shockUser = config().get(CFG_PROP_SHOCK_USER);
            if (shockUser != null && shockUser.trim().isEmpty()) {
                shockUser = null;
            }
            String shockPwd = config().get(CFG_PROP_SHOCK_PASSWORD);
            String shockTokenText = config().get(CFG_PROP_SHOCK_TOKEN);
            if (shockTokenText != null && shockTokenText.trim().isEmpty()) {
                shockTokenText = null;
            }
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_SHOCK_USER +" = " + (shockUser == null ? "<not-set>" : shockUser));
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_SHOCK_PASSWORD +" = " + (shockPwd == null ? "<not-set>" : "[*****]"));
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_SHOCK_TOKEN +" = " + (shockTokenText == null ? "<not-set>" : "[*****]"));
            String endpointHost = config().get(CFG_PROP_ENDPOINT_HOST);
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_ENDPOINT_HOST +" = " + (endpointHost == null ? "<not-set>" : endpointHost));
            String endpointBase = config().get(CFG_PROP_ENDPOINT_BASE);
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_ENDPOINT_BASE +" = " + (endpointBase == null ? "<not-set>" : endpointBase));
            String defaultTag = getDefaultTag();
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_DEFAULT_TAG +" = " + (defaultTag == null ? "<not-set> ('dev' will be used)" : defaultTag));
            if (defaultTag == null)
                defaultTag = "dev";
            String authServiceUrl = config().get(CFG_PROP_AUTH_SERVICE_URL);
            if (authServiceUrl == null) {
                throw new IllegalStateException("Parameter " + CFG_PROP_AUTH_SERVICE_URL + " is not defined in configuration");
            }
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_AUTH_SERVICE_URL +" = " + authServiceUrl);
            String authAllowInsecure = config().get(CFG_PROP_AUTH_INSECURE);
            System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_AUTH_INSECURE +" = " + 
                    (authAllowInsecure == null ? "<not-set> ('false' will be used)" : authAllowInsecure));
            AuthToken shockToken = null;
            if (shockUser != null || shockTokenText != null) {
                ConfigurableAuthService authService = new ConfigurableAuthService(
                        new AuthConfig().withKBaseAuthServerURL(new URL(authServiceUrl))
                        .withAllowInsecureURLs("true".equals(authAllowInsecure)));
                if (shockTokenText == null) {
                    shockToken = authService.login(shockUser, shockPwd == null ? "" : shockPwd).getToken();
                } else {
                    shockToken = authService.validateToken(shockTokenText);
                }
            }
            localGitDB = new LocalGitDB(new URL(getGitRepo()), getGitBranch(), new File(getGitLocalDir()), getGitRefreshRate(), getCacheSize(), 
                    new MongoDynamicRepoDB(getMongoHost(), getMongoDbname(), dbUser, dbPwd, adminUsers, mongoRO, 
                            shockUrl == null ? null : new URL(shockUrl), shockToken), new File(getTempDir()),
                            new ServiceUrlTemplateEvaluater(endpointHost, endpointBase), RepoTag.valueOf(defaultTag));
        }
        return localGitDB;
    }
    //END_CLASS_HEADER

    public NarrativeMethodStoreServer() throws Exception {
        super("NarrativeMethodStore");
        //BEGIN_CONSTRUCTOR
        getLocalGitDB();
        //END_CONSTRUCTOR
    }

    /**
     * <p>Original spec-file function name: ver</p>
     * <pre>
     * Returns the current running version of the NarrativeMethodStore.
     * </pre>
     * @return   instance of String
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.ver", async=true)
    public String ver(RpcContext jsonRpcContext) throws Exception {
        String returnVal = null;
        //BEGIN ver
        config();
        returnVal = VERSION;
        //END ver
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: status</p>
     * <pre>
     * Simply check the status of this service to see what Spec repository it is
     * using, and what commit it is on
     * </pre>
     * @return   instance of type {@link us.kbase.narrativemethodstore.Status Status}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.status", async=true)
    public Status status(RpcContext jsonRpcContext) throws Exception {
        Status returnVal = null;
        //BEGIN status
        config();
        returnVal = new Status()
        				.withGitSpecUrl(getGitRepo())
        				.withGitSpecBranch(getGitBranch())
        				.withGitSpecCommit(getLocalGitDB().getCommitInfo())
        				.withUpdateInterval(Integer.toString(getGitRefreshRate()));
        //END status
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: list_categories</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListCategoriesParams ListCategoriesParams}
     * @return   multiple set: (1) parameter "categories" of mapping from String to type {@link us.kbase.narrativemethodstore.Category Category}, (2) parameter "methods" of mapping from String to type {@link us.kbase.narrativemethodstore.MethodBriefInfo MethodBriefInfo}, (3) parameter "apps" of mapping from String to type {@link us.kbase.narrativemethodstore.AppBriefInfo AppBriefInfo}, (4) parameter "types" of mapping from String to type {@link us.kbase.narrativemethodstore.TypeInfo TypeInfo}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_categories", tuple = true, async=true)
    public Tuple4<Map<String,Category>, Map<String,MethodBriefInfo>, Map<String,AppBriefInfo>, Map<String,TypeInfo>> listCategories(ListCategoriesParams params, RpcContext jsonRpcContext) throws Exception {
        Map<String,Category> return1 = null;
        Map<String,MethodBriefInfo> return2 = null;
        Map<String,AppBriefInfo> return3 = null;
        Map<String,TypeInfo> return4 = null;
        //BEGIN list_categories
        config();
        boolean returnLoadedMethods = false;
        if(params.getLoadMethods()!=null) {
        	if(params.getLoadMethods()==1) {
        		returnLoadedMethods = true;
        	}
        }
        boolean returnLoadedApps = false;
        if(params.getLoadApps()!=null) {
        	if(params.getLoadApps()==1) {
        		returnLoadedApps = true;
        	}
        }
        boolean returnLoadedTypes = false;
        if(params.getLoadTypes()!=null) {
        	if(params.getLoadTypes()==1) {
        		returnLoadedTypes = true;
        	}
        }
        NarrativeCategoriesIndex narCatIndex = getLocalGitDB().getCategoriesIndex();
        return1 = narCatIndex.getCategories();
        if(returnLoadedMethods) {
        	return2 = narCatIndex.getMethods(params.getTag());
        } else {
        	return2 = new HashMap<String,MethodBriefInfo>();
        }
        if (returnLoadedApps) {
        	return3 = narCatIndex.getApps();
        } else {
        	return3 = new HashMap<String, AppBriefInfo>();
        }
        if (returnLoadedTypes) {
        	return4 = narCatIndex.getTypes();
        } else {
        	return4 = new HashMap<String, TypeInfo>();
        }
        //END list_categories
        Tuple4<Map<String,Category>, Map<String,MethodBriefInfo>, Map<String,AppBriefInfo>, Map<String,TypeInfo>> returnVal = new Tuple4<Map<String,Category>, Map<String,MethodBriefInfo>, Map<String,AppBriefInfo>, Map<String,TypeInfo>>();
        returnVal.setE1(return1);
        returnVal.setE2(return2);
        returnVal.setE3(return3);
        returnVal.setE4(return4);
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: get_category</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetCategoryParams GetCategoryParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.Category Category}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_category", async=true)
    public List<Category> getCategory(GetCategoryParams params, RpcContext jsonRpcContext) throws Exception {
        List<Category> returnVal = null;
        //BEGIN get_category
        config();
        returnVal = new ArrayList<Category>();
        for (String catId : params.getIds()) {
        	Category cat = getLocalGitDB().getCategoriesIndex().getCategories().get(catId);
        	if (cat == null)
        		throw new IllegalStateException("No category with id=" + catId);
        	returnVal.add(cat);
        }
        //END get_category
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: list_methods</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.MethodBriefInfo MethodBriefInfo}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_methods", async=true)
    public List<MethodBriefInfo> listMethods(ListParams params, RpcContext jsonRpcContext) throws Exception {
        List<MethodBriefInfo> returnVal = null;
        //BEGIN list_methods
        config();
        returnVal = new ArrayList<MethodBriefInfo>(getLocalGitDB().getCategoriesIndex().getMethods(params.getTag()).values());
        returnVal = trim(returnVal, params);
        //END list_methods
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: list_methods_full_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.MethodFullInfo MethodFullInfo}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_methods_full_info", async=true)
    public List<MethodFullInfo> listMethodsFullInfo(ListParams params, RpcContext jsonRpcContext) throws Exception {
        List<MethodFullInfo> returnVal = null;
        //BEGIN list_methods_full_info
        config();
        List<String> methodIds = new ArrayList<String>(getLocalGitDB().listMethodIds(false, params.getTag()));
        methodIds = trim(methodIds, params);
        returnVal = getMethodFullInfo(new GetMethodParams().withIds(methodIds).withTag(params.getTag()), jsonRpcContext);
        //END list_methods_full_info
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: list_methods_spec</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.MethodSpec MethodSpec}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_methods_spec", async=true)
    public List<MethodSpec> listMethodsSpec(ListParams params, RpcContext jsonRpcContext) throws Exception {
        List<MethodSpec> returnVal = null;
        //BEGIN list_methods_spec
        config();
        List<String> methodIds = new ArrayList<String>(getLocalGitDB().listMethodIds(false, params.getTag()));
        methodIds = trim(methodIds, params);
        returnVal = getMethodSpec(new GetMethodParams().withIds(methodIds).withTag(params.getTag()), jsonRpcContext);
        //END list_methods_spec
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: list_method_ids_and_names</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListMethodIdsAndNamesParams ListMethodIdsAndNamesParams}
     * @return   instance of mapping from String to String
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_method_ids_and_names", async=true)
    public Map<String,String> listMethodIdsAndNames(ListMethodIdsAndNamesParams params, RpcContext jsonRpcContext) throws Exception {
        Map<String,String> returnVal = null;
        //BEGIN list_method_ids_and_names
        config();
        returnVal = new TreeMap<String, String>();
        for (Map.Entry<String, MethodBriefInfo> entry : getLocalGitDB().getCategoriesIndex().getMethods(params.getTag()).entrySet())
        	returnVal.put(entry.getKey(), entry.getValue().getName());
        //END list_method_ids_and_names
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: list_apps</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.AppBriefInfo AppBriefInfo}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_apps", async=true)
    public List<AppBriefInfo> listApps(ListParams params, RpcContext jsonRpcContext) throws Exception {
        List<AppBriefInfo> returnVal = null;
        //BEGIN list_apps
        config();
        returnVal = new ArrayList<AppBriefInfo>(getLocalGitDB().getCategoriesIndex().getApps().values());
        returnVal = trim(returnVal, params);
        //END list_apps
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: list_apps_full_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.AppFullInfo AppFullInfo}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_apps_full_info", async=true)
    public List<AppFullInfo> listAppsFullInfo(ListParams params, RpcContext jsonRpcContext) throws Exception {
        List<AppFullInfo> returnVal = null;
        //BEGIN list_apps_full_info
        config();
        List<String> appIds = new ArrayList<String>(getLocalGitDB().listAppIds(false));
        appIds = trim(appIds, params);
        returnVal = getAppFullInfo(new GetAppParams().withIds(appIds), jsonRpcContext);
        //END list_apps_full_info
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: list_apps_spec</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.AppSpec AppSpec}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_apps_spec", async=true)
    public List<AppSpec> listAppsSpec(ListParams params, RpcContext jsonRpcContext) throws Exception {
        List<AppSpec> returnVal = null;
        //BEGIN list_apps_spec
        config();
        List<String> appIds = new ArrayList<String>(getLocalGitDB().listAppIds(false));
        appIds = trim(appIds, params);
        returnVal = getAppSpec(new GetAppParams().withIds(appIds), jsonRpcContext);
        //END list_apps_spec
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: list_app_ids_and_names</p>
     * <pre>
     * </pre>
     * @return   instance of mapping from String to String
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_app_ids_and_names", async=true)
    public Map<String,String> listAppIdsAndNames(RpcContext jsonRpcContext) throws Exception {
        Map<String,String> returnVal = null;
        //BEGIN list_app_ids_and_names
        config();
        returnVal = new TreeMap<String, String>();
        for (Map.Entry<String, AppBriefInfo> entry : getLocalGitDB().getCategoriesIndex().getApps().entrySet())
        	returnVal.put(entry.getKey(), entry.getValue().getName());
        //END list_app_ids_and_names
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: list_types</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.TypeInfo TypeInfo}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_types", async=true)
    public List<TypeInfo> listTypes(ListParams params, RpcContext jsonRpcContext) throws Exception {
        List<TypeInfo> returnVal = null;
        //BEGIN list_types
        config();
        returnVal = new ArrayList<TypeInfo>(getLocalGitDB().getCategoriesIndex().getTypes().values());
        returnVal = trim(returnVal, params);
        //END list_types
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: get_method_brief_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetMethodParams GetMethodParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.MethodBriefInfo MethodBriefInfo}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_method_brief_info", async=true)
    public List<MethodBriefInfo> getMethodBriefInfo(GetMethodParams params, RpcContext jsonRpcContext) throws Exception {
        List<MethodBriefInfo> returnVal = null;
        //BEGIN get_method_brief_info
        config();
        List <String> methodIds = params.getIds();
        returnVal = new ArrayList<MethodBriefInfo>(methodIds.size());
        for(String id: methodIds) {
        	returnVal.add(getLocalGitDB().getMethodBriefInfo(id, params.getTag()));
        }
        //END get_method_brief_info
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: get_method_full_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetMethodParams GetMethodParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.MethodFullInfo MethodFullInfo}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_method_full_info", async=true)
    public List<MethodFullInfo> getMethodFullInfo(GetMethodParams params, RpcContext jsonRpcContext) throws Exception {
        List<MethodFullInfo> returnVal = null;
        //BEGIN get_method_full_info
        config();
        List <String> methodIds = params.getIds();
        returnVal = new ArrayList<MethodFullInfo>(methodIds.size());
        for(String id: methodIds) {
        	returnVal.add(getLocalGitDB().getMethodFullInfo(id, params.getTag()));
        }
        //END get_method_full_info
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: get_method_spec</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetMethodParams GetMethodParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.MethodSpec MethodSpec}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_method_spec", async=true)
    public List<MethodSpec> getMethodSpec(GetMethodParams params, RpcContext jsonRpcContext) throws Exception {
        List<MethodSpec> returnVal = null;
        //BEGIN get_method_spec
        config();
        List<String> methodIds = params.getIds();
        returnVal = new ArrayList<MethodSpec>(methodIds.size());
        for (String id : methodIds)
        	returnVal.add(getLocalGitDB().getMethodSpec(id, params.getTag()));
        //END get_method_spec
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: get_app_brief_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetAppParams GetAppParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.AppBriefInfo AppBriefInfo}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_app_brief_info", async=true)
    public List<AppBriefInfo> getAppBriefInfo(GetAppParams params, RpcContext jsonRpcContext) throws Exception {
        List<AppBriefInfo> returnVal = null;
        //BEGIN get_app_brief_info
        config();
        List <String> appIds = params.getIds();
        returnVal = new ArrayList<AppBriefInfo>(appIds.size());
        for(String id: appIds)
        	returnVal.add(getLocalGitDB().getAppBriefInfo(id));
        //END get_app_brief_info
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: get_app_full_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetAppParams GetAppParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.AppFullInfo AppFullInfo}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_app_full_info", async=true)
    public List<AppFullInfo> getAppFullInfo(GetAppParams params, RpcContext jsonRpcContext) throws Exception {
        List<AppFullInfo> returnVal = null;
        //BEGIN get_app_full_info
        config();
        List <String> appIds = params.getIds();
        returnVal = new ArrayList<AppFullInfo>(appIds.size());
        for(String id: appIds) {
        	returnVal.add(getLocalGitDB().getAppFullInfo(id));
        }
        //END get_app_full_info
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: get_app_spec</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetAppParams GetAppParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.AppSpec AppSpec}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_app_spec", async=true)
    public List<AppSpec> getAppSpec(GetAppParams params, RpcContext jsonRpcContext) throws Exception {
        List<AppSpec> returnVal = null;
        //BEGIN get_app_spec
        config();
        List<String> appIds = params.getIds();
        returnVal = new ArrayList<AppSpec>(appIds.size());
        for (String id : appIds)
        	returnVal.add(getLocalGitDB().getAppSpec(id));
        //END get_app_spec
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: get_type_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetTypeParams GetTypeParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.TypeInfo TypeInfo}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_type_info", async=true)
    public List<TypeInfo> getTypeInfo(GetTypeParams params, RpcContext jsonRpcContext) throws Exception {
        List<TypeInfo> returnVal = null;
        //BEGIN get_type_info
        config();
        List<String> typeNames = params.getTypeNames();
        returnVal = new ArrayList<TypeInfo>(typeNames.size());
        for(String typeName: typeNames)
        	returnVal.add(getLocalGitDB().getTypeInfo(typeName));
        //END get_type_info
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: validate_method</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ValidateMethodParams ValidateMethodParams}
     * @return   instance of type {@link us.kbase.narrativemethodstore.ValidationResults ValidationResults}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.validate_method", async=true)
    public ValidationResults validateMethod(ValidateMethodParams params, RpcContext jsonRpcContext) throws Exception {
        ValidationResults returnVal = null;
        //BEGIN validate_method
        returnVal = Validator.validateMethod(params);
        //END validate_method
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: validate_app</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ValidateAppParams ValidateAppParams}
     * @return   instance of type {@link us.kbase.narrativemethodstore.ValidationResults ValidationResults}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.validate_app", async=true)
    public ValidationResults validateApp(ValidateAppParams params, RpcContext jsonRpcContext) throws Exception {
        ValidationResults returnVal = null;
        //BEGIN validate_app
        returnVal = Validator.validateApp(params);
        //END validate_app
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: validate_type</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ValidateTypeParams ValidateTypeParams}
     * @return   instance of type {@link us.kbase.narrativemethodstore.ValidationResults ValidationResults}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.validate_type", async=true)
    public ValidationResults validateType(ValidateTypeParams params, RpcContext jsonRpcContext) throws Exception {
        ValidationResults returnVal = null;
        //BEGIN validate_type
        returnVal = Validator.validateType(params);
        //END validate_type
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: load_widget_java_script</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.LoadWidgetParams LoadWidgetParams}
     * @return   parameter "java_script" of String
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.load_widget_java_script", async=true)
    public String loadWidgetJavaScript(LoadWidgetParams params, RpcContext jsonRpcContext) throws Exception {
        String returnVal = null;
        //BEGIN load_widget_java_script
        returnVal = getLocalGitDB().loadWidgetJavaScript(params.getModuleName(), 
                params.getVersion(), params.getWidgetId(), params.getTag());
        //END load_widget_java_script
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: register_repo</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.RegisterRepoParams RegisterRepoParams}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.register_repo", async=true)
    public void registerRepo(RegisterRepoParams params, AuthToken authPart, RpcContext jsonRpcContext) throws Exception {
        //BEGIN register_repo
        getLocalGitDB().registerRepo(authPart.getUserName(), params.getGitUrl(), params.getGitCommitHash());
        //END register_repo
    }

    /**
     * <p>Original spec-file function name: disable_repo</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.DisableRepoParams DisableRepoParams}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.disable_repo", async=true)
    public void disableRepo(DisableRepoParams params, AuthToken authPart, RpcContext jsonRpcContext) throws Exception {
        //BEGIN disable_repo
        getLocalGitDB().setRepoState(authPart.getUserName(), params.getModuleName(), "disabled");
        //END disable_repo
    }

    /**
     * <p>Original spec-file function name: enable_repo</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.EnableRepoParams EnableRepoParams}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.enable_repo", async=true)
    public void enableRepo(EnableRepoParams params, AuthToken authPart, RpcContext jsonRpcContext) throws Exception {
        //BEGIN enable_repo
        getLocalGitDB().setRepoState(authPart.getUserName(), params.getModuleName(), "ready");
        //END enable_repo
    }

    /**
     * <p>Original spec-file function name: push_repo_to_tag</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.PushRepoToTagParams PushRepoToTagParams}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.push_repo_to_tag", async=true)
    public void pushRepoToTag(PushRepoToTagParams params, AuthToken authPart, RpcContext jsonRpcContext) throws Exception {
        //BEGIN push_repo_to_tag
        getLocalGitDB().pushRepoToTag(params.getModuleName(), params.getTag(), authPart.getUserName());
        //END push_repo_to_tag
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            new NarrativeMethodStoreServer().startupServer(Integer.parseInt(args[0]));
        } else if (args.length == 3) {
            JsonServerSyslog.setStaticUseSyslog(false);
            JsonServerSyslog.setStaticMlogFile(args[1] + ".log");
            new NarrativeMethodStoreServer().processRpcCall(new File(args[0]), new File(args[1]), args[2]);
        } else {
            System.out.println("Usage: <program> <server_port>");
            System.out.println("   or: <program> <context_json_file> <output_json_file> <token>");
            return;
        }
    }
}
