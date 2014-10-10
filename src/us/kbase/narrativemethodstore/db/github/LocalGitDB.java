package us.kbase.narrativemethodstore.db.github;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.kbase.narrativemethodstore.db.NarrativeMethodData;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreInitializationException;

public class LocalGitDB {

	
	protected URL gitRepoUrl;
	protected String gitBranch;
	protected File gitLocalPath;
	private final ObjectMapper mapper = new ObjectMapper();
	private final Yaml yaml = new Yaml();
	
	public LocalGitDB(URL gitRepoUrl, String branch, File localPath) throws NarrativeMethodStoreInitializationException {
		this.gitRepoUrl = gitRepoUrl;
		this.gitBranch = branch;
		this.gitLocalPath = localPath;
		if (!localPath.exists())
			localPath.mkdirs();
		initializeLocalRepo();
	}
	
	protected void initializeLocalRepo() throws NarrativeMethodStoreInitializationException {
		try {
			FileUtils.deleteDirectory(gitLocalPath);
		} catch (IOException e) {
			throw new NarrativeMethodStoreInitializationException("Cannot clone "+gitRepoUrl+", error deleting old directory: " + e.getMessage(), e);
		}
		String cloneStatus = gitClone();
		System.out.println(cloneStatus);
		System.out.println(gitPull());
	}

	/**
	 * Clones the configured git repo to the target local file location, returns standard output of the command
	 * if successful, otherwise throws an exception.
	 */
	protected String gitClone() throws NarrativeMethodStoreInitializationException {
		try {
			return gitCommand("git clone --branch "+gitBranch+" "+gitRepoUrl+" "+gitLocalPath.getAbsolutePath(), 
					"clone", gitLocalPath.getCanonicalFile().getParentFile());
		} catch (IOException e) {
			throw new NarrativeMethodStoreInitializationException("Cannot clone "+gitRepoUrl+": " + e.getMessage(), e);
		}
	}
	
	protected String gitCommand(String fullCmd, String nameOfCmd, File curDir) throws NarrativeMethodStoreInitializationException {
		try {
			Process p = Runtime.getRuntime().exec(fullCmd, null, curDir);
			
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			StringBuilder out = new StringBuilder();
			StringBuilder error = new StringBuilder();
			Thread outT = readInThread(stdOut, out);
			Thread errT = readInThread(stdError, error);
			
			outT.join();
			errT.join();
			int exitcode = p.waitFor();
			
			if(exitcode!=0) {
				throw new NarrativeMethodStoreInitializationException("Cannot " + nameOfCmd + " "+gitRepoUrl+": " + error);
			}
			return out.toString();
		} catch (Exception e) {
			throw new NarrativeMethodStoreInitializationException("Cannot " + nameOfCmd + " "+gitRepoUrl+": " + e.getMessage(), e);
		}
	}

	private Thread readInThread(final BufferedReader stdOut, final StringBuilder out) {
		Thread ret = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String s1 = null;
					while ((s1 = stdOut.readLine()) != null) { out.append(s1+"\n"); }
				} catch (IOException ex) {
					throw new IllegalStateException(ex);
				}
			}
		});
		ret.start();
		return ret;
	}
	
	/**
	 * Runs a git pull on the local git spec repo.
	 */
	protected String gitPull() throws NarrativeMethodStoreInitializationException {
		return gitCommand("git pull", "pull", gitLocalPath);
	}
	
	
	
	
	public static void main(String[] args) throws NarrativeMethodStoreInitializationException, 
			MalformedURLException, NarrativeMethodStoreException {
		
		String giturl = "https://github.com/msneddon/narrative_method_specs.git";
		String branch = "dev";
		//String localpath = "/kb/deployment/services/narrative_method_store/narrative_method_specs";
		String localpath = "temp_files/narrative_method_specs";
		LocalGitDB db = new LocalGitDB(new URL(giturl), branch, new File(localpath));
		
		String mId = db.listMethodIds().get(0);

		NarrativeMethodData data = db.loadMethodData(mId);
		
		System.out.println(mId + ", " + data.getMethodFullInfo().getDescription());
	}
	
	
	protected File getMethodsDir() {
		return new File(gitLocalPath, "methods");
	}

	protected File getCategoriesDir() {
		return new File(gitLocalPath, "categories");
	}

	public List<String> listMethodIds() throws NarrativeMethodStoreException {
		List <String> methodList = new ArrayList<String>();  //methodListJson.size());
		for (File sub : getMethodsDir().listFiles()) {
			if (sub.isDirectory())
				methodList.add(sub.getName());
		}
		
		//System.out.println("method list:");
		//for(String id : methodList) 
		//	System.out.println(" --- "+id);
		
		return methodList;
	}

	
	public NarrativeMethodData loadMethodData(String methodId) throws NarrativeMethodStoreException {
		try {
			// Fetch the resources needed
			JsonNode spec = getResourceAsJson("methods/"+methodId+"/spec.json");
			Map<String,Object> display = getResourceAsYamlMap("methods/"+methodId+"/display.yaml");

			// Initialize the actual data
			NarrativeMethodData data = new NarrativeMethodData(methodId, spec, display);
			return data;
		} catch (Exception ex) {
			throw new NarrativeMethodStoreException(ex);
		}
	}
	
	
	protected JsonNode getResourceAsJson(String path) throws JsonProcessingException, IOException {
		File f = new File(gitLocalPath, path);
		return getAsJson(f);
	}
	
	protected String getResource(String path) throws IOException {
		File f = new File(gitLocalPath, path);
		return get(f);
	}
	
	protected Map<String,Object> getResourceAsYamlMap(String path) throws IOException {
		File f = new File(gitLocalPath, path);
		String document = get(f);
		@SuppressWarnings("unchecked")
		Map<String,Object> data = (Map<String, Object>) yaml.load(document);
		//System.out.println("fetched yaml ("+url+"):\n"+yaml.dump(data));
		return data;
	}
	
	
	protected JsonNode getAsJson(File f) throws JsonProcessingException, IOException {
		return mapper.readTree(get(f));
	}
	
	protected String get(URL url) throws IOException {
		return get(url.openStream());
	}
	
	protected String get(File f) throws IOException {
		return get(new FileInputStream(f));
	}
	
	protected String get(InputStream is) throws IOException {
		StringBuilder response = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = in.readLine()) != null) 
			response.append(line+"\n");
		in.close();
		return response.toString();
	}
}
