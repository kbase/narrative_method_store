package us.kbase.narrativemethodstore.test;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import us.kbase.common.service.Tuple2;
import us.kbase.narrativemethodstore.Category;
import us.kbase.narrativemethodstore.ListCategoriesParams;
import us.kbase.narrativemethodstore.ListParams;
import us.kbase.narrativemethodstore.MethodBriefInfo;
import us.kbase.narrativemethodstore.NarrativeMethodStoreServer;

/*
 * 
 * 
 */
public class FullServerTest {
	
	private static File tempDir;
	
	private static NarrativeMethodStoreServer SERVER;
	
	private static boolean removeTempDir;
	
	private static class ServerThread extends Thread {
		private NarrativeMethodStoreServer server;
		private ServerThread(NarrativeMethodStoreServer server) {
			this.server = server;
		}
		public void run() {
			try {
				server.startupServer();
			} catch (Exception e) {
				System.err.println("Can't start server:");
				e.printStackTrace();
			}
		}
	}
	
	//http://quirkygba.blogspot.com/2009/11/setting-environment-variables-in-java.html
	@SuppressWarnings("unchecked")
	private static Map<String, String> getenv() throws NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException {
		Map<String, String> unmodifiable = System.getenv();
		Class<?> cu = unmodifiable.getClass();
		Field m = cu.getDeclaredField("m");
		m.setAccessible(true);
		return (Map<String, String>) m.get(unmodifiable);
	}
	
	
	
	@Test
	public void testListMethodIds() throws Exception {
		Map<String, String> methods = SERVER.listMethodIdsAndNames();
		assertTrue("Testing that test_method_1 returns from listMethodIdsAndNames()",
				methods.get("test_method_1").equals("Test Method 1"));
	}
	
	@Test
	public void testListMethods() throws Exception {
		ListParams params = new ListParams();
		List<MethodBriefInfo> methods = SERVER.listMethods(params);
		boolean foundTestMethod1 = false;
		for(MethodBriefInfo m : methods) {
			
			// check specific things in specific test methods
			if(m.getId().equals("test_method_1")) {
				foundTestMethod1 = true;
				
				assertTrue("Testing that test_method_1 name in brief info from listMethods is correct",
						m.getName().equals("Test Method 1"));
				assertTrue("Testing that test_method_1 ver in brief info from listMethods is correct",
						m.getVer().equals("1.0.1"));
				assertTrue("Testing that test_method_1 id in brief info from listMethods is correct",
						m.getId().equals("test_method_1"));
				assertTrue("Testing that test_method_1 categories in brief info from listMethods is correct",
						m.getCategories().get(0).equals("testmethods"));
			}
		}

		assertTrue("Testing that test_method_1 was returned from listMethods",
				foundTestMethod1);
	}
	
	@Test
	public void testListCategories() throws Exception {
		ListCategoriesParams params = new ListCategoriesParams().withLoadMethods(0L);
		Tuple2<Map<String, Category>, Map<String, MethodBriefInfo>> methods = SERVER.listCategories(params);
		
		//first just check that the method did not return methods if we did not request them
		assertTrue("We should not get methods from listCategories if we did not ask.", methods.getE2().size()==0);
		assertTrue("We should get categories from listCategories.", methods.getE1().size()>0);
		assertTrue("We should get the proper category name for testmethods.", methods.getE1().get("testmethods").getName().equals("Test Methods"));
		
		params = new ListCategoriesParams().withLoadMethods(1L);
		methods = SERVER.listCategories(params);
		
		//first just check that the method did not return methods if we did not request them
		assertTrue("We should get methods from listCategories if we asked for it.", methods.getE2().size()>0);
		assertTrue("We should get a proper method in the methods returned by listCategories.", methods.getE2().get("test_method_1").getName().equals("Test Method 1"));
		assertTrue("We should get categories from listCategories.", methods.getE1().size()>0);
		assertTrue("We should get the proper category name for testmethods.", methods.getE1().get("testmethods").getName().equals("Test Methods"));
	}
	
	
	
	
	
	
	
	
	
	
	@BeforeClass
	public static void setUpClass() throws Exception {

		// Parse the test config variables
		String tempDirName = System.getProperty("test.temp-dir");
		
		String gitRepo = System.getProperty("test.method-spec-git-repo");
		String gitRepoBranch = System.getProperty("test.method-spec-git-repo-branch");
		String gitRepoRefreshRate = System.getProperty("test.method-spec-git-repo-refresh-rate");
		String gitRepoCacheSize = System.getProperty("test.method-spec-cache-size");
		
		String s = System.getProperty("test.remove-temp-dir");
		removeTempDir = false;
		if(s!=null) {
			if(s.trim().equals("1") || s.trim().equals("yes")) {
				removeTempDir = true;
			}
		}
		
		System.out.println("test.temp-dir    = " + tempDirName);
		
		System.out.println("test.method-spec-git-repo              = " + gitRepo);
		System.out.println("test.method-spec-git-repo-branch       = " + gitRepoBranch);
		System.out.println("test.method-spec-git-repo-refresh-rate = " + gitRepoRefreshRate);
		System.out.println("test.method-spec-cache-size            = " + gitRepoCacheSize);
		
		//create the temp directory for this test
		tempDir = new File(tempDirName);
		if (!tempDir.exists())
			tempDir.mkdirs();
		
		//create the server config file
		File iniFile = File.createTempFile("test", ".cfg", tempDir);
		if (iniFile.exists()) {
			iniFile.delete();
		}
		System.out.println("Created temporary config file: " + iniFile.getAbsolutePath());
		
		Ini ini = new Ini();
		Section ws = ini.add("NarrativeMethodStore");
		ws.add("method-spec-git-repo", gitRepo);
		ws.add("method-spec-git-repo-branch", gitRepoBranch);
		ws.add("method-spec-git-repo-local-dir", tempDir.getAbsolutePath()+"/narrative_method_specs");
		ws.add("method-spec-git-repo-refresh-rate", gitRepoRefreshRate);
		ws.add("method-spec-cache-size", gitRepoCacheSize);
		
		ini.store(iniFile);
		iniFile.deleteOnExit();

		Map<String, String> env = getenv();
		env.put("KB_DEPLOYMENT_CONFIG", iniFile.getAbsolutePath());
		env.put("KB_SERVICE_NAME", "NarrativeMethodStore");

		SERVER = new NarrativeMethodStoreServer();
		new ServerThread(SERVER).start();
		System.out.println("Main thread waiting for server to start up");
		while (SERVER.getServerPort() == null) {
			Thread.sleep(100);
		}
		System.out.println("Test server listening on "+SERVER.getServerPort() );
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		if (SERVER != null) {
			System.out.print("Killing narrative method store server... ");
			SERVER.stopServer();
			System.out.println("Done");
		}
		if(removeTempDir) {
			FileUtils.deleteDirectory(tempDir);
		}
	}
	
}
