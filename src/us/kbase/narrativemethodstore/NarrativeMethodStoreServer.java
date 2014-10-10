package us.kbase.narrativemethodstore;

import java.util.List;
import java.util.Map;

import us.kbase.common.service.JsonServerMethod;
import us.kbase.common.service.JsonServerServlet;
import us.kbase.common.service.Tuple2;



//BEGIN_HEADER
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import org.ini4j.Ini;

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
    
    private static Throwable configError = null;
    private static Map<String, String> config = null;

    static {
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
    }

    private Map<String, String> config() {
    	if (config != null)
    		return config;
        if (configError != null)
        	throw new IllegalStateException("There was an error while loading configuration", configError);
    	throw new IllegalStateException("There was unknown error in service initialization when checking"
    			+ "the configuration: is the ["+SERVICE_DEPLOYMENT_NAME+"] config group defined?");
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
    private String getGitRefreshRate() {
    	String ret = config().get(CFG_PROP_GIT_REFRESH_RATE);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_GIT_REFRESH_RATE + " is not defined in configuration");
    	return ret;
    }
    
    private LocalGitDB localGitDB;
    
    //END_CLASS_HEADER

    public NarrativeMethodStoreServer() throws Exception {
        super("NarrativeMethodStore");
        //BEGIN_CONSTRUCTOR
        
        System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_GIT_REPO +" = " + getGitRepo());
        System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_GIT_BRANCH +" = " + getGitBranch());
        System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_GIT_LOCAL_DIR +" = " + getGitLocalDir());
        System.out.println(NarrativeMethodStoreServer.class.getName() + ": " + CFG_PROP_GIT_REFRESH_RATE +" = " + getGitRefreshRate());
        
        localGitDB = new LocalGitDB(new URL(getGitRepo()), getGitBranch(), new File(getGitLocalDir()), Integer.parseInt(getGitRefreshRate()));
        
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
        //END ver
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: list_categories</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListCategoriesParams ListCategoriesParams}
     * @return   multiple set: (1) parameter "categories" of mapping from String to type {@link us.kbase.narrativemethodstore.Category Category}, (2) parameter "methods" of mapping from String to type {@link us.kbase.narrativemethodstore.MethodBriefInfo MethodBriefInfo}
     */
    @JsonServerMethod(rpc = "NarrativeMethodStore.list_categories", tuple = true)
    public Tuple2<Map<String,Category>, Map<String,MethodBriefInfo>> listCategories(ListCategoriesParams params) throws Exception {
        Map<String,Category> return1 = null;
        Map<String,MethodBriefInfo> return2 = null;
        //BEGIN list_categories
        //END list_categories
        Tuple2<Map<String,Category>, Map<String,MethodBriefInfo>> returnVal = new Tuple2<Map<String,Category>, Map<String,MethodBriefInfo>>();
        returnVal.setE1(return1);
        returnVal.setE2(return2);
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
        //END list_method_ids_and_names
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
        
        /// SIMPLE TEST
        /// TODO switch to proper cached version, this always pulls everything fresh from git
        List <String> methodIds = params.getIds();
        returnVal = new ArrayList<MethodBriefInfo>(methodIds.size());
        for(String id: methodIds) {
        	returnVal.add(localGitDB.loadMethodData(id).getMethodBriefInfo());
        }
        /// END SIMPLE TEST
        
        
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
        List <String> methodIds = params.getIds();
        returnVal = new ArrayList<MethodFullInfo>(methodIds.size());
        for(String id: methodIds) {
        	returnVal.add(localGitDB.loadMethodData(id).getMethodFullInfo());
        }
        /// END SIMPLE TEST
        
        
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
        //END get_method_spec
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
