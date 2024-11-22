package us.kbase.narrativemethodstore.db.github;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreInitializationException;

public class GitUtils {

    /**
     * Clones the configured git repo to the target local file location, returns standard output of the command
     * if successful, otherwise throws an exception.
     */
    public static String gitClone(URL gitRepoUrl, String gitBranch, File gitLocalPath) throws NarrativeMethodStoreInitializationException {
        try {
            return gitCommand("git clone --branch "+gitBranch+" "+gitRepoUrl+" "+gitLocalPath.getAbsolutePath(), 
                    "clone", gitLocalPath.getCanonicalFile().getParentFile(), gitRepoUrl);
        } catch (IOException e) {
            throw new NarrativeMethodStoreInitializationException("Cannot clone "+gitRepoUrl+": " + e.getMessage(), e);
        }
    }

    /**
     * Clones the configured git repo to the target local file location, returns standard output of the command
     * if successful, otherwise throws an exception.
     */
    public static String gitClone(URL gitRepoUrl, File gitLocalPath) throws NarrativeMethodStoreInitializationException {
        try {
            return gitCommand("git clone "+gitRepoUrl+" "+gitLocalPath.getAbsolutePath(), 
                    "clone", gitLocalPath.getCanonicalFile().getParentFile(), gitRepoUrl);
        } catch (IOException e) {
            throw new NarrativeMethodStoreInitializationException("Cannot clone "+gitRepoUrl+": " + e.getMessage(), e);
        }
    }

    /**
     * Runs a git pull on the local git spec repo.
     */
    public static String gitPull(File gitLocalPath, URL gitRepoUrl) throws NarrativeMethodStoreInitializationException {
        return gitCommand("git pull", "pull", gitLocalPath, gitRepoUrl);
    }

    public static String gitCheckout(File gitLocalPath, URL gitRepoUrl, String commitHash) throws NarrativeMethodStoreInitializationException {
        return gitCommand("git checkout " + commitHash, "checkout", gitLocalPath, gitRepoUrl);
    }

    public static String getCommitInfo(File gitLocalPath, URL gitRepoUrl) throws NarrativeMethodStoreInitializationException {
        return gitCommand("git log -n 1", "log -n 1", gitLocalPath, gitRepoUrl);
    }

    public static String getCommitHash(File gitLocalPath, URL gitRepoUrl) throws NarrativeMethodStoreInitializationException {
        return gitCommand("git rev-parse HEAD", "rev-parse HEAD", gitLocalPath, gitRepoUrl).trim();
    }
    
    public static String gitCommand(String fullCmd, String nameOfCmd, File curDir, URL gitRepoUrl) throws NarrativeMethodStoreInitializationException {
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

    private static Thread readInThread(final BufferedReader stdOut, final StringBuilder out) {
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

}
