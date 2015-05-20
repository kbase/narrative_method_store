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

    public GitHubRepoProvider(URL url, File parentTempDir) throws NarrativeMethodStoreException {
        this(url, "master", parentTempDir);
    }

    public GitHubRepoProvider(URL url, String branch, File parentTempDir) throws NarrativeMethodStoreException {
        super(generateTempDir(parentTempDir));
        GitUtils.gitClone(url, branch, rootDir);
        this.url = url;
        this.branch = branch;
    }
    
    private static File generateTempDir(File parentTempDir) throws NarrativeMethodStoreException {
        try {
            return File.createTempFile("github_", ".temp", parentTempDir);
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException(ex.getMessage(), ex);
        }
    }

    @Override
    public String getUrl() {
        return "" + url;
    }
    
    @Override
    public void dispose() throws NarrativeMethodStoreException {
        try {
            FileUtils.deleteDirectory(rootDir);
        } catch (IOException e) {
            throw new NarrativeMethodStoreInitializationException("Error cleaning up temporary directory: " + 
                    rootDir + " (" + e.getMessage() + ")", e);
        }
    }
}
