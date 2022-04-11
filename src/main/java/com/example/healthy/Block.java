package com.example.healthy;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Block implements Serializable {
    private long index;
    private long timestamp;
    private String data;
    private HashMap<String, String> verify;
    private int randNum;
    private String previous_hash;
    private String hash;
    public static Block last = null;

    Block(long index, long timestamp, String data, HashMap<String, String> verify, String previous_hash) {
        this.data = data;
        this.verify = verify;
        this.index = index;
        this.previous_hash = previous_hash;
        this.timestamp = timestamp;
        this.randNum = 0;
        this.hash = SHA256.getSHA256(data + index + timestamp + randNum + previous_hash);
        while (!(this.hash.substring(0, 4).equals("0000"))) {
            this.randNum++;
            this.hash = SHA256.getSHA256(data + index + verify.get("ID") + verify.get("PublicKey") + verify.get("SigNature") + timestamp + randNum + previous_hash);
        }
    }

    public static void addToChains(long timestamp, String data, HashMap<String, String> verify) {
        Block now;
        now = new Block(last.index + 1, timestamp, data, verify, last.hash);
        toFromFile.saveObjToFile(now, String.format("C:/blocks/block%d.dat", now.index));
        last = now;
    }

    public static void init(Connection conn) {
        if (last != null) return;  //已经进行过初始化
        File dir = new File("C:/blocks/");
        String[] names = dir.list();
        Boolean isDone = true;
        //读取区块文件失败，生成创世区块
        if (names.length == 0) {
            System.out.println("Hello");
            HashMap<String, String> v = new HashMap<>();
            v.put("ID", "");
            v.put("PublicKey", "");
            v.put("Signature", "");
            String createTable = "create table persons(ID varchar(50),height varchar(255),weight varchar(255),tmp varchar(255),heart_rate varchar(255),high_pre varchar(255),low_pre varchar(255),sugar_blood varchar(255));";
            try {
                conn.prepareStatement(createTable).execute();
            } catch (SQLException throwables) {
                isDone = false;
                System.out.println("数据库操作失败");
                throwables.printStackTrace();
            }
            //将对数据库建表的操作写入创世区块
            if (isDone) {
                Block.last = new Block(0, new Date().getTime(), createTable, v, "0");
                System.out.println("Hello");
                System.out.println(toFromFile.saveObjToFile(Block.last, String.format("C:/blocks/block%d.dat", 0)));
            }
        } else {
            Block.last = (Block) toFromFile.loadObjFromFile("C:/blocks/" + names[names.length - 1]);
        }
    }

    public long getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getData() {
        return data;
    }

    public String getPrevious_hash() {
        return previous_hash;
    }

    public String getHash() {
        return hash;
    }
}
