package us.kbase.narrativemethodstore.db.mongo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;

import us.kbase.common.service.UObject;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoUtils {
    private static final String HEXES = "0123456789abcdef";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        final VisibilityChecker<?> checker = MAPPER.getSerializationConfig()
                .getDefaultVisibilityChecker();
        MAPPER.setVisibilityChecker(checker.withFieldVisibility(Visibility.ANY));
    }

    @SuppressWarnings({ "unchecked" })
    public static <T> List<T> getProjection(
            final MongoCollection<Document> infos,
            final Document whereCondition,
            final String selectField,
            final Class<T> type)
            throws NarrativeMethodStoreException {
        MongoCursor<Document> cursor = infos.find(whereCondition)
                .projection(Projections.include(selectField))
                .iterator();

        final List<Map<String, Object>> data = new LinkedList<>();
        while (cursor.hasNext()) {
            data.add(toMap(cursor.next()));
        }
        
        List<T> ret = new ArrayList<T>();
        for (Map<?,?> item : data) {
            Object value = item.get(selectField);
            if (value != null && !type.isInstance(value))
                value = UObject.transformObjectToObject(value, type);
            ret.add((T)value);
        }
        return ret;
    }

    /**
     * Map an object to a MongoDB {@link Document}. The object must be serializable by
     * an {@link ObjectMapper} configured so private fields are visible.
     *
     * @param obj the object to map.
     * @return the new mongo document.
     */
    public static Document toDocument(final Object obj) {
        return new Document(objToMap(obj));
    }
    
    private static Map<String, Object> objToMap(final Object obj) {
        return MAPPER.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }

    /** Map a MongoDB {@link Document} to a class.
     * @param doc the MongoDB document to transform.
     * @param clazz the class to which the object will be transformed.
     * @return the transformed object.
     */
    public static <T> T toObject(final Document doc, final Class<T> clazz) {
        return doc == null ? null : MAPPER.convertValue(doc, clazz);
    }
    
    /** Map a MongoDB {@link Document} to a standard map.
     * @param doc the MongoDB document to transform to a standard map.
     * @return the transformed object, or null if the argument was null.
     */
    public static Map<String, Object> toMap(final Document doc) {
        return doc;
    }

    public static String stringToHex(String text) {
        return byteToHex(text.getBytes(Charset.forName("utf-8")));
    }

    public static String byteToHex(byte[] bytes) {
        final StringBuilder hex = new StringBuilder(2 * bytes.length);
        for (final byte b : bytes)
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        return hex.toString();
    }

    public static String streamToHex(InputStream is) throws NarrativeMethodStoreException {
        byte[] buffer = new byte[10000];
        final StringBuilder hex = new StringBuilder();
        try {
            while (true) {
                int len = is.read(buffer);
                if (len < 0)
                    break;
                for (int i = 0; i < len; i++) {
                    byte b = buffer[i];
                    hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
                }
            }
        } catch (IOException ex) {
            throw new NarrativeMethodStoreException(ex);
        }
        return hex.toString();
    }

    public static String hexToString(String hex) {
        return new String(hexToBytes(hex), Charset.forName("utf-8"));
    }
    
    public static byte[] hexToBytes(String hex) {
        hex = hex.toLowerCase();
        byte[] ret = new byte[hex.length() / 2];
        for (int i = 0; i < ret.length; i++)
            ret[i] = (byte)Integer.parseInt(hex.substring(i * 2, (i + 1) * 2), 16);
        return ret;
    }
    
    public static String getMD5(InputStream is) throws NarrativeMethodStoreException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[10000];
            while (true) {
                int len = is.read(buffer);
                if (len < 0)
                    break;
                if (len == 0)
                    continue;
                digest.update(buffer, 0, len);
            }
            return byteToHex(digest.digest());
        } catch (Exception ex) {
            throw new NarrativeMethodStoreException(ex);
        }
    }
}
