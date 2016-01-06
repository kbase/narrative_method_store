package us.kbase.narrativemethodstore.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
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

import us.kbase.common.service.ServerException;
import us.kbase.common.service.Tuple4;
import us.kbase.narrativemethodstore.AppFullInfo;
import us.kbase.narrativemethodstore.AppSpec;
import us.kbase.narrativemethodstore.Category;
import us.kbase.narrativemethodstore.GetAppParams;
import us.kbase.narrativemethodstore.GetCategoryParams;
import us.kbase.narrativemethodstore.GetMethodParams;
import us.kbase.narrativemethodstore.GetTypeParams;
import us.kbase.narrativemethodstore.ListCategoriesParams;
import us.kbase.narrativemethodstore.ListParams;
import us.kbase.narrativemethodstore.MethodBriefInfo;
import us.kbase.narrativemethodstore.MethodFullInfo;
import us.kbase.narrativemethodstore.MethodSpec;
import us.kbase.narrativemethodstore.AppBriefInfo;
import us.kbase.narrativemethodstore.NarrativeMethodStoreClient;
import us.kbase.narrativemethodstore.NarrativeMethodStoreServer;
import us.kbase.narrativemethodstore.Publication;
import us.kbase.narrativemethodstore.RegexMatcher;
import us.kbase.narrativemethodstore.Status;
import us.kbase.narrativemethodstore.TextSubdataOptions;
import us.kbase.narrativemethodstore.TypeInfo;
import us.kbase.narrativemethodstore.ValidateAppParams;
import us.kbase.narrativemethodstore.ValidateMethodParams;
import us.kbase.narrativemethodstore.ValidateTypeParams;
import us.kbase.narrativemethodstore.ValidationResults;

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
	public void testVersion() throws Exception {
		String ver = CLIENT.ver();
		assertTrue("Testing that ver() returns a version string that looks valid",
				ver.matches("^\\d+\\.\\d+\\.\\d+$"));
		System.out.println("Testing NMS Server Version "+ver);
	}
	
	@Test
	public void testStatus() throws Exception {
		Status status = CLIENT.status();
		
		assertTrue("Testing that status() returns a git spec branch that is not null",
				status.getGitSpecBranch()!=null);
		assertTrue("Testing that status() returns a git spec branch that is not empty",
				status.getGitSpecBranch().length()>0);
		assertTrue("Testing that status() returns a git spec commit that is not null",
				status.getGitSpecCommit()!=null);
		assertTrue("Testing that status() returns a git spec commit that is not empty",
				status.getGitSpecCommit().length()>0);
		assertTrue("Testing that status() returns a git spec repo url that is not null",
				status.getGitSpecUrl()!=null);
		assertTrue("Testing that status() returns a git spec repo url that is not empty",
				status.getGitSpecUrl().length()>0);
		assertTrue("Testing that status() returns a git spec update interval that is not null",
				status.getUpdateInterval()!=null);
		assertTrue("Testing that status() returns a git spec update interval that is not empty",
				status.getUpdateInterval().length()>0);
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
		boolean foundTestMethod8 = false;
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
			if(m.getId().equals("test_method_8")) {
				foundTestMethod8 = true;
			}
		}

		assertTrue("Testing that test_method_1 was returned from listMethods",
				foundTestMethod1);
		assertTrue("Testing that test_method_8 was returned from listMethods",
				foundTestMethod8);
	}
	
	
	@Test
	public void testGetCategory() throws Exception {
		//first just check that we didn't get anything if we didn't ask for anything
		GetCategoryParams params = new GetCategoryParams().withIds(new ArrayList<String>());
		List<Category> categories = CLIENT.getCategory(params);
		assertTrue("Get categories without categories should return an empty list", categories.size()==0);
		
		//next check that what we asked for is returned
		params = new GetCategoryParams().withIds(Arrays.asList("testmethods"));
		categories = CLIENT.getCategory(params);
		assertTrue("Get categories with one valid category should return exactly one thing", categories.size()==1);
		assertTrue("The one category should be the one we asked for", categories.get(0).getId().compareTo("testmethods")==0);
		assertTrue("The one category should have the right name", categories.get(0).getName().compareTo("Test Methods")==0);
	
		// test that we don't get something if it doesn't exist
		try {
			params = new GetCategoryParams().withIds(Arrays.asList("blah_blah_blah_category"));
			categories = CLIENT.getCategory(params);
			fail("Get category with an invalid category id worked, but it shouldn't");
		} catch (ServerException e) {
			assertTrue("Getting an invalid category throws an error, and the error has the correct message",
					e.getMessage().compareTo("No category with id=blah_blah_blah_category")==0);
		}
	}
	
	
	@Test
	public void testListCategories() throws Exception {
		ListCategoriesParams params = new ListCategoriesParams().withLoadMethods(0L);
		Tuple4<Map<String,Category>, Map<String,MethodBriefInfo>, Map<String,AppBriefInfo>, Map<String,TypeInfo>> methods = CLIENT.listCategories(params);
		
		//first just check that the method did not return methods if we did not request them
		assertTrue("We should not get methods from listCategories if we did not ask.", methods.getE2().size()==0);
		assertTrue("We should get categories from listCategories.", methods.getE1().size()>0);
		assertTrue("We should get the proper category name for testmethods.", methods.getE1().get("testmethods").getName().equals("Test Methods"));
		
		params = new ListCategoriesParams().withLoadMethods(1L);
		methods = CLIENT.listCategories(params);
		
		//check that the method did not return methods if we did not request them
		assertTrue("We should get methods from listCategories if we asked for it.", methods.getE2().size()>0);
		assertTrue("We should get a proper method in the methods returned by listCategories.", methods.getE2().get("test_method_1").getName().equals("Test Method 1"));
		assertTrue("We should get categories from listCategories.", methods.getE1().size()>0);
		assertTrue("We should get the proper category name for testmethods.", methods.getE1().get("testmethods").getName().equals("Test Methods"));
		
	}
	
	
	
	
	@Test
	public void testListMethodsBriefInfo() throws Exception {
		ListParams params = new ListParams();
		List<MethodBriefInfo> methods = CLIENT.listMethods(params);
		boolean foundTestMethod1 = false;
		boolean foundTestMethod7 = false;
		for(MethodBriefInfo m : methods) {
			
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
				
				assertTrue("Testing that test_method_1 does not have an icon",
						m.getIcon()==null);
			}
			
			// check specific things in specific test methods
			if(m.getId().equals("test_method_7")) {
				foundTestMethod7 = true;
				assertTrue("Testing that test_method_7 has an icon",
						m.getIcon()!=null);
				assertTrue("Testing that test_method_7 has an icon url",
						m.getIcon().getUrl()!=null);
				assertEquals("img?method_id=test_method_7&image_name=icon.png",m.getIcon().getUrl());
			}
		}
		assertTrue("Testing that test_method_1 was returned from listMethodsFullInfo",
				foundTestMethod1);
		assertTrue("Testing that test_method_7 was returned from listMethodsFullInfo",
				foundTestMethod7);
	}
	
	
	@Test
	public void testListAppIdsAndNames() throws Exception {
		Map<String, String> apps = CLIENT.listAppIdsAndNames();

		assertTrue("listing apps and names got test_app_1",apps.containsKey("test_app_1"));
		assertTrue("listing apps and names got test_app_1, and name is correct",apps.get("test_app_1").compareTo("Test All 1")==0);
		assertTrue("listing apps and names got test_app_2",apps.containsKey("test_app_2"));
		assertTrue("listing apps and names got test_app_2, and name is correct",apps.get("test_app_1").compareTo("Test All 1")==0);
	}
	
	@Test
	public void testListAppSpecs() throws Exception {
		List<AppSpec> apps = CLIENT.listAppsSpec(new ListParams());
		boolean foundApp1 = false; boolean foundApp2 = false;
		for(AppSpec a:apps) {
			if(a.getInfo().getId().compareTo("test_app_1")==0) {
				foundApp1 = true;
			} else if (a.getInfo().getId().compareTo("test_app_2")==0) {
				foundApp2 = true;
			}
		}
		assertTrue("Testing that test_app_1 was returned from listAppsSpec",
				foundApp1);
		assertTrue("Testing that test_app_2 was returned from listAppsSpec",
				foundApp2);
	}
	
	@Test
	public void testGetAppBriefInfo() throws Exception {
		GetAppParams params = new GetAppParams().withIds(Arrays.asList("test_app_1"));
		List<AppBriefInfo> apps = CLIENT.getAppBriefInfo(params);
		
		boolean foundTestApp1 = false;
		boolean foundTestApp2 = false;
		assertTrue("Testing that exactly one app was returned as requested",apps.size()==1);
		for(AppBriefInfo a : apps) {
			// check specific things in specific test methods
			if(a.getId().equals("test_app_1")) {
				foundTestApp1 = true;
				assertTrue("Testing that test_app_1 does not have an icon",
						a.getIcon()==null);
			}
			if(a.getId().equals("test_app_2")) {
				foundTestApp2 = true;
			}
		}

		assertTrue("Testing that test_app_1 was returned from getAppBriefInfo",
				foundTestApp1);
		assertFalse("Testing that test_app_2 was not returned from getAppBriefInfo because it was not in arguements",
				foundTestApp2);
	}
	
	@Test
	public void testListApps() throws Exception {
		ListParams params = new ListParams();
		List<AppBriefInfo> apps = CLIENT.listApps(params);
	
		boolean foundTestApp1 = false;
		boolean foundTestApp2 = false;
		for(AppBriefInfo a : apps) {
			
			// check specific things in specific test methods
			if(a.getId().equals("test_app_1")) {
				foundTestApp1 = true;
				assertTrue("Testing that test_app_1 does not have an icon",
						a.getIcon()==null);
			}
			if(a.getId().equals("test_app_2")) {
				foundTestApp2 = true;
				assertTrue("Testing that test_app_2 has an icon",
						a.getIcon()!=null);
				assertTrue("Testing that test_app_2 has an icon url",
						a.getIcon().getUrl()!=null);
				assertEquals("img?method_id=test_app_2&image_name=someIcon.png",a.getIcon().getUrl());
			}
		}

		assertTrue("Testing that test_app_1 was returned from listApps",
				foundTestApp1);
		assertTrue("Testing that test_app_2 was returned from listApps",
				foundTestApp2);
	}
	
	@Test
	public void testListAppsFullInfo() throws Exception {
		ListParams params = new ListParams();
		List<AppFullInfo> methods = CLIENT.listAppsFullInfo(params);
		boolean foundTestApp1 = false;
		boolean foundTestApp2 = false;
		for(AppFullInfo a : methods) {
			
			// check specific things in specific test methods
			if(a.getId().equals("test_app_1")) {
				foundTestApp1 = true;
				
				assertTrue("Testing that test_app_1 does not have an icon",
						a.getIcon()==null);
				
				assertTrue("Testing that test_app_1 has suggestions defined",
						a.getSuggestions()!=null);
				assertTrue("Testing that test_app_1 has suggestions for related apps defined",
						a.getSuggestions().getRelatedApps()!=null);
				assertTrue("Testing that test_app_1 has suggestions for next apps defined",
						a.getSuggestions().getNextApps()!=null);
				assertTrue("Testing that test_app_1 has suggestions for related methods defined",
						a.getSuggestions().getRelatedMethods()!=null);
				assertTrue("Testing that test_app_1 has suggestions for next methods defined",
						a.getSuggestions().getNextMethods()!=null);
				assertTrue("Testing that test_app_1 has no suggestions for related apps",
						a.getSuggestions().getRelatedApps().size()==0);
				assertTrue("Testing that test_app_1 has no suggestions for next apps",
						a.getSuggestions().getNextApps().size()==0);
				assertTrue("Testing that test_app_1 has no suggestions for related methods",
						a.getSuggestions().getRelatedMethods().size()==0);
				assertTrue("Testing that test_app_1 has no suggestions for next methods",
						a.getSuggestions().getNextMethods().size()==0);
				
			}
			
			// check specific things in specific test methods
			if(a.getId().equals("test_app_2")) {
				foundTestApp2 = true;
				assertTrue("Testing that test_app_2 technical description is empty",
						a.getTechnicalDescription().trim().length()==0);
				assertTrue("Testing that test_app_2 has an icon",
						a.getIcon()!=null);
				assertTrue("Testing that test_app_2 has an icon url",
						a.getIcon().getUrl()!=null);
				assertEquals("img?method_id=test_app_2&image_name=someIcon.png",a.getIcon().getUrl());
				
				
				assertTrue("Testing that test_app_2 has suggestions defined",
						a.getSuggestions()!=null);
				assertTrue("Testing that test_app_2 has suggestions for related apps defined",
						a.getSuggestions().getRelatedApps()!=null);
				assertTrue("Testing that test_app_2 has suggestions for next apps defined",
						a.getSuggestions().getNextApps()!=null);
				assertTrue("Testing that test_app_2 has suggestions for related methods defined",
						a.getSuggestions().getRelatedMethods()!=null);
				assertTrue("Testing that test_app_2 has suggestions for next methods defined",
						a.getSuggestions().getNextMethods()!=null);
				
				assertTrue("Testing that test_app_2 has suggestions for related apps",
						a.getSuggestions().getRelatedApps().size()==1);
				assertTrue("Testing that test_app_2 has suggestions for next apps",
						a.getSuggestions().getNextApps().size()==1);
				assertTrue("Testing that test_app_2 has no suggestions for related methods",
						a.getSuggestions().getRelatedMethods().size()==0);
				assertTrue("Testing that test_app_2 has no suggestions for next methods",
						a.getSuggestions().getNextMethods().size()==0);
				
			}
		}
		assertTrue("Testing that test_app_1 was returned from listAppsFullInfo",
				foundTestApp1);
		assertTrue("Testing that test_app_2 was returned from listAppsFullInfo",
				foundTestApp2);
	}
	
	
	@Test
	public void testListMethodsFullInfo() throws Exception {
		ListParams params = new ListParams();
		List<MethodFullInfo> methods = CLIENT.listMethodsFullInfo(params);
		boolean foundTestMethod1 = false;
		boolean foundTestMethod7 = false;
		boolean foundTestMethod8 = false;
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
				assertTrue("Testing that test_method_1 does not have an icon",
						m.getIcon()==null);
				

				assertTrue("Testing that test_method_1 has suggestions defined",
						m.getSuggestions()!=null);
				assertTrue("Testing that test_method_1 has suggestions for related apps defined",
						m.getSuggestions().getRelatedApps()!=null);
				assertTrue("Testing that test_method_1 has suggestions for next apps defined",
						m.getSuggestions().getNextApps()!=null);
				assertTrue("Testing that test_method_1 has suggestions for related methods defined",
						m.getSuggestions().getRelatedMethods()!=null);
				assertTrue("Testing that test_method_1 has suggestions for next methods defined",
						m.getSuggestions().getNextMethods()!=null);
				assertTrue("Testing that test_method_1 has no suggestions for related apps",
						m.getSuggestions().getRelatedApps().size()==0);
				assertTrue("Testing that test_method_1 has no suggestions for next apps",
						m.getSuggestions().getNextApps().size()==0);
				assertTrue("Testing that test_method_1 has no suggestions for related methods",
						m.getSuggestions().getRelatedMethods().size()==0);
				assertTrue("Testing that test_method_1 has no suggestions for next methods",
						m.getSuggestions().getNextMethods().size()==0);
				
			}
			
			// check specific things in specific test methods
			if(m.getId().equals("test_method_7")) {
				foundTestMethod7 = true;
				assertTrue("Testing that test_method_7 technical description is empty",
						m.getTechnicalDescription().trim().length()==0);
				assertTrue("Testing that test_method_7 has an icon",
						m.getIcon()!=null);
				assertTrue("Testing that test_method_7 has an icon url",
						m.getIcon().getUrl()!=null);
				assertEquals("img?method_id=test_method_7&image_name=icon.png",m.getIcon().getUrl());
				
				
				assertTrue("Testing that test_method_7 has suggestions defined",
						m.getSuggestions()!=null);
				assertTrue("Testing that test_method_7 has suggestions for related apps defined",
						m.getSuggestions().getRelatedApps()!=null);
				assertTrue("Testing that test_method_7 has suggestions for next apps defined",
						m.getSuggestions().getNextApps()!=null);
				assertTrue("Testing that test_method_7 has suggestions for related methods defined",
						m.getSuggestions().getRelatedMethods()!=null);
				assertTrue("Testing that test_method_7 has suggestions for next methods defined",
						m.getSuggestions().getNextMethods()!=null);
				
				assertTrue("Testing that test_method_7 has suggestions for related apps",
						m.getSuggestions().getRelatedApps().size()==1);
				assertTrue("Testing that test_method_7 has suggestions for next apps",
						m.getSuggestions().getNextApps().size()==1);
				assertTrue("Testing that test_method_7 has suggestions for related methods",
						m.getSuggestions().getRelatedMethods().size()==1);
				assertEquals("test_method_3",
						m.getSuggestions().getRelatedMethods().get(0));
				assertTrue("Testing that test_method_7 has suggestions for next methods",
						m.getSuggestions().getNextMethods().size()==2);
				assertEquals("test_method_1",
						m.getSuggestions().getNextMethods().get(0));
				assertEquals("test_method_2",
						m.getSuggestions().getNextMethods().get(1));
				
			}
			
			// check subdata parameter in test_method_8
			if(m.getId().equals("test_method_8")) {
				foundTestMethod8 = true;
				assertTrue("Testing that test_method_8 technical description is empty",
					m.getTechnicalDescription().trim().length()==0);
				assertTrue("Testing that test_method_8 has an icon",
					m.getIcon()!=null);
			}
			
		}
		assertTrue("Testing that test_method_1 was returned from listMethodsFullInfo",
				foundTestMethod1);
		assertTrue("Testing that test_method_7 was returned from listMethodsFullInfo",
				foundTestMethod7);
		assertTrue("Testing that test_method_8 was returned from listMethodsFullInfo",
				foundTestMethod8);
	}

	
	@Test
	public void testListMethodsSpec() throws Exception {
		ListParams params = new ListParams();
		List<MethodSpec> methods = CLIENT.listMethodsSpec(params);
		boolean foundTestMethod1 = false;
		boolean foundTestMethod3 = false;
		boolean foundTestMethod4 = false;
		boolean foundTestMethod5 = false;
		boolean foundTestMethod7 = false;
		boolean foundTestMethod8 = false;
		for(MethodSpec m : methods) {
			// check specific things in specific test methods
			if(m.getInfo().getId().equals("test_method_1")) {
				foundTestMethod1 = true;
				assertEquals(0, m.getFixedParameters().size());
				
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
				foundTestMethod3 = true;
				
				assertEquals(7, m.getParameters().size());
				assertEquals(0, m.getFixedParameters().size());
				////////////////////////
				assertEquals("param0", m.getParameters().get(0).getId());
				assertEquals("checkbox", m.getParameters().get(0).getFieldType());
				assertEquals(10L, (long)m.getParameters().get(0).getCheckboxOptions().getCheckedValue());
				assertEquals(-10L, (long)m.getParameters().get(0).getCheckboxOptions().getUncheckedValue());
				////////////////////////
				assertEquals("param0.1", m.getParameters().get(1).getId());
				assertEquals("text", m.getParameters().get(1).getFieldType());
				assertEquals("parameter",m.getParameters().get(1).getUiClass());
				////////////////////////
				assertEquals("param1", m.getParameters().get(2).getId());
				assertEquals("floatslider", m.getParameters().get(2).getFieldType());
				assertEquals(1.0, (double)m.getParameters().get(2).getFloatsliderOptions().getMin(), 1e-10);
				assertEquals(10.0, (double)m.getParameters().get(2).getFloatsliderOptions().getMax(), 1e-10);
				////////////////////////
				assertEquals("param2", m.getParameters().get(3).getId());
				assertEquals("textarea", m.getParameters().get(3).getFieldType());
				assertEquals(10L, (long)m.getParameters().get(3).getTextareaOptions().getNRows());
				assertEquals("place holder here", m.getParameters().get(3).getTextareaOptions().getPlaceholder());
				////////////////////////
				assertEquals("param2.2", m.getParameters().get(4).getId());
				assertEquals("textarea", m.getParameters().get(4).getFieldType());
				assertEquals(10L, (long)m.getParameters().get(4).getTextareaOptions().getNRows());
				assertEquals("", m.getParameters().get(4).getTextareaOptions().getPlaceholder());
				////////////////////////
				assertEquals("param3", m.getParameters().get(5).getId());
				assertEquals("dropdown", m.getParameters().get(5).getFieldType());
				assertEquals(2, m.getParameters().get(5).getDropdownOptions().getOptions().size());
				assertEquals("item0", m.getParameters().get(5).getDropdownOptions().getOptions().get(0).getValue());
				////////////////////////
				assertEquals("param4", m.getParameters().get(6).getId());
				assertEquals("radio", m.getParameters().get(6).getFieldType());
				assertEquals(2, m.getParameters().get(6).getRadioOptions().getIdsToOptions().size());
				assertEquals("First", m.getParameters().get(6).getRadioOptions().getIdsToOptions().get("item0"));
				assertEquals("First tooltip", m.getParameters().get(6).getRadioOptions().getIdsToTooltip().get("item0"));
			} else if (m.getInfo().getId().equals("test_method_4")) {
				foundTestMethod4 = true;
				assertEquals(0, m.getFixedParameters().size());
				assertEquals("Test Method 4 was run on {{genome}} to produce a new genome named {{output_genome}}.\n", m.getReplacementText());
				assertEquals(new Long(1), m.getParameters().get(1).getTextOptions().getIsOutputName());
				assertEquals("output",m.getParameters().get(1).getUiClass());
				assertEquals("select a genome", m.getParameters().get(0).getTextOptions().getPlaceholder());
				assertEquals("input",m.getParameters().get(0).getUiClass());
			} else if (m.getInfo().getId().equals("test_method_5")) {
				foundTestMethod5 = true;
				
				assertEquals(4, m.getParameters().size());
				assertEquals(0, m.getFixedParameters().size());

				assertEquals("text_int_number", m.getParameters().get(0).getId());
				assertEquals("text", m.getParameters().get(0).getFieldType());
				assertEquals(new Long(0), m.getParameters().get(0).getTextOptions().getMinInt());
				assertEquals(new Long(20), m.getParameters().get(0).getTextOptions().getMaxInt());
				assertEquals(new Long(0), m.getParameters().get(0).getDisabled());
				assertEquals("parameter",m.getParameters().get(0).getUiClass());

				assertEquals("text_int_number_disabled", m.getParameters().get(1).getId());
				assertEquals("text", m.getParameters().get(1).getFieldType());
				assertEquals(new Long(0), m.getParameters().get(1).getTextOptions().getMinInt());
				assertEquals(new Long(20), m.getParameters().get(1).getTextOptions().getMaxInt());
				assertEquals(new Long(1), m.getParameters().get(1).getDisabled());
				assertEquals("parameter",m.getParameters().get(1).getUiClass());

				assertEquals("text_float_number", m.getParameters().get(2).getId());
				assertEquals("text", m.getParameters().get(2).getFieldType());
				assertEquals(new Double(0.5), m.getParameters().get(2).getTextOptions().getMinFloat());
				assertEquals(new Double(20.2), m.getParameters().get(2).getTextOptions().getMaxFloat());
				assertEquals("parameter",m.getParameters().get(2).getUiClass());

				assertEquals("regex", m.getParameters().get(3).getId());
				assertEquals("text", m.getParameters().get(3).getFieldType());
				assertEquals("parameter",m.getParameters().get(3).getUiClass());
				List<RegexMatcher> rm = m.getParameters().get(3).getTextOptions().getRegexConstraint();
				assertEquals(new Long(1), rm.get(0).getMatch());
				assertEquals("^good", rm.get(0).getRegex());
				assertEquals("input must start with good", rm.get(0).getErrorText());
				
				assertEquals(new Long(0), rm.get(1).getMatch());
				assertEquals("bad$", rm.get(1).getRegex());
				assertEquals("input cannot end in bad", rm.get(1).getErrorText());
			} else if (m.getInfo().getId().equals("test_method_7")) {
				foundTestMethod7 = true;
				assertEquals(2, m.getFixedParameters().size());

				assertEquals("FixedParam1", m.getFixedParameters().get(0).getUiName());
				assertEquals("a fixed parameter", m.getFixedParameters().get(0).getDescription());
				assertEquals("FixedParam2", m.getFixedParameters().get(1).getUiName());
				assertEquals("another fixed parameter", m.getFixedParameters().get(1).getDescription());
			} else if (m.getInfo().getId().equals("test_method_8")) {
				foundTestMethod8 = true;
				assertEquals(3, m.getParameters().size());
				assertEquals("genome_input", m.getParameters().get(0).getId());
				assertEquals("text", m.getParameters().get(0).getFieldType());
				assertNotNull(m.getParameters().get(0).getTextOptions());
				assertNull(m.getParameters().get(0).getTextsubdataOptions());
				
				assertEquals("feature_input", m.getParameters().get(1).getId());
				assertEquals("textsubdata", m.getParameters().get(1).getFieldType());
				assertNotNull(m.getParameters().get(1).getTextsubdataOptions());
				TextSubdataOptions tso = m.getParameters().get(1).getTextsubdataOptions();
				assertNotNull(tso.getSubdataSelection());
				assertEquals(new Long(0), tso.getMultiselection());
				assertEquals(new Long(1), tso.getShowSrcObj());
				assertEquals(new Long(0), tso.getAllowCustom());
				assertEquals("genome_input", tso.getSubdataSelection().getParameterId());
				assertNull(tso.getSubdataSelection().getConstantRef());
				assertEquals(3, tso.getSubdataSelection().getSubdataIncluded().size());
				assertEquals("features/[*]/id", tso.getSubdataSelection().getSubdataIncluded().get(0));
				assertEquals("features/[*]/aliases", tso.getSubdataSelection().getSubdataIncluded().get(1));
				assertEquals("features/[*]/function", tso.getSubdataSelection().getSubdataIncluded().get(2));
				assertEquals(1, tso.getSubdataSelection().getPathToSubdata().size());
				assertEquals("features", tso.getSubdataSelection().getPathToSubdata().get(0));
				assertEquals("id", tso.getSubdataSelection().getSelectionId());
				assertEquals(2, tso.getSubdataSelection().getSelectionDescription().size());
				assertEquals("aliases", tso.getSubdataSelection().getSelectionDescription().get(0));
				assertEquals("function", tso.getSubdataSelection().getSelectionDescription().get(1));
				assertEquals("({{aliases}}, {{function}})", tso.getSubdataSelection().getDescriptionTemplate());
				
				assertEquals("more_features", m.getParameters().get(2).getId());
				assertEquals("textsubdata", m.getParameters().get(2).getFieldType());
				assertNotNull(m.getParameters().get(2).getTextsubdataOptions());
				tso = m.getParameters().get(2).getTextsubdataOptions();
				assertNotNull(tso.getSubdataSelection());
				assertEquals(new Long(1), tso.getMultiselection());
				assertEquals(new Long(1), tso.getShowSrcObj());
				assertEquals(new Long(0), tso.getAllowCustom());
				
				assertNull(tso.getSubdataSelection().getParameterId());
				assertEquals(2,tso.getSubdataSelection().getConstantRef().size());
				assertEquals("12/31",tso.getSubdataSelection().getConstantRef().get(0));
				assertEquals("ws/MyObj",tso.getSubdataSelection().getConstantRef().get(1));
				assertEquals(2, tso.getSubdataSelection().getSubdataIncluded().size());
				assertEquals("features/[*]/id", tso.getSubdataSelection().getSubdataIncluded().get(0));
				assertEquals("features/[*]/aliases", tso.getSubdataSelection().getSubdataIncluded().get(1));
				assertEquals(1, tso.getSubdataSelection().getPathToSubdata().size());
				assertEquals("features", tso.getSubdataSelection().getPathToSubdata().get(0));
				assertEquals("id", tso.getSubdataSelection().getSelectionId());
				assertNull(tso.getSubdataSelection().getSelectionDescription());
				assertNull(tso.getSubdataSelection().getDescriptionTemplate());

			}
		}
		assertTrue("Testing that test_method_1 was returned from listMethodSpec",
				foundTestMethod1);
		assertTrue("Testing that test_method_3 was returned from listMethodSpec",
				foundTestMethod3);
		assertTrue("Testing that test_method_4 was returned from listMethodSpec",
				foundTestMethod4);
		assertTrue("Testing that test_method_5 was returned from listMethodSpec",
				foundTestMethod5);
		assertTrue("Testing that test_method_7 was returned from listMethodSpec",
				foundTestMethod7);
		assertTrue("Testing that test_method_8 was returned from listMethodSpec",
				foundTestMethod8);
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
		
		
		List<Publication> pubs = m.getPublications();
		assertTrue("Publications are returned",pubs!=null);
		assertTrue("4 publications are present",pubs.size()==4);
		assertEquals("pub 0 pmid is correct",pubs.get(0).getPmid(),"2231712");
		assertEquals("pub 0 text is correct",pubs.get(0).getDisplayText(),"Basic local alignment search tool.");
		assertEquals("pub 0 link is correct",pubs.get(0).getLink(),"http://www.ncbi.nlm.nih.gov/pubmed/2231712");
		assertEquals("pub 1 pmid is correct",pubs.get(1).getPmid(),null);
		assertEquals("pub 1 text is correct",pubs.get(1).getDisplayText(),"Some made up paper");
		assertEquals("pub 1 link is correct",pubs.get(1).getLink(),null);
		assertEquals("pub 2 pmid is correct",pubs.get(2).getPmid(),null);
		assertEquals("pub 2 text is correct",pubs.get(2).getDisplayText(),"http://www.ncbi.nlm.nih.gov/pubmed/2231712");
		assertEquals("pub 2 link is correct",pubs.get(2).getLink(),"http://www.ncbi.nlm.nih.gov/pubmed/2231712");
		assertEquals("pub 3 pmid is correct",pubs.get(3).getPmid(),"2231712");
		assertEquals("pub 3 text is correct",pubs.get(3).getDisplayText(),"2231712");
		assertEquals("pub 3 link is correct",pubs.get(3).getLink(),null);

		List<String> authors = m.getAuthors();
		assertTrue("Authors are returned",authors!=null);
		assertTrue("4 publications are present",authors.size()==2);
		assertEquals("first author",authors.get(0),"msneddon");
		assertEquals("second author",authors.get(1),"wstester1");
		List<String> kb_contributors = m.getKbContributors();
		assertTrue("KB Contributers are returned",kb_contributors!=null);
		assertEquals("first contributer",kb_contributors.get(0),"wstester3");
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
		Tuple4<Map<String,Category>, Map<String,MethodBriefInfo>, Map<String,AppBriefInfo>, Map<String,TypeInfo>> ret = 
				CLIENT.listCategories(new ListCategoriesParams().withLoadMethods(1L).withLoadApps(1L).withLoadTypes(1L));
		Map<String, MethodBriefInfo> methodBriefInfo = ret.getE2();
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
				System.out.println("Unexpected error [" + errorId + "]: " + methodBriefInfo.get(errorId).getLoadingError());
				Assert.fail(methodBriefInfo.get(errorId).getLoadingError());
			}
		}
		Map<String, AppBriefInfo> appBriefInfo = ret.getE3();
		for (String errorId : appBriefInfo.keySet()) {
			if (appBriefInfo.get(errorId).getLoadingError() != null && !errorId.startsWith("test_error_")) {
				System.out.println("Unexpected error[" + errorId + "]: " + appBriefInfo.get(errorId).getLoadingError());
				Assert.fail(appBriefInfo.get(errorId).getLoadingError());
			}
		}
		Map<String, TypeInfo> typeInfo = ret.getE4();
		for (String errorId : typeInfo.keySet()) {
			if (typeInfo.get(errorId).getLoadingError() != null && !errorId.startsWith("Test.Error")) {
				System.out.println("Unexpected error[" + errorId + "]: " + typeInfo.get(errorId).getLoadingError());
				Assert.fail(typeInfo.get(errorId).getLoadingError());
			}
		}
		MethodFullInfo err6 = CLIENT.getMethodFullInfo(new GetMethodParams().withIds(Arrays.asList("test_error_6"))).get(0);
		String text = err6.getPublications().get(0).getDisplayText();
		Assert.assertTrue(text.contains("977 982"));
	}
	
	@Test
	public void testApp() throws Exception {
		Map<String, AppBriefInfo> appBriefInfo = CLIENT.listCategories(new ListCategoriesParams().withLoadMethods(1L).withLoadApps(1L)).getE3();
		assertNotNull(appBriefInfo.get("test_app_1"));
		//System.out.println(appBriefInfo.get("test_app_1"));
		AppSpec as = CLIENT.getAppSpec(new GetAppParams().withIds(Arrays.asList("test_app_1"))).get(0);
		assertEquals(2, as.getSteps().size());
		assertEquals("step_1", as.getSteps().get(0).getStepId());
		assertEquals("step_1", as.getSteps().get(1).getInputMapping().get(0).getStepSource());
		

		List<AppSpec> spec = CLIENT.getAppSpec(new GetAppParams().withIds(Arrays.asList("test_app_1")));
		//System.out.println(spec.get(0));
		assertEquals(1, spec.size());
	}
	
	@Test
	public void testServiceParamMapping() throws Exception {
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
		assertEquals("re2", spec.getJobIdOutputField());
	}
	
	@Test
	public void testScriptParamMapping() throws Exception {
		MethodSpec spec = CLIENT.getMethodSpec(new GetMethodParams().withIds(Arrays.asList("test_method_6"))).get(0);
		assertNotNull(spec.getBehavior().getScriptModule());
		assertNotNull(spec.getBehavior().getScriptName());
		assertEquals(1L, (long)spec.getBehavior().getScriptHasFiles());
		assertEquals(8, spec.getBehavior().getScriptInputMapping().size());
		assertEquals("assembly_input", spec.getBehavior().getScriptInputMapping().get(0).getInputParameter());
		assertNotNull(spec.getBehavior().getScriptInputMapping().get(0).getTargetProperty());
		assertEquals(1, spec.getBehavior().getScriptOutputMapping().size());
		assertEquals("output_contigset", spec.getBehavior().getScriptOutputMapping().get(0).getInputParameter());
		assertNotNull(spec.getBehavior().getScriptOutputMapping().get(0).getTargetProperty());
	}

	@Test
	public void testListTypes() throws Exception {
		List<TypeInfo> typeInfo = CLIENT.listTypes(new ListParams());
		assertTrue("Got list of types", typeInfo.size()>0);
		boolean foundTestType1 = false;
		for(TypeInfo ti : typeInfo) {
			if(ti.getTypeName().compareTo("Test.Type1")==0) {
				foundTestType1 = true;
				assertTrue("Test.Type1 has name Genome", ti.getName().compareTo("Genome")==0);
			}
		}
		assertTrue("Type1 was returned successfully in list types.",foundTestType1);
	}
	
	@Test
	public void testType() throws Exception {
		Map<String, TypeInfo> typeInfo = CLIENT.listCategories(new ListCategoriesParams().withLoadTypes(1L)).getE4();
		TypeInfo ti = typeInfo.get("Test.Type1");
		assertNotNull(ti);
		assertEquals("Genome", ti.getName());
		assertEquals(1, ti.getViewMethodIds().size());
		assertEquals(1, ti.getImportMethodIds().size());
		assertEquals("genomes", ti.getLandingPageUrlPrefix());
		ti = CLIENT.getTypeInfo(new GetTypeParams().withTypeNames(Arrays.asList("Test.Type1"))).get(0);
		assertEquals("Genome", ti.getName());
		assertEquals(1, ti.getViewMethodIds().size());
		assertEquals(1, ti.getImportMethodIds().size());
		assertEquals("genomes", ti.getLandingPageUrlPrefix());
	}
	
	@Test
	public void testValidateMethod() throws Exception {
		// Test a valid spec
		ValidateMethodParams params = 
				new ValidateMethodParams()
					.withId("test_method_1")
					.withDisplayYaml(getTestFileFromSpecsRepo("methods/test_method_1/display.yaml"))
					.withSpecJson(getTestFileFromSpecsRepo("methods/test_method_1/spec.json"));
		ValidationResults results = CLIENT.validateMethod(params);
		assertTrue("Method validation results of test_method_1 returns is valid", results.getIsValid()==1L);
		assertTrue("Method validation contains an empty error report",results.getErrors().isEmpty());
		assertTrue("Method validation results of test_method_1 spec is not null", results.getMethodSpec()!=null);
		assertTrue("Method validation results of test_method_1 full info is not null", results.getMethodFullInfo()!=null);
		assertTrue("Method validation results of test_method_1 app spec is null", results.getAppSpec()==null);
		assertTrue("Method validation results of test_method_1 app full info info is null", results.getAppFullInfo()==null);
		assertTrue("Method validation results of test_method_1 type info is null", results.getTypeInfo()==null);
		assertTrue("Method validation results of test_method_1 got the right name", results.getMethodFullInfo().getName().compareTo("Test Method 1")==0);
		
		// Test an error case
		params = 
				new ValidateMethodParams()
					.withId("test_error_1")
					.withDisplayYaml(getTestFileFromSpecsRepo("methods/test_error_1/display.yaml"))
					.withSpecJson(getTestFileFromSpecsRepo("methods/test_error_1/spec.json"));
		results = CLIENT.validateMethod(params);
		assertTrue("Method validation results of test_error_1 returns is not valid", results.getIsValid()==0L);
		assertTrue("Method validation contains some error report",results.getErrors().size()>0);
		assertTrue("Method validation results of test_method_1 spec is null", results.getMethodSpec()==null);
		assertTrue("Method validation results of test_method_1 full info is null", results.getMethodFullInfo()==null);
		assertTrue("Method validation results of test_method_1 app spec is null", results.getAppSpec()==null);
		assertTrue("Method validation results of test_method_1 app full info info is null", results.getAppFullInfo()==null);
		assertTrue("Method validation results of test_method_1 type info is null", results.getTypeInfo()==null);
	}
	
	
	@Test
	public void testValidateApp() throws Exception {
		// Test a valid spec
		ValidateAppParams params = 
				new ValidateAppParams()
					.withId("test_method_1")
					.withDisplayYaml(getTestFileFromSpecsRepo("apps/test_app_1/display.yaml"))
					.withSpecJson(getTestFileFromSpecsRepo("apps/test_app_1/spec.json"));
		ValidationResults results = CLIENT.validateApp(params);
		assertTrue("App validation results of test_app_1 returns is valid", results.getIsValid()==1L);
		assertTrue("App validation contains an empty error report",results.getErrors().isEmpty());
		assertTrue("App validation results of test_app_1 spec is not null", results.getAppSpec()!=null);
		assertTrue("App validation results of test_app_1 full info is not null", results.getAppFullInfo()!=null);
		assertTrue("App validation results of test_app_1 method spec is null", results.getMethodSpec()==null);
		assertTrue("App validation results of test_app_1 method full info info is null", results.getMethodFullInfo()==null);
		assertTrue("App validation results of test_app_1 type info is null", results.getTypeInfo()==null);
		assertTrue("App validation results of test_app_1 got the right name", results.getAppFullInfo().getName().compareTo("Test All 1")==0);
		
		// Test an error case
		params = 
				new ValidateAppParams()
					.withId("test_error_1")
					.withDisplayYaml("madeup: nothing")
					.withSpecJson(getTestFileFromSpecsRepo("methods/test_error_1/spec.json"));
		results = CLIENT.validateApp(params);
		assertTrue("App validation results of test_error_1 returns is not valid", results.getIsValid()==0L);
		assertTrue("App validation contains some error report",results.getErrors().size()>0);
		assertTrue("App validation results of test_method_1 spec is null", results.getMethodSpec()==null);
		assertTrue("App validation results of test_method_1 full info is null", results.getMethodFullInfo()==null);
		assertTrue("App validation results of test_method_1 app spec is null", results.getAppSpec()==null);
		assertTrue("App validation results of test_method_1 app full info info is null", results.getAppFullInfo()==null);
		assertTrue("App validation results of test_method_1 type info is null", results.getTypeInfo()==null);
	}
	
	@Test
	public void testValidateType() throws Exception {
		// Test a valid spec
		ValidateTypeParams params = 
				new ValidateTypeParams()
					.withId("Test.Type1")
					.withDisplayYaml(getTestFileFromSpecsRepo("types/Test.Type1/display.yaml"))
					.withSpecJson(getTestFileFromSpecsRepo("types/Test.Type1/spec.json"));
		ValidationResults results = CLIENT.validateType(params);
		assertTrue("Type validation results of Test.Type1 returns is valid", results.getIsValid()==1L);
		assertTrue("Type validation contains an empty error report",results.getErrors().isEmpty());
		assertTrue("Type validation results of Test.Type1 spec is null", results.getMethodSpec()==null);
		assertTrue("Type validation results of Test.Type1 full info is null", results.getMethodFullInfo()==null);
		assertTrue("Type validation results of Test.Type1 app spec is null", results.getAppSpec()==null);
		assertTrue("Type validation results of Test.Type1 app full info info is null", results.getAppFullInfo()==null);
		assertTrue("Type validation results of Test.Type1 type info is not null", results.getTypeInfo()!=null);
		assertTrue("Type validation results of Test.Type1 got the right name", results.getTypeInfo().getName().compareTo("Genome")==0);
		
		// Test an error case
		params = 
				new ValidateTypeParams()
					.withId("Test.Type1")
					.withDisplayYaml("not a field: 23\n\n").withSpecJson("{}");
		results = CLIENT.validateType(params);
		assertTrue("Type validation results of test_error_1 returns is not valid", results.getIsValid()==0L);
		assertTrue("Type validation contains some error report",results.getErrors().size()>0);
		assertTrue("Type validation results of test_method_1 spec is null", results.getMethodSpec()==null);
		assertTrue("Type validation results of test_method_1 full info is null", results.getMethodFullInfo()==null);
		assertTrue("Type validation results of test_method_1 app spec is null", results.getAppSpec()==null);
		assertTrue("Type validation results of test_method_1 app full info info is null", results.getAppFullInfo()==null);
		assertTrue("Type validation results of test_method_1 type info is null", results.getTypeInfo()==null);
	}
	
	

	private static String getTestFileFromSpecsRepo(String path) {
		StringBuilder content = new StringBuilder();
		try {
			URL githubFile = new URL(gitRepo + "/raw/" + gitRepoBranch + "/"+path);
	        BufferedReader in = new BufferedReader(new InputStreamReader(githubFile.openStream()));
	        String line;
	        while ((line = in.readLine()) != null) {
	        	content.append(line+"\n");
	        }
	        in.close();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		
		return content.toString();
	}
	

	private static String tempDirName;
	private static String gitRepo;
	private static String gitRepoBranch;
	private static String gitRepoRefreshRate;
	private static String gitRepoCacheSize;
	
	@BeforeClass
	public static void setUpClass() throws Exception {

		// Parse the test config variables
		tempDirName = System.getProperty("test.temp-dir");
		
		gitRepo = System.getProperty("test.method-spec-git-repo");
		gitRepoBranch = System.getProperty("test.method-spec-git-repo-branch");
		gitRepoRefreshRate = System.getProperty("test.method-spec-git-repo-refresh-rate");
		gitRepoCacheSize = System.getProperty("test.method-spec-cache-size");
		
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
