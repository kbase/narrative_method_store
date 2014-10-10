package us.kbase.narrativemethodstore.exceptions;

public class NarrativeMethodStoreException extends Exception {

	private static final long serialVersionUID = 4313592688446078015L;

	public NarrativeMethodStoreException(String message) {
		super(message);
	}

	public NarrativeMethodStoreException(String message, Throwable e) {
		super(message,e);
	}

	public NarrativeMethodStoreException(Throwable e) {
		super(e.getMessage() == null ? "Unknown error" : e.getMessage(), e);
	}
}
