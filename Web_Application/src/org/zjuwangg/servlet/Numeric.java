package org.zjuwangg.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.zjuwangg.tools.FileOperator;

public class Numeric extends HttpServlet {
	
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
    public static String response(String disease, String range) {
        Double dec_per = Double.parseDouble(range);
        JSONObject object = new JSONObject();
        //*
        List<Double> result = getaccsnsp(Numeric.class.getClassLoader().getResource(disease + ".csv").getPath(),dec_per);
        object.put("acc", result.get(0));
        object.put("sn", result.get(1));
        object.put("sp", result.get(2));
        object.put("des",disease);
        //System.out.println(tSentence);
        //System.out.println(ms.toString());
        return object.toString();
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

		String disease = request.getParameter("disease");
		if (disease != null) {
			disease = new String(disease.getBytes("ISO-8859-1"), "UTF-8");
		}
		String range = request.getParameter("range");
		try {
			String jsonResponse = response(disease, range);
			request.setAttribute("jsonResponse", jsonResponse);
			request.getRequestDispatcher("/numeric.jsp").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
