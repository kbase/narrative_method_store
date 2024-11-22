package us.kbase.narrativemethodstore.db.github;

import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class MethodId implements Comparable<MethodId> {
    private final String repoModuleName;
    private final String methodId;
    private final RepoTag tag;
    
    public MethodId(String repoModuleName, String methodId, RepoTag tag) {
        this.repoModuleName = repoModuleName;
        this.methodId = methodId;
        this.tag = tag;
    }
    
    public MethodId(String fullMethodId) {
        String[] moduleMethodTag = fullMethodId.split("/");
        if (moduleMethodTag.length == 1) {
            repoModuleName = null;
            methodId = fullMethodId;
            tag = null;
        } else {
            repoModuleName = moduleMethodTag[0];
            methodId = moduleMethodTag[1];
            tag = moduleMethodTag.length >= 3 ? 
                    RepoTag.valueOf(moduleMethodTag[2]) : RepoTag.dev;
        }
    }
    
    public MethodId(String externalId, RepoTag tag) throws NarrativeMethodStoreException {
        String[] moduleMethod = externalId.split("/");
        if (moduleMethod.length == 1) {
            repoModuleName = null;
            methodId = externalId;
            this.tag = null;
        } else {
            if (moduleMethod.length > 2)
                throw new NarrativeMethodStoreException("Unsupported Method ID format: " + 
                        externalId);
            repoModuleName = moduleMethod[0];
            methodId = moduleMethod[1];
            this.tag = tag == null ? RepoTag.dev : tag;
        }
    }
    
    public String getRepoModuleName() {
        return repoModuleName;
    }
    
    public String getMethodId() {
        return methodId;
    }
    
    public RepoTag getTag() {
        return tag;
    }

    public boolean isDynamic() {
        return repoModuleName != null;
    }
    
    @Override
    public String toString() {
        return isDynamic() ? (repoModuleName + "/" + methodId + "/" + tag) : methodId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((methodId == null) ? 0 : methodId.hashCode());
        result = prime * result
                + ((repoModuleName == null) ? 0 : repoModuleName.hashCode());
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MethodId other = (MethodId) obj;
        if (methodId == null) {
            if (other.methodId != null)
                return false;
        } else if (!methodId.equals(other.methodId))
            return false;
        if (repoModuleName == null) {
            if (other.repoModuleName != null)
                return false;
        } else if (!repoModuleName.equals(other.repoModuleName))
            return false;
        if (tag != other.tag)
            return false;
        return true;
    }

    public String getExternalId() {
        return isDynamic() ? (repoModuleName + "/" + methodId) : methodId;
    }
    
    @Override
    public int compareTo(MethodId o) {
        int cmp = Boolean.compare(isDynamic(), o.isDynamic());
        if (cmp != 0)
            return cmp;
        return toString().compareTo(o.toString());
    }
}
