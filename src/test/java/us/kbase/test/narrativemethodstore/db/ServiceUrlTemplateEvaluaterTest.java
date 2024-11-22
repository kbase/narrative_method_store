package us.kbase.test.narrativemethodstore.db;

import junit.framework.Assert;

import org.junit.Test;

import us.kbase.narrativemethodstore.db.ServiceUrlTemplateEvaluater;

public class ServiceUrlTemplateEvaluaterTest {

    @Test
    public void mainTest() throws Exception {
        String host = "https://ci.kbase.us";
        String base = "/services";
        String moduleName = "MyModule";
        String module = moduleName.toLowerCase();
        String version = "123456789abcdef0";
        ServiceUrlTemplateEvaluater sute = new ServiceUrlTemplateEvaluater(host, base);
        Assert.assertEquals(host + base + "/" + module + ":" + version, 
                sute.evaluate("${url}", moduleName, version));
        Assert.assertEquals(host + base + "/" + module, 
                sute.evaluate("${url}", moduleName, null));
        Assert.assertEquals(host + base + "/" + module, 
                sute.evaluate("${unversioned-url}", moduleName, version));
        Assert.assertEquals(host + base + "/" + module, 
                sute.evaluate("${endpoint}/${module}", moduleName, null));
        Assert.assertEquals(host + base + "/" + moduleName, 
                sute.evaluate("${endpoint}/${module-name}", moduleName, null));
        Assert.assertEquals(host + ":8080" + base, 
                sute.evaluate("${endpoint-host}:8080${endpoint-base}", moduleName, null));
    }
}
