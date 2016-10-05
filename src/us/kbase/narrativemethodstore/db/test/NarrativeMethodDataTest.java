package us.kbase.narrativemethodstore.db.test;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.kbase.narrativemethodstore.MethodParameterGroup;
import us.kbase.narrativemethodstore.db.FileLookup;
import us.kbase.narrativemethodstore.db.NarrativeMethodData;
import us.kbase.narrativemethodstore.db.github.YamlUtils;
import us.kbase.narrativemethodstore.util.TextUtils;

public class NarrativeMethodDataTest {
    
    @Test
    public void testGroups() throws Exception {
        NarrativeMethodData data = load(1);
        Assert.assertEquals(1, data.getMethodSpec().getParameterGroups().size());
        MethodParameterGroup group = data.getMethodSpec().getParameterGroups().get(0);
        System.out.println(data.getMethodSpec());
        Assert.assertNotNull(group.getId());
        Assert.assertEquals(3, group.getParameterIds().size());
        Assert.assertNull(group.getDescription());
        Assert.assertEquals(1L, (long)group.getAllowMultiple());
        Assert.assertEquals(1L, (long)group.getOptional());
        Assert.assertEquals(3, group.getIdMapping().size());
        Assert.assertNotNull(group.getParameterOptionalityMode());
        Assert.assertNotNull(group.getUiName());
        Assert.assertNotNull(group.getShortHint());
        Assert.assertEquals(0L, (long)group.getWithBorder());
    }
    
    private static NarrativeMethodData load(int num) throws Exception {
        FileLookup fl = new FileLookup() {
            @Override
            public String loadFileContent(String fileName) {
                return null;
            }
            @Override
            public boolean fileExists(String fileName) {
                return false;
            }
        };
        JsonNode spec = new ObjectMapper().readTree(
                loadTextResource("spec_" + num + ".properties"));
        Map<String,Object> display = YamlUtils.getDocumentAsYamlMap(
                loadTextResource("display_" + num + ".properties"));
        return new NarrativeMethodData("method_" + num, 
                spec, display, fl, null, null, null, null, null);
    }
    
    private static String loadTextResource(String name) throws Exception {
        return TextUtils.text(NarrativeMethodDataTest.class.getResourceAsStream(name));
    }
}
