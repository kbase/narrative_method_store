package us.kbase.narrativemethodstore.exceptions;

public class NarrativeMethodStoreInitializationException extends NarrativeMethodStoreException {

	private static final long serialVersionUID = -2255964987661660068L;

	public NarrativeMethodStoreInitializationException(String message) {
		super(message);
	}

	public NarrativeMethodStoreInitializationException(String message, Throwable e) {
		super(message,e);
	}

	public NarrativeMethodStoreInitializationException(Throwable e) {
		super(e.getMessage() == null ? "Unknown error" : e.getMessage(), e);
	}
}
