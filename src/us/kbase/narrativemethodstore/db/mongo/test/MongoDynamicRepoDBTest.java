package us.kbase.narrativemethodstore.db.mongo.test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import us.kbase.common.mongo.GetMongoDB;
import us.kbase.narrativemethodstore.db.JsonRepoProvider;
import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.db.github.GitHubRepoProvider;
import us.kbase.narrativemethodstore.db.mongo.MongoDynamicRepoDB;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class MongoDynamicRepoDBTest {
    private static final String dbName = "test_repo_registry_mongo";

    private static final MongoDBHelper dbHelper = new MongoDBHelper("registry");
    
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
        String repoModuleName = "GenomeFeatureComparator";
        String globalAdmin = "admin";
        String user1 = "user1";
        String user2 = "user2";
        String unregModuleName = "Unregistered";
        
        String host = "localhost:" + dbHelper.getMongoPort();
        MongoDynamicRepoDB db = new MongoDynamicRepoDB(host, dbName, null, null, Arrays.asList(globalAdmin));
        Assert.assertEquals(0, db.listRepoModuleNames().size());
        RepoProvider pvd = new GitHubRepoProvider(
                new URL("https://github.com/kbaseIncubator/genome_feature_comparator"), dbHelper.getWorkDir());
        db.registerRepo(user1, pvd);
        Assert.assertEquals("[" + repoModuleName + "]", db.listRepoModuleNames().toString());
        Assert.assertTrue(db.isRepoOwner(repoModuleName, user1));
        Assert.assertTrue(db.isRepoAdmin(repoModuleName, user1));
        Assert.assertEquals("[" + user1 + "]", db.listRepoOwners(repoModuleName).toString());
        long ver1 = db.getRepoLastVersion(repoModuleName);
        List<Long> verHist1 = db.listRepoVersions(repoModuleName);
        Assert.assertEquals(1, verHist1.size());
        Assert.assertEquals(ver1, (long)verHist1.get(0));

        try {
            db.registerRepo(user2, pvd);
            Assert.fail("User " + user2 + " is not in owner list at this point");
        } catch (NarrativeMethodStoreException ex) {
            Assert.assertEquals("User " + user2 + " is not owner of repository " + 
                    repoModuleName, ex.getMessage());
        }
        db.setRepoOwner(user1, repoModuleName, user2, false);
        Assert.assertTrue(db.isRepoOwner(repoModuleName, user2));
        Assert.assertFalse(db.isRepoAdmin(repoModuleName, user2));
        Assert.assertEquals("[" + user1 + ", " + user2 + "]", 
                db.listRepoOwners(repoModuleName).toString());        
        db.registerRepo(user2, pvd);
        long ver2 = db.getRepoLastVersion(repoModuleName);
        List<Long> verHist2 = db.listRepoVersions(repoModuleName);
        Assert.assertEquals(2, verHist2.size());
        Assert.assertEquals(ver1, (long)verHist2.get(0));
        Assert.assertEquals(ver2, (long)verHist2.get(1));
        Assert.assertTrue("Versions " + ver1 + " and " + ver2 + " should be different", 
                ver1 != ver2);
        
        try {
            db.setRepoOwner(user2, repoModuleName, "user3", false);
            Assert.fail("User " + user2 + " is not in admin list at this point");
        } catch (NarrativeMethodStoreException ex) {
            Assert.assertEquals("User " + user2 + " is not admin of repository " + 
                    repoModuleName, ex.getMessage());
        }
        db.removeRepoOwner(globalAdmin, repoModuleName, user2);
        Assert.assertEquals("[" + user1 + "]", db.listRepoOwners(repoModuleName).toString());
        
        RepoProvider savedRP = db.getRepoDetails(repoModuleName);
        Assert.assertEquals(JsonRepoProvider.repoProviderToJsonString(pvd), 
                JsonRepoProvider.repoProviderToJsonString(savedRP));
        Assert.assertFalse(savedRP.getGitCommitHash().contains("\n"));
        Assert.assertEquals(40, savedRP.getGitCommitHash().length());
        
        Assert.assertFalse(db.isRepoRegistered(unregModuleName));
        try {
            db.getRepoDetails(unregModuleName);
            Assert.fail("Repository " + unregModuleName + " wasn't registered at this point");
        } catch (NarrativeMethodStoreException ex) {
            Assert.assertEquals("Repository " + unregModuleName + " wasn't registered", 
                    ex.getMessage());
        }
    }
    
    @Before 
    @After
    public void cleanup() throws Exception {
        String host = "localhost:" + dbHelper.getMongoPort();
        GetMongoDB.getDB(host, dbName, 0, 10).dropDatabase();
    }
}
