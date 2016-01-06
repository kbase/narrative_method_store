package us.kbase.narrativemethodstore.db;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import us.kbase.narrativemethodstore.db.github.RepoTag;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public interface DynamicRepoDB {
    
    public boolean isRepoRegistered(String repoModuleName, boolean withDisabled) throws NarrativeMethodStoreException;
    
    public void registerRepo(String userId, RepoProvider repoDetails) throws NarrativeMethodStoreException;

    public Long getRepoLastVersion(String repoModuleName, RepoTag tag) throws NarrativeMethodStoreException;
    
    public List<String> listRepoModuleNames(boolean withDisabled, RepoTag tag) throws NarrativeMethodStoreException;
    
    public RepoProvider getRepoDetails(String repoModuleName, RepoTag tag) throws NarrativeMethodStoreException;
    
    public List<Long> listRepoVersions(String repoModuleName, RepoTag tag) throws NarrativeMethodStoreException;
    
    public RepoProvider getRepoDetailsHistory(String repoModuleName, long version) throws NarrativeMethodStoreException;
    
    public void pushRepoToTag(String repoModuleName, RepoTag tag, String userId) throws NarrativeMethodStoreException;

    public Set<String> listRepoOwners(String repoModuleName) throws NarrativeMethodStoreException;
    
    public boolean isRepoOwner(String repoModuleName, String userId) throws NarrativeMethodStoreException;
    
    public RepoState getRepoState(String repoModuleName) throws NarrativeMethodStoreException;
    
    public void setRepoState(String userId, String repoModuleName, RepoState state) throws NarrativeMethodStoreException;
    
    public FileId saveFile(String moduleName, File file) throws NarrativeMethodStoreException;

    public FileId saveFile(String moduleName, FileProvider file) throws NarrativeMethodStoreException;

    public FilePointer loadFile(FileId fileId) throws NarrativeMethodStoreException;
    
    public enum RepoState {
        ready(true), building(true), testing(true), disabled(false);
        
        private boolean adminOnly;
        
        private RepoState(boolean adminOnly) {
            this.adminOnly = adminOnly;
        }
        
        public boolean isAdminOnly() {
            return adminOnly;
        }
    }
    
    public interface FileProvider {
        public String getName() throws NarrativeMethodStoreException;
        public long length() throws NarrativeMethodStoreException;
        public InputStream openStream() throws NarrativeMethodStoreException;
    }
}
