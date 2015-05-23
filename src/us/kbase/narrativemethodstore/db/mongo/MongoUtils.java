package us.kbase.narrativemethodstore.db.mongo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jongo.MongoCollection;

import us.kbase.narrativemethodstore.exceptions.NarrativeMethodStoreException;

import com.google.common.collect.Lists;

public class MongoUtils {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> List<T> getProjection(MongoCollection infos,
            String whereCondition, String selectField, Class<T> type, Object... params)
            throws NarrativeMethodStoreException {
        List<Map> data = Lists.newArrayList(infos.find(whereCondition, params).projection(
                "{" + selectField + ":1}").as(Map.class));
        List<T> ret = new ArrayList<T>();
        for (Map<?,?> item : data) {
            Object value = item.get(selectField);
            if (value == null || !(type.isInstance(value)))
                throw new NarrativeMethodStoreException("Value is wrong: " + value);
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
            if (value == null || !(valueType.isInstance(value)))
                throw new NarrativeMethodStoreException("Value is wrong: " + value);
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

}
