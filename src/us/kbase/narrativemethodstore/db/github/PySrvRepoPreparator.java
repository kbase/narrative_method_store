package us.kbase.narrativemethodstore.db.github;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import us.kbase.jkidl.IncludeProvider;
import us.kbase.kidl.KbModule;
import us.kbase.kidl.KidlParseException;
import us.kbase.mobu.compiler.RunCompileCommand;
import us.kbase.mobu.util.DiskFileSaver;
import us.kbase.mobu.util.FileSaver;
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
        } catch (NarrativeMethodStoreException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException(ex);
        }
    }
}
