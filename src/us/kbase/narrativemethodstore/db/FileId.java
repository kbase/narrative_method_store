package us.kbase.narrativemethodstore.db;

/**
 * Class handles string id pointing to file data stored 
 * in dynamic repo database.
 * @author rsutormin
 */
public class FileId {
    private final String id;
    
    public FileId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
}
