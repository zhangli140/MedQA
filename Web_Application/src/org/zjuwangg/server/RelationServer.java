package org.zjuwangg.server;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zjuwangg.mysql.MySql;
import org.zjuwangg.nlp.NlpirProcess;
import org.zjuwangg.pattern.Normalize;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by wanggang on 2015/5/31.
 */
class ServerThread1 implements Runnable {
    public static final int OTHER_CODE = 0;
    public static final int ND_CODE = 1;
    public static final int NJ_CODE = 2;
    public static final int NM_CODE = 3;
    public static final int NW_CODE = 4;
    public static final int NB_CODE = 5;

    private Socket client = null;

    public ServerThread1(Socket client) {
        this.client = client;
    }

    public static int judge(String item) {
        if (item.endsWith("/nd"))
            return ND_CODE;
        else if (item.endsWith("/nj"))
            return NJ_CODE;
        else if (item.endsWith("/nb"))
            return NB_CODE;
        else if (item.endsWith("/nm"))
            return NM_CODE;
        else if (item.contains("/n"))
            return NW_CODE;
        else
            return OTHER_CODE;
    }

    public static String preProcess(String scontent, Vector<String> vnd, Vector<String> vnj, Vector<String> vnm, Vector<String> vnb) {
        StringBuffer sb = new StringBuffer();
        String[] items = scontent.split("\\s+");
        for (String item : items) {
            String[] t = item.split("/");
            String oitem = "";
            if (t.length != 0)
                oitem = t[0];
            else
                continue;
            switch (judge(item)) {
                case OTHER_CODE:
                    sb.append(oitem);
                    break;
                case ND_CODE:
                    sb.append("<a href='#' title='属性：疾病&#13;'><font color='red'> " + oitem + " </font></a>");
                    vnd.add(oitem);
                    break;
                case NJ_CODE:
                    sb.append("<a href='#' title='属性：症状&#13;'><font color='orange'> " + oitem + " </font></a>");
                    vnj.add(oitem);
                    break;
                case NM_CODE:
                    sb.append("<a href='#' title='属性：药物&#13;'><font color='green'> " + oitem + " </font></a>");
                    vnm.add(oitem);
                    break;
                case NB_CODE:
                    sb.append("<a href='#' title='属性：部位&#13;'><font color='blue'> " + oitem + " </font></a>");
                    vnb.add(oitem);
                    break;
                case NW_CODE:
                    sb.append(oitem);
                    break;
                default:
                    break;
            }
        }
        return sb.toString();
    }

