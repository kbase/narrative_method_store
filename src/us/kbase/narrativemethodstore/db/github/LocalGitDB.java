package us.kbase.narrativemethodstore.db.github;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreInitializationException;

public class LocalGitDB {

	
	protected URL gitRepoUrl;
	protected String gitBranch;
	protected File gitLocalPath;
	
	public LocalGitDB(URL gitRepoUrl, String branch, File localPath) throws NarrativeMethodStoreInitializationException {
		this.gitRepoUrl = gitRepoUrl;
		this.gitBranch = branch;
		this.gitLocalPath = localPath;
		initializeLocalRepo();
	}
	
	protected void initializeLocalRepo() throws NarrativeMethodStoreInitializationException {
		try {
			FileUtils.deleteDirectory(gitLocalPath);
		} catch (IOException e) {
			throw new NarrativeMethodStoreInitializationException("Cannot clone "+gitRepoUrl+", error deleting old directory: " + e.getMessage(), e);
		}
		String cloneStatus = gitClone();
		//System.out.println(cloneStatus);
		//System.out.println(gitPull());
	}
	
	
	/**
	 * Clones the configured git repo to the target local file location, returns standard output of the command
	 * if successful, otherwise throws an exception.
	 */
	protected String gitClone() throws NarrativeMethodStoreInitializationException {
		try {
			String gitCloneCmd = "git clone --branch "+gitBranch+" "+gitRepoUrl+" "+gitLocalPath.getAbsolutePath();
			Process p = Runtime.getRuntime().exec(gitCloneCmd);
			int exitcode = p.waitFor();
			
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String s="", out="", error="";
			while ((s = stdOut.readLine()) != null) { out += s+"\n"; }
			while ((s = stdError.readLine()) != null) { error += s+"\n"; }
			
			if(exitcode!=0) {
				throw new NarrativeMethodStoreInitializationException("Cannot clone "+gitRepoUrl+": " + error);
			}
			return out;
		} catch (IOException | InterruptedException e) {
			throw new NarrativeMethodStoreInitializationException("Cannot clone "+gitRepoUrl+": " + e.getMessage(),e);
		}
	}
	
	/**
	 * Runs a git pull on the local git spec repo.
	 */
	protected String gitPull() throws NarrativeMethodStoreInitializationException {
		try {
			String gitPullCmd  = "git pull";
			Process p = Runtime.getRuntime().exec(gitPullCmd, null, gitLocalPath);
			int exitcode = p.waitFor();
			
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String s="", out="", error="";
			while ((s = stdOut.readLine()) != null) { out += s+"\n"; }
			while ((s = stdError.readLine()) != null) { error += s+"\n"; }
			
			if(exitcode!=0) {
				throw new NarrativeMethodStoreInitializationException("Cannot pull "+gitRepoUrl+": " + error);
			}
			return out;
		} catch (IOException | InterruptedException e) {
			throw new NarrativeMethodStoreInitializationException("Cannot pull "+gitRepoUrl+": " + e.getMessage(),e);
		}
	}
	
	
	
	
	public static void main(String[] args) throws NarrativeMethodStoreInitializationException, MalformedURLException {
		
		String giturl = "https://github.com/msneddon/narrative_method_specs.git";
		String branch = "dev";
		String localpath = "/kb/deployment/services/narrative_method_store/narrative_method_specs";
		
		LocalGitDB db = new LocalGitDB(new URL(giturl), branch, new File(localpath));
		
		
	}
	
	
	
	
	
	
	
	
}
