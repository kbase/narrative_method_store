package us.kbase.narrativemethodstore.db.test;

import java.io.File;
import java.util.Collections;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import us.kbase.narrativemethodstore.ValidateMethodParams;
import us.kbase.narrativemethodstore.ValidationResults;
import us.kbase.narrativemethodstore.db.Validator;

public class ValidatorTest {
    
    @Test
    public void testPublicationError() throws Exception {
        String methodSpecId = "generate_heatmaps";
        String specJson = readTestMethodValidationFile(methodSpecId, "spec.json");
        String displayYaml = readTestMethodValidationFile(methodSpecId, "display.yaml");
        ValidationResults vr = Validator.validateMethod(
                new ValidateMethodParams().withId(methodSpecId)
                .withSpecJson(specJson).withDisplayYaml(displayYaml)
                .withExtraFiles(Collections.<String,String>emptyMap()));
        Assert.assertEquals("" + vr, 1L, (long)vr.getIsValid());
    }
    
    private static String readTestMethodValidationFile(String methodSpecId, 
            String fileName) throws Exception {
        return FileUtils.readFileToString(new File(new File(
                "test/data/validation/methods", methodSpecId), fileName));
    }
}
