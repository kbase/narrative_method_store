package us.kbase.narrativemethodstore.db;

import java.io.File;
import java.io.OutputStream;

import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public interface FilePointer {

    public File getFile();
    
    public FileId getFileId();
    
    public String getName();
    
    public long length();
    
    public void saveToStream(OutputStream os) throws NarrativeMethodStoreException;

}
