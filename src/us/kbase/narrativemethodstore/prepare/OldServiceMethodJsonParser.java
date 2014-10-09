package us.kbase.narrativemethodstore.prepare;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import us.kbase.common.service.UObject;

public class OldServiceMethodJsonParser {
	public static void main(String[] args) throws Exception {
		Map<String, OldMethodCategory> data = UObject.getMapper().readValue(
				OldServiceMethodJsonParser.class.getResourceAsStream("services.json.properties"), 
				new TypeReference<Map<String, OldMethodCategory>>() {});
		System.out.println(UObject.getMapper().writeValueAsString(data));
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