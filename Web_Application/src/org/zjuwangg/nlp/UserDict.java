package org.zjuwangg.nlp;

import org.zjuwangg.mysql.MySql;
import org.zjuwangg.tools.FileOperator;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Created by wanggang on 2015/5/29.
 */
public class UserDict {

    private static String DICT_PATH = UserDict.class.getClassLoader().getResource("Dict").getPath();
    public static String ND_DICT = DICT_PATH + "/nd.dict";
    public static String NJ_DICT = DICT_PATH + "/nj.dict";
    public static String NW_DICT = DICT_PATH + "/nw.dict";
    public static String NM_DICT = DICT_PATH + "/nm.dict";
    public static String NB_DICT = DICT_PATH + "/nb.dict";

    /**
     * nd : 疾病
     */
    public static void ndDict() {
        String fileName = ND_DICT;
        try {
            FileOperator.createFile(fileName);
            MySql mysql = new MySql();
            mysql.connect();
            String query = "select Disease,Aliases from jbk39disease";
            Statement st = mysql.con.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                String disease = rs.getString(1);
                String aliases = rs.getString(2);
                if (!disease.trim().isEmpty())
                    FileOperator.appendFileByNextLine(fileName, disease.trim() + " nd");
                if (!aliases.isEmpty()) {
                    String[] as = aliases.trim().split("，");
                    for (String s : as) {
                        FileOperator.appendFileByNextLine(fileName, s + " nd");
                    }
                }
            }
            mysql.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ns : 症状
     */
    public static void njDict() {
        String fileName = NJ_DICT;
        try {
            FileOperator.createFile(fileName);
            MySql mysql = new MySql();
            mysql.connect();
            String query = "select Symptom,Aliases from jbk39symptom";
            Statement st = mysql.con.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                String disease = rs.getString(1);
                String aliases = rs.getString(2);
                if (!disease.trim().isEmpty())
                    FileOperator.appendFileByNextLine(fileName, disease.trim() + " nj");
                if (!aliases.isEmpty()) {
                    String[] as = aliases.trim().split("，");
                    for (String s : as) {
                        FileOperator.appendFileByNextLine(fileName, s + " nj");
                    }
                }
            }
            mysql.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * nm : 药物
     */
    public static void nmDict() {
        String fileName = NM_DICT;
        String source1 = DICT_PATH + "药品名称大全.txt";
        String source2 = DICT_PATH + "nackin医药商品精简.txt";
        FileOperator.createFile(fileName);
        List<String> s1 = FileOperator.readFileByLines(source1);
        List<String> s2 = FileOperator.readFileByLines(source2);
        try {
            for (String s : s1) {
                FileOperator.appendFileByNextLine(fileName, s + " nm");
            }
            for(String s:s2){
                if(!s1.contains(s))
                    FileOperator.appendFileByNextLine(fileName,s+" nm");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * nb: 身体部位
     */
    public static void nbDict() {
        String fileName = NB_DICT;
        String source = DICT_PATH + "人体解剖学名词（中文）2009.txt";
        List<String> ss = FileOperator.readFileByLines(source);
        try {
            for (String s : ss) {
                FileOperator.appendFileByNextLine(fileName, s + " nb");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * nr : 风险因子
     */
    public static void nwDict() {

    }

    public static void ImportDict(){
        int status;
        status = NlpirProcess.CLibrary.Instance.NLPIR_Init(NlpirProcess.class.getClassLoader().getResource("").getPath(),NlpirProcess.CHARSET,"0");
        System.out.println("Init code :" + status);
//        status = NlpirProcess.CLibrary.Instance.NLPIR_ImportUserDict(ND_DICT,false);
//        System.out.println("ND code :" + status);
        status = NlpirProcess.CLibrary.Instance.NLPIR_ImportUserDict(NJ_DICT,false);
        System.out.println("NJ code :" + status);
//        status = NlpirProcess.CLibrary.Instance.NLPIR_ImportUserDict(NM_DICT,false);
//        System.out.println("NM code :" + status);
//        status = NlpirProcess.CLibrary.Instance.NLPIR_ImportUserDict(NB_DICT,false);
//        System.out.println("NB code :" + status);
        status = NlpirProcess.CLibrary.Instance.NLPIR_SaveTheUsrDic();
        System.out.println("SAVE code :" + status);
        NlpirProcess.CLibrary.Instance.NLPIR_Exit();
    }

    public static void main(String[] args) {
//        ndDict();
//        njDict();
//        nmDict();
//        nbDict();
//        ImportDict();
    }
}
