package us.kbase.narrativemethodstore.db.mongo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.BSONObject;
import org.bson.LazyBSONList;
import org.bson.types.BasicBSONList;
import org.jongo.MongoCollection;

import us.kbase.common.service.UObject;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoUtils {
    private static final String HEXES = "0123456789abcdef";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @SuppressWarnings({ "unchecked" })
    public static <T> List<T> getProjection(
            final DBCollection infos,
            final DBObject whereCondition,
            final String selectField,
            final Class<T> type)
            throws NarrativeMethodStoreException {
        final DBCursor cur = infos.find(whereCondition, new BasicDBObject(selectField, 1));
        final List<Map<String, Object>> data = new LinkedList<>();
        for (final DBObject dbo: cur) {
            data.add(toMap(dbo));
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <KT, VT> Map<KT, VT> getProjection(MongoCollection infos, String whereCondition, 
            String keySelectField, Class<KT> keyType, String valueSelectField, Class<VT> valueType, 
            Object... params) throws NarrativeMethodStoreException {
        List<Map> data = Lists.newArrayList(infos.find(whereCondition, params).projection(
                "{'" + keySelectField + "':1,'" + valueSelectField + "':1}").as(Map.class));
        Map<KT, VT> ret = new LinkedHashMap<KT, VT>();
        for (Map<?,?> item : data) {
            Object key = getMongoProp(item, keySelectField);
            if (key == null || !(keyType.isInstance(key)))
                throw new NarrativeMethodStoreException("Key is wrong: " + key);
            Object value = getMongoProp(item, valueSelectField);
            if (value == null)
                throw new NullPointerException("Value is not defined for selected " +
                        "field: " + valueSelectField);
            if (!valueType.isInstance(value))
                value = UObject.transformObjectToObject(value, valueType);
            ret.put((KT)key, (VT)value);
        }
        return ret;
    }
    
    private static Object getMongoProp(Map<?,?> data, String propWithDots) {
        String[] parts = propWithDots.split(Pattern.quote("."));
        Object value = null;
        for (String part : parts) {
            if (value != null) {
                data = (Map<?,?>)value;
            }
            value = data.get(part);
        }
        return value;
    }

    /** Map an object to a MongoDB {@link DBObject}. The object must be serializable by
     * a default {@link ObjectMapper}.
     * @param obj the object to map.
     * @return the new mongo compatible object.
     */
    public static DBObject toDBObject(final Object obj) {
        return new BasicDBObject(objToMap(obj));
    }
    
    private static Map<String, Object> objToMap(final Object obj) {
        return MAPPER.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }
    
    /** Map a MongoDB {@link BSONObject} to a standard map.
     * This method expects that all maps and lists in the objects are implemented as
     * {@link BSONObject}s or derived classes, not standard maps, lists, or other classes.
     * @param dbo the MongoDB object to transform to a standard map.
     * @return the transformed object, or null if the argument was null.
     */
    public static Map<String, Object> toMap(final BSONObject dbo) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> ret = (Map<String, Object>) cleanObject(dbo);
        return ret;
    }
    
    // this assumes there aren't BSONObjects embedded in standard objects, which should
    // be the case for stuff returned from mongo
    
    // Unimplemented error for dbo.toMap()
    // dbo is read only
    // can't call ObjectMapper.convertValue() on dbo since it has a 'size' field outside of
    // the internal map
    // and just weird shit happens when you do anyway
    private static Object cleanObject(final Object dbo) {
        // sometimes it's lazy, sometimes it's basic. Not sure when or why.
        if (dbo instanceof LazyBSONList) {
            final List<Object> ret = new LinkedList<>();
            // don't stream, sometimes has issues with nulls
            for (final Object obj: (LazyBSONList) dbo) {
                ret.add(cleanObject(obj));
            }
            return ret;
        } else if (dbo instanceof BasicBSONList) {
            final List<Object> ret = new LinkedList<>();
            // don't stream, sometimes has issues with nulls
            for (final Object obj: (BasicBSONList) dbo) {
                ret.add(cleanObject(obj));
            }
        } else if (dbo instanceof BSONObject) {
            // can't stream because streams don't like null values at HashMap.merge()
            final BSONObject m = (BSONObject) dbo;
            final Map<String, Object> ret = new HashMap<>();
            for (final String k: m.keySet()) {
                if (!k.equals("_id")) {
                    final Object v = m.get(k);
                    ret.put(k, cleanObject(v));
                }
            }
            return ret;
        }
        return dbo;
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
