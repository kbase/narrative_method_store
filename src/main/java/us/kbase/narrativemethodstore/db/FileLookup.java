package us.kbase.narrativemethodstore.db;

public interface FileLookup {
	public String loadFileContent(String fileName);
	public boolean fileExists(String fileName);
}
