package us.kbase.narrativemethodstore.exceptions;

import us.kbase.narrativemethodstore.AppBriefInfo;
import us.kbase.narrativemethodstore.Category;
import us.kbase.narrativemethodstore.MethodBriefInfo;
import us.kbase.narrativemethodstore.TypeInfo;

public class NarrativeMethodStoreException extends Exception {

	private static final long serialVersionUID = 4313592688446078015L;
	private MethodBriefInfo errorMethod = null;
	private Category errorCategory = null;
	private AppBriefInfo errorApp = null;
	private TypeInfo errorType = null;
	
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

	public AppBriefInfo getErrorApp() {
		return errorApp;
	}
	
	public void setErrorApp(AppBriefInfo errorApp) {
		this.errorApp = errorApp;
		String msg = getMessage();
		if (msg == null)
			msg = getCause().getClass().getName();
		this.errorApp.setLoadingError(msg);
	}

	public TypeInfo getErrorType() {
		return errorType;
	}
	
	public void setErrorType(TypeInfo errorType) {
		this.errorType = errorType;
		String msg = getMessage();
		if (msg == null)
			msg = getCause().getClass().getName();
		this.errorType.setLoadingError(msg);
	}
}
