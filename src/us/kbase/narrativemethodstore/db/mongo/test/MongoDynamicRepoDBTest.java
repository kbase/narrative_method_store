package us.kbase.narrativemethodstore.db.mongo.test;

import static us.kbase.narrativemethodstore.db.mongo.MongoUtils.toMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import us.kbase.auth.AuthToken;
import us.kbase.common.service.UObject;
import us.kbase.narrativemethodstore.MethodBriefInfo;
import us.kbase.narrativemethodstore.MethodParameter;
import us.kbase.narrativemethodstore.MethodSpec;
import us.kbase.narrativemethodstore.TextOptions;
import us.kbase.narrativemethodstore.db.FileLookup;
import us.kbase.narrativemethodstore.db.FilePointer;
import us.kbase.narrativemethodstore.db.JsonRepoProvider;
import us.kbase.narrativemethodstore.db.NarrativeMethodData;
import us.kbase.narrativemethodstore.db.RepoProvider;
import us.kbase.narrativemethodstore.db.DynamicRepoDB.RepoState;
import us.kbase.narrativemethodstore.db.github.FileRepoProvider;
import us.kbase.narrativemethodstore.db.github.GitHubRepoProvider;
import us.kbase.narrativemethodstore.db.github.PySrvRepoPreparator;
import us.kbase.narrativemethodstore.db.github.YamlUtils;
import us.kbase.narrativemethodstore.db.mongo.MongoDynamicRepoDB;
import us.kbase.narrativemethodstore.db.mongo.OutputComparatorStream;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;
import us.kbase.narrativemethodstore.util.FileUtils;
import us.kbase.narrativemethodstore.util.TextUtils;
import us.kbase.shock.client.BasicShockClient;
import us.kbase.shock.client.ShockNodeId;

