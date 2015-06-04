package us.kbase.narrativemethodstore.db.mongo.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.DB;

import us.kbase.auth.AuthToken;
import us.kbase.common.mongo.GetMongoDB;
import us.kbase.narrativemethodstore.db.JsonRepoProvider;
import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.db.DynamicRepoDB.RepoState;
import us.kbase.narrativemethodstore.db.github.FileRepoProvider;
import us.kbase.narrativemethodstore.db.github.GitHubRepoProvider;
import us.kbase.narrativemethodstore.db.mongo.MongoDynamicRepoDB;
import us.kbase.narrativemethodstore.db.mongo.OutputComparatorStream;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;
import us.kbase.shock.client.BasicShockClient;
import us.kbase.shock.client.ShockNodeId;

public class MongoDynamicRepoDBTest {
    private static final String dbName = "test_repo_registry_mongo";
    private static final MongoDBHelper dbHelper = new MongoDBHelper("registry");

    private static URL shockUrl = null;
    private static AuthToken shockToken = null;

    @BeforeClass
    public static void beforeClass() throws Exception {
        dbHelper.startup(System.getProperty("mongod.path"));
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        if (dbHelper != null)
            dbHelper.shutdown();
    }
    
    @Test
    public void mainTest() throws Exception {
        testRepo(true);
        //testRepo(false);
    }
    
    private void testRepo(boolean localFiles) throws Exception {
        String gitUrl = "https://github.com/kbaseIncubator/genome_feature_comparator";
        String localPath = "test/data/test_repo_1";
        String repoModuleName = "GenomeFeatureComparator";
        String globalAdmin = "admin";
        String user1 = "rsutormin";
        String user2 = "user2";
        String unregModuleName = "Unregistered";
        
        String host = "localhost:" + dbHelper.getMongoPort();
        MongoDynamicRepoDB db = new MongoDynamicRepoDB(host, dbName, null, null, 
                Arrays.asList(globalAdmin), false, shockUrl, shockToken);
        Assert.assertEquals(0, db.listRepoModuleNames(false).size());
        RepoProvider pvd = localFiles ? new FileRepoProvider(new File(localPath)) :
            new GitHubRepoProvider(new URL(gitUrl), dbHelper.getWorkDir());
        db.registerRepo(user1, pvd);
        Assert.assertEquals("[" + repoModuleName + "]", 
                db.listRepoModuleNames(false).toString());
        Assert.assertTrue(db.isRepoOwner(repoModuleName, user1));
        Assert.assertEquals("[msneddon, " + user1 + "]", 
                db.listRepoOwners(repoModuleName).toString());
        long ver1 = db.getRepoLastVersion(repoModuleName);
        List<Long> verHist1 = db.listRepoVersions(repoModuleName);
        Assert.assertEquals(1, verHist1.size());
        Assert.assertEquals(ver1, (long)verHist1.get(0));
        Assert.assertEquals(RepoState.ready, db.getRepoState(repoModuleName));

        try {
            db.registerRepo(user2, pvd);
            Assert.fail("User " + user2 + " is not in owner list at this point");
        } catch (NarrativeMethodStoreException ex) {
            Assert.assertEquals("User " + user2 + " is not owner of repository " + 
                    repoModuleName, ex.getMessage());
        }
        db.setRepoState(user1, repoModuleName, RepoState.disabled);
        Assert.assertEquals(RepoState.disabled, db.getRepoState(repoModuleName));
        Assert.assertEquals(0, db.listRepoModuleNames(false).size());
        Assert.assertEquals(1, db.listRepoModuleNames(true).size());
        // Register second version
        db.registerRepo(globalAdmin, pvd);
        long ver2 = db.getRepoLastVersion(repoModuleName);
        List<Long> verHist2 = db.listRepoVersions(repoModuleName);
        Assert.assertEquals(2, verHist2.size());
        Assert.assertEquals(ver1, (long)verHist2.get(0));
        Assert.assertEquals(ver2, (long)verHist2.get(1));
        Assert.assertTrue("Versions " + ver1 + " and " + ver2 + " should be different", 
                ver1 != ver2);
        
        RepoProvider savedRP = db.getRepoDetails(repoModuleName);
        Assert.assertEquals(JsonRepoProvider.repoProviderToJsonString(db, pvd), 
                JsonRepoProvider.repoProviderToJsonString(db, savedRP));
        if (!localFiles) {
            Assert.assertFalse(savedRP.getGitCommitHash().contains("\n"));
            Assert.assertEquals(40, savedRP.getGitCommitHash().length());
        }
        
        try {
            db.setRepoState(user2, repoModuleName, RepoState.disabled);
            Assert.fail("User " + user2 + " is not in owner list at this point");
        } catch (NarrativeMethodStoreException ex) {
            Assert.assertEquals("User " + user2 + " is not owner of repository " + 
                    repoModuleName, ex.getMessage());
        }
        try {
            db.setRepoState(user1, repoModuleName, RepoState.testing);
            Assert.fail("User " + user1 + " is not global admin");
        } catch (NarrativeMethodStoreException ex) {
            Assert.assertEquals("User " + user1 + " is not global admin", ex.getMessage());
        }
        db.setRepoState(globalAdmin, repoModuleName, RepoState.testing);
        db.setRepoState(globalAdmin, repoModuleName, RepoState.disabled);
        Assert.assertEquals(0, db.listRepoModuleNames(false).size());
        Assert.assertEquals(1, db.listRepoModuleNames(true).size());

        Assert.assertFalse(db.isRepoRegistered(unregModuleName, true));
        try {
            db.getRepoDetails(unregModuleName);
            Assert.fail("Repository " + unregModuleName + " wasn't registered at this " +
            		"point");
        } catch (NarrativeMethodStoreException ex) {
            Assert.assertEquals("Repository " + unregModuleName + " wasn't registered", 
                    ex.getMessage());
        }
        
        String methodId = "compare_genome_features";
        Assert.assertTrue("Screenshots: " + pvd.listScreenshotIDs(methodId).size(), pvd.listScreenshotIDs(methodId).size() > 0);
        String imgId = pvd.listScreenshotIDs(methodId).get(0);
        File imgFile = pvd.getScreenshot(methodId, imgId).getFile();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStreams(new FileInputStream(imgFile), baos, 10000);
        byte[] imgData = baos.toByteArray();
        File imgFile2 = new File(imgFile.getAbsolutePath() + ".2");
        FileOutputStream fos = new FileOutputStream(imgFile2);
        fos.write(imgData, 0, imgData.length / 2);
        fos.close();
        imgData[100] = (byte)(imgData[100] + 1);
        File imgFile3 = new File(imgFile.getAbsolutePath() + ".3");
        fos = new FileOutputStream(imgFile3);
        fos.write(imgData);
        fos.close();
        int[] bufferSizes = {100, 1000, 10000, 100000};
        for (int bufferSize: bufferSizes) {
            Assert.assertTrue("Buffer size: " + bufferSize, diffFiles(imgFile, imgFile2, bufferSize));
            Assert.assertTrue("Buffer size: " + bufferSize, diffFiles(imgFile2, imgFile, bufferSize));
            Assert.assertTrue("Buffer size: " + bufferSize, diffFiles(imgFile, imgFile3, bufferSize));
            Assert.assertFalse("Buffer size: " + bufferSize, diffFiles(imgFile, imgFile, bufferSize));
        }
    }
    
