package us.kbase.narrativemethodstore.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextUtils {
    private static Pattern wsDiv = Pattern.compile("\\s+");
    
    public static String text(URL url) throws IOException {
        return text(url.openStream());
    }
    
    public static String text(File f) throws IOException {
        return text(new FileInputStream(f));
    }
    
    public static String text(InputStream is) throws IOException {
        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = in.readLine()) != null) 
            response.append(line+"\n");
        in.close();
        return response.toString();
    }

    public static List<String> lines(URL url) throws IOException {
        return lines(url.openStream());
    }

    public static List<String> lines(File f) throws IOException {
        return lines(new FileInputStream(f));
    }

    public static List<String> lines(String text) {
        try {
            return lines(new StringReader(text));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static List<String> lines(InputStream is) throws IOException {
        return lines(new InputStreamReader(is));
    }
    
    public static List<String> lines(Reader r) throws IOException {
        List<String> ret = new ArrayList<String>();
        BufferedReader in = new BufferedReader(r);
        String line;
        while ((line = in.readLine()) != null) 
            ret.add(line);
        in.close();
        return ret;
    }
    
    public static String[] splitByWhiteSpaces(String line) {
        return wsDiv.split(line.trim());
    }

    public static void writeLines(List<String> lines, File targetFile) throws IOException {
        PrintWriter pw = new PrintWriter(targetFile);
        for (String l : lines)
            pw.println(l);
        pw.close();
    }
    
    public static List<String> grep(List<String> lines, String substring) {
        List<String> ret = new ArrayList<String>();
        for (String l : lines)
            if (l.contains(substring))
                ret.add(l);
        return ret;
    }
}
