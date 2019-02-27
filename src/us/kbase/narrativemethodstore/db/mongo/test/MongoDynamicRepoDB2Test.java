package us.kbase.narrativemethodstore.db.mongo.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;

import us.kbase.narrativemethodstore.db.DynamicRepoDB.FileProvider;
import us.kbase.narrativemethodstore.db.DynamicRepoDB.RepoState;
import us.kbase.narrativemethodstore.db.FileId;
import us.kbase.narrativemethodstore.db.JsonRepoProvider;
import us.kbase.narrativemethodstore.db.JsonRepoProvider.RepoData;
import us.kbase.narrativemethodstore.db.github.RepoTag;
import us.kbase.narrativemethodstore.db.mongo.MongoDynamicRepoDB;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

/* Soooo there's already a test class for MongoDynamicRepoDB called, unsurprisingly, 
 * MongoDynamicRepoDBTest. For an unknown reason that test is @Ignored. I don't want to mess with
 * it so I'm making another test class where I can safely add my tests.
 * 
 * The purpose of writing these tests is to cover stuff that will need to be changed as part
 * of the MongoDB 3.x upgrade that is not currently covered by FullServerTest.java. As such,
 * the tests here do not necessarily cover all of the class.
 */

public class MongoDynamicRepoDB2Test {

