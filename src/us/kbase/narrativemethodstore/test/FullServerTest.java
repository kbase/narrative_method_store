package us.kbase.narrativemethodstore.test;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import us.kbase.common.service.Tuple2;
import us.kbase.narrativemethodstore.Category;
import us.kbase.narrativemethodstore.GetMethodParams;
import us.kbase.narrativemethodstore.ListCategoriesParams;
import us.kbase.narrativemethodstore.ListParams;
import us.kbase.narrativemethodstore.MethodBriefInfo;
import us.kbase.narrativemethodstore.MethodFullInfo;
import us.kbase.narrativemethodstore.MethodSpec;
import us.kbase.narrativemethodstore.NarrativeMethodStoreClient;
import us.kbase.narrativemethodstore.NarrativeMethodStoreServer;

/*
 * 
 * 
 */
public class FullServerTest {
	
	private static File tempDir;
	
	private static NarrativeMethodStoreServer SERVER;
	private static NarrativeMethodStoreClient CLIENT;
	
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
		Map<String, String> methods = CLIENT.listMethodIdsAndNames();
		assertTrue("Testing that test_method_1 returns from listMethodIdsAndNames()",
				methods.get("test_method_1").equals("Test Method 1"));
	}
	
	
	@Test
	public void testListMethods() throws Exception {
		ListParams params = new ListParams();
		List<MethodBriefInfo> methods = CLIENT.listMethods(params);
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
		Tuple2<Map<String, Category>, Map<String, MethodBriefInfo>> methods = CLIENT.listCategories(params);
		
		//first just check that the method did not return methods if we did not request them
		assertTrue("We should not get methods from listCategories if we did not ask.", methods.getE2().size()==0);
		assertTrue("We should get categories from listCategories.", methods.getE1().size()>0);
		assertTrue("We should get the proper category name for testmethods.", methods.getE1().get("testmethods").getName().equals("Test Methods"));
		
		params = new ListCategoriesParams().withLoadMethods(1L);
		methods = CLIENT.listCategories(params);
		
		//first just check that the method did not return methods if we did not request them
		assertTrue("We should get methods from listCategories if we asked for it.", methods.getE2().size()>0);
		assertTrue("We should get a proper method in the methods returned by listCategories.", methods.getE2().get("test_method_1").getName().equals("Test Method 1"));
		assertTrue("We should get categories from listCategories.", methods.getE1().size()>0);
		assertTrue("We should get the proper category name for testmethods.", methods.getE1().get("testmethods").getName().equals("Test Methods"));
		
	}
	
	
	@Test
	public void testListMethodsFullInfo() throws Exception {
		ListParams params = new ListParams();
		List<MethodFullInfo> methods = CLIENT.listMethodsFullInfo(params);
		boolean foundTestMethod1 = false;
		for(MethodFullInfo m : methods) {
			
			// check specific things in specific test methods
			if(m.getId().equals("test_method_1")) {
				foundTestMethod1 = true;
				
				assertTrue("Testing that test_method_1 name from listMethodsFullInfo is correct",
						m.getName().equals("Test Method 1"));
				assertTrue("Testing that test_method_1 ver from listMethodsFullInfo is correct",
						m.getVer().equals("1.0.1"));
				assertTrue("Testing that test_method_1 id from listMethodsFullInfo is correct",
						m.getId().equals("test_method_1"));
				assertTrue("Testing that test_method_1 categories from listMethodsFullInfo is correct",
						m.getCategories().get(0).equals("testmethods"));
				
				assertTrue("Testing that test_method_1 description from listMethodsFullInfo is present",
						m.getDescription().trim().length()>0);
				assertTrue("Testing that test_method_1 technical description from listMethodsFullInfo is present",
						m.getTechnicalDescription().trim().length()>0);
			}
		}
		assertTrue("Testing that test_method_1 was returned from listMethodsFullInfo",
				foundTestMethod1);
	}

	
	@Test
	public void testListMethodsSpec() throws Exception {
		ListParams params = new ListParams();
		List<MethodSpec> methods = CLIENT.listMethodsSpec(params);
		boolean foundTestMethod1 = false;
		for(MethodSpec m : methods) {
			// check specific things in specific test methods
			if(m.getInfo().getId().equals("test_method_1")) {
				foundTestMethod1 = true;
				
				assertTrue("Testing that test_method_1 name from listMethodSpec is correct",
						m.getInfo().getName().equals("Test Method 1"));
				assertTrue("Testing that test_method_1 ver from listMethodSpec is correct",
						m.getInfo().getVer().equals("1.0.1"));
				assertTrue("Testing that test_method_1 id from listMethodSpec is correct",
						m.getInfo().getId().equals("test_method_1"));
				assertTrue("Testing that test_method_1 categories from listMethodSpec is correct",
						m.getInfo().getCategories().get(0).equals("testmethods"));
				
				assertTrue("Testing that test_method_1 from listMethodSpec has 2 parameters",
						m.getParameters().size()==2);
				
				assertTrue("Testing that test_method_1 from listMethodSpec parameter id is correct",
						m.getParameters().get(0).getId().equals("genome"));
				assertTrue("Testing that test_method_1 from listMethodSpec parameter name is correct",
						m.getParameters().get(0).getUiName().equals("Genome"));
				assertTrue("Testing that test_method_1 from listMethodSpec parameter short hint is correct",
						m.getParameters().get(0).getShortHint().equals("The genome object you wish to test."));
				assertTrue("Testing that test_method_1 from listMethodSpec parameter short hint is correct",
						m.getParameters().get(0).getShortHint().equals("The genome object you wish to test."));
				assertTrue("Testing that test_method_1 from listMethodSpec parameter valid ws type is correct",
						m.getParameters().get(0).getTextOptions().getValidWsTypes().get(0).equals("KBaseGenomes.Genome"));
				assertTrue("Testing that test_method_1 from listMethodSpec parameter valid ws type is correct",
						m.getParameters().get(0).getTextOptions().getValidWsTypes().get(1).equals("KBaseGenomes.PlantGenome"));
				
				assertTrue("Testing that test_method_1 output widget from listMethodSpec is correct",
						m.getWidgets().getOutput().equals("KBaseDefaultViewer"));
			} else if (m.getInfo().getId().equals("test_method_3")) {
				assertEquals(5, m.getParameters().size());
				////////////////////////
				assertEquals("param0", m.getParameters().get(0).getId());
				assertEquals("checkbox", m.getParameters().get(0).getFieldType());
				assertEquals(10L, (long)m.getParameters().get(0).getCheckboxOptions().getCheckedValue());
				assertEquals(-10L, (long)m.getParameters().get(0).getCheckboxOptions().getUncheckedValue());
				////////////////////////
				assertEquals("param1", m.getParameters().get(1).getId());
				assertEquals("floatslider", m.getParameters().get(1).getFieldType());
				assertEquals(1.0, (double)m.getParameters().get(1).getFloatsliderOptions().getMin(), 1e-10);
				assertEquals(10.0, (double)m.getParameters().get(1).getFloatsliderOptions().getMax(), 1e-10);
				////////////////////////
				assertEquals("param2", m.getParameters().get(2).getId());
				assertEquals("textarea", m.getParameters().get(2).getFieldType());
				assertEquals(10L, (long)m.getParameters().get(2).getTextareaOptions().getNRows());
				////////////////////////
				assertEquals("param3", m.getParameters().get(3).getId());
				assertEquals("dropdown", m.getParameters().get(3).getFieldType());
				assertEquals(2, m.getParameters().get(3).getDropdownOptions().getIdsToOptions().size());
				assertEquals("First", m.getParameters().get(3).getDropdownOptions().getIdsToOptions().get("item0"));
				////////////////////////
				assertEquals("param4", m.getParameters().get(4).getId());
				assertEquals("radio", m.getParameters().get(4).getFieldType());
				assertEquals(2, m.getParameters().get(4).getRadioOptions().getIdsToOptions().size());
				assertEquals("First", m.getParameters().get(4).getRadioOptions().getIdsToOptions().get("item0"));
				assertEquals("First tooltip", m.getParameters().get(4).getRadioOptions().getIdsToTooltip().get("item0"));
			}
		}
		assertTrue("Testing that test_method_1 was returned from listMethodSpec",
				foundTestMethod1);
	}
	
	
	@Test
	public void getMethodBriefInfo() throws Exception {
		GetMethodParams params = new GetMethodParams().withIds(Arrays.asList("test_method_1"));
		List<MethodBriefInfo> methods = CLIENT.getMethodBriefInfo(params);
		assertTrue("Testing that test_method_1 can be fetched from getMethodBriefInfo",
				methods.size()==1);
		
		MethodBriefInfo m = methods.get(0);
		assertTrue("Testing that test_method_1 name from getMethodBriefInfo is correct",
				m.getName().equals("Test Method 1"));
		assertTrue("Testing that test_method_1 ver from getMethodBriefInfo is correct",
				m.getVer().equals("1.0.1"));
		assertTrue("Testing that test_method_1 id from getMethodBriefInfo is correct",
				m.getId().equals("test_method_1"));
		assertTrue("Testing that test_method_1 categories from getMethodBriefInfo is correct",
				m.getCategories().get(0).equals("testmethods"));
	}
	
	
	@Test
	public void testGetMethodFullInfo() throws Exception {
		GetMethodParams params = new GetMethodParams().withIds(Arrays.asList("test_method_1"));
		List<MethodFullInfo> methods = CLIENT.getMethodFullInfo(params);
		assertTrue("Testing that test_method_1 can be fetched from getMethodFullInfo",
				methods.size()==1);
		
		MethodFullInfo m = methods.get(0);
		assertTrue("Testing that test_method_1 name from getMethodFullInfo is correct",
				m.getName().equals("Test Method 1"));
		assertTrue("Testing that test_method_1 ver from getMethodFullInfo is correct",
				m.getVer().equals("1.0.1"));
		assertTrue("Testing that test_method_1 id from getMethodFullInfo is correct",
				m.getId().equals("test_method_1"));
		assertTrue("Testing that test_method_1 categories from getMethodFullInfo is correct",
				m.getCategories().get(0).equals("testmethods"));
		
		assertTrue("Testing that test_method_1 description from getMethodFullInfo is present",
				m.getDescription().trim().length()>0);
		assertTrue("Testing that test_method_1 technical description from getMethodFullInfo is present",
				m.getTechnicalDescription().trim().length()>0);
	}
	
	
	@Test
	public void testGetMethodSpec() throws Exception {
		GetMethodParams params = new GetMethodParams().withIds(Arrays.asList("test_method_1"));
		List<MethodSpec> methods = CLIENT.getMethodSpec(params);
		assertTrue("Testing that test_method_1 can be fetched from getMethodSpec",
				methods.size()==1);
		
		MethodSpec m = methods.get(0);
		assertTrue("Testing that test_method_1 name from listMethodSpec is correct",
				m.getInfo().getName().equals("Test Method 1"));
		assertTrue("Testing that test_method_1 ver from listMethodSpec is correct",
				m.getInfo().getVer().equals("1.0.1"));
		assertTrue("Testing that test_method_1 id from listMethodSpec is correct",
				m.getInfo().getId().equals("test_method_1"));
		assertTrue("Testing that test_method_1 categories from listMethodSpec is correct",
				m.getInfo().getCategories().get(0).equals("testmethods"));

		assertTrue("Testing that test_method_1 from listMethodSpec has 2 parameters",
				m.getParameters().size()==2);

		assertTrue("Testing that test_method_1 from listMethodSpec parameter id is correct",
				m.getParameters().get(0).getId().equals("genome"));
		assertTrue("Testing that test_method_1 from listMethodSpec parameter name is correct",
				m.getParameters().get(0).getUiName().equals("Genome"));
		assertTrue("Testing that test_method_1 from listMethodSpec parameter short hint is correct",
				m.getParameters().get(0).getShortHint().equals("The genome object you wish to test."));
		assertTrue("Testing that test_method_1 from listMethodSpec parameter short hint is correct",
				m.getParameters().get(0).getShortHint().equals("The genome object you wish to test."));
		assertTrue("Testing that test_method_1 from listMethodSpec parameter valid ws type is correct",
				m.getParameters().get(0).getTextOptions().getValidWsTypes().get(0).equals("KBaseGenomes.Genome"));
		assertTrue("Testing that test_method_1 from listMethodSpec parameter valid ws type is correct",
				m.getParameters().get(0).getTextOptions().getValidWsTypes().get(1).equals("KBaseGenomes.PlantGenome"));

		assertTrue("Testing that test_method_1 output widget from listMethodSpec is correct",
				m.getWidgets().getOutput().equals("KBaseDefaultViewer"));
	}
	
	@Test
	public void testErrors() throws Exception {
		Map<String, MethodBriefInfo> methodBriefInfo = CLIENT.listCategories(new ListCategoriesParams().withLoadMethods(1L)).getE2();
		MethodBriefInfo error1 = methodBriefInfo.get("test_error_1");
		Assert.assertTrue(error1.getLoadingError(), error1.getLoadingError().contains("Unexpected character ('{' (code 123)): was expecting double-quote to start field name\n at [Source: java.io.StringReader"));
		MethodBriefInfo error2 = methodBriefInfo.get("test_error_2");
		Assert.assertEquals(error2.getLoadingError(), "Can't find sub-node [parameters] within path [/] in spec.json");
		MethodBriefInfo error3 = methodBriefInfo.get("test_error_3");
		Assert.assertEquals(error3.getLoadingError(), "Can't find sub-node [id] within path [parameters/0] in spec.json");
		MethodBriefInfo error4 = methodBriefInfo.get("test_error_4");
		Assert.assertEquals(error4.getLoadingError(), "Can't find property [name] within path [/] in display.yaml");
		MethodBriefInfo error5 = methodBriefInfo.get("test_error_5");
		Assert.assertEquals(error5.getLoadingError(), "Can't find property [ui-name] within path [parameters/genome] in display.yaml");
		for (String errorId : methodBriefInfo.keySet()) {
			if (methodBriefInfo.get(errorId).getLoadingError() != null && !errorId.startsWith("test_error_")) {
				System.out.println("Unexpected error: " + methodBriefInfo.get(errorId).getLoadingError());
				Assert.fail(methodBriefInfo.get(errorId).getLoadingError());
			}
		}
	}
	
	@Test
	public void testParamMapping() throws Exception {
		MethodSpec spec = CLIENT.getMethodSpec(new GetMethodParams().withIds(Arrays.asList("test_method_2"))).get(0);
		assertNotNull(spec.getBehavior().getKbServiceUrl());
		assertNotNull(spec.getBehavior().getKbServiceName());
		assertNotNull(spec.getBehavior().getKbServiceMethod());
		assertEquals("genome", spec.getBehavior().getKbServiceInputMapping().get(0).getInputParameter());
		assertNotNull(spec.getBehavior().getKbServiceInputMapping().get(0).getTargetProperty());
		assertNotNull(spec.getBehavior().getKbServiceInputMapping().get(0).getTargetTypeTransform());
		assertEquals("genome_", spec.getBehavior().getKbServiceInputMapping().get(0).getGeneratedValue().getPrefix());
		assertEquals(8L, (long)spec.getBehavior().getKbServiceInputMapping().get(0).getGeneratedValue().getSymbols());
		assertEquals(".obj", spec.getBehavior().getKbServiceInputMapping().get(0).getGeneratedValue().getSuffix());
		assertEquals("workspace", spec.getBehavior().getKbServiceInputMapping().get(1).getNarrativeSystemVariable());		
		assertNotNull(spec.getBehavior().getKbServiceInputMapping().get(1).getTargetArgumentPosition());
		assertEquals("[0,\"1\",2.0]", spec.getBehavior().getKbServiceOutputMapping().get(0).getConstantValue().toJsonString());
		assertEquals("ret1", spec.getBehavior().getKbServiceOutputMapping().get(0).getTargetProperty());
		assertEquals("[key1, key2]", spec.getBehavior().getKbServiceOutputMapping().get(1).getServiceMethodOutputPath().toString());
		assertEquals("re2", spec.getBehavior().getKbServiceOutputMapping().get(1).getTargetProperty());
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
		CLIENT = new NarrativeMethodStoreClient(new URL("http://localhost:" + SERVER.getServerPort()));
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
