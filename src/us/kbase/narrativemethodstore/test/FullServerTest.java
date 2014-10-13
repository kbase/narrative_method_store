package us.kbase.narrativemethodstore.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import us.kbase.narrativemethodstore.NarrativeMethodStoreServer;

/*
 * 
 * 
 */
public class FullServerTest {
	
	final private static String TMP_FILE_SUBDIR = "tempForScriptTestRunner";

	private static File tempDir;
	
	private static NarrativeMethodStoreServer SERVER;
	
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
	public void runTestServerUp() {
		System.out.println("test...");
		System.out.println(getTestURL());
	}
	
	
	
	
	private static String getTestURL() {
		int testport = SERVER.getServerPort();
		return "http://localhost:"+testport+"/rpc";
	}
	
	
	@BeforeClass
	public static void setUpClass() throws Exception {

		// Parse the test config variables
		String tempDirName = System.getProperty("test.temp-dir");
		
		String gitRepo = System.getProperty("test.method-spec-git-repo");
		String gitRepoBranch = System.getProperty("test.method-spec-git-repo-branch");
		String gitRepoRefreshRate = System.getProperty("test.method-spec-git-repo-refresh-rate");
		String gitRepoCacheSize = System.getProperty("test.method-spec-cache-size");
		
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
		env.put("KB_SERVICE_NAME", "Workspace");

		//NarrativeMethodStoreServer.clearConfigForTests();
		SERVER = new NarrativeMethodStoreServer();
		new ServerThread(SERVER).start();
		System.out.println("Main thread waiting for server to start up");
		while (SERVER.getServerPort() == null) {
			Thread.sleep(100);
		}
		
		
	}
	
	
	/*
	private static NarrativeMethodStoreServer startupServer(
			String mongohost,
			DB db,
			String typedb,
			String handleUser,
			String handlePwd)
			throws InvalidHostException, UnknownHostException, IOException,
			NoSuchFieldException, IllegalAccessException, Exception,
			InterruptedException {
		
		WorkspaceTestCommon.initializeGridFSWorkspaceDB(db, typedb);

		//write the server config file:
		File iniFile = File.createTempFile("test", ".cfg",
				new File(WorkspaceTestCommon.getTempDir()));
		if (iniFile.exists()) {
			iniFile.delete();
		}
		System.out.println("Created temporary config file: " +
				iniFile.getAbsolutePath());
		Ini ini = new Ini();
		Section ws = ini.add("Workspace");
		ws.add("mongodb-host", mongohost);
		ws.add("mongodb-database", db.getName());
		ws.add("backend-secret", "foo");
		ws.add("handle-service-url", "http://localhost:" +
				HANDLE.getHandleServerPort());
		ws.add("handle-manager-url", "http://localhost:" +
				HANDLE.getHandleManagerPort());
		ws.add("handle-manager-user", handleUser);
		ws.add("handle-manager-pwd", handlePwd);
		ws.add("ws-admin", USER2);
		ws.add("temp-dir", Paths.get(WorkspaceTestCommon.getTempDir())
				.resolve(TMP_FILE_SUBDIR));
		ini.store(iniFile);
		iniFile.deleteOnExit();

		//set up env
		Map<String, String> env = getenv();
		env.put("KB_DEPLOYMENT_CONFIG", iniFile.getAbsolutePath());
		env.put("KB_SERVICE_NAME", "Workspace");

		WorkspaceServer.clearConfigForTests();
		WorkspaceServer server = new WorkspaceServer();
		new ServerThread(server).start();
		System.out.println("Main thread waiting for server to start up");
		while (server.getServerPort() == null) {
			Thread.sleep(1000);
		}
		return server;
	}
	
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		if (SERVER != null) {
			System.out.print("Killing workspace server... ");
			SERVER.stopServer();
			System.out.println("Done");
		}
		if (HANDLE != null) {
			System.out.print("Destroying handle service... ");
			HANDLE.destroy(WorkspaceTestCommon.getDeleteTempFiles());
			System.out.println("Done");
		}
		if (SHOCK != null) {
			System.out.print("Destroying shock service... ");
			SHOCK.destroy(WorkspaceTestCommon.getDeleteTempFiles());
			System.out.println("Done");
		}
		if (MONGO != null) {
			System.out.print("Destroying mongo test service... ");
			MONGO.destroy(WorkspaceTestCommon.getDeleteTempFiles());
			System.out.println("Done");
		}
		if (MYSQL != null) {
			System.out.print("Destroying mysql test service... ");
			MYSQL.destroy(WorkspaceTestCommon.getDeleteTempFiles());
			System.out.println("Done");
		}
	}
	*/
	
}
