package org.zjuwangg.servlet;

import java.io.IOException;
import java.io.PrintWriter;
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

public class Relation extends HttpServlet {
	
	public static final int OTHER_CODE = 0;
    public static final int ND_CODE = 1;
    public static final int NJ_CODE = 2;
    public static final int NM_CODE = 3;
    public static final int NW_CODE = 4;
    public static final int NB_CODE = 5;

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

			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			out.print(response(param));
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
