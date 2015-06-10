package us.kbase.narrativemethodstore.db.docker.test;

import java.io.File;

import org.junit.Test;

import us.kbase.narrativemethodstore.db.docker.DockerImageBuilder;

public class DockerImageBuilderTest {
    
    @Test
    public void mainTest() throws Exception {
        String tempDirName = System.getProperty("test.temp-dir");
        String dockerRegistry = System.getProperty("test.docker-registry");
        File tempDir = new File(tempDirName);
        DockerImageBuilder dib = new DockerImageBuilder(dockerRegistry, tempDir);
        String imageName = "GenomeFeatureComparator".toLowerCase();
        String imageVer = "1433804926692";
        StringBuilder log = new StringBuilder();
        dib.build(imageName, imageVer, new File("test/data/test_repo_1"), log);
    }
}
