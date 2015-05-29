package us.kbase.narrativemethodstore.db.github;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreInitializationException;

public class GitHubRepoProvider extends FileRepoProvider {
    protected URL url;
    protected String branch;
    protected String commitHash;

    public GitHubRepoProvider(URL url, File parentTempDir) throws NarrativeMethodStoreException {
        this(url, "master", parentTempDir);
    }

    public GitHubRepoProvider(URL url, String branch, File parentTempDir) throws NarrativeMethodStoreException {
        super(prepareGitClone(url, branch, generateTempDir(parentTempDir)));
        this.url = url;
        this.branch = branch;
        try {
            this.commitHash = GitUtils.getCommitHash(rootDir, url);
        } catch (NarrativeMethodStoreException ex) {
            dispose(rootDir);
            throw ex;
        }
    }
    
    private static File prepareGitClone(URL url, String branch, File rootDir) throws NarrativeMethodStoreException {
        try {
            GitUtils.gitClone(url, branch, rootDir);
            return rootDir;
        } catch (NarrativeMethodStoreException ex) {
            dispose(rootDir);
            throw ex;
        }
    }
    
    private static File generateTempDir(File parentTempDir) throws NarrativeMethodStoreException {
        try {
            long start = System.currentTimeMillis();
            while (true) {
                File dir = new File(parentTempDir, "github_" + start + ".temp");
                if (!dir.exists()) {
                    dir.mkdirs();
                    return dir;
                }
                start++;
            }
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException(ex.getMessage(), ex);
        }
    }

    @Override
    public String getUrl() {
        return "" + url;
    }
    
    @Override
    public String getGitCommitHash() {
        return commitHash;
    }
    
    @Override
    public void dispose() throws NarrativeMethodStoreException {
        dispose(rootDir);
    }
    
    private static void dispose(File rootDir) throws NarrativeMethodStoreException {
        try {
            FileUtils.deleteDirectory(rootDir);
        } catch (IOException e) {
            throw new NarrativeMethodStoreInitializationException("Error cleaning up temporary directory: " + 
                    rootDir + " (" + e.getMessage() + ")", e);
        }
    }
}
