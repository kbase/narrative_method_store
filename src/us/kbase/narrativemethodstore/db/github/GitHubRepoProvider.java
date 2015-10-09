package us.kbase.narrativemethodstore.db.github;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import us.kbase.narrativemethodstore.db.FilePointer;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreInitializationException;

public class GitHubRepoProvider extends FileRepoProvider {
    protected String commitHash;

    public GitHubRepoProvider(URL url, String commitHash, File parentTempDir) throws NarrativeMethodStoreException {
        super(prepareGitClone(url, generateTempDir(parentTempDir)), url);
        try {
            if (commitHash != null)
                GitUtils.gitCheckout(rootDir, url, commitHash);
            this.commitHash = GitUtils.getCommitHash(rootDir, url);
        } catch (NarrativeMethodStoreException ex) {
            dispose(rootDir);
            throw ex;
        }
    }

    private static File prepareGitClone(URL url, File rootDir) throws NarrativeMethodStoreException {
        try {
            GitUtils.gitClone(url, rootDir);
            return rootDir;
        } catch (NarrativeMethodStoreException ex) {
            dispose(rootDir);
            throw ex;
        }
    }

    private static File generateTempDir(File parentTempDir) throws NarrativeMethodStoreException {
        try {
            return us.kbase.narrativemethodstore.util.FileUtils.generateTempDir(parentTempDir, "github_", ".temp");
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException(ex.getMessage(), ex);
        }
    }
    
    @Override
    public String getGitCommitHash() {
        return commitHash;
    }
    
    @Override
    public FilePointer getRepoZip() throws NarrativeMethodStoreException {
        // We don't create git repo zip because we have git-url + commit-hash to track files.
        return null;
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
