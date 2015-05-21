package us.kbase.narrativemethodstore.db.mongo.test;

import java.net.URL;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import us.kbase.common.mongo.GetMongoDB;
import us.kbase.narrativemethodstore.db.github.GitHubRepoProvider;
import us.kbase.narrativemethodstore.db.mongo.MongoDynamicRepoDB;

public class MongoDynamicRepoDBTest extends MongoDBTester {
    private static final String dbName = "test_repo_registry_mongo";

    static {
        testName = "registry";
    }
    
    @Test
    public void mainTest() throws Exception {
        String host = "localhost:" + mongoPort;
        MongoDynamicRepoDB db = new MongoDynamicRepoDB(host, dbName);
        Assert.assertEquals(0, db.listRepoModuleNames().size());
        GitHubRepoProvider pvd = new GitHubRepoProvider(
                new URL("https://github.com/kbaseIncubator/genome_feature_comparator"), workDir);
        db.registerRepo("u1", pvd);
        Assert.assertEquals("[]", db.listRepoModuleNames().toString());
    }
    
    @Before 
    @After
    public void cleanup() throws Exception {
        String host = "localhost:" + mongoPort;
        GetMongoDB.getDB(host, dbName, 0, 10).dropDatabase();
    }
}
