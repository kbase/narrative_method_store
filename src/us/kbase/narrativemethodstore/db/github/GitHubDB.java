package us.kbase.narrativemethodstore.db.github;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.kbase.narrativemethodstore.db.MethodSpecDB;
import us.kbase.narrativemethodstore.db.NarrativeMethodData;




public class GitHubDB implements MethodSpecDB {

	public static final String GITHUB_API_URL_DEFAULT = "https://api.github.com";
	public static final String GITHUB_RAW_CONTENT_URL_DEFAULT = "https://raw.githubusercontent.com";
	
	private final ObjectMapper mapper = new ObjectMapper();
	private final Yaml yaml = new Yaml();
	
	// github config variables
	private String GITHUB_API_URL;
	private String GITHUB_RAW_CONTENT_URL;
	protected String owner;
	protected String repo;
	protected String branch;
	
	protected String latest_sha;
	
	public GitHubDB(String owner, String repo, String branch) throws JsonProcessingException, IOException {
		this.initalize(owner, repo, branch, GITHUB_API_URL_DEFAULT, GITHUB_RAW_CONTENT_URL_DEFAULT);
	}
	
	public GitHubDB(String owner, String repo, String branch, String githubApiUrl, String githubResourceUrl) throws JsonProcessingException, IOException {
		this.initalize(owner, repo, branch, githubApiUrl, githubResourceUrl);
	}
	
	
	
	
	
	
	protected void initalize(String owner, String repo, String branch, String githubApiUrl, String githubResourceUrl) {
		this.GITHUB_API_URL = githubApiUrl;
		this.GITHUB_RAW_CONTENT_URL = githubResourceUrl;
		this.owner = owner;
		this.repo = repo;
		this.branch = branch;
		
		this.latest_sha = "";
		
		try {
			URL repoInfoUrl = new URL(GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/git/refs/heads/" + branch);
			JsonNode repoInfo = getAsJson(repoInfoUrl);
			latest_sha = repoInfo.get("object").get("sha").textValue();
			System.out.println(latest_sha);
		} catch (IOException e) {
			
		}
	}
	
	/** returns true if the latest commit we have does not match the head commit, false otherwise; if we cannot
	 * connect to github, then we just report that new data is not available */
	protected boolean newDataAvailable() {
		URL repoInfoUrl;
		try {
			repoInfoUrl = new URL(GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/git/refs/heads/" + branch);
			
			JsonNode repoInfo = getAsJson(repoInfoUrl);
			if(!latest_sha.equals(repoInfo.get("object").get("sha").textValue())) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	protected JsonNode methodIndex;
	
	
	protected void refreshMethodIndex() throws JsonProcessingException, IOException {
		URL methodIndexUrl = new URL(GITHUB_RAW_CONTENT_URL + "/" + owner + "/" + repo + "/"+branch+"/methods/index.json");
		
		JsonNode methodIndex = getAsJson(methodIndexUrl);
		System.out.println(methodIndex);
	}
	
	
	public void loadMethodIndex() throws JsonProcessingException, MalformedURLException, IOException {
		JsonNode methodListJson = getAsJson(new URL(GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/contents/methods?ref=" + branch));
		
		List <String> methodList = new ArrayList<String>(methodListJson.size());
		for(int m=0; m<methodListJson.size(); m++) {
			if(methodListJson.get(m).get("type").asText().equals("dir")) {
				methodList.add(methodListJson.get(m).get("name").asText());
			}
		}
		
		System.out.println("method list:");
		for(String id : methodList) {
			System.out.println(" --- "+id);
		}
		
	}
	
	
	public NarrativeMethodData loadMethodData(String methodId) throws JsonProcessingException, IOException {
		// Fetch the resources needed
		JsonNode spec = getResourceAsJson("methods/"+methodId+"/spec.json");
		Map<String,Object> display = getResourceAsYamlMap("methods/"+methodId+"/display.yaml");
		
		// Initialize the actual data
		NarrativeMethodData data = new NarrativeMethodData(methodId, spec, display);
		return data;
	}
	
	
	protected JsonNode getResourceAsJson(String path) throws JsonProcessingException, IOException {
		URL url = new URL(GITHUB_RAW_CONTENT_URL + "/" + owner + "/" + repo + "/"+branch+"/"+path);
		return getAsJson(url);
	}
	
	protected String getResource(String path) throws IOException {
		URL url = new URL(GITHUB_RAW_CONTENT_URL + "/" + owner + "/" + repo + "/"+branch+"/"+path);
		return get(url);
	}
	
	protected Map<String,Object> getResourceAsYamlMap(String path) throws IOException {
		URL url = new URL(GITHUB_RAW_CONTENT_URL + "/" + owner + "/" + repo + "/"+branch+"/"+path);
		String document = get(url);
		@SuppressWarnings("unchecked")
		Map<String,Object> data = (Map<String, Object>) yaml.load(document);
		//System.out.println("fetched yaml ("+url+"):\n"+yaml.dump(data));
		return data;
	}
	
	
	protected JsonNode getAsJson(URL url) throws JsonProcessingException, IOException {
		return mapper.readTree(get(url));
	}
	
	protected String get(URL url) throws IOException {
		StringBuilder response = new StringBuilder();
		URLConnection conn = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) 
			response.append(line+"\n");
		in.close();
		return response.toString();
	}
	 
	
	
	
	
	
	
	public static void main(String[] args) throws JsonProcessingException, IOException {
		System.out.println("testing github db");
		
		
		GitHubDB githubDB = new GitHubDB("msneddon","narrative_method_specs","master");
		
		NarrativeMethodData data = githubDB.loadMethodData("test_method_1");
		
		System.out.println(data.getMethodFullInfo().getDescription());
		githubDB.loadMethodIndex();
		return;
	}
	
}
