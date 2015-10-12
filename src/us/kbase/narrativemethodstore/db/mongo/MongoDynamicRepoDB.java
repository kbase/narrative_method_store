package us.kbase.narrativemethodstore.db.mongo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.DB;
import com.mongodb.MongoException.DuplicateKey;

import us.kbase.auth.AuthToken;
import us.kbase.auth.TokenExpiredException;
import us.kbase.common.mongo.GetMongoDB;
import us.kbase.narrativemethodstore.db.DynamicRepoDB;
import us.kbase.narrativemethodstore.db.FileId;
import us.kbase.narrativemethodstore.db.FilePointer;
import us.kbase.narrativemethodstore.db.JsonRepoProvider;
import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.db.JsonRepoProvider.RepoData;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;
import us.kbase.shock.client.BasicShockClient;
import us.kbase.shock.client.ShockNodeId;
import us.kbase.shock.client.exceptions.InvalidShockUrlException;
import us.kbase.shock.client.exceptions.ShockHttpException;

public class MongoDynamicRepoDB implements DynamicRepoDB {
    private final DB db;
    private final Jongo jdb;
    private final Set<String> globalAdmins;
    private final boolean isReadOnly;
    private final URL shockUrl;
    private final AuthToken serviceToken;
    public static final long MAX_MONGO_FILE_LENGTH = 1024 * 1024;
    ////////////////////////////////////////////////////////////////////
    private static final String TABLE_REPO_INFO = "repo_info";
    private static final String FIELD_RI_MODULE_NAME = "module_name";
    private static final String FIELD_RI_DOCKER_IMAGE = "docker_image";
    private static final String FIELD_RI_LAST_VERSION = "last_version";
    private static final String FIELD_RI_STATE = "state";
    ////////////////////////////////////////////////////////////////////
    private static final String TABLE_REPO_HISTORY = "repo_history";
    private static final String FIELD_RH_MODULE_NAME = "module_name";
    private static final String FIELD_RH_VERSION = "version";
    private static final String FIELD_RH_REPO_DATA = "repo_data";
    ////////////////////////////////////////////////////////////////////
    private static final String TABLE_REPO_FILES = "repo_files";
    private static final String FIELD_RF_FILE_ID = "file_id";
    private static final String FIELD_RF_MODULE_NAME = "module_name";
    private static final String FIELD_RF_FILE_NAME = "file_name";
    private static final String FIELD_RF_LENGTH = "length";
    private static final String FIELD_RF_MD5 = "md5";
    private static final String FIELD_RF_HEX_DATA = "hex_data";
    private static final String FIELD_RF_SHOCK_NODE_ID = "shock_node_id";
    
    public MongoDynamicRepoDB(String host, String database, String dbUser, String dbPwd,
            List<String> globalAdminUserIds, boolean isReadOnly, URL shockUrl,
            AuthToken serviceToken) throws NarrativeMethodStoreException {
        this.isReadOnly = isReadOnly;
        this.shockUrl = shockUrl;
        this.serviceToken = serviceToken;
        try {
            if (dbUser == null && dbPwd == null) {
                db = GetMongoDB.getDB(host, database, 0, 10);
            } else {
                db = GetMongoDB.getDB(host, database, dbUser, dbPwd, 0, 10);
            }
            jdb = new Jongo(db);
            if (!isReadOnly)
                ensureIndeces();
            globalAdmins = new HashSet<String>(globalAdminUserIds);
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException(ex);
        }
    }
    
    private void ensureIndeces() {
        MongoCollection repoData = jdb.getCollection(TABLE_REPO_INFO);
        repoData.ensureIndex(String.format("{%s:1}", FIELD_RI_MODULE_NAME), "{unique:true}");
        repoData.ensureIndex(String.format("{%s:1}", FIELD_RI_DOCKER_IMAGE), "{unique:false}");
        MongoCollection repoHist = jdb.getCollection(TABLE_REPO_HISTORY);
        repoHist.ensureIndex(String.format("{%s:1,%s:1}", FIELD_RH_MODULE_NAME, 
                FIELD_RH_VERSION), "{unique:true}");
        MongoCollection repoFiles = jdb.getCollection(TABLE_REPO_FILES);
        repoFiles.ensureIndex(String.format("{%s:1}", FIELD_RF_FILE_ID), "{unique:true}");
        repoFiles.ensureIndex(String.format("{%s:1,%s:1,%s:1,%s:1}", FIELD_RF_MODULE_NAME, 
                FIELD_RF_FILE_NAME, FIELD_RF_LENGTH, FIELD_RF_MD5), "{unique:false}");
    }
    
