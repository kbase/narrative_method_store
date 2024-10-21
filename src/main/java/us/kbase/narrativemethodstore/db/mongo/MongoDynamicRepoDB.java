package us.kbase.narrativemethodstore.db.mongo;

import static us.kbase.narrativemethodstore.db.mongo.MongoUtils.toDocument;
import static us.kbase.narrativemethodstore.db.mongo.MongoUtils.toMap;
import static us.kbase.narrativemethodstore.db.mongo.MongoUtils.toObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.mongodb.MongoClientSettings;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import org.bson.Document;

import us.kbase.narrativemethodstore.db.DynamicRepoDB;
import us.kbase.narrativemethodstore.db.FileId;
import us.kbase.narrativemethodstore.db.FilePointer;
import us.kbase.narrativemethodstore.db.JsonRepoProvider;
import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.db.JsonRepoProvider.RepoData;
import us.kbase.narrativemethodstore.db.github.RepoTag;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class MongoDynamicRepoDB implements DynamicRepoDB {
    private final MongoDatabase db;
    private final Set<String> globalAdmins;
    private final boolean isReadOnly;
    ////////////////////////////////////////////////////////////////////
    private static final String TABLE_REPO_INFO = "repo_info";
    private static final String FIELD_RI_MODULE_NAME = "module_name";
    private static final String FIELD_RI_DOCKER_IMAGE = "docker_image";
    private static final String FIELD_RI_LAST_VERSION = "last_version";
    private static final String FIELD_RI_LAST_BETA_VERSION = "last_beta_version";
    private static final String FIELD_RI_LAST_RELEASE_VERSION = "last_release_version";
    private static final String FIELD_RI_STATE = "state";
    ////////////////////////////////////////////////////////////////////
    private static final String TABLE_REPO_HISTORY = "repo_history";
    private static final String FIELD_RH_MODULE_NAME = "module_name";
    private static final String FIELD_RH_VERSION = "version";
    private static final String FIELD_RH_REPO_DATA = "repo_data";
    private static final String FIELD_RH_IS_BETA = "is_beta";
    private static final String FIELD_RH_IS_RELEASE = "is_release";
    ////////////////////////////////////////////////////////////////////
    private static final String TABLE_REPO_FILES = "repo_files";
    private static final String FIELD_RF_FILE_ID = "file_id";
    private static final String FIELD_RF_MODULE_NAME = "module_name";
    private static final String FIELD_RF_FILE_NAME = "file_name";
    private static final String FIELD_RF_LENGTH = "length";
    private static final String FIELD_RF_MD5 = "md5";
    private static final String FIELD_RF_HEX_DATA = "hex_data";
    private static final String FIELD_RF_SHOCK_NODE_ID = "shock_node_id";

    public MongoDynamicRepoDB(
            final String host,
            final String database,
            final String dbUser,
            final String dbPwd,
            final List<String> globalAdminUserIds,
            final boolean isReadOnly)
            throws NarrativeMethodStoreException {
        this.isReadOnly = isReadOnly;
        try {
            db = getDB(host, database, dbUser, dbPwd);
            if (!isReadOnly)
                ensureIndeces();
            globalAdmins = new HashSet<String>(globalAdminUserIds);
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException(ex);
        }
    }
    
    private MongoDatabase getDB(final String host, final String db, final String user, final String pwd) {
        final MongoClientSettings.Builder mongoBuilder = MongoClientSettings.builder().applyToClusterSettings(
                builder -> builder.hosts(Arrays.asList(new ServerAddress(host))));
        final MongoClient cli;
        if (user != null) {
            final MongoCredential creds = MongoCredential.createCredential(user, db, pwd.toCharArray());
            // unclear if and when it's safe to clear the password
            cli = MongoClients.create(mongoBuilder.credential(creds).build());
        } else {
            cli = MongoClients.create(mongoBuilder.build());
        }
        return cli.getDatabase(db);
    }
    
    private void ensureIndeces() {
        final IndexOptions uniq = new IndexOptions().unique(true);

        // Collection for repo data
        final MongoCollection<Document> repoData = db.getCollection(TABLE_REPO_INFO);
        repoData.createIndex(Indexes.ascending(FIELD_RI_MODULE_NAME), uniq);
        repoData.createIndex(Indexes.ascending(FIELD_RI_DOCKER_IMAGE));

        // Collection for repo history
        final MongoCollection<Document> repoHist = db.getCollection(TABLE_REPO_HISTORY);
        repoHist.createIndex(Indexes.ascending(FIELD_RH_MODULE_NAME, FIELD_RH_VERSION), uniq);
        repoHist.createIndex(Indexes.ascending(FIELD_RH_MODULE_NAME, FIELD_RH_REPO_DATA + ".gitCommitHash"));

        // Collection for repo files
        final MongoCollection<Document> repoFiles = db.getCollection(TABLE_REPO_FILES);
        repoFiles.createIndex(Indexes.ascending(FIELD_RF_FILE_ID), uniq);
        repoFiles.createIndex(Indexes.ascending(FIELD_RF_MODULE_NAME, FIELD_RF_FILE_NAME, FIELD_RF_LENGTH, FIELD_RF_MD5));
    }
    
    @Override
    public boolean isRepoRegistered(String repoModuleName, boolean withDisabled)
            throws NarrativeMethodStoreException {
        List<String> dis = MongoUtils.getProjection(db.getCollection(TABLE_REPO_INFO),
                new Document(FIELD_RI_MODULE_NAME, repoModuleName),
                FIELD_RI_STATE, String.class);
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
        final RepoData repoData = JsonRepoProvider.repoProviderToData(this, repoDetails);
        final MongoCollection<Document> hist = db.getCollection(TABLE_REPO_HISTORY);
        hist.insertOne(new Document()
                .append(FIELD_RH_MODULE_NAME, repoModuleName)
                .append(FIELD_RH_VERSION, newVersion)
                .append(FIELD_RH_REPO_DATA, repoData));
        
        final MongoCollection<Document> data = db.getCollection(TABLE_REPO_INFO);
        //should just do an upsert rather than handling the logic application side
        if (wasReg) {
            Document existingInfo = data.find(Filters.eq(FIELD_RI_MODULE_NAME, repoModuleName)).first();

            if (existingInfo != null) {
                existingInfo.put(FIELD_RI_LAST_VERSION, newVersion);
                existingInfo.put(FIELD_RI_STATE, RepoState.ready.toString());

                // update the existing document
                data.replaceOne(Filters.eq(FIELD_RI_MODULE_NAME, repoModuleName), existingInfo);
            }
        } else {
            data.insertOne(new Document()
                    .append(FIELD_RI_MODULE_NAME, repoModuleName)
                    .append(FIELD_RI_LAST_VERSION, newVersion)
                    .append(FIELD_RI_STATE, RepoState.ready.toString()));
        }
    }
    
    /*private boolean isDockerImageInUse(String dockerImage) 
            throws NarrativeMethodStoreException {
        List<String> ret = MongoUtils.getProjection(jdb.getCollection(TABLE_REPO_INFO),
                String.format("{%s:#}", FIELD_RI_DOCKER_IMAGE), 
                FIELD_RI_MODULE_NAME, String.class, dockerImage);
        return !ret.isEmpty();
    }*/
    
    private long getRepoLastVersion(String repoModuleName)
            throws NarrativeMethodStoreException {
        List<Long> vers = MongoUtils.getProjection(db.getCollection(TABLE_REPO_INFO),
                new Document(FIELD_RI_MODULE_NAME, repoModuleName),
                FIELD_RI_LAST_VERSION, Long.class);
        checkRepoRegistered(repoModuleName, vers);
        return vers.get(0);
    }

    @Override
    public Long getRepoLastVersion(String repoModuleName, RepoTag tag)
            throws NarrativeMethodStoreException {
        if (tag == null || tag.equals(RepoTag.dev))
            return getRepoLastVersion(repoModuleName);
        List<Long> vers;
        if (tag.isGitCommitHash()) {
            vers = listRepoVersions(repoModuleName, tag);
        } else {
            String versionField = null;
            if (tag.equals(RepoTag.beta)) {
                versionField = FIELD_RI_LAST_BETA_VERSION;
            } else if (tag.equals(RepoTag.release)) {
                versionField = FIELD_RI_LAST_RELEASE_VERSION;
            } else {
                // this is impossible based on the current RepoTag class
                throw new NarrativeMethodStoreException("Unsupported tag: " + tag);
            }
            vers = MongoUtils.getProjection(db.getCollection(TABLE_REPO_INFO),
                    new Document(FIELD_RI_MODULE_NAME, repoModuleName),
                    versionField, Long.class);
        }
        checkRepoRegistered(repoModuleName, vers);
        return Collections.max(vers);
    }

    @Override
    public List<String> listRepoModuleNames() throws NarrativeMethodStoreException {
        // in commit 65f21e2 this method could filter based on the repo tag and disabled state
        // however, the tag lookup was broken and would always return no values for beta or
        // release
        // The method was only used in one place which didn't supply a tag and filtered out
        // disabled repos
        // As such, that functionality has been removed and the method simplified for now

        List<String> ret = new ArrayList<String>();

        FindIterable<Document> docs = db.getCollection(TABLE_REPO_INFO).find()
                .projection(Projections.include(FIELD_RI_MODULE_NAME, FIELD_RI_STATE));

        for (Document doc : docs) {
            if (RepoState.valueOf(doc.getString(FIELD_RI_STATE)) != RepoState.disabled) {
                ret.add(doc.getString(FIELD_RI_MODULE_NAME));
            }
        }
        return ret;
    }
    
    @Override
    public RepoProvider getRepoDetails(String repoModuleName, RepoTag tag)
            throws NarrativeMethodStoreException {
        Long version = getRepoLastVersion(repoModuleName, tag);
        if (version == null)
            return null;
        return getRepoDetailsHistory(repoModuleName, version);
    }
    
    @Override
    public List<Long> listRepoVersions(String repoModuleName, RepoTag tag)
            throws NarrativeMethodStoreException {
        checkRepoRegistered(repoModuleName);
        List<Long> ret;
        if (tag != null && tag.isGitCommitHash()) {
            ret = MongoUtils.getProjection(db.getCollection(TABLE_REPO_HISTORY),
                    new Document(FIELD_RI_MODULE_NAME, repoModuleName)
                            .append(FIELD_RH_REPO_DATA + ".gitCommitHash", tag.toString()),
                    FIELD_RH_VERSION, Long.class);
        } else {
            final Document whereCondition = new Document(FIELD_RH_MODULE_NAME,
                    repoModuleName);
            if (tag == null || tag.equals(RepoTag.dev)) {
                // do nothing
            } else if (tag.equals(RepoTag.beta) || tag.equals(RepoTag.release)) {
                whereCondition.append(
                        tag.equals(RepoTag.beta) ? FIELD_RH_IS_BETA : FIELD_RH_IS_RELEASE, 1);
            } else {
                // this is impossible based on the current RepoTag code
                throw new NarrativeMethodStoreException("Unsupported tag: " + tag);
            }
            ret = MongoUtils.getProjection(db.getCollection(TABLE_REPO_HISTORY), whereCondition,
                    FIELD_RH_VERSION, Long.class);
        }
        return ret;
    }

    @Override
    public RepoProvider getRepoDetailsHistory(String repoModuleName,
            long version) throws NarrativeMethodStoreException {
        List<RepoData> ret = MongoUtils.getProjection(db.getCollection(TABLE_REPO_HISTORY),
                new Document(FIELD_RH_MODULE_NAME, repoModuleName)
                        .append(FIELD_RH_VERSION, version),
                FIELD_RH_REPO_DATA, RepoData.class);
        checkRepoRegistered(repoModuleName, ret);
        return new JsonRepoProvider(this, ret.get(0));
    }

    @Override
    public void pushRepoToTag(String repoModuleName, RepoTag tag, String userId)
            throws NarrativeMethodStoreException {
        checkRepoRegistered(repoModuleName);
        checkAdmin(userId);
        if (tag == null || tag.equals(RepoTag.dev))
            return;
        final MongoCollection<Document> data = db.getCollection(TABLE_REPO_INFO);
        Document info = data.find(Filters.eq(FIELD_RI_MODULE_NAME, repoModuleName)).first();

        if (info == null) {
            throw new NarrativeMethodStoreException("repository not found: " + repoModuleName);
        }

        long version = info.getLong(FIELD_RI_LAST_VERSION);
        Long betaVer = info.getLong(FIELD_RI_LAST_BETA_VERSION);
        Long releaseVer = info.getLong(FIELD_RI_LAST_RELEASE_VERSION);
        long changedVer;

        if (tag.equals(RepoTag.beta)) {
            betaVer = version;
            info.put(FIELD_RI_LAST_BETA_VERSION, betaVer);
            changedVer = version;
        } else if (tag.equals(RepoTag.release)) {
            if (betaVer == null)
                throw new NarrativeMethodStoreException("Repository " + repoModuleName + 
                        " cannot be released cause it was never pushed to beta tag");
            releaseVer = betaVer;
            info.put(FIELD_RI_LAST_RELEASE_VERSION, releaseVer);
            changedVer = betaVer;
        } else {
            throw new NarrativeMethodStoreException("Unsupported tag: " + tag);
        }

        data.replaceOne(Filters.eq(FIELD_RI_MODULE_NAME, repoModuleName), info);

        final MongoCollection<Document> data2 = db.getCollection(TABLE_REPO_HISTORY);

        final RepoHistory hist = toObject(data2.find(Filters.and(
                Filters.eq(FIELD_RH_MODULE_NAME, repoModuleName),
                Filters.eq(FIELD_RH_VERSION, changedVer))).first(), RepoHistory.class);

        hist.repo_data.repackForMongoDB();
        if (tag.equals(RepoTag.beta)) {
            hist.is_beta = 1L;
        } else {
            hist.is_release = 1L;
        }

        data2.replaceOne(Filters.and(
                Filters.eq(FIELD_RH_MODULE_NAME, repoModuleName),
                Filters.eq(FIELD_RH_VERSION, changedVer)),
                toDocument(hist));
    }
    
    @Override
    public Set<String> listRepoOwners(String repoModuleName)
            throws NarrativeMethodStoreException {
        return new TreeSet<String>(getRepoDetails(repoModuleName, null).listOwners());
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

    /*private void checkRepoOwner(RepoProvider repo, String userId)
            throws NarrativeMethodStoreException {
        if (globalAdmins.contains(userId))
            return;
        if (!new TreeSet<String>(repo.listOwners()).contains(userId))
            throw new NarrativeMethodStoreException("User " + userId + 
                    " is not owner of repository " + repo.getModuleName());
    }*/

    /*private void checkRepoOwner(String repoModuleName, String userId)
            throws NarrativeMethodStoreException {
        if (!isRepoOwner(repoModuleName, userId))
            throw new NarrativeMethodStoreException("User " + userId + 
                    " is not owner of repository " + repoModuleName);
    }*/
    
    @Override
    public RepoState getRepoState(String repoModuleName)
            throws NarrativeMethodStoreException {
        List<String> state = MongoUtils.getProjection(db.getCollection(TABLE_REPO_INFO),
                new Document(FIELD_RI_MODULE_NAME, repoModuleName),
                FIELD_RI_STATE, String.class);
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
        final MongoCollection<Document> info = db.getCollection(TABLE_REPO_INFO);
        Document doc = info.find(Filters.eq(FIELD_RI_MODULE_NAME, repoModuleName)).first();
        doc.put(FIELD_RI_STATE, state.toString());
        info.replaceOne(Filters.eq(FIELD_RI_MODULE_NAME, repoModuleName), doc);
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

        final MongoCollection<Document> files = db.getCollection(TABLE_REPO_FILES);
        final Document existingFileDoc = files.find(Filters.and(
                Filters.eq(FIELD_RF_MODULE_NAME, moduleName),
                Filters.eq(FIELD_RF_FILE_NAME, fileName),
                Filters.eq(FIELD_RF_LENGTH, length),
                Filters.eq(FIELD_RF_MD5, md5)
        )).first();

        if (existingFileDoc != null) {
            is = file.openStream();
            try {
                OutputComparatorStream ocs = new OutputComparatorStream(is);
                loadFile(toMap(existingFileDoc), ocs);
                if (!ocs.isDifferent()) {
                    String fileId = existingFileDoc.getString(FIELD_RF_FILE_ID);
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
        String hexData;
        is = file.openStream();
        try {
            // note that mongo can take ~16MB of data this way tops or it'll throw an error
            // it's been running in production for years this way so I guess it works...?
            hexData = MongoUtils.streamToHex(is);
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
                Document newFileDoc = new Document(FIELD_RF_FILE_ID, String.valueOf(fileIdNum));
                files.insertOne(newFileDoc);
                break;
            } catch (DuplicateKeyException ex) {
                // there's not really any reasonable way to test this, since saving a file
                // takes >> 1ms in my tests. Would need multiple threads
                fileIdNum++;
            }
        }

        // if this fails, there will be an incomplete file record in the DB.
        // if no shock url is configured the file is saved in a normal mongo object. Since the
        // file is converted to hex, that means a file > ~8MB will throw a mongo error
        String fileId = String.valueOf(fileIdNum);
        Document fileDoc = new Document(FIELD_RF_FILE_ID, fileId)
                .append(FIELD_RF_MODULE_NAME, moduleName)
                .append(FIELD_RF_FILE_NAME, fileName)
                .append(FIELD_RF_LENGTH, length)
                .append(FIELD_RF_MD5, md5)
                .append(FIELD_RF_HEX_DATA, hexData)
                .append(FIELD_RF_SHOCK_NODE_ID, null);

        files.updateOne(Filters.eq(FIELD_RF_FILE_ID, fileId), new Document("$set", fileDoc));
        return new FileId(fileId);
    }
    
    private Map<String, Object> getFileObject(FileId fileId) 
            throws NarrativeMethodStoreException {
        MongoCollection<Document> filesCollection = db.getCollection(TABLE_REPO_FILES);
        Document doc =  filesCollection.find(Filters.eq(FIELD_RF_FILE_ID, fileId.getId())).first();
        if (doc == null)
            throw new NarrativeMethodStoreException("File with id=" + fileId.getId() + 
                    " is not found");
        return toMap(doc);
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
            target.write(MongoUtils.hexToBytes(hexData));
        } catch (IOException ex) {
            throw new NarrativeMethodStoreException(ex);
        }
    }
    
    private class DbFilePointer implements FilePointer {
        private final FileId fileId;
        private final String fileName;
        private final long length;
        
        private DbFilePointer(FileId fileId, String fileName, long length) {
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
    
    // it is actually used, just some of the fields aren't explicitly used. They're populated
    // by Jackson though.
    @SuppressWarnings("unused")
    private static class RepoHistory {
        String module_name;
        Long version;
        RepoData repo_data;
        Long is_beta;
        Long is_release;
    }
}
