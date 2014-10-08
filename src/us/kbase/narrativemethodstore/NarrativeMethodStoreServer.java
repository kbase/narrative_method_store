package us.kbase.narrativemethodstore;

import java.util.List;
import java.util.Map;
import us.kbase.common.service.JsonServerMethod;
import us.kbase.common.service.JsonServerServlet;

//BEGIN_HEADER
//END_HEADER

/**
 * <p>Original spec-file module name: NarrativeMethodStore</p>
 * <pre>
 * </pre>
 */
public class NarrativeMethodStoreServer extends JsonServerServlet {
    private static final long serialVersionUID = 1L;

    //BEGIN_CLASS_HEADER
    //END_CLASS_HEADER

    public NarrativeMethodStoreServer() throws Exception {
        super("NarrativeMethodStore");
        //BEGIN_CONSTRUCTOR
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
        //END ver
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
