package us.kbase.narrativemethodstore;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import us.kbase.auth.AuthToken;
import us.kbase.common.service.JsonClientCaller;
import us.kbase.common.service.JsonClientException;
import us.kbase.common.service.Tuple4;

/**
 * <p>Original spec-file module name: NarrativeMethodStore</p>
 * <pre>
 * </pre>
 */
public class NarrativeMethodStoreClient {
    private JsonClientCaller caller;
    private static URL DEFAULT_URL = null;
    static {
        try {
            DEFAULT_URL = new URL("https://kbase.us/services/narrative_method_store/rpc");
        } catch (MalformedURLException mue) {
            throw new RuntimeException("Compile error in client - bad url compiled");
        }
    }

    /** Constructs a client with the default url and no user credentials.*/
    public NarrativeMethodStoreClient() {
       caller = new JsonClientCaller(DEFAULT_URL);
    }


    /** Constructs a client with a custom URL and no user credentials.
     * @param url the URL of the service.
     */
    public NarrativeMethodStoreClient(URL url) {
        caller = new JsonClientCaller(url);
    }

    /** Get the URL of the service with which this client communicates.
     * @return the service URL.
     */
    public URL getURL() {
        return caller.getURL();
    }

    /** Set the timeout between establishing a connection to a server and
     * receiving a response. A value of zero or null implies no timeout.
     * @param milliseconds the milliseconds to wait before timing out when
     * attempting to read from a server.
     */
    public void setConnectionReadTimeOut(Integer milliseconds) {
        this.caller.setConnectionReadTimeOut(milliseconds);
    }

    /** Check if this client allows insecure http (vs https) connections.
     * @return true if insecure connections are allowed.
     */
    public boolean isInsecureHttpConnectionAllowed() {
        return caller.isInsecureHttpConnectionAllowed();
    }

    /** Deprecated. Use isInsecureHttpConnectionAllowed().
     * @deprecated
     */
    public boolean isAuthAllowedForHttp() {
        return caller.isAuthAllowedForHttp();
    }

    /** Set whether insecure http (vs https) connections should be allowed by
     * this client.
     * @param allowed true to allow insecure connections. Default false
     */
    public void setIsInsecureHttpConnectionAllowed(boolean allowed) {
        caller.setInsecureHttpConnectionAllowed(allowed);
    }

    /** Deprecated. Use setInsecureHttpConnectionAllowed().
     * @deprecated
     */
    public void setAuthAllowedForHttp(boolean isAuthAllowedForHttp) {
        caller.setAuthAllowedForHttp(isAuthAllowedForHttp);
    }

    /** Set whether all SSL certificates, including self-signed certificates,
     * should be trusted.
     * @param trustAll true to trust all certificates. Default false.
     */
    public void setAllSSLCertificatesTrusted(final boolean trustAll) {
        caller.setAllSSLCertificatesTrusted(trustAll);
    }
    
    /** Check if this client trusts all SSL certificates, including
     * self-signed certificates.
     * @return true if all certificates are trusted.
     */
    public boolean isAllSSLCertificatesTrusted() {
        return caller.isAllSSLCertificatesTrusted();
    }
    /** Sets streaming mode on. In this case, the data will be streamed to
     * the server in chunks as it is read from disk rather than buffered in
     * memory. Many servers are not compatible with this feature.
     * @param streamRequest true to set streaming mode on, false otherwise.
     */
    public void setStreamingModeOn(boolean streamRequest) {
        caller.setStreamingModeOn(streamRequest);
    }

    /** Returns true if streaming mode is on.
     * @return true if streaming mode is on.
     */
    public boolean isStreamingModeOn() {
        return caller.isStreamingModeOn();
    }

    public void _setFileForNextRpcResponse(File f) {
        caller.setFileForNextRpcResponse(f);
    }

