package us.kbase.narrativemethodstore.db;

import java.util.List;
import java.util.Set;

import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public interface DynamicRepoDB {
    
    public boolean isRepoRegistered(String repoModuleName) throws NarrativeMethodStoreException;
    
    public void registerRepo(String userId, RepoProvider repoDetails) throws NarrativeMethodStoreException;

    public long getRepoLastVersion(String repoModuleName) throws NarrativeMethodStoreException;
    
    public List<String> listRepoModuleNames() throws NarrativeMethodStoreException;
    
    public RepoProvider getRepoDetails(String repoModuleName) throws NarrativeMethodStoreException;
    
    public List<Long> listRepoVersions(String repoModuleName) throws NarrativeMethodStoreException;
    
    public RepoProvider getRepoDetailsHistory(String repoModuleName, long version) throws NarrativeMethodStoreException;
    
    public Set<String> listRepoOwners(String repoModuleName) throws NarrativeMethodStoreException;
    
    //public void setRepoOwner(String currentUserId, String repoModuleName, String changedUserId, boolean isAdmin) throws NarrativeMethodStoreException;
    
    //public void removeRepoOwner(String currentUserId, String repoModuleName, String removedUserId) throws NarrativeMethodStoreException;
    
    public boolean isRepoOwner(String repoModuleName, String userId) throws NarrativeMethodStoreException;
    
    //public boolean isRepoAdmin(String repoModuleName, String userId) throws NarrativeMethodStoreException;
    
}
