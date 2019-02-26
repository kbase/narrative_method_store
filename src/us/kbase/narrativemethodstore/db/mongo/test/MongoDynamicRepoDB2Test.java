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
import java.util.List;

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
 * of the MongoDB 3.x upgrade that is not currently covered by FullServerTest.java.
 */

public class MongoDynamicRepoDB2Test {

    private static final MongoDBHelper MONGO = new MongoDBHelper(
            MongoDynamicRepoDB2Test.class.getSimpleName());
    
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
        final DB db = mc.getDB(MongoDynamicRepoDB2Test.class.getSimpleName());
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
        assertCloseToNow(db.getRepoLastVersion("someModule", RepoTag.dev));
        
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

    private void registerRepo(final MongoDynamicRepoDB db, final String repoName)
            throws Exception {
        // we save the files ahead of time otherwise we have to use real files to register a repo
        final FileProvider fp = mock(FileProvider.class);
        when(fp.getName()).thenReturn("filename2");
        when(fp.length()).thenReturn(67L);
        when(fp.openStream()).thenReturn(new ByteArrayInputStream("contents".getBytes()));
        final FileId fid = db.saveFile(repoName, fp);

        // ugh. You have to read the db save method to see which fields are required and their
        // expected contents
        // needs a builder
        final RepoData repoData = new RepoData();
        repoData.moduleName = repoName;
        repoData.readmeFile = fid.getId();
        repoData.owners = Arrays.asList("o1", "o2");
        repoData.uiNarrativeMethodIds = Collections.emptyList();
        
        // so regular users can't register a repo? Or maybe it has to go thru catalog?
        db.registerRepo("nmsadmin", new JsonRepoProvider(db, repoData));
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

    private MongoDynamicRepoDB getDB(final List<String> admins)
            throws NarrativeMethodStoreException {
        return new MongoDynamicRepoDB(
                "localhost:" + MONGO.getMongoPort(),
                "test_" + MongoDynamicRepoDB2Test.class.getSimpleName(),
                null,
                null,
                admins,
                false,
                null,
                null);
    }
    
}