// It'd be nice to know why this is ignored
@Ignore
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
            dbHelper.shutdown(true);
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
        Assert.assertEquals(0, db.listRepoModuleNames().size());
        RepoProvider pvd = localFiles ? new FileRepoProvider(new File(localPath)) :
            new GitHubRepoProvider(new URL(gitUrl), null, dbHelper.getWorkDir());
        try {
            db.registerRepo(user1, pvd);
            Assert.assertEquals("[" + repoModuleName + "]", 
                    db.listRepoModuleNames().toString(), null);
            Assert.assertTrue(db.isRepoOwner(repoModuleName, user1));
            Assert.assertEquals("[msneddon, " + user1 + "]", 
                    db.listRepoOwners(repoModuleName).toString());
            long ver1 = db.getRepoLastVersion(repoModuleName, null);
            List<Long> verHist1 = db.listRepoVersions(repoModuleName, null);
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
            Assert.assertEquals(0, db.listRepoModuleNames().size());
            // Register second version
            db.registerRepo(globalAdmin, pvd);
            long ver2 = db.getRepoLastVersion(repoModuleName, null);
            List<Long> verHist2 = db.listRepoVersions(repoModuleName, null);
            Assert.assertEquals(2, verHist2.size());
            Assert.assertEquals(ver1, (long)verHist2.get(0));
            Assert.assertEquals(ver2, (long)verHist2.get(1));
            Assert.assertTrue("Versions " + ver1 + " and " + ver2 + " should be different", 
                    ver1 != ver2);

            RepoProvider savedRP = db.getRepoDetails(repoModuleName, null);
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
            Assert.assertEquals(0, db.listRepoModuleNames().size());

            Assert.assertFalse(db.isRepoRegistered(unregModuleName, true));
            try {
                db.getRepoDetails(unregModuleName, null);
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
        } finally {
            try {
                pvd.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    @Test
    public void testPy() throws Exception {
        File repoDir = FileUtils.generateTempDir(
                dbHelper.getWorkDir(), "local_", ".temp");
        String userId = "user1";
        String moduleName = "AsyncPyModule";
        String methodId = "async_py_method_test";
        MethodSpec methodSpec = new MethodSpec().withInfo(
                new MethodBriefInfo().withId(methodId).withName("Asynchronous Python Method Test"))
                .withParameters(Arrays.asList(new MethodParameter().withId("genomeA")
                        .withTextOptions(new TextOptions().withValidWsTypes(
                                Arrays.asList("KBaseGenomes.Genome"))),
                                new MethodParameter().withId("genomeB")
                                .withTextOptions(new TextOptions().withValidWsTypes(
                                        Arrays.asList("KBaseGenomes.Genome")))));
        String pythonCode = "returnVal = {'params': params, 'token': ctx['token']}";
        String dockerCommands = "RUN DEBIAN_FRONTEND=noninteractive apt-get update;" + 
                "apt-get -y upgrade;apt-get install -y libblas3gf liblapack3gf libhdf5-serial-dev\n" +
                "RUN pip install tables";
        PySrvRepoPreparator.prepare(userId, moduleName, methodSpec, pythonCode, dockerCommands, repoDir);
        String implText = TextUtils.text(new File(repoDir, "service/" + moduleName + "Impl.py"));
        Assert.assertTrue(implText.contains("class " + moduleName));
        Assert.assertTrue(implText.contains("        " + pythonCode));
        //buildAndRunDockerImage(dbHelper.getWorkDir(), repoDir, moduleName, methodName);
        String globalAdmin = "admin";
        String host = "localhost:" + dbHelper.getMongoPort();
        MongoDynamicRepoDB db = new MongoDynamicRepoDB(host, dbName, null, null, 
                Arrays.asList(globalAdmin), false, shockUrl, shockToken);
        Assert.assertEquals(0, db.listRepoModuleNames().size());
        RepoProvider pvd = new FileRepoProvider(repoDir);
        db.registerRepo(userId, pvd);
        Assert.assertEquals("[" + moduleName + "]", 
                db.listRepoModuleNames().toString());
        Assert.assertTrue(db.isRepoOwner(moduleName, userId));
        Assert.assertEquals("[" + userId + "]", 
                db.listRepoOwners(moduleName).toString());
        long ver1 = db.getRepoLastVersion(moduleName, null);
        List<Long> verHist1 = db.listRepoVersions(moduleName, null);
        Assert.assertEquals(1, verHist1.size());
        Assert.assertEquals(ver1, (long)verHist1.get(0));
        Assert.assertEquals(RepoState.ready, db.getRepoState(moduleName));
        JsonNode spec = UObject.getMapper().readTree(asText(pvd.getUINarrativeMethodSpec(methodId)));
        Map<String,Object>display = YamlUtils.getDocumentAsYamlMap(asText(pvd.getUINarrativeMethodDisplay(methodId)));
        NarrativeMethodData parser = new NarrativeMethodData(methodId, spec, display, new FileLookup() {
            @Override
            public String loadFileContent(String fileName) {
                return null;
            }
            @Override
            public boolean fileExists(String fileName) {
                return false;
            }
        }, null);
        Assert.assertEquals(methodId, parser.getMethodBriefInfo().getId());
        Assert.assertEquals(methodId, parser.getMethodFullInfo().getId());
        Assert.assertEquals(methodId, parser.getMethodSpec().getInfo().getId());
        Assert.assertEquals(2, parser.getMethodSpec().getParameters().size());
        Assert.assertEquals("genomeA", parser.getMethodSpec().getParameters().get(0).getId());
    }
    
    private static String asText(FilePointer fp) throws NarrativeMethodStoreException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fp.saveToStream(baos);
        return new String(baos.toByteArray(), Charset.forName("utf-8"));
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
    
    @Before 
    @After
    public void cleanup() throws Exception {
        String host = "localhost:" + dbHelper.getMongoPort();
        final MongoClient mc = new MongoClient(host);
        final DB db = mc.getDB(dbName);
        if (shockUrl != null) {
            try {
                final DBCollection files = db.getCollection("repo_files");
                final DBCursor it = files.find();
                for (final DBObject dbo: it) {
                    Map<String, Object> obj = toMap(dbo);
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
        mc.close();
    }
}