    private static void copyStreams(InputStream is, OutputStream os, int bufferSize) throws Exception {
        byte[] buf = new byte[bufferSize];
        while (true) {
            int r = is.read(buf);
            if (r == -1)
                break;
            if (r == 0)
                continue;
            os.write(buf, 0, r);
        }
        is.close();
        os.close();
    }
    
    private static boolean diffFiles(File f1, File f2, int bufferSize) throws Exception {
        OutputComparatorStream ocs = new OutputComparatorStream(new FileInputStream(f1));
        FileInputStream fis = new FileInputStream(f2);
        copyStreams(fis, ocs, bufferSize);
        return ocs.isDifferent();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Before 
    @After
    public void cleanup() throws Exception {
        String host = "localhost:" + dbHelper.getMongoPort();
        DB db = GetMongoDB.getDB(host, dbName, 0, 10);
        if (shockUrl != null) {
            try {
                Jongo jdb = new Jongo(db);
                MongoCollection files = jdb.getCollection("repo_files");
                Iterator<Map> it = files.find("{}").as(Map.class).iterator();
                while (it.hasNext()) {
                    Map<String, Object> obj = it.next();
                    String fileName = (String)obj.get("file_name");
                    String shockNodeId = (String)obj.get("shock_node_id");
                    if (shockNodeId != null) {
                        System.out.println("Deleting shock node for " + fileName);
                        BasicShockClient cl = new BasicShockClient(shockUrl, shockToken);
                        cl.deleteNode(new ShockNodeId(shockNodeId));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        db.dropDatabase();
    }
}