    private static final MongoDBHelper MONGO = new MongoDBHelper(
            MongoDynamicRepoDB2Test.class.getSimpleName());
    private final static String DB_NAME = "test_" + MongoDynamicRepoDB2Test.class.getSimpleName();
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        // the key is not actually settable via make or ant, but it's the same as FullServerTest
        // *shrug*
        MONGO.startup(System.getProperty("test.mongo-exe-path"));
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        if (MONGO != null) {
            MONGO.shutdown(true);
        }
    }
    
    @Before
    public void before() throws Exception {
        final MongoClient mc = new MongoClient("localhost:" + MONGO.getMongoPort());
        final DB db = mc.getDB(DB_NAME);
        for (final String name: db.getCollectionNames()) {
            if (!name.startsWith("system.")) {
                // dropping collection also drops indexes
                db.getCollection(name).remove(new BasicDBObject());
            }
        }
    }
    
    @Test
    public void isRepoRegistered() throws Exception {
        final MongoDynamicRepoDB db = getDB(Arrays.asList("nmsadmin"));
        registerRepo(db, "someModule");
        
        assertThat("incorrect is reg", db.isRepoRegistered("someModule", false), is(true));
        assertThat("incorrect is reg", db.isRepoRegistered("someModule2", false), is(false));
        assertThat("incorrect is reg", db.isRepoRegistered("someModule2", true), is(false));
        
        // check disabling the module shows it as unregistered
        // which seems dangerous - can this cause name collisions?
        db.setRepoState("nmsadmin", "someModule", RepoState.disabled);
        
        assertThat("incorrect is reg", db.isRepoRegistered("someModule", false), is(false));
        assertThat("incorrect is reg", db.isRepoRegistered("someModule", true), is(true));
    }
    

    @Test
    public void getRepoLastVersion() throws Exception {
        final MongoDynamicRepoDB db = getDB(Arrays.asList("nmsadmin"));
        registerRepo(db, "someModule");
        
        final Long ver = db.getRepoLastVersion("someModule", null);
        assertCloseToNow(ver);
        assertThat("incorrect dev version", db.getRepoLastVersion("someModule", RepoTag.dev),
                is(ver));
        
        getRepoLastVersionFail(db, "someModule2", null, new NarrativeMethodStoreException(
                "Repository someModule2 wasn't registered"));
        getRepoLastVersionFail(db, "someModule2", RepoTag.dev, new NarrativeMethodStoreException(
                "Repository someModule2 wasn't registered"));
        getRepoLastVersionFail(db, "someModule2", RepoTag.beta, new NarrativeMethodStoreException(
                "Repository someModule2 wasn't registered"));
        
        assertThat("incorrect beta ver", db.getRepoLastVersion("someModule", RepoTag.beta),
                nullValue());
        
        db.pushRepoToTag("someModule", RepoTag.beta, "nmsadmin");
        
        assertThat("incorrect beta ver", db.getRepoLastVersion("someModule", RepoTag.beta),
                is(ver));
        
        Thread.sleep(2); // DB uses millisecond timestamp as the version...
        registerRepo(db, "someModule");
        final Long ver2 = db.getRepoLastVersion("someModule", null);
        assertCloseToNow(ver2);
        assertThat("incorrect dev version", db.getRepoLastVersion("someModule", RepoTag.dev),
                is(ver2));
        assertThat("incorrect beta ver", db.getRepoLastVersion("someModule", RepoTag.beta),
                is(ver));
        
        db.pushRepoToTag("someModule", RepoTag.beta, "nmsadmin");
        assertThat("incorrect beta ver", db.getRepoLastVersion("someModule", RepoTag.beta),
                is(ver2));
    }
    
    private void getRepoLastVersionFail(
            final MongoDynamicRepoDB db,
            final String moduleName,
            final RepoTag tag,
            final Exception expected) {
        try {
            db.getRepoLastVersion(moduleName, tag);
            fail("expected exception");
        } catch (Exception got) {
            assertExceptionCorrect(got, expected);
        }
    }
    
    @Test
    public void listRepoVersions() throws Exception {
        final MongoDynamicRepoDB db = getDB(Arrays.asList("nmsadmin"));
        final String h1 = "aaaaaaaaaabbbbbbbbbbccccccccccdddddddddd";
        final String h2 = "aaaaaaaaaabbbbbbbbbbccccccccccddddddddde";
        
        registerRepo(db, "someModule", h1);
        final Long ver = db.getRepoLastVersion("someModule", null);
        
        db.pushRepoToTag("someModule", RepoTag.beta, "nmsadmin");
        db.pushRepoToTag("someModule", RepoTag.release, "nmsadmin");
        Thread.sleep(2); // version is based on ms timestamp
        
        registerRepo(db, "someModule", h1);
        final Long ver2 = db.getRepoLastVersion("someModule", null);
        db.pushRepoToTag("someModule", RepoTag.beta, "nmsadmin");
        Thread.sleep(2); // version is based on ms timestamp
        
        registerRepo(db, "someModule", h2);
        final Long ver3 = db.getRepoLastVersion("someModule", null);
        
        registerRepo(db, "someModule2");
        final Long otherver = db.getRepoLastVersion("someModule2", null);
        
        assertThat("incorrect versions", db.listRepoVersions("someModule", null),
                is(Arrays.asList(ver, ver2, ver3)));
        // not sorted by version 
        assertThat("incorrect versions",
                new HashSet<>(db.listRepoVersions("someModule", RepoTag.valueOf(h1))),
                is(set(ver, ver2)));
        assertThat("incorrect versions", db.listRepoVersions("someModule", RepoTag.valueOf(h2)),
                is(Arrays.asList(ver3)));
        assertThat("incorrect versions", db.listRepoVersions("someModule", RepoTag.dev),
                is(Arrays.asList(ver, ver2, ver3)));
        assertThat("incorrect versions", db.listRepoVersions("someModule", RepoTag.beta),
                is(Arrays.asList(ver, ver2)));
        assertThat("incorrect versions", db.listRepoVersions("someModule", RepoTag.release),
                is(Arrays.asList(ver)));
        assertThat("incorrect versions", db.listRepoVersions("someModule2", null),
                is(Arrays.asList(otherver)));
        assertThat("incorrect versions", db.listRepoVersions("someModule2", RepoTag.beta),
                is(Collections.emptyList()));
        
        listRepoVersionsFail(db, "someModule3", null, new NarrativeMethodStoreException(
                "Repository someModule3 wasn't registered"));
    }
    
    private void listRepoVersionsFail(
            final MongoDynamicRepoDB db,
            final String modName,
            final RepoTag tag,
            final Exception expected) {
        try {
            db.listRepoVersions(modName, tag);
            fail("expected exception");
        } catch (Exception got) {
            assertExceptionCorrect(got, expected);
        }
    }
    
    @Test
    public void loadFileFailNoID() throws Exception {
        final MongoDynamicRepoDB db = getDB(Arrays.asList("nmsadmin"));
        final FileProvider fp = mock(FileProvider.class);
        when(fp.getName()).thenReturn("filename2");
        when(fp.length()).thenReturn(67L);
        // returned data twice since called twice, owise data is exhausted for first call
        when(fp.openStream()).thenReturn(getBAIS("contents"), getBAIS("contents"));
        final FileId fid = db.saveFile("somerepo", fp);
        final String newid = (Long.parseLong(fid.getId()) + 1) + "";
        
        try {
            db.loadFile(new FileId(newid));
            fail("expected exception");
        } catch (Exception got) {
            assertExceptionCorrect(got, new NarrativeMethodStoreException(String.format(
                    "File with id=%s is not found", newid)));
        }
    }

    private void registerRepo(
            final MongoDynamicRepoDB db,
            final String repoName)
            throws Exception {
        registerRepo(db, repoName, null);
    }
    
    private void registerRepo(
            final MongoDynamicRepoDB db,
            final String repoName,
            final String gitHash)
            throws Exception {
        // we save the files ahead of time otherwise we have to use real files to register a repo
        final FileProvider fp = mock(FileProvider.class);
        when(fp.getName()).thenReturn("filename2");
        when(fp.length()).thenReturn(67L);
        // returned data twice since called twice, owise data is exhausted for first call
        when(fp.openStream()).thenReturn(getBAIS("contents"), getBAIS("contents"));
        final FileId fid = db.saveFile(repoName, fp);

        // ugh. You have to read the db save method to see which fields are required and their
        // expected contents
        // needs a builder
        final RepoData repoData = new RepoData();
        repoData.moduleName = repoName;
        repoData.readmeFile = fid.getId();
        repoData.owners = Arrays.asList("o1", "o2");
        repoData.uiNarrativeMethodIds = Collections.emptyList();
        repoData.gitCommitHash = gitHash;
        
        // so regular users can't register a repo? Or maybe it has to go thru catalog?
        db.registerRepo("nmsadmin", new JsonRepoProvider(db, repoData));
    }

    private ByteArrayInputStream getBAIS(final String contents) {
        return new ByteArrayInputStream(contents.getBytes());
    }
    
    public static void assertExceptionCorrect(
            final Throwable got,
            final Throwable expected) {
        assertThat("incorrect exception. trace:\n" +
                ExceptionUtils.getStackTrace(got),
                got.getLocalizedMessage(),
                is(expected.getLocalizedMessage()));
        assertThat("incorrect exception type", got, instanceOf(expected.getClass()));
    }
    
    public static void assertCloseToNow(final long epochMillis) {
        final long now = Instant.now().toEpochMilli();
        assertThat(String.format("time (%s) not within 10000ms of now: %s", epochMillis, now),
                Math.abs(epochMillis - now) < 10000, is(true));
    }
    
    @SafeVarargs
    public static <T> Set<T> set(T... objects) {
        return new HashSet<T>(Arrays.asList(objects));
    }

    private MongoDynamicRepoDB getDB(final List<String> admins)
            throws NarrativeMethodStoreException {
        return new MongoDynamicRepoDB(
                "localhost:" + MONGO.getMongoPort(),
                DB_NAME,
                null,
                null,
                admins,
                false,
                null,
                null);
    }
    
}
