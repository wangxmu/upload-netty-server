package com.cnc.qoss.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import sun.applet.Main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * json工具类
 * Author: lifei
 * Email: lifei@chinanetcenter.com
 * Date: 2015/5/13
 * Description: JsonUtils
 */
public class JsonUtils {
    public static <T> T toBean(String json, Class<T> cls) {
        return JSON.parseObject(json, cls);
    }

    public static <T> List<T> toList(String json, Class<T> cls) {
        return JSON.parseArray(json, cls);
    }
    public static List<Map<String,Object>> toList(String json) {
        return JSON.parseObject(json, new TypeReference<List<Map<String, Object>>>() {
        });
    }

    public static Object toObject(String json) {
        return JSON.parse(json);
    }

    public static <T> T toType(String json, TypeReference<T> type) {
        return JSON.parseObject(json, type);
    }

    /**
     *
     * @param json
     * @param keyClass 不能为基本类型Integer,Long等
     * @param valueClass 不能为基本类型Integer,Long等
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> keyClass, Class<V> valueClass) {
        return JSON.parseObject(json,new TypeReference<Map<K, V>>() {});
    }
    public static Map<String, Object> toMap(String json) {
        return JSON.parseObject(json,new TypeReference<Map<String, Object>>() {});
    }

    public static String toString(Object object) {
        return JSON.toJSONString(object);
    }


}
