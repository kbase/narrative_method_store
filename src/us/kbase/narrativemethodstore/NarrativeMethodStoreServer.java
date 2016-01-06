package us.kbase.narrativemethodstore;

import java.util.List;
import java.util.Map;
import us.kbase.common.service.JsonServerMethod;
import us.kbase.common.service.JsonServerServlet;
import us.kbase.common.service.Tuple4;

//BEGIN_HEADER
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.ini4j.Ini;

import us.kbase.narrativemethodstore.db.NarrativeCategoriesIndex;
import us.kbase.narrativemethodstore.db.Validator;
import us.kbase.narrativemethodstore.db.github.LocalGitDB;
//END_HEADER

/**
 * <p>Original spec-file module name: NarrativeMethodStore</p>
 * <pre>
 * </pre>
 */
public class NarrativeMethodStoreServer extends JsonServerServlet {
    private static final long serialVersionUID = 1L;

    //BEGIN_CLASS_HEADER
    public static final String SYS_PROP_KB_DEPLOYMENT_CONFIG = "KB_DEPLOYMENT_CONFIG";
    public static final String SERVICE_DEPLOYMENT_NAME = "NarrativeMethodStore";
    
    public static final String         CFG_PROP_GIT_REPO = "method-spec-git-repo";
    public static final String       CFG_PROP_GIT_BRANCH = "method-spec-git-repo-branch";
    public static final String    CFG_PROP_GIT_LOCAL_DIR = "method-spec-git-repo-local-dir";
    public static final String CFG_PROP_GIT_REFRESH_RATE = "method-spec-git-repo-refresh-rate";
    public static final String       CFG_PROP_CACHE_SIZE = "method-spec-cache-size";
    
    public static final String VERSION = "0.2.8";
    
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
    
