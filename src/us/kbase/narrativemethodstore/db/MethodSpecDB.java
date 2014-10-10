package us.kbase.narrativemethodstore.db;

import java.util.List;

import us.kbase.narrativemethodstore.MethodBriefInfo;
import us.kbase.narrativemethodstore.MethodFullInfo;
import us.kbase.narrativemethodstore.MethodSpec;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public interface MethodSpecDB {
	public List<String> listMethodIds();
	public MethodBriefInfo getMethodBriefInfo(String methodId) throws NarrativeMethodStoreException;
	public MethodFullInfo getMethodFullInfo(String methodId) throws NarrativeMethodStoreException;
	public MethodSpec getMethodSpec(String methodId) throws NarrativeMethodStoreException;
}
