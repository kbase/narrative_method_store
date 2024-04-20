package us.kbase.narrativemethodstore.db.mongo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class emulates output stream in order to compare data written into 
 * this stream with data provided by separate input stream.
 * @author rsutormin
 */
public class OutputComparatorStream extends OutputStream {
    private final InputStream is;
    private final byte[] buffer = new byte[10000];
    private final byte[] oneByteBuffer = new byte[1];
    private boolean isClosed = false;
    private boolean diff = false;
    
    public OutputComparatorStream(InputStream compareWith) {
        this.is = compareWith;
    }
    
    @Override
    public void write(int b) throws IOException {
        oneByteBuffer[0] = (byte)b;
        write(oneByteBuffer, 0, 1);
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (diff || len == 0)
            return;
        int pos = 0;
        while (pos < len) {
            int len2 = is.read(buffer, 0, Math.min(buffer.length, len - pos));
            if (len2 < 0) {
                diff = true;
                break;
            }
            if (len2 == 0)
                continue;
            for (int i = 0; i < len2; i++) {
                if (b[off + pos + i] != buffer[i]) {
                    diff = true;
                    break;
                }
            }
            if (diff)
                break;
            pos += len2;
        }
    }
    
    @Override
    public void close() throws IOException {
        if (isClosed)
            return;
        if (!diff) {
            while (true) {
                int len2 = is.read(buffer, 0, 1); 
                if (len2 == 0)
                    continue;
                if (len2 > 0)
                    diff = true;
                break;
            }
        }
        isClosed = true;
    }
    
    public boolean isDifferent() {
        try {
            close();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return diff;
    }
}
