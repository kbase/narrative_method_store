package us.kbase.narrativemethodstore.db.mongo.test;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.Arrays;

import us.kbase.common.utils.ProcessHelper;
import us.kbase.narrativemethodstore.util.TextUtils;

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
        File dataDir = new File(dir, "data");
        dataDir.mkdir();
        File logFile = new File(dir, "mongodb.log");
        int port = findFreePort();
        File configFile = new File(dir, "mongod.conf");
        TextUtils.writeLines(Arrays.asList(
                "dbpath=" + dataDir.getAbsolutePath(),
                "logpath=" + logFile.getAbsolutePath(),
                "logappend=true",
                "port=" + port,
                "bind_ip=127.0.0.1"
                ), configFile);
        File scriptFile = new File(dir, "start_mongo.sh");
        TextUtils.writeLines(Arrays.asList(
                "#!/bin/bash",
                "cd " + dir.getAbsolutePath(),
                mongodExePath + " --nojournal --config " + configFile.getAbsolutePath() + " >out.txt 2>err.txt & pid=$!",
                "echo $pid > pid.txt"
                ), scriptFile);
        ProcessHelper.cmd("bash", scriptFile.getCanonicalPath()).exec(dir);
        boolean ready = false;
        int waitSec = 120;
        for (int n = 0; n < waitSec; n++) {
            Thread.sleep(1000);
            if (logFile.exists()) {
                if (TextUtils.grep(TextUtils.lines(logFile), "waiting for connections on port " + port).size() > 0) {
                    ready = true;
                    break;
                }
            }
        }
        if (!ready) {
            if (logFile.exists())
                for (String l : TextUtils.lines(logFile))
                    System.err.println("MongoDB log: " + l);
            throw new IllegalStateException("MongoDB couldn't startup in " + waitSec + " seconds");
        }
        System.out.println(dir.getName() + " was started up");
        return port;
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
