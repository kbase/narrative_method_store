package us.kbase.narrativemethodstore.db.github;

import java.io.IOException;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

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
        //System.out.println("fetched yaml ("+url+"):\n"+yaml.dump(data));
        return data;
    }

}
