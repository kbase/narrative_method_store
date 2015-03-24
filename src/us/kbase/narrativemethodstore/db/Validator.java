package us.kbase.narrativemethodstore.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.kbase.narrativemethodstore.ValidateAppParams;
import us.kbase.narrativemethodstore.ValidateMethodParams;
import us.kbase.narrativemethodstore.ValidateTypeParams;
import us.kbase.narrativemethodstore.ValidationResults;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class Validator {

	protected static final ObjectMapper mapper = new ObjectMapper();
	protected static final Yaml yaml = new Yaml();
	
	@SuppressWarnings("unchecked")
	public static ValidationResults validateMethod(ValidateMethodParams params) {
		// grab the relevant input
		String spec = params.getSpecJson();
		String display = params.getDisplayYaml();
		
		//setup results
		long isValid = 0L;
		List <String> errors = new ArrayList<String>();
		List <String> warnings = new ArrayList<String>();
		
		// parse spec as JSON, trap any errors
		JsonNode parsedSpec = null;
		try {
			parsedSpec = mapper.readTree(spec);
		} catch (IOException e1) {
			errors.add(e1.getMessage());
		}
		
		Map<String,Object> parsedDisplay = null;
		Object parsedDisplayObject = null;
		try {
			parsedDisplayObject = yaml.load(display);
			parsedDisplay = (Map<String,Object>) parsedDisplayObject;
		} catch(ClassCastException e) {
			errors.add("display.yaml could not be parsed as a structure. It was mapped to:"+parsedDisplayObject.getClass().getName() +
					"\n Make sure the top level of the YAML file are 'fields:values', not a list, string, or other construct.");
		} catch (Exception e) {
			errors.add(e.getMessage());
		}

		if(parsedDisplay==null) {
			errors.add("Unable to parse display.yaml");
		}
		
		ValidationResults results = new ValidationResults();
		if(parsedSpec!= null && parsedDisplay!=null) {
			try {
				NarrativeMethodData nmd = new NarrativeMethodData(params.getId(), parsedSpec, parsedDisplay, null);
				results.setMethodFullInfo(nmd.getMethodFullInfo());
				// it all seemed to parse fine, but we can add additional checks or warnings here if desired
				isValid = 1L;
			} catch (NarrativeMethodStoreException e) {
				errors.add(e.getMessage());
			} catch (Exception e) {
				errors.add(e.getMessage());
			}
		}
		
		results.setErrors(errors);
		results.setWarnings(warnings);
		results.setIsValid(isValid);
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public static ValidationResults validateApp(ValidateAppParams params) {
		// grab the relevant input
		String spec = params.getSpecJson();
		String display = params.getDisplayYaml();
		
		//setup results
		long isValid = 0L;
		List <String> errors = new ArrayList<String>();
		List <String> warnings = new ArrayList<String>();
		
		// parse spec as JSON, trap any errors
		JsonNode parsedSpec = null;
		try {
			parsedSpec = mapper.readTree(spec);
		} catch (IOException e1) {
			errors.add(e1.getMessage());
		}
		
		Map<String,Object> parsedDisplay = null;
		Object parsedDisplayObject = null;
		try {
			parsedDisplayObject = yaml.load(display);
			parsedDisplay = (Map<String,Object>) parsedDisplayObject;
		} catch(ClassCastException e) {
			errors.add("display.yaml could not be parsed as a structure. It was mapped to:"+parsedDisplayObject.getClass().getName() +
					"\n Make sure the top level of the YAML file are 'fields:values', not a list, string, or other construct.");
		} catch (Exception e) {
			errors.add(e.getMessage());
		}

		if(parsedDisplay==null) {
			errors.add("Unable to parse display.yaml");
		}
		
		ValidationResults results = new ValidationResults();
		if(parsedSpec!= null && parsedDisplay!=null) {
			try {
				NarrativeAppData nad = new NarrativeAppData(params.getId(), parsedSpec, parsedDisplay, null);
				results.setAppFullInfo(nad.getAppFullInfo());
				// it all seemed to parse fine, but we can add additional checks or warnings here if desired
				isValid = 1L;
			} catch (NarrativeMethodStoreException e) {
				errors.add(e.getMessage());
			} catch (Exception e) {
				errors.add(e.getMessage());
			}
		}
		
		results.setErrors(errors);
		results.setWarnings(warnings);
		results.setIsValid(isValid);
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public static ValidationResults validateType(ValidateTypeParams params) {
		// grab the relevant input
		String spec = params.getSpecJson();
		String display = params.getDisplayYaml();
		
		//setup results
		long isValid = 0L;
		List <String> errors = new ArrayList<String>();
		List <String> warnings = new ArrayList<String>();
		
		// parse spec as JSON, trap any errors
		JsonNode parsedSpec = null;
		try {
			parsedSpec = mapper.readTree(spec);
		} catch (IOException e1) {
			errors.add(e1.getMessage());
		}
		
		Map<String,Object> parsedDisplay = null;
		Object parsedDisplayObject = null;
		try {
			parsedDisplayObject = yaml.load(display);
			parsedDisplay = (Map<String,Object>) parsedDisplayObject;
		} catch(ClassCastException e) {
			errors.add("display.yaml could not be parsed as a structure. It was mapped to:"+parsedDisplayObject.getClass().getName() +
					"\n Make sure the top level of the YAML file are 'fields:values', not a list, string, or other construct.");
		} catch (Exception e) {
			errors.add(e.getMessage());
		}

		if(parsedDisplay==null) {
			errors.add("Unable to parse display.yaml");
		}
		
		ValidationResults results = new ValidationResults();
		if(parsedSpec!= null && parsedDisplay!=null) {
			try {
				NarrativeTypeData nad = new NarrativeTypeData(params.getId(), parsedSpec, parsedDisplay, null);
				results.setTypeInfo(nad.getTypeInfo());
				// it all seemed to parse fine, but we can add additional checks or warnings here if desired
				isValid = 1L;
			} catch (NarrativeMethodStoreException e) {
				errors.add(e.getMessage());
			} catch (Exception e) {
				errors.add(e.getMessage());
			}
		}
		
		results.setErrors(errors);
		results.setWarnings(warnings);
		results.setIsValid(isValid);
		return results;
	}
	
	
	
	
}
