package com.hexmeet.hjt.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Map;

public class JsonUtil {
    
    public static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();
    

    public static final Type STRING_OBJECT_MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    private final static Gson gson = new Gson();
    
    public static String toJson(Object src){
        return gson.toJson(src);
    }
    
    public static <T> T toObject(String src, Class<T> clz){
        return gson.fromJson(src, clz);
    }
    
    public static <T> T toObject(ByteBuffer buf, Class<T> clz){
        InputStreamReader reader = new InputStreamReader(new BufferInputStream(buf));
        T result = gson.fromJson(reader, clz);
        try {reader.close();} catch (IOException e) {}
        return result;
    }
    
    public static <T> T toObject(InputStream in, Class<T> clz){
        InputStreamReader reader = new InputStreamReader(in);
        T result = gson.fromJson(reader, clz);
        try {reader.close();} catch (IOException e) {}
        return result;
    }
    
    public static <T> T toObject(InputStream in, Type clz){
        InputStreamReader reader = new InputStreamReader(in);
        T result = gson.fromJson(reader, clz);
        try {reader.close();} catch (IOException e) {}
        return result;
    }
    
    public static <T> T toObject(String src, Type clz){
        return gson.fromJson(src, clz);
    }
    
    public static <T> T toObject(ByteBuffer buf, Type clz){
        InputStreamReader reader = new InputStreamReader(new BufferInputStream(buf));
        T result = gson.fromJson(reader, clz);
        try {reader.close();} catch (IOException e) {}
        return result;
    }
}
