package us.kbase.narrativemethodstore.db.mongo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.google.common.collect.Lists;
import com.mongodb.DB;

import us.kbase.common.mongo.GetMongoDB;
import us.kbase.narrativemethodstore.db.DynamicRepoDB;
import us.kbase.narrativemethodstore.db.JsonRepoProvider;
import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class MongoDynamicRepoDB implements DynamicRepoDB {
    private final DB db;
    private final Jongo jdb;
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
    
    public MongoDynamicRepoDB(String host, String database) 
            throws NarrativeMethodStoreException {
        try {
            db = GetMongoDB.getDB(host, database, 0, 10);
            jdb = new Jongo(db);
            ensureIndeces();
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
        List<Long> vers = getProjection(jdb.getCollection(TABLE_REPO_DATA),
                String.format("{%s:#}", FIELD_RD_MODULE_NAME), 
                FIELD_RD_LAST_VERSION, Long.class, repoModuleName);
        return vers.size() > 0;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected <T> List<T> getProjection(MongoCollection infos,
            String whereCondition, String selectField, Class<T> type, Object... params)
            throws NarrativeMethodStoreException {
        List<Map> data = Lists.newArrayList(infos.find(whereCondition, params).projection(
                "{" + selectField + ":1}").as(Map.class));
        List<T> ret = new ArrayList<T>();
        for (Map<?,?> item : data) {
            Object value = item.get(selectField);
            if (value == null || !(type.isInstance(value)))
                throw new NarrativeMethodStoreException("Value is wrong: " + value);
            ret.add((T)value);
        }
        return ret;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected <KT, VT> Map<KT, VT> getProjection(MongoCollection infos, String whereCondition, 
            String keySelectField, Class<KT> keyType, String valueSelectField, Class<VT> valueType, 
            Object... params) throws NarrativeMethodStoreException {
        List<Map> data = Lists.newArrayList(infos.find(whereCondition, params).projection(
                "{'" + keySelectField + "':1,'" + valueSelectField + "':1}").as(Map.class));
        Map<KT, VT> ret = new LinkedHashMap<KT, VT>();
        for (Map<?,?> item : data) {
            Object key = getMongoProp(item, keySelectField);
            if (key == null || !(keyType.isInstance(key)))
                throw new NarrativeMethodStoreException("Key is wrong: " + key);
            Object value = getMongoProp(item, valueSelectField);
            if (value == null || !(valueType.isInstance(value)))
                throw new NarrativeMethodStoreException("Value is wrong: " + value);
            ret.put((KT)key, (VT)value);
        }
        return ret;
    }
    
    private static Object getMongoProp(Map<?,?> data, String propWithDots) {
        String[] parts = propWithDots.split(Pattern.quote("."));
        Object value = null;
        for (String part : parts) {
            if (value != null) {
                data = (Map<?,?>)value;
            }
            value = data.get(part);
        }
        return value;
    }
    
    @Override
    public void registerRepo(String userId, RepoProvider repoDetails) 
            throws NarrativeMethodStoreException {
        String repoModuleName = repoDetails.getModuleName();
        long newVersion = System.currentTimeMillis();
        boolean wasReg = isRepoRegistered(repoModuleName);
        if (wasReg) {
            if (!isRepoOwner(repoModuleName, userId))
                throw new NarrativeMethodStoreException("User " + userId + 
                        " is not owner of repository " + repoModuleName);
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
        MongoCollection data = jdb.getCollection(TABLE_REPO_HISTORY);
        if (wasReg) {
            data.update(String.format("{%s:#}", FIELD_RD_MODULE_NAME), 
                    repoModuleName).with(String.format("{%s:#,%s:#,%s:#}", 
                            FIELD_RD_LAST_VERSION, FIELD_RD_JSON_DATA, 
                            FIELD_RD_IS_INVALID), newVersion, jsonData, false);
        } else {
            data.insert(String.format("{%s:#,%s:#,%s:#,%s:#}", FIELD_RD_MODULE_NAME,
                    FIELD_RD_LAST_VERSION, FIELD_RD_JSON_DATA, FIELD_RD_IS_INVALID), 
                    repoModuleName, newVersion, jsonData, false);
        }
    }
    
    @Override
    public long getRepoLastVersion(String repoModuleName)
            throws NarrativeMethodStoreException {
        List<Long> vers = getProjection(jdb.getCollection(TABLE_REPO_DATA),
                String.format("{%s:#}", FIELD_RD_MODULE_NAME), 
                FIELD_RD_LAST_VERSION, Long.class, repoModuleName);
        checkRepoRegistered(repoModuleName, vers);
        return vers.get(0);
    }

    private void checkRepoRegistered(String repoModuleName, List<?> rows)
            throws NarrativeMethodStoreException {
        if (rows.isEmpty())
            throwRepoWasntRegistered(repoModuleName);
    }

    private void throwRepoWasntRegistered(String repoModuleName)
            throws NarrativeMethodStoreException {
        throw new NarrativeMethodStoreException("Repository wasn't " +
        		"registered: " + repoModuleName);
    }
    
    @Override
    public List<String> listRepoModuleNames()
            throws NarrativeMethodStoreException {
        List<String> ret = getProjection(jdb.getCollection(TABLE_REPO_DATA),
                "{}", FIELD_RD_MODULE_NAME, String.class);
        return ret;
    }
    
    @Override
    public RepoProvider getRepoDetails(String repoModuleName)
            throws NarrativeMethodStoreException {
        List<String> ret = getProjection(jdb.getCollection(TABLE_REPO_DATA),
                String.format("{%s:#}", FIELD_RD_MODULE_NAME), 
                FIELD_RD_JSON_DATA, String.class, repoModuleName);
        checkRepoRegistered(repoModuleName, ret);
        return new JsonRepoProvider(ret.get(0));
    }
    
    @Override
    public List<Long> getRepoVersions(String repoModuleName)
            throws NarrativeMethodStoreException {
        if (!isRepoRegistered(repoModuleName))
            throwRepoWasntRegistered(repoModuleName);
        List<Long> ret = getProjection(jdb.getCollection(TABLE_REPO_HISTORY),
                String.format("{%s:#}", FIELD_RH_MODULE_NAME), 
                FIELD_RH_VERSION, Long.class, repoModuleName);
        return ret;
    }
    
    @Override
    public RepoProvider getRepoDetailsHistory(String repoModuleName,
            long version) throws NarrativeMethodStoreException {
        List<String> ret = getProjection(jdb.getCollection(TABLE_REPO_HISTORY),
                String.format("{%s:#,%s:#}", FIELD_RH_MODULE_NAME, FIELD_RH_VERSION), 
                FIELD_RD_JSON_DATA, String.class, repoModuleName, version);
        checkRepoRegistered(repoModuleName, ret);
        return new JsonRepoProvider(ret.get(0));
    }

    private Map<String, Boolean> getRepoOwnersWithAdminValues(
            String repoModuleName) throws NarrativeMethodStoreException {
        if (!isRepoRegistered(repoModuleName))
            throwRepoWasntRegistered(repoModuleName);
        Map<String, Boolean> ret = getProjection(jdb.getCollection(TABLE_REPO_OWNERS),
                String.format("{%s:#}", FIELD_RO_MODULE_NAME), 
                FIELD_RO_USER_ID, String.class, FIELD_RO_IS_ADMIN, Boolean.class, repoModuleName);
        return ret;
    }

    @Override
    public Set<String> getRepoOwners(String repoModuleName)
            throws NarrativeMethodStoreException {
        return new TreeSet<String>(getRepoOwnersWithAdminValues(repoModuleName).keySet());
    }
    
    @Override
    public void setRepoOwner(String repoModuleName, String userId, boolean isAdmin)
            throws NarrativeMethodStoreException {
        Map<String, Boolean> map = getRepoOwnersWithAdminValues(repoModuleName);
        MongoCollection owners = jdb.getCollection(TABLE_REPO_OWNERS);
        if (map.containsKey(userId)) {
            owners.update(String.format("{%s:#,%s:#}", FIELD_RO_MODULE_NAME,
                    FIELD_RO_USER_ID), repoModuleName, userId).with(
                            String.format("{%s:#}", FIELD_RO_IS_ADMIN), isAdmin);
        } else {
            owners.insert(String.format("{%s:#,%s:#,%s:#}", FIELD_RO_MODULE_NAME,
                    FIELD_RO_USER_ID, FIELD_RO_IS_ADMIN), repoModuleName,
                    userId, isAdmin);
        }
    }
    
    @Override
    public void removeRepoOwner(String repoModuleName, String userId)
            throws NarrativeMethodStoreException {
        if (!isRepoRegistered(repoModuleName))
            throwRepoWasntRegistered(repoModuleName);
        MongoCollection owners = jdb.getCollection(TABLE_REPO_OWNERS);
        owners.remove(String.format("{%s:#,%s:#}", FIELD_RO_MODULE_NAME,
                FIELD_RO_USER_ID), repoModuleName, userId);
    }
    
    @Override
    public boolean isRepoOwner(String repoModuleName, String userId)
            throws NarrativeMethodStoreException {
        return getRepoOwners(repoModuleName).contains(userId);
    }
    
    @Override
    public boolean isRepoAdmin(String repoModuleName, String userId)
            throws NarrativeMethodStoreException {
        Map<String, Boolean> map = getRepoOwnersWithAdminValues(repoModuleName);
        if (!map.containsKey(userId))
            throw new NarrativeMethodStoreException("User " + userId + " is not " +
            		"the owner of repository: " + repoModuleName);
        return map.get(userId);
    }
}
