package com.example.healthy;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;


@WebServlet(name = "registerServlet", value = "/register-servlet")
public class RegisterServlet extends HttpServlet {
    private Connection conn;

    public void init() {
        conn = JdbcConnection.getConnection("jdbc:postgresql://www.cosikng.top:5432/info", "postgres", "2333");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("Register");

        String id_number = request.getParameter("id_number");
        String pkey = request.getParameter("publicKey");
        String data = "";
        String sql = "";
        System.out.println("身份证："+id_number+"\n公钥："+pkey);

        if (conn == null) {
            System.out.println("连接数据库失败");
        } else {
            try {
                ResultSet r = conn.prepareStatement(String.format("select * from id_verify where id='%s'", id_number)).executeQuery();
                if (r.next()) {//数据库已有该条记录
                    data="您输入的身份证号已与公钥："+r.getString("PublicKey")+"绑定，请检查身份证号，如需更改绑定请联系管理员。";
                } else {//数据库尚未录入这条信息
                    sql = String.format("insert into id_verify values ('%s','%s')", id_number, request.getParameter("publicKey"));

                    if (!conn.prepareStatement(sql).execute()) {
                        data = "注册成功";
                    } else data = "注册失败，请稍后再试";
                }
            } catch (Exception throwables) {
                throwables.printStackTrace();
            }
        }

        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(data.trim());
        out.flush();
        out.close();
    }

    public void destroy() {
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}