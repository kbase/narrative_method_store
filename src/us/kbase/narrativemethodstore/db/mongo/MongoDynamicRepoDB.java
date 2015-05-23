package us.kbase.narrativemethodstore.db.mongo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.DB;

import us.kbase.common.mongo.GetMongoDB;
import us.kbase.narrativemethodstore.db.DynamicRepoDB;
import us.kbase.narrativemethodstore.db.JsonRepoProvider;
import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class MongoDynamicRepoDB implements DynamicRepoDB {
    private final DB db;
    private final Jongo jdb;
    private final Set<String> globalAdmins;
    ////////////////////////////////////////////////////////////////////
    private static final String TABLE_REPO_DATA = "repo_data";
    private static final String FIELD_RD_MODULE_NAME = "module_name";
    private static final String FIELD_RD_LAST_VERSION = "last_version";
    private static final String FIELD_RD_JSON_DATA = "json_data";
    private static final String FIELD_RD_IS_INVALID = "is_invalid";
    ////////////////////////////////////////////////////////////////////
    private static final String TABLE_REPO_HISTORY = "repo_history";
    private static final String FIELD_RH_MODULE_NAME = "module_name";
    private static final String FIELD_RH_VERSION = "version";
    private static final String FIELD_RH_JSON_DATA = "json_data";
    ////////////////////////////////////////////////////////////////////
    private static final String TABLE_REPO_OWNERS = "repo_owners";
    private static final String FIELD_RO_MODULE_NAME = "module_name";
    private static final String FIELD_RO_USER_ID = "user_id";
    private static final String FIELD_RO_IS_ADMIN = "is_admin";
    ////////////////////////////////////////////////////////////////////
    
    public MongoDynamicRepoDB(String host, String database, 
            List<String> globalAdminUserIds) throws NarrativeMethodStoreException {
        try {
            db = GetMongoDB.getDB(host, database, 0, 10);
            jdb = new Jongo(db);
            ensureIndeces();
            globalAdmins = new HashSet<String>(globalAdminUserIds);
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException(ex);
        }
    }
    
    private void ensureIndeces() {
        MongoCollection repoData = jdb.getCollection(TABLE_REPO_DATA);
        repoData.ensureIndex(String.format("{%s:1}", FIELD_RD_MODULE_NAME), "{unique:true}");
        MongoCollection repoHist = jdb.getCollection(TABLE_REPO_HISTORY);
        repoHist.ensureIndex(String.format("{%s:1,%s:1}", FIELD_RH_MODULE_NAME, 
                FIELD_RH_VERSION), "{unique:true}");
        MongoCollection owners = jdb.getCollection(TABLE_REPO_OWNERS);
        owners.ensureIndex(String.format("{%s:1,%s:1}", FIELD_RO_MODULE_NAME, 
                FIELD_RO_USER_ID), "{unique:true}");
        owners.ensureIndex(String.format("{%s:1}", FIELD_RO_USER_ID), "{unique:false}");
    }
    
    @Override
    public boolean isRepoRegistered(String repoModuleName)
            throws NarrativeMethodStoreException {
        List<Long> vers = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_DATA),
                String.format("{%s:#}", FIELD_RD_MODULE_NAME), 
                FIELD_RD_LAST_VERSION, Long.class, repoModuleName);
        return vers.size() > 0;
    }
    
    private void checkRepoRegistered(String repoModuleName, List<?> rows)
            throws NarrativeMethodStoreException {
        if (rows.isEmpty())
            throwRepoWasntRegistered(repoModuleName);
    }

    private void checkRepoRegistered(String repoModuleName)
            throws NarrativeMethodStoreException {
        if (!isRepoRegistered(repoModuleName))
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
        String repoModuleName = repoDetails.getModuleName();
        long newVersion = System.currentTimeMillis();
        boolean wasReg = isRepoRegistered(repoModuleName);
        if (wasReg) {
            checkRepoOwner(repoModuleName, userId);
            long oldVersion = getRepoLastVersion(repoModuleName);
            if (newVersion <= oldVersion)
                newVersion = oldVersion + 1;
        } else {
            MongoCollection owners = jdb.getCollection(TABLE_REPO_OWNERS);
            owners.insert(String.format("{%s:#,%s:#,%s:#}", FIELD_RO_MODULE_NAME,
                    FIELD_RO_USER_ID, FIELD_RO_IS_ADMIN), repoModuleName,
                    userId, true);
        }
        String jsonData = JsonRepoProvider.repoProviderToJsonString(repoDetails);
        MongoCollection hist = jdb.getCollection(TABLE_REPO_HISTORY);
        hist.insert(String.format("{%s:#,%s:#,%s:#}", FIELD_RH_MODULE_NAME,
                FIELD_RH_VERSION, FIELD_RH_JSON_DATA), repoModuleName,
                newVersion, jsonData);
        MongoCollection data = jdb.getCollection(TABLE_REPO_DATA);
        if (wasReg) {
            data.update(String.format("{%s:#}", FIELD_RD_MODULE_NAME), 
                    repoModuleName).with(String.format("{%s:#,%s:#,%s:#,%s:#}", 
                            FIELD_RD_MODULE_NAME, FIELD_RD_LAST_VERSION, FIELD_RD_JSON_DATA, 
                            FIELD_RD_IS_INVALID), repoModuleName, newVersion, jsonData, false);
        } else {
            data.insert(String.format("{%s:#,%s:#,%s:#,%s:#}", FIELD_RD_MODULE_NAME,
                    FIELD_RD_LAST_VERSION, FIELD_RD_JSON_DATA, FIELD_RD_IS_INVALID), 
                    repoModuleName, newVersion, jsonData, false);
        }
    }
    
    @Override
    public long getRepoLastVersion(String repoModuleName)
            throws NarrativeMethodStoreException {
        List<Long> vers = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_DATA),
                String.format("{%s:#}", FIELD_RD_MODULE_NAME), 
                FIELD_RD_LAST_VERSION, Long.class, repoModuleName);
        checkRepoRegistered(repoModuleName, vers);
        return vers.get(0);
    }
    
    @Override
    public List<String> listRepoModuleNames()
            throws NarrativeMethodStoreException {
        List<String> ret = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_DATA),
                "{}", FIELD_RD_MODULE_NAME, String.class);
        return ret;
    }
    
    @Override
    public RepoProvider getRepoDetails(String repoModuleName)
            throws NarrativeMethodStoreException {
        List<String> ret = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_DATA),
                String.format("{%s:#}", FIELD_RD_MODULE_NAME), 
                FIELD_RD_JSON_DATA, String.class, repoModuleName);
        checkRepoRegistered(repoModuleName, ret);
        return new JsonRepoProvider(ret.get(0));
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
        List<String> ret = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_HISTORY),
                String.format("{%s:#,%s:#}", FIELD_RH_MODULE_NAME, FIELD_RH_VERSION), 
                FIELD_RD_JSON_DATA, String.class, repoModuleName, version);
        checkRepoRegistered(repoModuleName, ret);
        return new JsonRepoProvider(ret.get(0));
    }

    @Override
    public Set<String> listRepoOwners(String repoModuleName)
            throws NarrativeMethodStoreException {
        checkRepoRegistered(repoModuleName);
        List<String> ret = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_OWNERS),
                String.format("{%s:#}", FIELD_RO_MODULE_NAME), 
                FIELD_RO_USER_ID, String.class, repoModuleName);
        return new TreeSet<String>(ret);
    }
    
    @Override
    public void setRepoOwner(String currentUserId, String repoModuleName, 
            String changedUserId, boolean isAdmin) throws NarrativeMethodStoreException {
        checkRepoAdmin(repoModuleName, currentUserId);
        Boolean ret = getOwnerAdminInfo(repoModuleName, changedUserId);
        MongoCollection owners = jdb.getCollection(TABLE_REPO_OWNERS);
        if (ret == null) {
            owners.insert(String.format("{%s:#,%s:#,%s:#}", FIELD_RO_MODULE_NAME,
                    FIELD_RO_USER_ID, FIELD_RO_IS_ADMIN), repoModuleName,
                    changedUserId, isAdmin);
        } else {
            owners.update(String.format("{%s:#,%s:#}", FIELD_RO_MODULE_NAME,
                    FIELD_RO_USER_ID), repoModuleName, changedUserId).with(
                            String.format("{%s:#,%s:#,%s:#}", FIELD_RO_MODULE_NAME,
                                    FIELD_RO_USER_ID, FIELD_RO_IS_ADMIN), 
                                    repoModuleName, changedUserId, isAdmin);
        }
    }
    
    @Override
    public void removeRepoOwner(String currentUserId, String repoModuleName, 
            String removedUserId) throws NarrativeMethodStoreException {
        checkRepoAdmin(repoModuleName, currentUserId);
        checkRepoOwner(repoModuleName, removedUserId);
        MongoCollection owners = jdb.getCollection(TABLE_REPO_OWNERS);
        owners.remove(String.format("{%s:#,%s:#}", FIELD_RO_MODULE_NAME,
                FIELD_RO_USER_ID), repoModuleName, removedUserId);
    }
    
    @Override
    public boolean isRepoOwner(String repoModuleName, String userId)
            throws NarrativeMethodStoreException {
        if (globalAdmins.contains(userId))
            return true;
        Boolean ret = getOwnerAdminInfo(repoModuleName, userId);
        return ret != null;
    }

    private void checkRepoOwner(String repoModuleName, String userId)
            throws NarrativeMethodStoreException {
        if (!isRepoOwner(repoModuleName, userId))
            throw new NarrativeMethodStoreException("User " + userId + 
                    " is not owner of repository " + repoModuleName);
    }

    private Boolean getOwnerAdminInfo(String repoModuleName, String userId)
            throws NarrativeMethodStoreException {
        checkRepoRegistered(repoModuleName);
        List<Boolean> ret = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_OWNERS),
                String.format("{%s:#,%s:#}", FIELD_RO_MODULE_NAME, FIELD_RO_USER_ID), 
                FIELD_RO_IS_ADMIN, Boolean.class, 
                repoModuleName, userId);
        return ret.isEmpty() ? null : ret.get(0);
    }
    
    @Override
    public boolean isRepoAdmin(String repoModuleName, String userId)
            throws NarrativeMethodStoreException {
        if (globalAdmins.contains(userId))
            return true;
        Boolean ret = getOwnerAdminInfo(repoModuleName, userId);
        return ret != null && ret;
    }
    
    private void checkRepoAdmin(String repoModuleName, String userId)
            throws NarrativeMethodStoreException {
        if (!isRepoAdmin(repoModuleName, userId))
            throw new NarrativeMethodStoreException("User " + userId + " is not " +
                    "admin of repository " + repoModuleName);
    }
}
