package us.kbase.narrativemethodstore.db.github;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import us.kbase.jkidl.IncludeProvider;
import us.kbase.kidl.KbModule;
import us.kbase.kidl.KidlParseException;
import us.kbase.mobu.compiler.RunCompileCommand;
import us.kbase.mobu.util.DiskFileSaver;
import us.kbase.narrativemethodstore.MethodSpec;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;
import us.kbase.narrativemethodstore.util.TextUtils;

public class PySrvRepoPreparator {
    public static void prepare(String userId, String moduleName, MethodSpec methodSpec, 
            String pyhtonCode, String dockerCommands, File repoDir) throws NarrativeMethodStoreException {
        try {
            String methodName = methodSpec.getInfo().getId();
            StringBuilder specFile = new StringBuilder();
            specFile.append("module ").append(moduleName).append(" {\n");
            specFile.append("    async funcdef ").append(methodName).append(
                    "(UnspecifiedObject params) returns (UnspecifiedObject)")
                    .append(" authentication required;\n");
            specFile.append("};\n");
            IncludeProvider ip = new IncludeProvider() {
                @Override
                public Map<String, KbModule> parseInclude(String l)
                        throws KidlParseException {
                    throw new KidlParseException("Includes are not supported: " + l);
                }  
            };
            File srvcDir = new File(repoDir, "service");
            srvcDir.mkdirs();
            String implName = moduleName + "Impl";
            File implFile = new File(srvcDir, implName + ".py");
            List<String> lines = new ArrayList<String>();
            lines.addAll(Arrays.asList(
                    "#BEGIN_HEADER",
                    "#END_HEADER",
                    "    #BEGIN_CLASS_HEADER",
                    "    #END_CLASS_HEADER",
                    "        #BEGIN_CONSTRUCTOR",
                    "        #END_CONSTRUCTOR",
                    "        #BEGIN " + methodName));
            for (String l : TextUtils.lines(pyhtonCode))
                lines.add("        " + l);
            lines.add("        #END " + methodName);
            TextUtils.writeLines(lines, implFile);
            String srvrName = moduleName + "Server";
            //File srvrFile = new File(srvcDir, srvrName + ".py");
            RunCompileCommand.generate(new StringReader(specFile.toString()), null, false, 
                    null, false, null, false, null, null, null, false, false, null, true, 
                    srvrName, implName, false, false, null, null, null, null, null, true, 
                    ip, new DiskFileSaver(srvcDir), null, true);
            File kbaseFile = new File(repoDir, "kbase.yml");
            TextUtils.writeLines(Arrays.asList(
                    "module-name:",
                    "    GenomeFeatureComparator",
                    "module-description:",
                    "    KBase module for comparing feature calls of two microbial genomes.",
                    "service-language:",
                    "    python",
                    "owners:",
                    "    [" + userId + "]"), kbaseFile);
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("module_name", moduleName);
            context.put("docker_commands", dockerCommands == null ? "" : dockerCommands);
            File dockerFile = new File(repoDir, "Dockerfile");
            formatTemplate("dockerfile", context, dockerFile);
            File makeFile = new File(srvcDir, "Makefile");
            formatTemplate("makefile", context, makeFile);
        } catch (NarrativeMethodStoreException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException(ex);
        }
    }
    
    public static boolean formatTemplate(String templateName, Map<?,?> context, 
            File output) throws NarrativeMethodStoreException {
        FileWriter fw = null;
        try {
            fw = new FileWriter(output);
            Reader input = new InputStreamReader(PySrvRepoPreparator.class.getResourceAsStream(
                    "py_srv_" + templateName + ".vm.properties"), Charset.forName("utf-8"));
            VelocityContext cntx = new VelocityContext(context);
            boolean ret = Velocity.evaluate(cntx, fw, "Template " + templateName, input);
            input.close();
            return ret;
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException("Problems with template evaluation (" + 
                    templateName + ")", ex);
        } finally {
            if (fw != null)
                try {
                    fw.close();
                } catch (Exception ignore) {}
        }
    }
}
