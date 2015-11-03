package us.kbase.narrativemethodstore.db.mongo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jongo.MongoCollection;

import us.kbase.common.service.UObject;
import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

import com.google.common.collect.Lists;

public class MongoUtils {
    private static final String HEXES = "0123456789abcdef";

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> List<T> getProjection(MongoCollection infos,
            String whereCondition, String selectField, Class<T> type, Object... params)
            throws NarrativeMethodStoreException {
        List<Map> data = Lists.newArrayList(infos.find(whereCondition, params).projection(
                "{" + selectField + ":1}").as(Map.class));
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
