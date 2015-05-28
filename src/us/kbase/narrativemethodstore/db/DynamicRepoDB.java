package us.kbase.narrativemethodstore.db;

import java.util.List;
import java.util.Set;

import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public interface DynamicRepoDB {
    
    public boolean isRepoRegistered(String repoModuleName, boolean withDisabled) throws NarrativeMethodStoreException;
    
    public void registerRepo(String userId, RepoProvider repoDetails) throws NarrativeMethodStoreException;

    public long getRepoLastVersion(String repoModuleName) throws NarrativeMethodStoreException;
    
    public List<String> listRepoModuleNames(boolean withDisabled) throws NarrativeMethodStoreException;
    
    public RepoProvider getRepoDetails(String repoModuleName) throws NarrativeMethodStoreException;
    
    public List<Long> listRepoVersions(String repoModuleName) throws NarrativeMethodStoreException;
    
    public RepoProvider getRepoDetailsHistory(String repoModuleName, long version) throws NarrativeMethodStoreException;
    
    public Set<String> listRepoOwners(String repoModuleName) throws NarrativeMethodStoreException;
    
    //public void setRepoOwner(String currentUserId, String repoModuleName, String changedUserId, boolean isAdmin) throws NarrativeMethodStoreException;
    
    //public void removeRepoOwner(String currentUserId, String repoModuleName, String removedUserId) throws NarrativeMethodStoreException;
    
    public boolean isRepoOwner(String repoModuleName, String userId) throws NarrativeMethodStoreException;
    
    //public boolean isRepoAdmin(String repoModuleName, String userId) throws NarrativeMethodStoreException;
    
    public RepoState getRepoState(String repoModuleName) throws NarrativeMethodStoreException;
    
    public void setRepoState(String userId, String repoModuleName, RepoState state) throws NarrativeMethodStoreException;
    
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
}