    public static void apiDMBJ(Vector<String> vnd, Vector<String> vnj, Vector<String> vnb, Vector<String> vnm,
                               JSONArray ds, JSONArray ss, JSONArray bs, JSONArray ms, JSONArray resArray) {
        Set<String> flagSet = new HashSet<String>();
        MySql sqlTool = new MySql();
        sqlTool.connect();
        try {
            Statement st = sqlTool.getConnection().createStatement();
            //ds
            for (String ditem : vnd) {
                String disease = Normalize.getDisease(ditem);
                if (flagSet.contains(disease))
                    continue;
                flagSet.add(disease);
                String query = "Select Disease,Aliases,RelatedSymptoms,BriefIntroduction from jbk39disease where Disease like '%" + disease + "%'";
                ResultSet resultSet = st.executeQuery(query);
                while (resultSet.next()) {
                    String name = resultSet.getString(1);
                    String relatedSymptom = resultSet.getString(3);
                    String[] rss = relatedSymptom.split(",");
                    for (String rs : rss) {
                        if(rs.length()<3)
                            continue;
                        JSONObject obj = new JSONObject();
                        obj.put("source", name);
                        obj.put("target", rs);
                        obj.put("type", "suit");
                        ds.put(obj);
                        resArray.put(obj);
                    }
                }
                resultSet.close();
            }
            //ss
            flagSet.clear();
            for (String sitem : vnj) {
                String symptom = Normalize.getSymptom(sitem);
                if (flagSet.contains(symptom))
                    continue;
                flagSet.add(sitem);
                String query = "Select Symptom,Aliases,RelatedDisease,BriefIntroduction from jbk39symptom where Symptom like '%" + symptom + "%'";
                ResultSet resultSet = st.executeQuery(query);
                while (resultSet.next()) {
                    String name = resultSet.getString(1);
                    String relatedSymptom = resultSet.getString(3);
                    for (String rs : relatedSymptom.split(",")) {
                        JSONObject obj = new JSONObject();
                        if(rs.length()<3)
                            continue;
                        obj.put("source", name);
                        obj.put("target", rs);
                        obj.put("type", "resolved");
                        ss.put(obj);
                        resArray.put(obj);
                    }
                }
                resultSet.close();
            }
            //bs
            flagSet.clear();
            for (String bitem : vnb) {
                if (flagSet.contains(bitem))
                    continue;
                flagSet.add(bitem);
                String query = "Select Body,Disease,Symptom,Sentence from bodydisease where Body like '%" + bitem + "%' limit 0,5";
                ResultSet resultSet = st.executeQuery(query);
                while (resultSet.next()) {
                    String name = resultSet.getString(1);
                    String disease = resultSet.getString(2);
                    String symptom = resultSet.getString(3);
                    for (String s : disease.split("#")) {
                        if(s.length()<3)
                            continue;
                        JSONObject obj = new JSONObject();
                        obj.put("source", name);
                        obj.put("target", s);
                        obj.put("type", "licensing");
                        bs.put(obj);
                        resArray.put(obj);
                    }
                    for (String s : symptom.split("#")) {
                        JSONObject obj = new JSONObject();
                        obj.put("source", name);
                        obj.put("target", s);
                        obj.put("type", "suit");
                        bs.put(obj);
                        resArray.put(obj);
                    }
                }
                resultSet.close();
            }
            //ms
            flagSet.clear();
            for (String mitem : vnm) {
                if (flagSet.contains(mitem))
                    continue;
                if (Normalize.diseaseNorm.containsKey(mitem))
                    continue;
                flagSet.add(mitem);
                String query = "Select Medicine,Disease,Symptom,Sentence from caredisease where Medicine like '%" + mitem + "%' limit 0,5";
                ResultSet resultSet = st.executeQuery(query);
                while (resultSet.next()) {
                    String medicine = resultSet.getString(1);
                    String disease = resultSet.getString(2);
                    String symptom = resultSet.getString(3);
                    for (String s : disease.split("#")) {
                        if(s.length()<3)
                            continue;
                        JSONObject obj = new JSONObject();
                        obj.put("source", medicine);
                        obj.put("target", s);
                        obj.put("type", "licensing");
                        ms.put(obj);
                        resArray.put(obj);
                    }
                    for (String s : symptom.split("#")) {
                        JSONObject obj = new JSONObject();
                        obj.put("source", medicine);
                        obj.put("target", s);
                        obj.put("type", "suit");
                        ms.put(obj);
                        resArray.put(obj);
                    }
                }
                resultSet.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sqlTool.closeConnection();
    }


    /**
     * 返回请求的json格式应答
     *
     * @param content
     * @return
     */
    public static String response(String content) {
        String scontent = NlpirProcess.spilt(content);
//        JSONObject object = new JSONObject();
        JSONArray resArray = new JSONArray();
        //*
        JSONArray ds = new JSONArray();
        JSONArray ss = new JSONArray();
        JSONArray bs = new JSONArray();
        JSONArray ms = new JSONArray();
        Vector<String> vnd = new Vector<String>();
        Vector<String> vnj = new Vector<String>();
        Vector<String> vnm = new Vector<String>();
        Vector<String> vnb = new Vector<String>();

        preProcess(scontent, vnd, vnj, vnm, vnb);
        apiDMBJ(vnd, vnj, vnb, vnm, ds, ss, bs, ms, resArray);

//        System.out.println(tSentence);

        System.out.println(resArray);
        return resArray.toString();
    }

    public static void execute(Socket client) {
        System.out.println(client.getRemoteSocketAddress());
        if (client.isClosed() || client == null)
            return;
        try {
            PrintStream out = new PrintStream(client.getOutputStream());
            BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()));
            boolean flag = true;
            StringBuffer sbf = new StringBuffer();
            while (flag) {
                String str = buf.readLine();
//                System.out.println(str);
                if (str.equals("####"))
                    break;
                else
                    sbf.append(str + "\n");
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

public class RelationServer {
    public static void Server() {
        try {
            NlpirProcess.init();
            ServerSocket serverSocket = new ServerSocket(20023);
            ThreadPoolExecutor tPool = new ThreadPoolExecutor(5, 8, 2, TimeUnit.HOURS, new LinkedBlockingDeque<Runnable>());
            while (true) {
                Socket client = serverSocket.accept();
                tPool.submit(new ServerThread1(client));
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
