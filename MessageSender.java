package com.example.a5725finalproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MessageSender{


    static String ipAddress;
    static String port;
    private static MessageSender messageSender;

    public MessageSender(String ip, String pt){
        this.ipAddress = ip;
        this.port = pt;
        setMessageSender(this);
    }

    public static MessageSender getinstance() {
        return messageSender;
    }

    public void setMessageSender(MessageSender messageSender) {
        MessageSender.messageSender = messageSender;
    }

    public void updateConnectionInfo(String ip, String pt){
        this.ipAddress = ip;
        this.port = pt;
    }

    public void sendWithNoReply(int message){
        MessageTask messageTask = new MessageTask(ipAddress, port, message);
        new Thread(messageTask).start();
    }

}
class MessageTask implements Runnable{
    String ipAddress, port;
    int message;
    public MessageTask(String ip, String pt, int msg){
        ipAddress = ip;
        port = pt;
        message = msg;
    }
    @Override
    public void run() {
        try {
            Socket s = new Socket(ipAddress, Integer.parseInt(port));
            s.setTcpNoDelay(true);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            pw.println(message);
            pw.flush();
            s.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

