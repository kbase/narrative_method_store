package us.kbase.narrativemethodstore.db.test;

import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.kbase.narrativemethodstore.MethodParameterGroup;
import us.kbase.narrativemethodstore.db.FileLookup;
import us.kbase.narrativemethodstore.db.NarrativeMethodData;
import us.kbase.narrativemethodstore.db.github.YamlUtils;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;
import us.kbase.narrativemethodstore.util.TextUtils;

public class NarrativeMethodDataTest {
    
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    
    @Test
    public void testGroupMultipleOk() throws Exception {
        NarrativeMethodData data = load(1);
        Assert.assertEquals(1, data.getMethodSpec().getParameterGroups().size());
        MethodParameterGroup group = data.getMethodSpec().getParameterGroups().get(0);
        System.out.println(data.getMethodSpec());
        Assert.assertNotNull(group.getId());
        Assert.assertEquals(3, group.getParameterIds().size());
        Assert.assertEquals("", group.getDescription());
        Assert.assertEquals(1L, (long)group.getAllowMultiple());
        Assert.assertEquals(1L, (long)group.getOptional());
        Assert.assertEquals(3, group.getIdMapping().size());
        Assert.assertNotNull(group.getUiName());
        Assert.assertNotNull(group.getShortHint());
        Assert.assertEquals(0L, (long)group.getWithBorder());
    }

    @Test
    public void testGroupNoIdError() throws Exception {
        expectedEx.expect(NarrativeMethodStoreException.class);
        expectedEx.expectMessage("Can't find sub-node [id] within path [parameter-groups/0] in " +
        		"spec.json");
        load(2);
    }

    @Test
    public void testGroupWrongParamError() throws Exception {
        expectedEx.expect(NarrativeMethodStoreException.class);
        expectedEx.expectMessage("Undeclared parameter [input_reads_label2] found within path " +
        		"[parameter-groups/0/parameters]");
        load(3);
    }

    @Test
    public void testGroupWrongMappingError() throws Exception {
        expectedEx.expect(NarrativeMethodStoreException.class);
        expectedEx.expectMessage("Undeclared parameter [input_reads_label2] found within path " +
                "[parameter-groups/0/mapping]");
        load(4);
    }

    @Test
    public void testGroupNoDisplayError() throws Exception {
        expectedEx.expect(NarrativeMethodStoreException.class);
        expectedEx.expectMessage("Can't find property [parameter-groups] within path [/] in " +
                "display.yaml");
        load(5);
    }

    @Test
    public void testGroupOneMappingError() throws Exception {
        expectedEx.expect(NarrativeMethodStoreException.class);
        expectedEx.expectMessage("Unsupported mapping found for one-copy parameter-group " +
        		"within path [parameter-groups/0]");
        load(6);
    }

    @Test
    public void testGroupOneOk() throws Exception {
        NarrativeMethodData data = load(7);
        Assert.assertEquals(1, data.getMethodSpec().getParameterGroups().size());
        MethodParameterGroup group = data.getMethodSpec().getParameterGroups().get(0);
        System.out.println(data.getMethodSpec());
        Assert.assertNotNull(group.getId());
        Assert.assertEquals(3, group.getParameterIds().size());
        Assert.assertEquals("", group.getDescription());
        Assert.assertEquals(0L, (long)group.getAllowMultiple());
        Assert.assertEquals(1L, (long)group.getOptional());
        Assert.assertNull(group.getIdMapping());
        Assert.assertNotNull(group.getUiName());
        Assert.assertNotNull(group.getShortHint());
        Assert.assertEquals(1L, (long)group.getWithBorder());
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
