package us.kbase.narrativemethodstore.db.github;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import us.kbase.narrativemethodstore.MethodSpec;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

public class PySrvRepoPreparator {
    public static void prepare(String userId, String moduleName, MethodSpec methodSpec, 
            String pyhtonCode, String dockerCommands, File repoDir) throws NarrativeMethodStoreException {
        /*try {
            String methodId = methodSpec.getInfo().getId();
            StringBuilder specFile = new StringBuilder();
            specFile.append("module ").append(moduleName).append(" {\n");
            specFile.append("    async funcdef ").append(methodId).append(
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
                    "        #BEGIN " + methodId));
            for (String l : TextUtils.lines(pyhtonCode))
                lines.add("        " + l);
            lines.add("        #END " + methodId);
            TextUtils.writeLines(lines, implFile);
            String srvrName = moduleName + "Server";
            RunCompileCommand.generate(new StringReader(specFile.toString()), null, false, 
                    null, false, null, false, null, null, null, false, false, null, true, 
                    srvrName, implName, false, false, null, null, null, null, null, true, 
                    ip, new DiskFileSaver(srvcDir), null, true);
            File kbaseFile = new File(repoDir, "kbase.yml");
            String methodFullName = methodSpec.getInfo().getName() == null ? methodId :
                methodSpec.getInfo().getName();
            String methodDescr = methodSpec.getInfo().getTooltip() == null ? "" : 
                (" (" + methodSpec.getInfo().getTooltip() + ")");
            TextUtils.writeLines(Arrays.asList(
                    "module-name:",
                    "    " + moduleName,
                    "",
                    "module-description:",
                    "    Module with \"" + methodFullName + "\" function " + methodDescr + ".",
                    "",
                    "service-language:",
                    "    python",
                    "",        
                    "owners:",
                    "    [" + userId + "]"), kbaseFile);
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("module_name", moduleName);
            context.put("docker_commands", dockerCommands == null ? "" : dockerCommands);
            File dockerFile = new File(repoDir, "Dockerfile");
            formatTemplate("dockerfile", context, dockerFile);
            File makeFile = new File(srvcDir, "Makefile");
            formatTemplate("makefile", context, makeFile);
            List<String> displayData = new ArrayList<String>(Arrays.asList(
                    "name : " + methodFullName,
                    "",
                    "tooltip : |",
                    "    " + nonEmpty(methodSpec.getInfo().getTooltip()),
                    "",
                    "screenshots : []",
                    "",
                    "description : |",
                    "    " + nonEmpty(methodSpec.getInfo().getSubtitle()),
                    "",
                    "publications : []",
                    "",
                    "",
                    "parameters :"));
            Map<String, Object> specData = new LinkedHashMap<String, Object>();
            specData.put("ver", "1.0.0");
            specData.put("authors", Arrays.asList(userId));
            specData.put("contact", "help@kbase.us");
            specData.put("visible", true);
            specData.put("categories", Arrays.asList("active"));
            specData.put("widgets", new LinkedHashMap<String, Object>());
            List<Map<String, Object>> params = new ArrayList<Map<String, Object>>();
            specData.put("parameters", params);
            Map<String, Object> behavior = new LinkedHashMap<String, Object>();
            specData.put("behavior", behavior);
            Map<String, Object> serviceMapping = new LinkedHashMap<String, Object>();
            behavior.put("service-mapping", serviceMapping);
            serviceMapping.put("url", "async");
            serviceMapping.put("name", moduleName);
            serviceMapping.put("method", methodId);
            Map<String, Object> inputWsMapping = new LinkedHashMap<String, Object>();
            inputWsMapping.put("narrative_system_variable", "workspace");
            inputWsMapping.put("target_property", "workspaceName");
            List<Map<String, Object>> inputMappings = new ArrayList<Map<String, Object>>(
                    Arrays.asList(inputWsMapping));
            serviceMapping.put("input_mapping", inputMappings);
            Map<String, Object> outputMainMapping = new LinkedHashMap<String, Object>();
            outputMainMapping.put("service_method_output_path", new ArrayList<Object>());
            outputMainMapping.put("target_property", "/");
            List<Map<String, Object>> outputMappings = new ArrayList<Map<String, Object>>(
                    Arrays.asList(outputMainMapping));
            serviceMapping.put("output_mapping", outputMappings);
            List<String> defaultValue = Arrays.asList("");
            for (MethodParameter param : methodSpec.getParameters()) {
                String paramId = param.getId();
                displayData.addAll(Arrays.asList(
                        "    " + paramId + " :",
                        "        ui-name : |",
                        "            " + nonEmpty(param.getUiName(), paramId),
                        "        short-hint : |",
                        "            " + nonEmpty(param.getShortHint()),
                        ""));
                Map<String, Object> pm = new LinkedHashMap<String, Object>();
                pm.put("id", paramId);
                pm.put("optional", bool(param.getOptional(), false));
                pm.put("advanced", bool(param.getAdvanced(), false));
                pm.put("allow_multiple", bool(param.getAllowMultiple(), false));
                pm.put("default_values", notNull(param.getDefaultValues(), defaultValue));
                pm.put("field_type", notNull(param.getFieldType(), "text"));
                pm.put("text_options", param.getTextOptions());
                params.add(pm);
                Map<String, Object> mapping = new LinkedHashMap<String, Object>();
                mapping.put("input_parameter", paramId);
                mapping.put("target_property", paramId);
                inputMappings.add(mapping);
            }
            if (methodSpec.getParameters().size() == 0)
                displayData.add("    []");
            File methodDir = new File(repoDir, "ui/narrative/methods/" + methodId);
            methodDir.mkdirs();
            TextUtils.writeLines(displayData, new File(methodDir, "display.yaml"));
            UObject.getMapper().writerWithDefaultPrettyPrinter().writeValue(
                    new File(methodDir, "spec.json"), specData);
            TextUtils.writeLines(Arrays.asList(
                    "This repository was generated automatically by KBase registry ",
                    "as a service wrapper around Narrative python code."), 
                    new File(repoDir, "README.md"));
        } catch (NarrativeMethodStoreException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException(ex);
        }*/
    }

    private static Object notNull(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }
    
    private static boolean bool(Long value, boolean defaultValue) {
        return value == null ? defaultValue : ((long)value != 0L);
    }
    
    private static String nonEmpty(String text, String defaultText) {
        return nonEmpty(text == null || text.trim().isEmpty() ? defaultText : text);
    }
    
    private static String nonEmpty(String text) {
        return text == null || text.trim().isEmpty() ? "-" : text;
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
