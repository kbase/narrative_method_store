package us.kbase.narrativemethodstore.db.github;

import java.io.IOException;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.type.TypeReference;

import us.kbase.common.service.UObject;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class YamlUtils {
    private static final Yaml yaml = new Yaml();

    public static Map<String,Object> getDocumentAsYamlMap(String document) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < document.length(); i++) {
            char ch = document.charAt(i);
            if ((ch < 32 && ch != 10 && ch != 13) || ch >= 127)
                ch = ' ';
            sb.append(ch);
        }
        document = sb.toString();
        @SuppressWarnings("unchecked")
        Map<String,Object> data = (Map<String, Object>) yaml.load(document);
        return data;
    }

    public static <T> T getPropertyNotNull(String source, Map<String,Object> map, String key, 
            Class<T> retType) throws NarrativeMethodStoreException {
        T ret = getPropertyOrNull(source, map, key, retType);
        if (ret == null)
            throw new NarrativeMethodStoreException("There is no property [" + key + "] " +
                    "in " + source);            
        return ret;
    }
    
    public static <T> T getPropertyOrNull(String source, Map<String,Object> map, String key, 
            Class<T> retType) throws NarrativeMethodStoreException {
        Object obj = map.get(key);
        try {
            return UObject.transformObjectToObject(obj, retType);
        } catch (Exception e) {
            throw new NarrativeMethodStoreException("Error reading property [" + key + "] " +
            		"in " + source + ": " + e.getMessage(), e);
        }
    }

    public static <T> T getPropertyNotNull(String source, Map<String,Object> map, String key, 
            TypeReference<T> retType) throws NarrativeMethodStoreException {
        T ret = getPropertyOrNull(source, map, key, retType);
        if (ret == null)
            throw new NarrativeMethodStoreException("There is no property [" + key + "] " +
                    "in " + source);            
        return ret;
    }
    
    public static <T> T getPropertyOrNull(String source, Map<String,Object> map, String key, 
            TypeReference<T> retType) throws NarrativeMethodStoreException {
        Object obj = map.get(key);
        try {
            return UObject.transformObjectToObject(obj, retType);
        } catch (Exception e) {
            throw new NarrativeMethodStoreException("Error reading property [" + key + "] " +
                    "in " + source + ": " + e.getMessage(), e);
        }
    }
}
