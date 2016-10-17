package us.kbase.narrativemethodstore.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import us.kbase.auth.AuthService;
import us.kbase.auth.AuthToken;
import us.kbase.common.service.ServerException;
import us.kbase.common.service.Tuple4;
import us.kbase.common.service.UObject;
import us.kbase.narrativemethodstore.AppBriefInfo;
import us.kbase.narrativemethodstore.Category;
import us.kbase.narrativemethodstore.GetMethodParams;
import us.kbase.narrativemethodstore.ListCategoriesParams;
import us.kbase.narrativemethodstore.ListReposParams;
import us.kbase.narrativemethodstore.MethodBriefInfo;
import us.kbase.narrativemethodstore.MethodFullInfo;
import us.kbase.narrativemethodstore.MethodSpec;
import us.kbase.narrativemethodstore.NarrativeMethodStoreClient;
import us.kbase.narrativemethodstore.RegisterRepoParams;
import us.kbase.narrativemethodstore.TypeInfo;

public class DevTester {
	//private static final String storeUrl = "http://dev19.berkeley.kbase.us/narrative_method_store/rpc";
	//private static final String storeUrl = "https://kbase.us/services/narrative_method_store/rpc";
    //private static final String storeUrl = "http://dev06.berkeley.kbase.us:8200/rpc";
    private static final String storeUrl = "https://ci.kbase.us/services/narrative_method_store/rpc";
	
	public static void main(String[] args) throws Exception {
	    AuthToken token = AuthService.login("kbasetest", "@Suite525").getToken();
        //AuthToken token = AuthService.login("rsutormin", "2qz3gm7c").getToken();
		NarrativeMethodStoreClient cl = new NarrativeMethodStoreClient(new URL(storeUrl), token);
		cl.setIsInsecureHttpConnectionAllowed(true);
		//showMethod(cl, "assemble_contigset_from_reads");
		//showMethod(cl, "error_fba");
		showSlashMethods(cl);
		//registerDynamicRepo(cl);
	}

	private static void registerDynamicRepo(NarrativeMethodStoreClient cl) throws Exception {
        String gitUrl = "https://github.com/kbaseIncubator/onerepotest";
        cl.registerRepo(new RegisterRepoParams().withGitUrl(gitUrl));
        //String gitUrl2 = "https://github.com/kbaseIncubator/contigcount";
        //cl.registerRepo(new RegisterRepoParams().withGitUrl(gitUrl2).withGitCommitHash("add03cfc41979c898acf679326a683a9527d5221"));
	}
	
	private static void showSlashMethods(NarrativeMethodStoreClient cl) throws Exception {
	    System.out.println("" + cl.ver());
        //System.out.println("Repos: " + cl.listRepoModuleNames(new ListReposParams()));
	    Tuple4<Map<String,Category>, Map<String,MethodBriefInfo>, Map<String,AppBriefInfo>, Map<String,TypeInfo>> data =
	            cl.listCategories(new ListCategoriesParams().withTag("dev").withLoadMethods(1L).withLoadApps(1L));
        for (MethodBriefInfo met : data.getE2().values()) {
            if (!met.getId().contains("/"))
                continue;
            System.out.println("Method: " + met.getId() + ", name=" + met.getName() + ", " +
                    "categories=" + met.getCategories() + ", error=" + met.getLoadingError());
            /*MethodFullInfo fi = cl.getMethodFullInfo(new GetMethodParams().withIds(Arrays.asList(met.getId()))).get(0);
            if ( fi.getScreenshots().size() > 0) {
                String url = storeUrl.replace("rpc", fi.getScreenshots().get(0).getUrl());
                System.out.println("Screenshot url: " + url);
                InputStream is = new URL(url).openStream();
                OutputStream os = new FileOutputStream(new File(met.getId().replace('/', '$') + ".jpg"));
                IOUtils.copy(is, os);
                os.close();
                is.close();
            }*/
            //MethodSpec ms = cl.getMethodSpec(new GetMethodParams().withIds(Arrays.asList(met.getId()))).get(0);
            //System.out.println("\tservice url: " + ms.getBehavior().getKbServiceUrl());
        }
	}

	private static void showMethod(NarrativeMethodStoreClient cl, String methodName) throws Exception {
		try {
			MethodSpec ms = cl.getMethodSpec(new GetMethodParams().withIds(Arrays.asList(methodName))).get(0);
			//System.out.println(jsonToPretty(ms));
			MethodFullInfo mfi = cl.getMethodFullInfo(new GetMethodParams().withIds(Arrays.asList(methodName))).get(0);
			System.out.println(mfi.getPublications().get(1).getDisplayText());
		} catch (ServerException ex) {
			System.out.println(ex.getData());
		}
	}
	
	private static void checkErrors(NarrativeMethodStoreClient cl) throws Exception {
		Tuple4<Map<String,Category>, Map<String,MethodBriefInfo>, Map<String,AppBriefInfo>, Map<String,TypeInfo>> data =
				cl.listCategories(new ListCategoriesParams().withLoadMethods(1L).withLoadApps(1L));
		for (Category cat : data.getE1().values()) {
			System.out.println("Category: " + cat.getId() + ", error=" + cat.getLoadingError());
		}
		for (MethodBriefInfo met : data.getE2().values()) {
			System.out.println("Method: " + met.getId() + ", categorys=" + met.getCategories() + ", error=" + met.getLoadingError());
		}
		//System.out.println(jsonToPretty(cl.getMethodSpec(new GetMethodParams().withIds(Arrays.asList("insert_genome_into_species_tree_generic"))).get(0)));
		for (AppBriefInfo app : data.getE3().values()) {
			System.out.println("App: " + app.getId() + ", categorys=" + app.getCategories() + ", error=" + app.getLoadingError());
		}
	}

	private static String jsonToPretty(Object obj) throws Exception {
		return UObject.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
	}
}
