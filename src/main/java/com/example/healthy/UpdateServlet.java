package com.example.healthy;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;


@WebServlet(name = "updateServlet", value = "/update-servlet")
public class UpdateServlet extends HttpServlet {
    private Connection conn, person_ver;
    private HashMap<String, String> tables;

    public void init() {
        tables = new HashMap<>();
        conn = JdbcConnection.getConnection("jdbc:postgresql://www.cosikng.top:5432/data", "postgres", "2333");
        person_ver = JdbcConnection.getConnection("jdbc:postgresql://www.cosikng.top:5432/info", "postgres", "2333");
        Block.init(conn);
        System.out.println("更新模块初始化完成");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("Update");

        boolean isHere = false;

        String id_number = request.getParameter("id_number");
        Info info = new Info();
        String sql = "";
        String data = "";

        String randText = tables.get(id_number);
        String sig = request.getParameter("sig").replace(" ", "+");
        if (randText == null || sig.equals("")) {
            Random random = new Random();
            randText = String.valueOf(random.nextLong());
            try {
                ResultSet r = conn.prepareStatement(String.format("select * from persons where id='%s'", id_number)).executeQuery();
                if (r.next()) {//数据库已有该条记录
                    sql = String.format("update persons set height='%s',weight='%s',tmp='%s',heart_rate='%s',high_pre='%s',low_pre='%s',sugar_blood='%s' where id='%s'", request.getParameter("height"), request
                                    .getParameter("weight"), request.getParameter("tmp"), request.getParameter("heart_rate"), request.getParameter("high_pre"), request.getParameter("low_pre"),
                            request.getParameter("blood_sugar"), id_number);
                } else {//数据库尚未录入这条信息
                    sql = String.format("insert into persons values ('%s','%s','%s','%s','%s','%s','%s','%s')", id_number, request.getParameter("height"), request
                                    .getParameter("weight"), request.getParameter("tmp"), request.getParameter("heart_rate"), request.getParameter("high_pre"), request.getParameter("low_pre"),
                            request.getParameter("blood_sugar"));
                }
            } catch (Exception throwables) {
                throwables.printStackTrace();
            }
            String message = "请对待签名的数据进行签名。";
            randText += sql;
            randText = byte2Hex(randText.getBytes(StandardCharsets.UTF_8));
            tables.put(id_number, randText);
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
                if (isHere)
                    info.setMessage_box("请稍后重试");
                else
                    info.setMessage_box("您还未注册");
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
                            boolean isSucceeded = false;
                            if (r.next()) {//数据库已有该条记录
                                sql = String.format("update persons set height='%s',weight='%s',tmp='%s',heart_rate='%s',high_pre='%s',low_pre='%s',sugar_blood='%s' where id='%s'", request.getParameter("height"), request
                                                .getParameter("weight"), request.getParameter("tmp"), request.getParameter("heart_rate"), request.getParameter("high_pre"), request.getParameter("low_pre"),
                                        request.getParameter("blood_sugar"), id_number);

                                if (!conn.prepareStatement(sql).execute()) {
                                    info.message_box = "更新成功";
                                    isSucceeded = true;
                                } else info.message_box = "更新失败";
                            } else {//数据库尚未录入这条信息
                                sql = String.format("insert into persons values ('%s','%s','%s','%s','%s','%s','%s','%s')", id_number, request.getParameter("height"), request
                                                .getParameter("weight"), request.getParameter("tmp"), request.getParameter("heart_rate"), request.getParameter("high_pre"), request.getParameter("low_pre"),
                                        request.getParameter("blood_sugar"));

                                if (!conn.prepareStatement(sql).execute()) {
                                    info.message_box = "更新成功";
                                    isSucceeded = true;
                                } else info.message_box = "更新失败";
                            }
                            if (isSucceeded) {
                                HashMap<String, String> v = new HashMap<>();
                                v.put("ID", id_number);
                                v.put("PublicKey", publicKey);
                                v.put("Signature", sig);
                                Block.addToChains(new Date().getTime(), sql, v);
                            }
                        } catch (Exception throwables) {
                            throwables.printStackTrace();
                        }
                    }
                } else {
                    info.message_box = "签名验证失败";
                }
            }

            if (info.randText == null) {
                info.randText = "";
            }


        }
        String json = "{}";
        json = JSON.objToJsonString(info);
        System.out.println(json);
        response.setContentType("text/html;charset=utf-8");
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

    private static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        String temp = null;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                // 1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    private static byte[] hexToBytes(String hex) {
        hex = hex.length() % 2 != 0 ? "0" + hex : hex;

        byte[] b = new byte[hex.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
}