    /**
     * <p>Original spec-file function name: ver</p>
     * <pre>
     * Returns the current running version of the NarrativeMethodStore.
     * </pre>
     * @return   instance of String
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public String ver() throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        TypeReference<List<String>> retType = new TypeReference<List<String>>() {};
        List<String> res = caller.jsonrpcCall("NarrativeMethodStore.ver", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: status</p>
     * <pre>
     * Simply check the status of this service to see what Spec repository it is
     * using, and what commit it is on
     * </pre>
     * @return   instance of type {@link us.kbase.narrativemethodstore.Status Status}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public Status status() throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        TypeReference<List<Status>> retType = new TypeReference<List<Status>>() {};
        List<Status> res = caller.jsonrpcCall("NarrativeMethodStore.status", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: list_categories</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListCategoriesParams ListCategoriesParams}
     * @return   multiple set: (1) parameter "categories" of mapping from String to type {@link us.kbase.narrativemethodstore.Category Category}, (2) parameter "methods" of mapping from String to type {@link us.kbase.narrativemethodstore.MethodBriefInfo MethodBriefInfo}, (3) parameter "apps" of mapping from String to type {@link us.kbase.narrativemethodstore.AppBriefInfo AppBriefInfo}, (4) parameter "types" of mapping from String to type {@link us.kbase.narrativemethodstore.TypeInfo TypeInfo}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public Tuple4<Map<String,Category>, Map<String,MethodBriefInfo>, Map<String,AppBriefInfo>, Map<String,TypeInfo>> listCategories(ListCategoriesParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<Tuple4<Map<String,Category>, Map<String,MethodBriefInfo>, Map<String,AppBriefInfo>, Map<String,TypeInfo>>> retType = new TypeReference<Tuple4<Map<String,Category>, Map<String,MethodBriefInfo>, Map<String,AppBriefInfo>, Map<String,TypeInfo>>>() {};
        Tuple4<Map<String,Category>, Map<String,MethodBriefInfo>, Map<String,AppBriefInfo>, Map<String,TypeInfo>> res = caller.jsonrpcCall("NarrativeMethodStore.list_categories", args, retType, true, false);
        return res;
    }

    /**
     * <p>Original spec-file function name: get_category</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetCategoryParams GetCategoryParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.Category Category}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<Category> getCategory(GetCategoryParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<Category>>> retType = new TypeReference<List<List<Category>>>() {};
        List<List<Category>> res = caller.jsonrpcCall("NarrativeMethodStore.get_category", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: list_methods</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.MethodBriefInfo MethodBriefInfo}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<MethodBriefInfo> listMethods(ListParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<MethodBriefInfo>>> retType = new TypeReference<List<List<MethodBriefInfo>>>() {};
        List<List<MethodBriefInfo>> res = caller.jsonrpcCall("NarrativeMethodStore.list_methods", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: list_methods_full_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.MethodFullInfo MethodFullInfo}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<MethodFullInfo> listMethodsFullInfo(ListParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<MethodFullInfo>>> retType = new TypeReference<List<List<MethodFullInfo>>>() {};
        List<List<MethodFullInfo>> res = caller.jsonrpcCall("NarrativeMethodStore.list_methods_full_info", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: list_methods_spec</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.MethodSpec MethodSpec}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<MethodSpec> listMethodsSpec(ListParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<MethodSpec>>> retType = new TypeReference<List<List<MethodSpec>>>() {};
        List<List<MethodSpec>> res = caller.jsonrpcCall("NarrativeMethodStore.list_methods_spec", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: list_method_ids_and_names</p>
     * <pre>
     * </pre>
     * @return   instance of mapping from String to String
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public Map<String,String> listMethodIdsAndNames() throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        TypeReference<List<Map<String,String>>> retType = new TypeReference<List<Map<String,String>>>() {};
        List<Map<String,String>> res = caller.jsonrpcCall("NarrativeMethodStore.list_method_ids_and_names", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: list_apps</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.AppBriefInfo AppBriefInfo}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<AppBriefInfo> listApps(ListParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<AppBriefInfo>>> retType = new TypeReference<List<List<AppBriefInfo>>>() {};
        List<List<AppBriefInfo>> res = caller.jsonrpcCall("NarrativeMethodStore.list_apps", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: list_apps_full_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.AppFullInfo AppFullInfo}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<AppFullInfo> listAppsFullInfo(ListParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<AppFullInfo>>> retType = new TypeReference<List<List<AppFullInfo>>>() {};
        List<List<AppFullInfo>> res = caller.jsonrpcCall("NarrativeMethodStore.list_apps_full_info", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: list_apps_spec</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.AppSpec AppSpec}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<AppSpec> listAppsSpec(ListParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<AppSpec>>> retType = new TypeReference<List<List<AppSpec>>>() {};
        List<List<AppSpec>> res = caller.jsonrpcCall("NarrativeMethodStore.list_apps_spec", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: list_app_ids_and_names</p>
     * <pre>
     * </pre>
     * @return   instance of mapping from String to String
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public Map<String,String> listAppIdsAndNames() throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        TypeReference<List<Map<String,String>>> retType = new TypeReference<List<Map<String,String>>>() {};
        List<Map<String,String>> res = caller.jsonrpcCall("NarrativeMethodStore.list_app_ids_and_names", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: list_types</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.ListParams ListParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.TypeInfo TypeInfo}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<TypeInfo> listTypes(ListParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<TypeInfo>>> retType = new TypeReference<List<List<TypeInfo>>>() {};
        List<List<TypeInfo>> res = caller.jsonrpcCall("NarrativeMethodStore.list_types", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: get_method_brief_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetMethodParams GetMethodParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.MethodBriefInfo MethodBriefInfo}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<MethodBriefInfo> getMethodBriefInfo(GetMethodParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<MethodBriefInfo>>> retType = new TypeReference<List<List<MethodBriefInfo>>>() {};
        List<List<MethodBriefInfo>> res = caller.jsonrpcCall("NarrativeMethodStore.get_method_brief_info", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: get_method_full_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetMethodParams GetMethodParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.MethodFullInfo MethodFullInfo}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<MethodFullInfo> getMethodFullInfo(GetMethodParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<MethodFullInfo>>> retType = new TypeReference<List<List<MethodFullInfo>>>() {};
        List<List<MethodFullInfo>> res = caller.jsonrpcCall("NarrativeMethodStore.get_method_full_info", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: get_method_spec</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetMethodParams GetMethodParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.MethodSpec MethodSpec}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<MethodSpec> getMethodSpec(GetMethodParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<MethodSpec>>> retType = new TypeReference<List<List<MethodSpec>>>() {};
        List<List<MethodSpec>> res = caller.jsonrpcCall("NarrativeMethodStore.get_method_spec", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: get_app_brief_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetAppParams GetAppParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.AppBriefInfo AppBriefInfo}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<AppBriefInfo> getAppBriefInfo(GetAppParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<AppBriefInfo>>> retType = new TypeReference<List<List<AppBriefInfo>>>() {};
        List<List<AppBriefInfo>> res = caller.jsonrpcCall("NarrativeMethodStore.get_app_brief_info", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: get_app_full_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetAppParams GetAppParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.AppFullInfo AppFullInfo}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<AppFullInfo> getAppFullInfo(GetAppParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<AppFullInfo>>> retType = new TypeReference<List<List<AppFullInfo>>>() {};
        List<List<AppFullInfo>> res = caller.jsonrpcCall("NarrativeMethodStore.get_app_full_info", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: get_app_spec</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetAppParams GetAppParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.AppSpec AppSpec}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<AppSpec> getAppSpec(GetAppParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<AppSpec>>> retType = new TypeReference<List<List<AppSpec>>>() {};
        List<List<AppSpec>> res = caller.jsonrpcCall("NarrativeMethodStore.get_app_spec", args, retType, true, false);
        return res.get(0);
    }

    /**
     * <p>Original spec-file function name: get_type_info</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link us.kbase.narrativemethodstore.GetTypeParams GetTypeParams}
     * @return   instance of list of type {@link us.kbase.narrativemethodstore.TypeInfo TypeInfo}
     * @throws IOException if an IO exception occurs
     * @throws JsonClientException if a JSON RPC exception occurs
     */
    public List<TypeInfo> getTypeInfo(GetTypeParams params) throws IOException, JsonClientException {
        List<Object> args = new ArrayList<Object>();
        args.add(params);
        TypeReference<List<List<TypeInfo>>> retType = new TypeReference<List<List<TypeInfo>>>() {};
        List<List<TypeInfo>> res = caller.jsonrpcCall("NarrativeMethodStore.get_type_info", args, retType, true, false);
        return res.get(0);
    }
}
