package us.kbase.narrativemethodstore.db.docker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd.Response;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.EventStreamItem;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PushEventStreamItem;
import com.github.dockerjava.core.DockerClientBuilder;

import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;
import us.kbase.narrativemethodstore.util.TextUtils;

public class DockerImageBuilder {
    private final String dockerRegistry;
    private final File rootTempDir;
    //
    private static final String kbaseImagePrefix = "kbase";
    
    public DockerImageBuilder(String dockerRegistry, File tempDir) {
        this.dockerRegistry = dockerRegistry;
        this.rootTempDir = tempDir;
    }

    public void build(String imageName, String version, File repoDir,
            StringBuilder log) throws Exception {
        build(imageName, version, repoDir, log, true);
    }
    
    public void build(String imageName, String version, File repoDir,
            StringBuilder log, boolean removeLocalCopy) throws Exception {
        File dockerFile = new File(repoDir, "Dockerfile");
        List<String> lines = TextUtils.lines(dockerFile);
        int fromCount = 0;
        String parentKbaseImageName = null;
        String parentKbaseImageVer = null;
        List<String> newLines = null;
        for (int linePos = 0; linePos < lines.size(); linePos++) {
            String line = lines.get(linePos);
            String[] words = TextUtils.splitByWhiteSpaces(line);
            if (words.length > 0) {
                if (words[0].equalsIgnoreCase("FROM")) {
                    fromCount++;
                    if (fromCount > 1)
                        throw new NarrativeMethodStoreException("More than one FROM " +
                        		"command in Dockerfile");
                    if (words.length != 2)
                        throw new NarrativeMethodStoreException("FROM command has " +
                        		"wrong format in Dockerfile: " +
                                Arrays.asList(words));
                    String srcImage = words[1];
                    if (srcImage.startsWith(kbaseImagePrefix + "/") && 
                            dockerRegistry != null) {
                        newLines = new ArrayList<String>(lines);
                        srcImage = srcImage.substring(kbaseImagePrefix.length() + 1);
                        newLines.set(linePos, "FROM " + dockerRegistry + "/" + srcImage);
                        String[] nameAndPort = srcImage.split(Pattern.quote(":"));
                        parentKbaseImageName = nameAndPort[0];
                        parentKbaseImageVer = nameAndPort.length > 1 ? nameAndPort[1] : 
                            "latest";
                        checkImagePulled(parentKbaseImageName, parentKbaseImageVer);
                    }
                }
            }
        }
        File oldDockerFile = null;
        try {
            if (parentKbaseImageName != null) {
                oldDockerFile = new File(repoDir, "Dockerfile." + System.currentTimeMillis());
                TextUtils.writeLines(lines, oldDockerFile);
                TextUtils.writeLines(newLines, dockerFile);
            }
            buildImage(repoDir, imageName, version, log);
            pushImage(imageName, version);
        } finally {
            try {
                if (oldDockerFile != null) {
                    TextUtils.writeLines(lines, dockerFile);
                    oldDockerFile.delete();
                }
            } catch (Exception ignore) {}
            if (removeLocalCopy)
                try {
                    removeImage(imageName, version);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }
    }
    
    private void buildImage(File repoDir, String targetImageName, 
            String targetImageVer, StringBuilder log) throws IOException, 
            NarrativeMethodStoreException {
        String fullTag = dockerRegistry + "/" + targetImageName + ":" + targetImageVer;
        DockerClient cl = createDockerClient();
        Response resp = cl.buildImageCmd(repoDir).withRemove(true)
                .withTag(fullTag).exec();
        Iterator<EventStreamItem> iter = resp.getItems().iterator();
        List<String> errors = new ArrayList<String>();
        List<String> containerIdsForDeletion = new ArrayList<String>();
        List<String> imageIdsForDeletion = new ArrayList<String>();
        while (iter.hasNext()) {
            EventStreamItem item = iter.next();
            if (item.getStream() != null) {
                log.append(item.getStream());
                if (item.getStream().startsWith(" ---> Running in ")) {
                    String[] parts = TextUtils.splitByWhiteSpaces(item.getStream().trim());
                    if (parts.length > 3) {
                        String cntId = parts[parts.length - 1];
                        Container cnt = findContainerByNameOrIdPrefix(cl, cntId);
                        if (cnt != null)
                            containerIdsForDeletion.add(cnt.getId());
                    }
                } else if (item.getStream().startsWith(" ---> ")) {
                    String[] parts = TextUtils.splitByWhiteSpaces(item.getStream().trim());
                    if (parts.length > 1) {
                        String imageId = parts[parts.length - 1];
                        Image img = findImageId(cl, imageId);
                        if (img != null && img.getRepoTags().length == 1 && 
                                img.getRepoTags()[0].equals("<none>:<none>"))
                            imageIdsForDeletion.add(img.getId());
                    }
                }
            }
            if (item.getError() != null) {
                log.append("Error building image: " + item.getError());
                errors.add(item.getError());
            }
        }
        if (!containerIdsForDeletion.isEmpty()) {
            for (String cntId : containerIdsForDeletion)
                try {
                    cl.removeContainerCmd(cntId).withRemoveVolumes(true).exec();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }
        if (!imageIdsForDeletion.isEmpty()) {
            for (String imageId : imageIdsForDeletion) 
                try {
                    cl.removeImageCmd(imageId).exec();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }
        if (!errors.isEmpty())
            throw new NarrativeMethodStoreException("Error building image: " +
                    (errors.size() == 1 ? errors.get(0) : errors.toString()));
    }
    
    private void pushImage(String targetImageName, String targetImageVer) 
            throws IOException, NarrativeMethodStoreException {
        DockerClient cl = createDockerClient();
        PushImageCmd.Response resp = cl.pushImageCmd(dockerRegistry + "/" + targetImageName)
                .withTag(targetImageVer).exec();
        for (PushEventStreamItem item : resp.getItems()) {
            System.out.println("Push: progress=" + item.getProgress() + ", status=" + item.getStatus());
        }
    }
    
    private void removeImage(String targetImageName, 
            String targetImageVer) throws IOException, NarrativeMethodStoreException {
        String fullTag = dockerRegistry + "/" + targetImageName + ":" + targetImageVer;
        DockerClient cl = createDockerClient();
        Image mainImage = findImageId(cl, fullTag);
        if (mainImage != null)
            cl.removeImageCmd(mainImage.getId()).exec();
    }
    
    private void checkImagePulled(String imageName, String version)
            throws NarrativeMethodStoreException {
        String requestedTag = dockerRegistry + "/" + imageName + ":" + version;
        DockerClient cl = createDockerClient();
        if (findImageId(cl, requestedTag) == null)
            cl.pullImageCmd(requestedTag).exec();
        if (findImageId(cl, requestedTag) == null) {
            List<String> dockerfile = null;
            try {
                URL url = DockerImageBuilder.class.getResource("Dockerfile." + 
                        imageName + "_" + version + ".properties");
                if (url != null)
                    dockerfile = TextUtils.lines(url.openStream());
            } catch (Exception ignore) {}
            if (dockerfile != null) {
                System.out.println("Image " + imageName + ":" + version + " is going to be built now...");
                long time = System.currentTimeMillis();
                File tempFile = null;
                File tempDir = null;
                StringBuilder log = new StringBuilder();
                try {
                    tempDir = new File(rootTempDir, imageName + "_" + version + "_" + System.currentTimeMillis());
                    tempDir.mkdir();
                    tempFile = new File(tempDir, "Dockerfile");
                    TextUtils.writeLines(dockerfile, tempFile);
                    build(imageName, version, tempDir, log, false);
                    time = System.currentTimeMillis() - time;
                    System.out.println("Image " + imageName + ":" + version + " was build in " + time + " ms.");
                } catch (Exception ex) {
                    throw new NarrativeMethodStoreException("Error building image: " + 
                            requestedTag + " (" + ex.getMessage() + ")", ex);
                } finally {
                    if (tempFile != null && tempFile.exists())
                        try {
                            tempFile.delete();
                        } catch (Exception ignore) {}
                    if (tempDir != null && tempDir.exists())
                        try {
                            tempDir.delete();
                        } catch (Exception ignore) {}
                }
            } else {
                throw new NarrativeMethodStoreException("Error pulling image: " + 
                        requestedTag);
            }
        }
    }

    private Image findImageId(DockerClient cl, String imageTagOrIdPrefix) {
        return findImageId(cl, imageTagOrIdPrefix, false);
    }
    
    private Image findImageId(DockerClient cl, String imageTagOrIdPrefix, boolean all) {
        for (Image image: cl.listImagesCmd().withShowAll(all).exec()) {
            if (image.getId().startsWith(imageTagOrIdPrefix))
                return image;
            for (String tag : image.getRepoTags())
                if (tag.equals(imageTagOrIdPrefix))
                    return image;
        }
        return null;
    }
    
    private Container findContainerByNameOrIdPrefix(DockerClient cl, String nameOrIdPrefix) {
        for (Container cnt : cl.listContainersCmd().withShowAll(true).exec()) {
            if (cnt.getId().startsWith(nameOrIdPrefix))
                return cnt;
            for (String name : cnt.getNames())
                if (name.equals(nameOrIdPrefix))
                    return cnt;
        }
        return null;
    }
    
    private DockerClient createDockerClient() throws NarrativeMethodStoreException {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "ERROR");
        Logger log = LoggerFactory.getLogger("com.github.dockerjava");
        if (log instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger log2 = (ch.qos.logback.classic.Logger)log;
            log2.setLevel(Level.ERROR);
        }
        return DockerClientBuilder.getInstance().build();
    }
    
    public List<String> reconstructDockerfile(String imageIdOrTag) throws Exception {
        DockerClient cl = createDockerClient();
        List<String> cmds = new ArrayList<String>();
        String fromTag = null;
        Image img = findImageId(cl, imageIdOrTag);
        while (true) {
            InspectImageResponse iir = cl.inspectImageCmd(img.getId()).exec();
            String[] cmd = iir.getContainerConfig().getCmd();
            String cmdLine;
            if (cmd.length == 3 && cmd[0].equals("/bin/sh") && cmd[1].equals("-c")) {
                cmdLine = cmd[2];
                if (cmdLine.startsWith("#(nop) ")) {
                    cmdLine = cmdLine.substring(7);
                } else {
                    cmdLine = "RUN " + cmdLine;
                }
            } else {
                cmdLine ="Unexpected command format: " + Arrays.asList(cmd);
            }
            cmds.add(0, cmdLine + "    #### ---> " + img.getId());
            String imageId = img.getParentId();
            if (imageId == null || imageId.trim().isEmpty())
                break;
            img = findImageId(cl, imageId, true);
            if (img.getRepoTags().length > 1 || (img.getRepoTags().length == 1 && 
                    !img.getRepoTags()[0].equals("<none>:<none>"))) {
                fromTag = img.getRepoTags()[0];
                break;
            }
        }
        if (fromTag != null)
            cmds.add(0, "FROM " + fromTag);
        return cmds;
    }
}
