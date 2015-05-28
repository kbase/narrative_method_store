package us.kbase.narrativemethodstore.db.mongo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.DB;

import us.kbase.common.mongo.GetMongoDB;
import us.kbase.narrativemethodstore.db.DynamicRepoDB;
import us.kbase.narrativemethodstore.db.JsonRepoProvider;
import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.db.JsonRepoProvider.RepoData;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class MongoDynamicRepoDB implements DynamicRepoDB {
    private final DB db;
    private final Jongo jdb;
    private final Set<String> globalAdmins;
    ////////////////////////////////////////////////////////////////////
    private static final String TABLE_REPO_INFO = "repo_info";
    private static final String FIELD_RI_MODULE_NAME = "module_name";
    private static final String FIELD_RI_LAST_VERSION = "last_version";
    private static final String FIELD_RI_STATE = "state";
    ////////////////////////////////////////////////////////////////////
    private static final String TABLE_REPO_HISTORY = "repo_history";
    private static final String FIELD_RH_MODULE_NAME = "module_name";
    private static final String FIELD_RH_VERSION = "version";
    private static final String FIELD_RH_REPO_DATA = "repo_data";
    ////////////////////////////////////////////////////////////////////
    
    public MongoDynamicRepoDB(String host, String database, String dbUser, String dbPwd,
            List<String> globalAdminUserIds) throws NarrativeMethodStoreException {
        try {
            if (dbUser == null && dbPwd == null) {
                db = GetMongoDB.getDB(host, database, 0, 10);
            } else {
                db = GetMongoDB.getDB(host, database, dbUser, dbPwd, 0, 10);
            }
            jdb = new Jongo(db);
            ensureIndeces();
            globalAdmins = new HashSet<String>(globalAdminUserIds);
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException(ex);
        }
    }
    
    private void ensureIndeces() {
        MongoCollection repoData = jdb.getCollection(TABLE_REPO_INFO);
        repoData.ensureIndex(String.format("{%s:1}", FIELD_RI_MODULE_NAME), "{unique:true}");
        MongoCollection repoHist = jdb.getCollection(TABLE_REPO_HISTORY);
        repoHist.ensureIndex(String.format("{%s:1,%s:1}", FIELD_RH_MODULE_NAME, 
                FIELD_RH_VERSION), "{unique:true}");
    }
    
    @Override
    public boolean isRepoRegistered(String repoModuleName, boolean withDisabled)
            throws NarrativeMethodStoreException {
        List<String> dis = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_INFO),
                String.format("{%s:#}", FIELD_RI_MODULE_NAME), 
                FIELD_RI_STATE, String.class, repoModuleName);
        return dis.size() > 0 && (withDisabled || RepoState.valueOf(dis.get(0)) != RepoState.disabled);
    }
    
    private void checkRepoRegistered(String repoModuleName, List<?> rows)
            throws NarrativeMethodStoreException {
        if (rows.isEmpty())
            throwRepoWasntRegistered(repoModuleName);
    }

    private void checkRepoRegistered(String repoModuleName)
            throws NarrativeMethodStoreException {
        if (!isRepoRegistered(repoModuleName, true))
            throwRepoWasntRegistered(repoModuleName);
    }

    private void throwRepoWasntRegistered(String repoModuleName)
            throws NarrativeMethodStoreException {
        throw new NarrativeMethodStoreException("Repository " + repoModuleName + 
                " wasn't registered");
    }
    
    @Override
    public void registerRepo(String userId, RepoProvider repoDetails) 
            throws NarrativeMethodStoreException {
        checkRepoOwner(repoDetails, userId);
        String repoModuleName = repoDetails.getModuleName();
        long newVersion = System.currentTimeMillis();
        boolean wasReg = isRepoRegistered(repoModuleName, true);
        if (wasReg) {
            long oldVersion = getRepoLastVersion(repoModuleName);
            if (newVersion <= oldVersion)
                newVersion = oldVersion + 1;
            RepoProvider oldDetails = getRepoDetailsHistory(repoModuleName, 
                    oldVersion);
            String oldUrl = oldDetails.getUrl();
            String newUrl = repoDetails.getUrl();
            if (!newUrl.equals(oldUrl)) {
                if (isRepoOwner(repoModuleName, userId))
                    throw new NarrativeMethodStoreException("Only current owner " +
                    		"can change git url of repository: " + oldUrl + " -> " +
                    				newUrl);
            }
        }
        RepoData repoData = JsonRepoProvider.repoProviderToData(repoDetails);
        MongoCollection hist = jdb.getCollection(TABLE_REPO_HISTORY);
        hist.insert(String.format("{%s:#,%s:#,%s:#}", FIELD_RH_MODULE_NAME,
                FIELD_RH_VERSION, FIELD_RH_REPO_DATA), repoModuleName,
                newVersion, repoData);
        MongoCollection data = jdb.getCollection(TABLE_REPO_INFO);
        if (wasReg) {
            data.update(String.format("{%s:#}", FIELD_RI_MODULE_NAME), 
                    repoModuleName).with(String.format("{%s:#,%s:#,%s:#}", 
                            FIELD_RI_MODULE_NAME, FIELD_RI_LAST_VERSION, 
                            FIELD_RI_STATE), repoModuleName, newVersion, RepoState.ready);
        } else {
            data.insert(String.format("{%s:#,%s:#,%s:#}", FIELD_RI_MODULE_NAME,
                    FIELD_RI_LAST_VERSION, FIELD_RI_STATE), 
                    repoModuleName, newVersion, RepoState.ready);
        }
    }
    
    @Override
    public long getRepoLastVersion(String repoModuleName)
            throws NarrativeMethodStoreException {
        List<Long> vers = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_INFO),
                String.format("{%s:#}", FIELD_RI_MODULE_NAME), 
                FIELD_RI_LAST_VERSION, Long.class, repoModuleName);
        checkRepoRegistered(repoModuleName, vers);
        return vers.get(0);
    }
    
    @Override
    public List<String> listRepoModuleNames(boolean withDisabled)
            throws NarrativeMethodStoreException {
        Map<String, String> map = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_INFO),
                "{}", FIELD_RI_MODULE_NAME, String.class, FIELD_RI_STATE, String.class);
        List<String> ret = new ArrayList<String>();
        for (Map.Entry<String, String> entry : map.entrySet())
            if (withDisabled || RepoState.valueOf(entry.getValue()) != RepoState.disabled)
                ret.add(entry.getKey());
        return ret;
    }
    
    @Override
    public RepoProvider getRepoDetails(String repoModuleName)
            throws NarrativeMethodStoreException {
        return getRepoDetailsHistory(repoModuleName, getRepoLastVersion(repoModuleName));
    }
    
    @Override
    public List<Long> listRepoVersions(String repoModuleName)
            throws NarrativeMethodStoreException {
        checkRepoRegistered(repoModuleName);
        List<Long> ret = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_HISTORY),
                String.format("{%s:#}", FIELD_RH_MODULE_NAME), 
                FIELD_RH_VERSION, Long.class, repoModuleName);
        return ret;
    }
    
    @Override
    public RepoProvider getRepoDetailsHistory(String repoModuleName,
            long version) throws NarrativeMethodStoreException {
        List<RepoData> ret = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_HISTORY),
                String.format("{%s:#,%s:#}", FIELD_RH_MODULE_NAME, FIELD_RH_VERSION), 
                FIELD_RH_REPO_DATA, RepoData.class, repoModuleName, version);
        checkRepoRegistered(repoModuleName, ret);
        return new JsonRepoProvider(ret.get(0));
    }

    @Override
    public Set<String> listRepoOwners(String repoModuleName)
            throws NarrativeMethodStoreException {
        return new TreeSet<String>(getRepoDetails(repoModuleName).listOwners());
    }
    
    @Override
    public boolean isRepoOwner(String repoModuleName, String userId)
            throws NarrativeMethodStoreException {
        checkRepoRegistered(repoModuleName);
        if (globalAdmins.contains(userId))
            return true;
        return listRepoOwners(repoModuleName).contains(userId);
    }

    private void checkRepoOwner(RepoProvider repo, String userId)
            throws NarrativeMethodStoreException {
        if (globalAdmins.contains(userId))
            return;
        if (!new TreeSet<String>(repo.listOwners()).contains(userId))
            throw new NarrativeMethodStoreException("User " + userId + 
                    " is not owner of repository " + repo.getModuleName());
    }

    private void checkRepoOwner(String repoModuleName, String userId)
            throws NarrativeMethodStoreException {
        if (!isRepoOwner(repoModuleName, userId))
            throw new NarrativeMethodStoreException("User " + userId + 
                    " is not owner of repository " + repoModuleName);
    }
    
    @Override
    public RepoState getRepoState(String repoModuleName)
            throws NarrativeMethodStoreException {
        List<String> state = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_INFO),
                String.format("{%s:#}", FIELD_RI_MODULE_NAME), 
                FIELD_RI_STATE, String.class, repoModuleName);
        checkRepoRegistered(repoModuleName, state);
        return RepoState.valueOf(state.get(0));
    }
    
    @Override
    public void setRepoState(String userId, String repoModuleName, RepoState state)
            throws NarrativeMethodStoreException {
        checkRepoOwner(repoModuleName, userId);
        if (state.isAdminOnly()) {
            if (!globalAdmins.contains(userId))
                throw new NarrativeMethodStoreException("User " + userId + 
                        " is not global admin");
        }
        MongoCollection info = jdb.getCollection(TABLE_REPO_INFO);
        @SuppressWarnings("unchecked")
        Map<String, Object> obj = info.findOne(String.format("{%s:#}", 
                FIELD_RI_MODULE_NAME), repoModuleName).as(Map.class);
        info.update(String.format("{%s:#}", FIELD_RI_MODULE_NAME), 
                repoModuleName).with(String.format("{%s:#,%s:#,%s:#}", 
                        FIELD_RI_MODULE_NAME, FIELD_RI_LAST_VERSION, 
                        FIELD_RI_STATE), repoModuleName, 
                        obj.get(FIELD_RI_LAST_VERSION), state.name());
    }
}