    private String getGitRepo() {
    	String ret = config().get(CFG_PROP_GIT_REPO);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_GIT_REPO + " is not defined in configuration");
    	return ret;
    }
    private String getGitBranch() {
    	String ret = config().get(CFG_PROP_GIT_BRANCH);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_GIT_BRANCH + " is not defined in configuration");
    	return ret;
    }
    private String getGitLocalDir() {
    	String ret = config().get(CFG_PROP_GIT_LOCAL_DIR);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_GIT_LOCAL_DIR + " is not defined in configuration");
    	return ret;
    }
    private int getGitRefreshRate() {
    	String ret = config().get(CFG_PROP_GIT_REFRESH_RATE);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_GIT_REFRESH_RATE + " is not defined in configuration");
    	try {
    		return Integer.parseInt(ret);
    	} catch (NumberFormatException ex) {
    		throw new IllegalStateException("Parameter " + CFG_PROP_GIT_REFRESH_RATE + " is not defined in configuration as integer: " + ret);
    	}
    }
    private int getCacheSize() {
    	String ret = config().get(CFG_PROP_CACHE_SIZE);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_CACHE_SIZE + " is not defined in configuration");
    	try {
    		return Integer.parseInt(ret);
    	} catch (NumberFormatException ex) {
    		throw new IllegalStateException("Parameter " + CFG_PROP_CACHE_SIZE + " is not defined in configuration as integer: " + ret);
    	}
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
    
    public static LocalGitDB getLocalGitDB() {
        return localGitDB;
    }
    //END_CLASS_HEADER

    public NarrativeMethodStoreServer() throws Exception {
        super("NarrativeMethodStore");
        //BEGIN_CONSTRUCTOR
        
        System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_GIT_REPO +" = " + getGitRepo());
        System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_GIT_BRANCH +" = " + getGitBranch());
        System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_GIT_LOCAL_DIR +" = " + getGitLocalDir());
        System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_GIT_REFRESH_RATE +" = " + getGitRefreshRate());
        System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_CACHE_SIZE +" = " + getCacheSize());
        
        localGitDB = new LocalGitDB(new URL(getGitRepo()), getGitBranch(), new File(getGitLocalDir()), getGitRefreshRate(), getCacheSize());
        
        //END_CONSTRUCTOR
    }

    /**
     * <p>Original spec-file function name: ver</p>
     * <pre>
     * Returns the current running version of the NarrativeMethodStore.
     * </pre>
     * @return   instance of String
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.ver")
    public String ver() throws Exception {
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.status")
    public Status status() throws Exception {
        Status returnVal = null;
        //BEGIN status
        config();
        returnVal = new Status()
        				.withGitSpecUrl(getGitRepo())
        				.withGitSpecBranch(getGitBranch())
        				.withGitSpecCommit(localGitDB.getCommitInfo())
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_categories", tuple = true)
    public Tuple4<Map<String,Category>, Map<String,MethodBriefInfo>, Map<String,AppBriefInfo>, Map<String,TypeInfo>> listCategories(ListCategoriesParams params) throws Exception {
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
        NarrativeCategoriesIndex narCatIndex = localGitDB.getCategoriesIndex();
        return1 = narCatIndex.getCategories();
        if(returnLoadedMethods) {
        	return2 = narCatIndex.getMethods();
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_category")
    public List<Category> getCategory(GetCategoryParams params) throws Exception {
        List<Category> returnVal = null;
        //BEGIN get_category
        config();
        returnVal = new ArrayList<Category>();
        for (String catId : params.getIds()) {
        	Category cat = localGitDB.getCategoriesIndex().getCategories().get(catId);
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_methods")
    public List<MethodBriefInfo> listMethods(ListParams params) throws Exception {
        List<MethodBriefInfo> returnVal = null;
        //BEGIN list_methods
        config();
        returnVal = new ArrayList<MethodBriefInfo>(localGitDB.getCategoriesIndex().getMethods().values());
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_methods_full_info")
    public List<MethodFullInfo> listMethodsFullInfo(ListParams params) throws Exception {
        List<MethodFullInfo> returnVal = null;
        //BEGIN list_methods_full_info
        config();
        List<String> methodIds = new ArrayList<String>(localGitDB.listMethodIds(false));
        methodIds = trim(methodIds, params);
        returnVal = getMethodFullInfo(new GetMethodParams().withIds(methodIds));
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_methods_spec")
    public List<MethodSpec> listMethodsSpec(ListParams params) throws Exception {
        List<MethodSpec> returnVal = null;
        //BEGIN list_methods_spec
        config();
        List<String> methodIds = new ArrayList<String>(localGitDB.listMethodIds(false));
        methodIds = trim(methodIds, params);
        returnVal = getMethodSpec(new GetMethodParams().withIds(methodIds));
        //END list_methods_spec
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: list_method_ids_and_names</p>
     * <pre>
     * </pre>
     * @return   instance of mapping from String to String
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_method_ids_and_names")
    public Map<String,String> listMethodIdsAndNames() throws Exception {
        Map<String,String> returnVal = null;
        //BEGIN list_method_ids_and_names
        config();
        returnVal = new TreeMap<String, String>();
        for (Map.Entry<String, MethodBriefInfo> entry : localGitDB.getCategoriesIndex().getMethods().entrySet())
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_apps")
    public List<AppBriefInfo> listApps(ListParams params) throws Exception {
        List<AppBriefInfo> returnVal = null;
        //BEGIN list_apps
        config();
        returnVal = new ArrayList<AppBriefInfo>(localGitDB.getCategoriesIndex().getApps().values());
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_apps_full_info")
    public List<AppFullInfo> listAppsFullInfo(ListParams params) throws Exception {
        List<AppFullInfo> returnVal = null;
        //BEGIN list_apps_full_info
        config();
        List<String> appIds = new ArrayList<String>(localGitDB.listAppIds(false));
        appIds = trim(appIds, params);
        returnVal = getAppFullInfo(new GetAppParams().withIds(appIds));
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_apps_spec")
    public List<AppSpec> listAppsSpec(ListParams params) throws Exception {
        List<AppSpec> returnVal = null;
        //BEGIN list_apps_spec
        config();
        List<String> appIds = new ArrayList<String>(localGitDB.listAppIds(false));
        appIds = trim(appIds, params);
        returnVal = getAppSpec(new GetAppParams().withIds(appIds));
        //END list_apps_spec
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: list_app_ids_and_names</p>
     * <pre>
     * </pre>
     * @return   instance of mapping from String to String
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_app_ids_and_names")
    public Map<String,String> listAppIdsAndNames() throws Exception {
        Map<String,String> returnVal = null;
        //BEGIN list_app_ids_and_names
        config();
        returnVal = new TreeMap<String, String>();
        for (Map.Entry<String, AppBriefInfo> entry : localGitDB.getCategoriesIndex().getApps().entrySet())
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_types")
    public List<TypeInfo> listTypes(ListParams params) throws Exception {
        List<TypeInfo> returnVal = null;
        //BEGIN list_types
        config();
        returnVal = new ArrayList<TypeInfo>(localGitDB.getCategoriesIndex().getTypes().values());
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_method_brief_info")
    public List<MethodBriefInfo> getMethodBriefInfo(GetMethodParams params) throws Exception {
        List<MethodBriefInfo> returnVal = null;
        //BEGIN get_method_brief_info
        config();
        List <String> methodIds = params.getIds();
        returnVal = new ArrayList<MethodBriefInfo>(methodIds.size());
        for(String id: methodIds) {
        	returnVal.add(localGitDB.getMethodBriefInfo(id));
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_method_full_info")
    public List<MethodFullInfo> getMethodFullInfo(GetMethodParams params) throws Exception {
        List<MethodFullInfo> returnVal = null;
        //BEGIN get_method_full_info
        config();
        List <String> methodIds = params.getIds();
        returnVal = new ArrayList<MethodFullInfo>(methodIds.size());
        for(String id: methodIds) {
        	returnVal.add(localGitDB.getMethodFullInfo(id));
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_method_spec")
    public List<MethodSpec> getMethodSpec(GetMethodParams params) throws Exception {
        List<MethodSpec> returnVal = null;
        //BEGIN get_method_spec
        config();
        List<String> methodIds = params.getIds();
        returnVal = new ArrayList<MethodSpec>(methodIds.size());
        for (String id : methodIds)
        	returnVal.add(localGitDB.getMethodSpec(id));
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_app_brief_info")
    public List<AppBriefInfo> getAppBriefInfo(GetAppParams params) throws Exception {
        List<AppBriefInfo> returnVal = null;
        //BEGIN get_app_brief_info
        config();
        List <String> appIds = params.getIds();
        returnVal = new ArrayList<AppBriefInfo>(appIds.size());
        for(String id: appIds)
        	returnVal.add(localGitDB.getAppBriefInfo(id));
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_app_full_info")
    public List<AppFullInfo> getAppFullInfo(GetAppParams params) throws Exception {
        List<AppFullInfo> returnVal = null;
        //BEGIN get_app_full_info
        config();
        List <String> appIds = params.getIds();
        returnVal = new ArrayList<AppFullInfo>(appIds.size());
        for(String id: appIds) {
        	returnVal.add(localGitDB.getAppFullInfo(id));
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_app_spec")
    public List<AppSpec> getAppSpec(GetAppParams params) throws Exception {
        List<AppSpec> returnVal = null;
        //BEGIN get_app_spec
        config();
        List<String> appIds = params.getIds();
        returnVal = new ArrayList<AppSpec>(appIds.size());
        for (String id : appIds)
        	returnVal.add(localGitDB.getAppSpec(id));
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.get_type_info")
    public List<TypeInfo> getTypeInfo(GetTypeParams params) throws Exception {
        List<TypeInfo> returnVal = null;
        //BEGIN get_type_info
        config();
        List<String> typeNames = params.getTypeNames();
        returnVal = new ArrayList<TypeInfo>(typeNames.size());
        for(String typeName: typeNames)
        	returnVal.add(localGitDB.getTypeInfo(typeName));
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.validate_method")
    public ValidationResults validateMethod(ValidateMethodParams params) throws Exception {
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.validate_app")
    public ValidationResults validateApp(ValidateAppParams params) throws Exception {
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
    @JsonServerMethod(rpc = "NarrativeMethodStore.validate_type")
    public ValidationResults validateType(ValidateTypeParams params) throws Exception {
        ValidationResults returnVal = null;
        //BEGIN validate_type
        returnVal = Validator.validateType(params);
        //END validate_type
        return returnVal;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: <program> <server_port>");
            return;
        }
        new NarrativeMethodStoreServer().startupServer(Integer.parseInt(args[0]));
    }
}
