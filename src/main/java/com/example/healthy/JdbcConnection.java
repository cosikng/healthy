package com.example.healthy;

import java.sql.*;

public class JdbcConnection {
    public static Connection getConnection(String url, String user, String password) {


        // 1. 加载Driver类，Driver类对象将自动被注册到DriverManager类中
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 2. 连接数据库，返回连接对象

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}