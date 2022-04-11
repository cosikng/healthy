package com.example.healthy;

import java.lang.reflect.Field;

public class JSON {
    public static String objToJsonString(Object obj) {

        // 初始化返回值
        String json = "";

        if (obj == null) {
            return json;
        }

        StringBuilder buff = new StringBuilder();
        Field[] fields = obj.getClass().getFields();
        try {
            //buff.append("[");
            buff.append("{");
            int i = 0;
            for (Field field : fields) {
                if (i != 0) {
                    buff.append(",");
                }
                buff.append("\"").append(field.getName()).append("\"");
                buff.append(":");
                buff.append("\"");
                buff.append(field.get(obj) == null ? "" : field.get(obj));
                buff.append("\"");
                i++;
            }
            buff.append("}");
            //buff.append("]");
            json = buff.toString();
        } catch (Exception e) {
            throw new RuntimeException("cause:" + e.toString());
        }
        return json;
    }
}
