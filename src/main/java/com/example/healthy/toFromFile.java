package com.example.healthy;

import java.io.*;

public class toFromFile {
    public static int saveObjToFile(Object obj,String filename){
        File file = new File(filename);

        FileOutputStream out;

        try {
            out = new FileOutputStream(file);

            ObjectOutputStream objOut = new ObjectOutputStream(out);

            objOut.writeObject(obj);

            objOut.flush();

            objOut.close();

            return 0;

        } catch (IOException e) {

            e.printStackTrace();
            return -1;
        }
    }
    public static Object loadObjFromFile(String filename){
        Object temp = null;
        File file = new File(filename);

        FileInputStream in;

        try {
            in = new FileInputStream(file);

            ObjectInputStream objIn = new ObjectInputStream(in);

            temp = objIn.readObject();

            objIn.close();

            return temp;
        } catch (IOException e) {
            System.out.println("read object failed");

            e.printStackTrace();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        }
        return null;
    }
}
