package us.kbase.test.narrativemethodstore.db.mongo;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.Arrays;

import us.kbase.common.utils.ProcessHelper;
import us.kbase.narrativemethodstore.util.TextUtils;
import us.kbase.testutils.controllers.mongo.MongoController;

public class MongoDBHelper {
    private String tempDirName = "test/temp";
    private final String testName;
    
    private File workDir = null;
    private File mongoDir = null;
    private int mongoPort = -1;

    public MongoDBHelper(String testName) {
        this.testName = testName;
    }

    public MongoDBHelper(String testName, String tempDirName) {
        this(testName);
        this.tempDirName = tempDirName;
    }

    public File getWorkDir() {
        return workDir;
    }
    
    public int getMongoPort() {
        return mongoPort;
    }
    
    public void startup(String mongoExePath) throws Exception {
        workDir = prepareWorkDir(testName);
        mongoDir = new File(workDir, "mongo");
        mongoPort = startupMongo(mongoExePath, mongoDir);
    }
    
    public void shutdown(boolean deleteTempDir) throws Exception {
        killPid(mongoDir);
        if (deleteTempDir && mongoDir.exists())
            deleteRecursively(mongoDir);
    }
    
    private static int startupMongo(String mongodExePath, File dir) throws Exception {
        if (mongodExePath == null)
            mongodExePath = "mongod";
        if (!dir.exists())
            dir.mkdirs();
        MongoController mongo = new MongoController(mongodExePath, dir.toPath(), true);
        System.out.println(String.format("Testing against mongo executable %s on port %s",
                mongodExePath, mongo.getServerPort()));
        return mongo.getServerPort();
    }

    private static void killPid(File dir) {
        if (dir == null)
            return;
        try {
            File pidFile = new File(dir, "pid.txt");
            if (pidFile.exists()) {
                String pid = TextUtils.lines(pidFile).get(0).trim();
                ProcessHelper.cmd("kill", pid).exec(dir);
                System.out.println(dir.getName() + " was stopped");
            }
        } catch (Exception ignore) {}
    }
    
    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {}
        throw new IllegalStateException("Can not find available port in system");
    }

    private File prepareWorkDir(String testName) throws IOException {
        File tempDir = new File(".").getCanonicalFile();
        if (!tempDir.getName().equals(tempDirName)) {
            tempDir = new File(tempDir, tempDirName);
            if (!tempDir.exists())
                tempDir.mkdir();
        }
        for (File dir : tempDir.listFiles()) {
            if (dir.isDirectory() && dir.getName().startsWith("test_" + testName + "_"))
                try {
                    deleteRecursively(dir);
                } catch (Exception e) {
                    System.out.println("Can not delete directory [" + dir.getName() + "]: " + e.getMessage());
                }
        }
        File workDir = new File(tempDir, "test_" + testName + "_" + System.currentTimeMillis());
        if (!workDir.exists())
            workDir.mkdir();
        return workDir;
    }

    private static void deleteRecursively(File fileOrDir) {
        if (fileOrDir.isDirectory() && !Files.isSymbolicLink(fileOrDir.toPath()))
            for (File f : fileOrDir.listFiles()) 
                deleteRecursively(f);
        fileOrDir.delete();
    }
}
