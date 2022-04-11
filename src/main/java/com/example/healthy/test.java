package com.example.healthy;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

public class test {

    public static void main(String[] args) {
//        /**
//         * 将目标目录封装成 File 对象。
//         */
//        File dir = new File("blocks/");
//
//        /**
//         * 获取目录下的所有文件和文件夹
//         */
//        String[] names = dir.list();
//        System.out.println(names==null);
//
//        for (String name : names) {
//            System.out.println(name);
//        }
//        Info info = new Info();
//        info.setMessage_box("你好");
//        String json = JSON.objToJsonString(info);
//        System.out.println(json);
        HashMap<String,String> h =new HashMap<>();
        h.put("1","123");
        h.put("1","789");
        h.put("1","456");
        System.out.println(h.get("1"));
    }

}