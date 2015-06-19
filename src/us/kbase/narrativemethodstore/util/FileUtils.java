package us.kbase.narrativemethodstore.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {

    public static File generateTempDir(File parentTempDir, String prefix, String suffix) {
        long start = System.currentTimeMillis();
        while (true) {
            File dir = new File(parentTempDir, prefix + start + suffix);
            if (!dir.exists()) {
                dir.mkdirs();
                return dir;
            }
            start++;
        }
    }

    public static void zip(File input, File outputZip) throws IOException {
        zip(input, new FileOutputStream(outputZip), false, null);
    }
    
    public static void zip(File input, OutputStream output, boolean withHidden,
            Set<String> excludeEntries) throws IOException {
        if (!input.exists())
            throw new IOException("File doesn't exist: " + input);
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(output);
            addToZip(input, "", zos, withHidden, excludeEntries);
        } finally {
            try {
                if (zos != null)
                    zos.close();
            } catch (Exception ignore) {}
        }
    }
    
    private static void addToZip(File input, String entryName, ZipOutputStream zos, 
            boolean withHidden, Set<String> excludeEntries) throws IOException {
        if (input.isHidden() && !withHidden)
            return;
        if (input.isDirectory()) {
            if (entryName.length() > 0)
                entryName += "/";
            for (File f : input.listFiles())
                addToZip(f, entryName + f.getName(), zos, withHidden,
                        excludeEntries);
        } else if (input.isFile()) {
            if (excludeEntries != null && excludeEntries.contains(entryName))
                return;
            FileInputStream fis = null;
            try {
                byte[] buffer = new byte[10000];
                fis = new FileInputStream(input);
                zos.putNextEntry(new ZipEntry(entryName));
                int length;
                while ((length = fis.read(buffer)) > 0)
                    zos.write(buffer, 0, length);
                zos.closeEntry();
            } finally {
                try {
                    if (fis != null)
                        fis.close();
                } catch (Exception ignore) {}
            }
        }
    }
}
