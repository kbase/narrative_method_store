package us.kbase.narrativemethodstore.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.kbase.narrativemethodstore.ValidateAppParams;
//import us.kbase.narrativemethodstore.ValidateCategoryParams;
import us.kbase.narrativemethodstore.ValidateMethodParams;
import us.kbase.narrativemethodstore.ValidateTypeParams;
import us.kbase.narrativemethodstore.ValidationResults;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class Validator {

	protected static final ObjectMapper mapper = new ObjectMapper();
	protected static final Yaml yaml = new Yaml();
	
	/**
	 * Just for local testing...
	 */
	public static void main(String []args) throws Exception {
		
		String name = "gapfill_a_metabolic_model";
		String spec = new String(Files.readAllBytes(Paths.get("/kb/dev_container/modules/narrative_method_specs/methods/"+name+"/spec.json")));
		String display = new String(Files.readAllBytes(Paths.get("/kb/dev_container/modules/narrative_method_specs/methods/"+name+"/display.yaml")));
		
		Map<String, String> extraFiles = new HashMap<String,String>();
		
		extraFiles.put("description.html", "blah");
		
		ValidateMethodParams params = new ValidateMethodParams().withId(name).withSpecJson(spec).withDisplayYaml(display)
				.withExtraFiles(extraFiles);
		ValidationResults vr = Validator.validateMethod(params);
		
		System.out.println(vr);
	}
	
	@SuppressWarnings("unchecked")
	public static ValidationResults validateMethod(ValidateMethodParams params) throws NarrativeMethodStoreException {
		if(params.getSpecJson()==null) {
			throw new NarrativeMethodStoreException("spec_json parameter was not defined, cannot validate");
		}
		
		// grab the relevant input
		String spec = params.getSpecJson();
		String display = cleanYaml(params.getDisplayYaml());
		
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
			if(e.getMessage()!=null) {
				errors.add(e.getMessage());
			} else {
				errors.add("An unknown error occured while parsing the display.yaml file");
			}
		}

		if(parsedDisplay==null) {
			errors.add("Unable to parse display.yaml");
		}
		
		ValidationResults results = new ValidationResults();
		if(parsedSpec!= null && parsedDisplay!=null) {
			try {
				NarrativeMethodData nmd = new NarrativeMethodData(params.getId(), parsedSpec, parsedDisplay, createFileLookup(params.getExtraFiles()));
				results.setMethodFullInfo(nmd.getMethodFullInfo());
				results.setMethodSpec(nmd.getMethodSpec());
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
		String display = cleanYaml(params.getDisplayYaml());
		
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
			if(e.getMessage()!=null) {
				errors.add(e.getMessage());
			} else {
				errors.add("An unknown error occured while parsing the display.yaml file");
			}
		}

		if(parsedDisplay==null) {
			errors.add("Unable to parse display.yaml");
		}
		
		ValidationResults results = new ValidationResults();
		if(parsedSpec!= null && parsedDisplay!=null) {
			try {
				NarrativeAppData nad = new NarrativeAppData(params.getId(), parsedSpec, parsedDisplay, createFileLookup(params.getExtraFiles()));
				results.setAppFullInfo(nad.getAppFullInfo());
				results.setAppSpec(nad.getAppSpec());
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
		String display = cleanYaml(params.getDisplayYaml());
		
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
			if(e.getMessage()!=null) {
				errors.add(e.getMessage());
			} else {
				errors.add("An unknown error occured while parsing the display.yaml file");
			}
		}

		if(parsedDisplay==null) {
			errors.add("Unable to parse display.yaml");
		}
		
		ValidationResults results = new ValidationResults();
		if(parsedSpec!= null && parsedDisplay!=null) {
			try {
				NarrativeTypeData nad = new NarrativeTypeData(params.getId(), parsedSpec, parsedDisplay, createFileLookup(params.getExtraFiles()));
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
	
	protected static FileLookup createFileLookup(final Map <String,String> extraFiles) {
		return new FileLookup() {
			@Override
			public String loadFileContent(String fileName) {
				if(extraFiles!=null) {
					if(extraFiles.containsKey(fileName)) {
						return extraFiles.get(fileName);
					}
				}
				return null;
			}
		};
	}
	
	protected static String cleanYaml(String display) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < display.length(); i++) {
			char ch = display.charAt(i);
			if ((ch < 32 && ch != 10 && ch != 13) || ch >= 127)
				ch = ' ';
			sb.append(ch);
		}
		return sb.toString();
	}
	
	
}
