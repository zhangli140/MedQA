package org.zjuwangg.server;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zjuwangg.nlp.NlpirProcess;
import org.zjuwangg.tools.FileOperator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/5/4.
 */
class NumericThread implements Runnable{
    private Socket client = null;

    public NumericThread(Socket client) {
        this.client = client;
    }

    public static List<Double> getaccsnsp(String filename,double dec_per){
        System.out.println(filename);
        List<String> data = FileOperator.readFileByLines(filename);
        List<Double> label = new ArrayList<Double>();
        List<Double> dec_value = new ArrayList<Double>();
        List<Double> plabel = new ArrayList<Double>();
        List<Double> result = new ArrayList<Double>();
        for(String line : data){
            label.add(Double.parseDouble(line.split(",")[0]));
            dec_value.add(Double.parseDouble(line.split(",")[1]));
        }
        double max = Collections.max(dec_value);
        double min = Collections.min(dec_value);
        double dec_val = (max-min)*dec_per+min;
        for(int i=0;i<dec_value.size();i++){
            if(dec_value.get(i) > dec_val){
                plabel.add(1.0);
            }
            else{
                plabel.add(-1.0);
            }
        }
//        for(int i=0;i<dec_value.size();i++){
//            if(dec_value.get(i)>3) {
//                System.out.print(label.get(i));
//                System.out.print(" ");
//                System.out.print(plabel.get(i));
//                System.out.print(" ");
//                System.out.println(dec_value.get(i));
//            }
//        }
        double patient = 0;
        double nopatient = 0;
        double sn = 0;
        double sp = 0;
        double acc = 0;
        for(int i=0;i<dec_value.size();i++){
            if(label.get(i)==1.0){
                patient++;
            }
            else{
                nopatient++;
            }
            if(label.get(i)+plabel.get(i)==2.0){
                sn++;
            }
            else if(label.get(i)+plabel.get(i)==-2.0){
                sp++;
            }
        }
        acc = sn+sp;
        result.add(acc/(patient+nopatient));
        result.add(sn/(patient));
        result.add(sp/(nopatient));
        return result;
    }

    public static void main(String args[]){
        List<Double> result = getaccsnsp("D:\\code\\Design\\resources\\肝癌.csv",0.3);
        for(double number:result){
            System.out.println(number);
        }
    }

    /**
     * 返回请求的json格式应答
     *
     * @param content
     * @return
     */
    public static String response(String content) {
        String[] arg = content.split("\n");
        String ill = arg[0];
        Double dec_per = Double.parseDouble(arg[1]);
        JSONObject object = new JSONObject();
        //*
        List<Double> result = getaccsnsp("D:\\code\\Design\\resources\\"+ill+".csv",dec_per);
        object.put("acc", result.get(0));
        object.put("sn", result.get(1));
        object.put("sp", result.get(2));
        object.put("des",ill);
        //System.out.println(tSentence);
        //System.out.println(ms.toString());
        return object.toString();
    }

    public static void execute(Socket client) {
        //System.out.println(client.getRemoteSocketAddress());
        if (client.isClosed() || client == null)
            return;
        try {
            PrintStream out = new PrintStream(client.getOutputStream());
            BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()));
            boolean flag = true;
            StringBuffer sbf = new StringBuffer();
            while (flag) {
                String str = buf.readLine();
                //System.out.println(str);
                if (str.equals("####"))
                    break;
                else
                    sbf.append(str+"\n");
            }
            System.out.print(sbf.toString());
            out.println(response(sbf.toString()));
            out.close();
            buf.close();
            client.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Finished!");
    }

    public void run() {
        execute(client);
    }
}

public class Numeric {
    public static void Server() {
        try {
            ServerSocket serverSocket = new ServerSocket(20024);
            ThreadPoolExecutor tPool = new ThreadPoolExecutor(5, 8, 2, TimeUnit.HOURS, new LinkedBlockingDeque<Runnable>());
            while (true) {
                Socket client = serverSocket.accept();
                tPool.submit(new NumericThread(client));
            }
        } catch (Exception e) {
            e.printStackTrace();
            NlpirProcess.exit();
        }
    }

    public static void main(String[] args) {
        Server();

    }
}
