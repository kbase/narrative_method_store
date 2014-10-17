package us.kbase.narrativemethodstore.prepare;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.kbase.common.service.UObject;

public class OldServiceMethodJsonParser {
	private static final File specRootDir = new File("../narrative_method_specs");
	
	public static void main(String[] args) throws Exception {
		ObjectMapper mpr = UObject.getMapper();
		Map<String, OldMethodCategory> data = mpr.readValue(
				OldServiceMethodJsonParser.class.getResourceAsStream("services.json.properties"), 
				new TypeReference<Map<String, OldMethodCategory>>() {});
		File metRootDir = new File(specRootDir, "methods");
		File catRootDir = new File(specRootDir, "categories");
		Set<String> catIds = new TreeSet<String>();
		Set<String> metIds = new TreeSet<String>();
		Set<String> metNames = new TreeSet<String>();
		Set<String> modelTypesUsed = new TreeSet<String>();
		for (String catName : data.keySet()) {
			boolean old = catName.equals("Microbes Services");
			//System.out.println("Category: " + catName);
			OldMethodCategory cat = data.get(catName);
			String catId = nameToId(catName, catIds);
			File catDir = new File(catRootDir, catId);
			if (!catDir.exists())
				catDir.mkdirs();
			Map<String, Object> catProps = new LinkedHashMap<String, Object>();
			catProps.put("name", catName);
			catProps.put("parent", new ArrayList<String>(1));
			catProps.put("tooltip", cat.desc);
			catProps.put("ver", "1.0.0");
			int visibleMethods = 0;
			for (OldMethod met : cat.methods) {
				//if (metNames.contains(met.title))
				//	System.out.println("Method name was already used: " + met.title + " (" + catId + ")");
				metNames.add(met.title);
				StringBuilder display = new StringBuilder();
				Map<String, Object> spec = new LinkedHashMap<String, Object>();
				String metId = met.title;
				if (old)
					metId += "_old";
				metId = nameToId(metId, metIds);
				File metDir = new File(metRootDir, metId);
				if (!metDir.exists())
					metDir.mkdirs();
				spec.put("name", met.title);
				display.append("#\n# Define basic display information\n#\n");
				display.append("name     : ").append(met.title).append("\n");
				display.append("subtitle : |\n    ").append(met.description).append("\n");
				display.append("tooltip  : |\n    ").append(met.description).append("\n");
				display.append("\nscreenshots :\n    []\n");
				display.append("\n#\n# Define the set of other narrative methods that should be suggested to the user.\n");
				display.append("#\nmethod-suggestions :\n    related :\n        []\n    next :\n        []\n\n\n");
				display.append("#\n# Configure the display and description of the parameters\n#\nparameters :\n");
				if (!met.type.equals("object"))
					throw new IllegalStateException("Unexpected method type: " + met.type);
				spec.put("ver", "1.0.0");
				spec.put("authors", new ArrayList<String>(1));
				spec.put("contact", "help@kbase.us");
				spec.put("visble", met.visible);
				if (met.visible)
					visibleMethods++;
				spec.put("categories", Arrays.asList(catId));
				spec.put("widgets", met.properties.widgets);
				List<Object> params = new ArrayList<Object>();
				List<String> paramIds = new ArrayList<String>(met.properties.parameters.keySet());
				Collections.sort(paramIds, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						if (o1.startsWith("param") && o2.startsWith("param")) {
							int val1 = Integer.parseInt(o1.substring(5));
							int val2 = Integer.parseInt(o2.substring(5));
							return Integer.compare(val1, val2);
						}
						return o1.compareTo(o2);
					}
				});
				for (String paramId : paramIds) {
					OldMethodParam param = met.properties.parameters.get(paramId);
					Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
					params.add(paramMap);
					paramMap.put("id", paramId);
					paramMap.put("optional", false);
					paramMap.put("advanced", false);
					paramMap.put("allow_multiple", false);
					paramMap.put("default_values", Arrays.asList(param.default_));
					String type = "text";
					List<String> validWsTypes = new ArrayList<String>();
					if (!param.type.equals("string")) {
						if (param.type.equals("a number")) {
							type = "int-slider";
						} else {
							validWsTypes.add(param.type);
							modelTypesUsed.add(param.type);
						}
					}
					paramMap.put("field_type", type);
					Map<String, Object> textOptions = new LinkedHashMap<String, Object>();
					textOptions.put("valid_ws_types", validWsTypes);
					paramMap.put("text_options", textOptions);
					display.append("    " + paramId + " :\n");
					display.append("        ui-name : |\n            " + param.ui_name + "\n");
					display.append("        short-hint : |\n            " + param.description + "\n");
					display.append("        long-hint  : |\n            " + param.description + "\n\n\n");
				}
				spec.put("parameters", params);
				Map<String, Object> behavior = new LinkedHashMap<String, Object>();
				behavior.put("python_class", catName);
				behavior.put("python_function", met.title);
				spec.put("behavior", behavior);
				display.append("description : |\n    " + met.description + "\n\n\n");
				display.append("technical-description : |\n    " + met.description + "\n");
				writeJsonIntoFile(spec, new File(metDir, "spec.json"));
				PrintWriter pw = new PrintWriter(new File(metDir, "display.yaml"));
				pw.print(display);
				pw.close();
				File imgDir = new File(metDir, "img");
				if (!imgDir.exists())
					imgDir.mkdir();
			}
			catProps.put("visible", visibleMethods > 0);
			writeJsonIntoFile(catProps, new File(catDir, "spec.json"));
		}
		//for (String type : modelTypesUsed)
		//	System.out.println(type);
	}
	
	private static void writeJsonIntoFile(Map<String, Object> data, File f) throws Exception {
		ObjectMapper mpr = new ObjectMapper();
		PrintWriter pw = new PrintWriter(f);
		pw.print(mpr.writerWithDefaultPrettyPrinter().writeValueAsString(data));
		pw.close();
	}
	
	private static String nameToId(String name, Set<String> ids) {
		StringBuilder sb = new StringBuilder();
		for (int pos = 0; pos < name.length(); pos++) {
			char ch = name.charAt(pos);
			if (!Character.isLetterOrDigit(ch))
				ch = '_';
			if (ch != '_' || (sb.length() > 0 && sb.charAt(sb.length() - 1) != '_'))
				sb.append(ch);
		}
		if (sb.charAt(sb.length() - 1) == '_')
			sb.deleteCharAt(sb.length() - 1);
		String id = sb.toString().toLowerCase();
		if (ids.contains(id)) {
			int suf = 2;
			while (ids.contains(id + "_" + suf))
				suf++;
			id += "_" + suf;
		}
		ids.add(id);
		return id;
	}
}

//{"KBase Commands": 
//	{"methods": 
//		[
//			{"description": "Execute given KBase command.", 
//			 "title": "Execute KBase Command", 
//			 "visible": true, 
//			 "returns": 
//				{"output0": 
//					{"type": "string", 
//					 "description": "Results"
//					}
//				}, 
//			 "type": "object", 
//			 "properties": 
//				{"widgets": 
//					{"input": null, 
//					 "output": "DisplayTextWidget"
//					}, 
//				 "parameters": 
//					{"param0": 
//						{"default": "", 
//						 "ui_name": "Command", 
//						 "type": "string", 
//						 "description": "command to run"
//						}
//					}
//				}
//			}
//		], 
//	 "version": [0, 0, "1"], 
//	 "name": "KBase Commands", 
//	 "desc": "Functions for executing KBase commands and manipulating the results"
//	}
//}