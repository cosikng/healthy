package com.example.healthy;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "queryServlet", value = "/query-servlet")
public class HelloServlet extends HttpServlet {
    private Connection conn, person_ver;
    private HashMap<String, String> tables;

    public void init() {
        tables = new HashMap<>();
        conn = JdbcConnection.getConnection("jdbc:postgresql://www.cosikng.top:5432/data", "postgres", "2333");
        person_ver = JdbcConnection.getConnection("jdbc:postgresql://www.cosikng.top:5432/info", "postgres", "2333");
        Block.init(conn);
        System.out.println("查询模块初始化完成");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("Query");

        boolean isHere = false;

        String id_number = request.getParameter("id_number");
        Info info = null;

        String randText = tables.get(id_number);
        String sig = request.getParameter("sig").replace(" ", "+");
        if (randText == null || sig.equals("")) {
            Random random = new Random();
            randText = String.valueOf(random.nextLong());
            tables.put(id_number, randText);
            String message = "请对待签名的数据进行签名。";
            info = new Info();
            info.setRandText(randText);
            info.setMessage_box(message);
        } else {
            String publicKey = null;
            try {
                ResultSet r = person_ver.prepareStatement(String.format("select * from id_verify where id='%s'", id_number)).executeQuery();
                if (r.next()) {
                    isHere = true;
                    publicKey = r.getString("publickey");
                }
            } catch (Exception throwables) {
                throwables.printStackTrace();
            }
            if (publicKey == null || publicKey.equals("")) {
                System.out.println("数据库错误");
                info = new Info();
                if (isHere)
                    info.setMessage_box("请稍后重试");
                else
                    info.setMessage_box("您还未注册");
                info.setRandText("");
            } else {
                boolean verified = false;
                try {
                    verified = sign_verify.verifySignature(randText, sig, publicKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (verified) {
                    tables.remove(id_number);
                    if (conn == null) {
                        System.out.println("连接数据库失败");
                    } else {
                        try {
                            ResultSet r = conn.prepareStatement(String.format("select * from persons where id='%s'", id_number)).executeQuery();
                            info = new Info();
                            if (r.next()) {
                                info.setBlood_sugar(r.getString("sugar_blood"));
                                info.setHeight(r.getString("height"));
                                info.setHeart_rate(r.getString("heart_rate"));
                                info.setHigh_pre(r.getString("high_pre"));
                                info.setLow_pre(r.getString("low_pre"));
                                info.setTemp(r.getString("tmp"));
                                info.setWeight(r.getString("weight"));
                                info.setMessage_box("查询成功");
                            }
                            else info.setMessage_box("未查到相关信息");
                            info.setRandText("");
                        } catch (Exception throwables) {
                            throwables.printStackTrace();
                        }
                    }
                } else {
                    info = new Info();
                    info.setMessage_box("签名验证失败");
                }
            }

        }


        String json = "{}";

        if (info != null) {
            json = JSON.objToJsonString(info);
        }
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(json.trim());
        out.flush();
        out.close();
    }

    public void destroy() {
        try {
            conn.close();
            person_ver.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}