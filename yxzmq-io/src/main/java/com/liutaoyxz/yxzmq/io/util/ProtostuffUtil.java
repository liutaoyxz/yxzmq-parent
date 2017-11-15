package com.liutaoyxz.yxzmq.io.util;

import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by liutao on 2017/11/6.
 */
public class ProtostuffUtil {

    private static final ConcurrentHashMap<String, Class> classNameToClass = new ConcurrentHashMap<String, Class>();

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtostuffUtil.class);

    /**
     * 反序列化
     *
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T get(byte[] bytes, Class<T> clazz) {

        T obj = null;
        try {
            obj = clazz.newInstance();
            Schema schema = RuntimeSchema.getSchema(obj.getClass());
            ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            LOGGER.error("deSerializable error ", e);
        }
        LOGGER.debug("get obj,obj is {}", obj);
        return obj;
    }

    /**
     * 反序列化
     */
    public static Object get(byte[] bytes, String className) {

        Object obj = null;
        try {

            Class clazz = classNameToClass.get(className);
            if (clazz == null) {
                clazz = Class.forName(className);
                classNameToClass.putIfAbsent(className, clazz);
            }
            obj = clazz.newInstance();
            Schema schema = RuntimeSchema.getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            LOGGER.error("deSerializable error ", e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("error className : {} ", className);
        }
        LOGGER.debug("get obj,obj is {}", obj);
        return obj;
    }


    /**
     * 序列化
     *
     * @return
     */
    public static <T> byte[] serializable(T obj) {
        Schema schema = RuntimeSchema.getSchema(obj.getClass());
        return ProtobufIOUtil.toByteArray(obj, schema, LinkedBuffer.allocate(256));
    }

    /**
     * 获取metadata 序列化bytes 长度
     *
     * @param bytes
     * @return
     */
    public static long getMetadataLength(byte[] bytes) {
        long length = new Long(new String(bytes, Charset.forName("utf-8")));
        return length;
    }

    public static String fillMetadataLength(Integer length) {
        String lstr = length.toString();
        int l = lstr.length();
        switch (l) {
            case 0:
                return FillStr.TEN+lstr;
            case 1:
                return FillStr.NINE+lstr;
            case 2:
                return FillStr.EIGHT+lstr;
            case 3:
                return FillStr.SEVEN+lstr;
            case 4:
                return FillStr.SIX+lstr;
            case 5:
                return FillStr.FIVE+lstr;
            case 6:
                return FillStr.FOUR+lstr;
            case 7:
                return FillStr.THREE+lstr;
            case 8:
                return FillStr.TWO+lstr;
            case 9:
                return FillStr.ONE+lstr;
            case 10:
                return lstr;
            default:
                return FillStr.TEN;
        }

    }

    static class FillStr {

        static String ONE = "0";
        static String TWO = "00";
        static String THREE = "000";
        static String FOUR = "0000";
        static String FIVE = "00000";
        static String SIX = "000000";
        static String SEVEN = "0000000";
        static String EIGHT = "00000000";
        static String NINE = "000000000";
        static String TEN = "0000000000";


    }


}
