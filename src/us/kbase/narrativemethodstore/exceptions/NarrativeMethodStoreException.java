package us.kbase.narrativemethodstore.exceptions;

import us.kbase.narrativemethodstore.Category;
import us.kbase.narrativemethodstore.MethodBriefInfo;

public class NarrativeMethodStoreException extends Exception {

	private static final long serialVersionUID = 4313592688446078015L;
	private MethodBriefInfo errorMethod = null;
	private Category errorCategory = null;
	
	public NarrativeMethodStoreException(String message) {
		super(message);
	}

	public NarrativeMethodStoreException(String message, Throwable e) {
		super(message,e);
	}

	public NarrativeMethodStoreException(Throwable e) {
		super(e.getMessage() == null ? "Unknown error" : e.getMessage(), e);
	}
	
	public MethodBriefInfo getErrorMethod() {
		return errorMethod;
	}
	
	public void setErrorMethod(MethodBriefInfo errorMethod) {
		this.errorMethod = errorMethod;
		String msg = getMessage();
		if (msg == null)
			msg = getCause().getClass().getName();
		this.errorMethod.setLoadingError(msg);
	}
	
	public Category getErrorCategory() {
		return errorCategory;
	}
	
	public void setErrorCategory(Category errorCategory) {
		this.errorCategory = errorCategory;
		String msg = getMessage();
		if (getCause() != null)
			msg = getCause().getClass().getName() + ": " + msg;
		this.errorCategory.setLoadingError(msg);
	}
}