    @Override
    public boolean isRepoRegistered(String repoModuleName, boolean withDisabled)
            throws NarrativeMethodStoreException {
        List<String> dis = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_INFO),
                String.format("{%s:#}", FIELD_RI_MODULE_NAME), 
                FIELD_RI_STATE, String.class, repoModuleName);
        return dis.size() > 0 && (withDisabled || 
                RepoState.valueOf(dis.get(0)) != RepoState.disabled);
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

    private void throwChangeOperation()
            throws NarrativeMethodStoreException {
        throw new NarrativeMethodStoreException("Change operation couldn't be performed in " +
        		"read-only mode");
    }

    @Override
    public void registerRepo(String userId, RepoProvider repoDetails) 
            throws NarrativeMethodStoreException {
        if (isReadOnly)
            throwChangeOperation();
        //checkRepoOwner(repoDetails, userId);
        checkAdmin(userId);
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
            if (oldUrl == null)
                oldUrl = "";
            String newUrl = repoDetails.getUrl();
            if (newUrl == null)
                newUrl = "";
            if ((!newUrl.equals(oldUrl)) && (!isRepoOwner(repoModuleName, userId)))
                throw new NarrativeMethodStoreException("Only current owner " +
                        "can change git url of repository: [" + oldUrl + "] -> [" +
                        newUrl + "]");
            if (newUrl.isEmpty() && (!isRepoOwner(repoModuleName, userId)))
                throw new NarrativeMethodStoreException("Only current owner " +
                        "can update non-git repository: [" + oldUrl + "] -> [" +
                        newUrl + "]");
            if (newUrl.isEmpty() && (!oldUrl.isEmpty()))
                throw new NarrativeMethodStoreException("Git repository " +
                        "can not be updated by non-git repository: [" + oldUrl + 
                        "] -> [" + newUrl + "]");
        }
        RepoData repoData = JsonRepoProvider.repoProviderToData(this, repoDetails);
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
    
    /*private boolean isDockerImageInUse(String dockerImage) 
            throws NarrativeMethodStoreException {
        List<String> ret = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_INFO),
                String.format("{%s:#}", FIELD_RI_DOCKER_IMAGE), 
                FIELD_RI_MODULE_NAME, String.class, dockerImage);
        return !ret.isEmpty();
    }*/
    
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
        return new JsonRepoProvider(this, ret.get(0));
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

    private void checkAdmin(String userId)
            throws NarrativeMethodStoreException {
        if (!globalAdmins.contains(userId))
            throw new NarrativeMethodStoreException("User " + userId + 
                    " is not global admin");
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
        if (isReadOnly)
            throwChangeOperation();
        /*checkRepoOwner(repoModuleName, userId);
        if (state.isAdminOnly()) {
            if (!globalAdmins.contains(userId))
                throw new NarrativeMethodStoreException("User " + userId + 
                        " is not global admin");
        }*/
        checkAdmin(userId);
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
    
    @Override
    public FileId saveFile(String moduleName, final File file) throws NarrativeMethodStoreException {
        return saveFile(moduleName, new FileProvider() {
            @Override
            public InputStream openStream() throws NarrativeMethodStoreException {
                try {
                    return new FileInputStream(file);
                } catch (IOException ex) {
                    throw new NarrativeMethodStoreException(ex);
                }
            }
            @Override
            public long length() throws NarrativeMethodStoreException {
                return file.length();
            }
            @Override
            public String getName() throws NarrativeMethodStoreException {
                return file.getName();
            }
        });
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public FileId saveFile(String moduleName, FileProvider file) 
            throws NarrativeMethodStoreException {
        if (isReadOnly)
            throwChangeOperation();
        String fileName = file.getName();
        long length = file.length();
        InputStream is = file.openStream();
        String md5 = MongoUtils.getMD5(is);
        try {
            is.close();
        } catch (IOException ex) {
            throw new NarrativeMethodStoreException(ex);
        }
        MongoCollection files = jdb.getCollection(TABLE_REPO_FILES);
        Iterator<Map> it = files.find(String.format("{%s:#,%s:#,%s:#,%s:#}", 
                FIELD_RF_MODULE_NAME, FIELD_RF_FILE_NAME, FIELD_RF_LENGTH, FIELD_RF_MD5), 
                moduleName, fileName, length, md5).as(Map.class).iterator();
        while (it.hasNext()) {
            Map<String, Object> obj = it.next();
            is = file.openStream();
            try {
                OutputComparatorStream ocs = new OutputComparatorStream(is);
                loadFile(obj, ocs);
                if (!ocs.isDifferent()) {
                    String fileId = (String)obj.get(FIELD_RF_FILE_ID);
                    //System.out.println("File was found: " + fileName + ", " + length + ", " + md5 + " -> " + fileId);
                    return new FileId(fileId);
                }
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    throw new NarrativeMethodStoreException(ex);
                }
            }
        }
        // We couldn't find file which is equal to save request. 
        // Let's store original copy of it.
        String hexData = null;
        String shockNodeId = null;
        is = file.openStream();
        try {
            if (length <= MAX_MONGO_FILE_LENGTH || shockUrl == null) {
                hexData = MongoUtils.streamToHex(is);
            } else {
                try {
                    BasicShockClient cl = new BasicShockClient(shockUrl, serviceToken);
                    shockNodeId = cl.addNode(is, fileName, "JSON").getId().getId();
                } catch (Exception ex) {
                    throw new NarrativeMethodStoreException(ex);
                }
            }
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                throw new NarrativeMethodStoreException(ex);
            }
        }
        long fileIdNum = System.currentTimeMillis();
        while (true) {
            try {
                files.insert(String.format("{%s:#}", FIELD_RF_FILE_ID), "" + fileIdNum);
                break;
            } catch (DuplicateKey ex) {
                fileIdNum++;
            }
        }
        String fileId = String.valueOf(fileIdNum);
        files.update(String.format("{%s:#}", FIELD_RF_FILE_ID), 
                fileId).with(String.format("{%s:#,%s:#,%s:#,%s:#,%s:#,%s:#,%s:#}", 
                        FIELD_RF_FILE_ID, FIELD_RF_MODULE_NAME, FIELD_RF_FILE_NAME, 
                        FIELD_RF_LENGTH, FIELD_RF_MD5, FIELD_RF_HEX_DATA, 
                        FIELD_RF_SHOCK_NODE_ID), fileId, moduleName, fileName, length, 
                        md5, hexData, shockNodeId);
        //System.out.println("File was saved: " + fileName + ", " + length + ", " + md5 + " -> " + fileId);
        return new FileId(fileId);
    }
    
    private Map<String, Object> getFileObject(FileId fileId) 
            throws NarrativeMethodStoreException {
        @SuppressWarnings("unchecked")
        Map<String, Object> obj = jdb.getCollection(TABLE_REPO_FILES)
                .findOne(String.format("{%s:#}", 
                FIELD_RF_FILE_ID), fileId.getId()).as(Map.class);
        if (obj == null)
            throw new NarrativeMethodStoreException("File with id=" + fileId.getId() + 
                    "is not found");
        return obj;
    }
    
    @Override
    public FilePointer loadFile(FileId fileId)
            throws NarrativeMethodStoreException {
        Map<String, Object> obj = getFileObject(fileId);
        String fileName = (String)obj.get(FIELD_RF_FILE_NAME);
        long length = (Long)obj.get(FIELD_RF_LENGTH);
        return new DbFilePointer(fileId, fileName, length);
    }

    private void loadFile(FileId fileId, OutputStream target) 
            throws NarrativeMethodStoreException {
        loadFile(getFileObject(fileId), target);
    }
    
    private void loadFile(Map<String, Object> obj, OutputStream target) 
            throws NarrativeMethodStoreException {
        try {
            String hexData = (String)obj.get(FIELD_RF_HEX_DATA);
            if (hexData == null) {
                String shockNodeId = (String)obj.get(FIELD_RF_SHOCK_NODE_ID);
                BasicShockClient cl = new BasicShockClient(shockUrl, serviceToken);
                cl.getFile(new ShockNodeId(shockNodeId), target);
            } else {
                target.write(MongoUtils.hexToBytes(hexData));
            }
        } catch (IOException ex) {
            throw new NarrativeMethodStoreException(ex);
        } catch (InvalidShockUrlException ex) {
            throw new NarrativeMethodStoreException(ex);
        } catch (TokenExpiredException ex) {
            throw new NarrativeMethodStoreException(ex);
        } catch (ShockHttpException ex) {
            throw new NarrativeMethodStoreException(ex);
        } catch (IllegalArgumentException ex) {
            throw new NarrativeMethodStoreException(ex);
        }
    }
    
    public class DbFilePointer implements FilePointer {
        private final FileId fileId;
        private final String fileName;
        private final long length;
        
        public DbFilePointer(FileId fileId, String fileName, long length) {
            this.fileId = fileId;
            this.fileName = fileName;
            this.length = length;
        }
        
        @Override
        public FileId getFileId() {
            return fileId;
        }
        
        @Override
        public File getFile() {
            return null;
        }
        
        @Override
        public String getName() {
            return fileName;
        }
        
        @Override
        public long length() {
            return length;
        }
        
        @Override
        public void saveToStream(OutputStream os)
                throws NarrativeMethodStoreException {
            loadFile(fileId, os);
        }
    }
}
