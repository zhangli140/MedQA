package org.zjuwangg.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zjuwangg.mysql.MySql;
import org.zjuwangg.nlp.NlpirProcess;
import org.zjuwangg.pattern.Normalize;

public class API extends HttpServlet {
	
	public static final int OTHER_CODE = 0;
    public static final int ND_CODE = 1;
    public static final int NJ_CODE = 2;
    public static final int NM_CODE = 3;
    public static final int NW_CODE = 4;
    public static final int NB_CODE = 5;
    
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
                    //System.out.println("other");
                    break;
                case ND_CODE:
                    sb.append("<a href='#' title='属性：疾病&#13;'><font color='red'> " + oitem + " </font></a>");
                    //System.out.println("nd");
                    vnd.add(oitem);
                    break;
                case NJ_CODE:
                    sb.append("<a href='#' title='属性：症状&#13;'><font color='orange'> " + oitem + " </font></a>");
                    //System.out.println("nj");
                    vnj.add(oitem);
                    break;
                case NM_CODE:
                    sb.append("<a href='#' title='属性：药物&#13;'><font color='green'> " + oitem + " </font></a>");
                    //System.out.println("nm");
                    vnm.add(oitem);
                    break;
                case NB_CODE:
                    sb.append("<a href='#' title='属性：部位&#13;'><font color='blue'> " + oitem + " </font></a>");
                    //System.out.println("nb");
                    vnb.add(oitem);
                    break;
                case NW_CODE:
                    sb.append(oitem);
                    //System.out.println("nw");
                    break;
                default:
                    //System.out.println("de");
                    break;
            }
        }
        return sb.toString();
    }

    public static void apiDMBJ(Vector<String> vnd, Vector<String> vnj, Vector<String> vnb, Vector<String> vnm,
                               JSONArray ds, JSONArray ss, JSONArray bs, JSONArray ms) {
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
                    JSONObject obj = new JSONObject();
                    String name = resultSet.getString(1);
                    String aliases = resultSet.getString(2);
                    String relatedSymptom = resultSet.getString(3);
                    String briefIntro = resultSet.getString(4);
                    obj.put("name", name);
                    obj.put("aliases", aliases);
                    obj.put("relatedSymptom", relatedSymptom);
                    obj.put("briefIntro", briefIntro);
                    ds.put(obj);
                }
                query = "Select Symptom,Aliases,RelatedDisease,BriefIntroduction from jbk39symptom where RelatedDisease like '%" + disease + "%'";
                resultSet = st.executeQuery(query);
                while (resultSet.next()) {
                    JSONObject obj = new JSONObject();
                    String name = resultSet.getString(1);
                    String aliases = resultSet.getString(2);
                    String relatedSymptom = resultSet.getString(3);
                    String briefIntro = resultSet.getString(4);
                    obj.put("name", name);
                    obj.put("aliases", aliases);
                    obj.put("relatedDisease", relatedSymptom);
                    obj.put("briefIntro", briefIntro);
                    ss.put(obj);
                }
                resultSet.close();
            }
            //ss
            flagSet.clear();
            if(vnj.size()!=0) {
                String query2 = "Select Disease,Aliases,RelatedSymptoms,BriefIntroduction from jbk39disease where ";
                for (String sitem : vnj) {
                    String symptom = Normalize.getSymptom(sitem);
                    if (flagSet.contains(symptom))
                        continue;
                    flagSet.add(sitem);
                    String query = "Select Symptom,Aliases,RelatedDisease,BriefIntroduction from jbk39symptom where Symptom like '%" + symptom + "%'";
                    ResultSet resultSet = st.executeQuery(query);
                    while (resultSet.next()) {
                        JSONObject obj = new JSONObject();
                        String name = resultSet.getString(1);
                        String aliases = resultSet.getString(2);
                        String relatedSymptom = resultSet.getString(3);
                        String briefIntro = resultSet.getString(4);
                        obj.put("name", name);
                        obj.put("aliases", aliases);
                        obj.put("relatedDisease", relatedSymptom);
                        obj.put("briefIntro", briefIntro);
                        ss.put(obj);
                    }
                    query2 += "RelatedSymptoms like '%" + symptom + "%'and ";
                }
                query2 = query2.substring(0, query2.length() - 4) + ";";
                ResultSet resultSet2 = st.executeQuery(query2);
                while (resultSet2.next()) {
                    JSONObject obj = new JSONObject();
                    String name = resultSet2.getString(1);
                    String aliases = resultSet2.getString(2);
                    String relatedSymptom = resultSet2.getString(3);
                    String briefIntro = resultSet2.getString(4);
                    obj.put("name", name);
                    obj.put("aliases", aliases);
                    obj.put("relatedSymptom", relatedSymptom);
                    obj.put("briefIntro", briefIntro);
                    ds.put(obj);
                }
                resultSet2.close();
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
                    JSONObject obj = new JSONObject();
                    String name = resultSet.getString(1);
                    String disease = resultSet.getString(2);
                    String symptom = resultSet.getString(3);
                    String sentence = resultSet.getString(4);
                    obj.put("name", name.replace('#', ','));
                    obj.put("disease", disease.replace('#', ','));
                    obj.put("symptom", symptom.replace('#', ','));
                    obj.put("sentence", sentence.replace('#', ','));
                    bs.put(obj);
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
                //System.out.println(query);
                ResultSet resultSet = st.executeQuery(query);
                while (resultSet.next()) {
                    JSONObject obj = new JSONObject();
                    String medicine = resultSet.getString(1);
                    String disease = resultSet.getString(2);
                    String symptom = resultSet.getString(3);
                    String sentence = resultSet.getString(4);
                    obj.put("medicine", medicine.replace('#', ','));
                    obj.put("disease", disease.replace('#', ','));
                    obj.put("symptom", symptom.replace('#', ','));
                    obj.put("sentence", sentence.replace('#', ','));
                    ms.put(obj);
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
        System.out.println(scontent);
        JSONObject object = new JSONObject();
        //*
        JSONArray ds = new JSONArray();
        JSONArray ss = new JSONArray();
        JSONArray bs = new JSONArray();
        JSONArray ms = new JSONArray();
        Vector<String> vnd = new Vector<String>();
        Vector<String> vnj = new Vector<String>();
        Vector<String> vnm = new Vector<String>();
        Vector<String> vnb = new Vector<String>();
        String tSentence = preProcess(scontent, vnd, vnj, vnm, vnb);
        apiDMBJ(vnd, vnj, vnb, vnm, ds, ss, bs, ms);
        object.put("tSentence", tSentence);
        object.put("disease", ds);
        object.put("symptom", ss);
        object.put("body", bs);
        object.put("medicine", ms);
        //System.out.println(tSentence);
        //System.out.println(ms.toString());
        return object.toString();
    }

	@Override
	public void destroy() {
		NlpirProcess.exit();
		super.destroy();
	}

	@Override
	public void init() throws ServletException {
		try {
			NlpirProcess.init();
		} catch (Exception e) {
			e.printStackTrace();
			NlpirProcess.exit();
		}
		super.init();
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			String param = request.getParameter("param");
			if (param != null) {
				param = new String(param.getBytes("ISO-8859-1"), "UTF-8");
			}
			String jsonResponse = response(param);
			request.setAttribute("jsonResponse", jsonResponse);
			request.getRequestDispatcher("/api.jsp").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
