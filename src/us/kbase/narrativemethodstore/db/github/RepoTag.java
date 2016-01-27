package us.kbase.narrativemethodstore.db.github;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepoTag {
    public static final RepoTag dev = new RepoTag("dev");
    public static final RepoTag beta = new RepoTag("beta");
    public static final RepoTag release = new RepoTag("release");
    
    private static List<RepoTag> values = Collections.unmodifiableList(
            Arrays.asList(dev, beta, release));
    private static Map<String, RepoTag> hashCache = 
            Collections.synchronizedMap(new HashMap<String, RepoTag>());
    
    private String tagName;
    private boolean isGitCommitHash;
    
    private RepoTag(String tagName) {
        this(tagName, false);
    }

    private RepoTag(String tagName, boolean isGitCommitHash) {
        this.tagName = tagName;
        this.isGitCommitHash = isGitCommitHash;
    }

    public boolean isGitCommitHash() {
        return isGitCommitHash;
    }
    
    @Override
    public String toString() {
        return tagName;
    }
    
    public String name() {
        if (isGitCommitHash)
            throw new IllegalStateException("Name property is not supported for version tags");
        return tagName;
    }
    
    @Override
    public int hashCode() {
        return tagName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RepoTag other = (RepoTag) obj;
        if (tagName == null) {
            if (other.tagName != null)
                return false;
        } else if (!tagName.equals(other.tagName))
            return false;
        return true;
    }

    public static RepoTag valueOf(String value) {
        if (value.length() == 40 && value.matches("[0-9a-fA-F]+")) {
            RepoTag ret = hashCache.get(value);
            if (ret == null) {
                ret = new RepoTag(value, true);
                hashCache.put(value, ret);
            }
            return ret;
        }
        if (dev.tagName.equals(value))
            return dev;
        if (beta.tagName.equals(value))
            return beta;
        if (release.tagName.equals(value))
            return release;
        throw new IllegalArgumentException("Repo-tag [" + value + "] is not supported");
    }
    
    public static List<RepoTag> values() {
        return values;
    }
}
