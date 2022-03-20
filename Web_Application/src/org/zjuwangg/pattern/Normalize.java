package org.zjuwangg.pattern;

import org.zjuwangg.mysql.MySql;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wanggang on 2015/5/31.
 */
public class Normalize {
    public static Map<String,String> diseaseNorm = new HashMap<String,String>();
    public static Map<String,String> symptomNorm = new HashMap<String,String>();
    static {
        try {
            MySql mysql = new MySql();
            mysql.connect();
            Statement st = mysql.con.createStatement();
            String query1 = "select Disease,Aliases from jbk39disease";
            String query2 = "select Symptom,Aliases from jbk39symptom";
            ResultSet rs1 = st.executeQuery(query1);
            while(rs1.next()){
                String disease = rs1.getString(1);
                String as = rs1.getString(2);
                diseaseNorm.put(disease,disease);
                if(!as.isEmpty()){
                    String[] ass = as.split("，");
                    for(String s:ass)
                        diseaseNorm.put(s.trim(),disease);
                }
            }
            System.out.println("Disease normlization Completed!");
            ResultSet rs2 = st.executeQuery(query2);
            while(rs2.next()){
                String symptom = rs2.getString(1);
                String as = rs2.getString(2);
                symptomNorm.put(symptom,symptom);
                if(!as.isEmpty()){
                    String[] ass = as.split("，");
                    for(String s:ass)
                        symptomNorm.put(s.trim(),symptom);
                }
            }
            System.out.println("Symptom normlization Completed!");
            mysql.closeConnection();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static String getDisease(String name){
        if(diseaseNorm.containsKey(name))
            return diseaseNorm.get(name);
        return name;
    }
    public static String getSymptom(String name){
        if(symptomNorm.containsKey(name))
            return symptomNorm.get(name);
        return name;
    }
    public static void test(){
        System.out.println(getDisease("痔疾"));
        System.out.println(getSymptom("胃脘痛"));
    }
    public static void main(String[] args){
        test();
    }
}
