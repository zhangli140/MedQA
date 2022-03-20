package org.zjuwangg.pattern;

import org.zjuwangg.mysql.MySql;
import org.zjuwangg.nlp.NlpirProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by wanggang on 2015/5/31.
 */
public class PatternMining {
    public static final int OTHER_CODE = 0;
    public static final int ND_CODE = 1;
    public static final int NJ_CODE = 2;
    public static final int NM_CODE = 3;
    public static final int NW_CODE = 4;
    public static final int NB_CODE = 5;
    public static MySql sql = new MySql();
//    public static final int R_bodydisease = 6;
//    public static final int R_caredisease = 7;
//    public static final int R_issymptom = 8;
//    public static final int R_riskfactor = 9;

    public static String concatVector(Vector<String> vector) {
        if (vector.isEmpty())
            return "";
        StringBuffer sb = new StringBuffer();
        for (String s : vector) {
            sb.append(s + '#');
        }
        String res = sb.substring(0, sb.length() - 1);
        if (res.length() > 5000)
            return res.substring(0, 4999);
        else
            return res;
    }

    public static void bodydisease(Vector<String> vnb, Vector<String> vnd, Vector<String> vnj, String sentence) {
        if (vnb.isEmpty() || vnd.isEmpty())
            return;
        Map<String, String> target = new HashMap<String, String>();
        String Body = concatVector(vnb);
        String Disease = concatVector(vnd);
        String Symptom = concatVector(vnj);
        if(sentence.length()>6000)
            sentence = sentence.substring(0,5999);

        target.put("Body", Body);
        target.put("Disease", Disease);
        target.put("Symptom", Symptom);
        target.put("Sentence", sentence);
        sql.insertItem("bodydisease", target);

    }

    public static void caredisease(Vector<String> vnm, Vector<String> vnd, Vector<String> vnj, String sentence) {
        if (vnm.isEmpty())
            return;
        if (vnd.isEmpty() && vnj.isEmpty())
            return;
        Map<String, String> target = new HashMap<String, String>();
        String nm = concatVector(vnm);
        String nd = concatVector(vnd);
        String nj = concatVector(vnj);
        if(sentence.length()>6000)
            sentence = sentence.substring(0,5999);
        target.put("Medicine", nm);
        target.put("Disease", nd);
        target.put("Symptom", nj);
        target.put("Sentence", sentence);
        sql.insertItem("caredisease", target);

    }

    public static void issymptom(Vector<String> vnj, Vector<String> vnd, String sentence) {
        if (vnj.isEmpty() || vnd.isEmpty())
            return;
        String nj = concatVector(vnj);
        String nd = concatVector(vnd);
        if(sentence.length()>6000)
            sentence = sentence.substring(0,5999);
        Map<String, String> target = new HashMap<String, String>();
        target.put("Disease", nd);
        target.put("Symptom", nj);
        target.put("Sentence", sentence);
        sql.insertItem("issymptom", target);
    }

    public static void riskfactor(Vector<String> vnw, Vector<String> vnd, Vector<String> vnj, String sentence) {
        if (vnw.isEmpty())
            return;
        if (vnd.isEmpty() && vnj.isEmpty())
            return;
        String nw = concatVector(vnw);
        String nd = concatVector(vnd);
        String nj = concatVector(vnj);
        if(sentence.length()>6000)
            sentence = sentence.substring(0,5999);
        Map<String, String> target = new HashMap<String, String>();
        target.put("Risk", nw);
        target.put("Disease", nd);
        target.put("Symptom", nj);
        target.put("Sentence", sentence);
        sql.insertItem("riskfactor", target);
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

    /**
     * 挖掘源函数入口
     *
     * @param source
     */
    public static void pattern(String source) {
        Vector<String> vnd = new Vector<String>();
        Vector<String> vnj = new Vector<String>();
        Vector<String> vnm = new Vector<String>();
        Vector<String> vnw = new Vector<String>();
        Vector<String> vnb = new Vector<String>();
        /**
         * 句子分词
         */
        String[] sentences = source.trim().split("。|？|！");
        for (String sentence : sentences) {
            String[] segments = NlpirProcess.spilt(sentence).split("，");
            for (String s : segments) {
                vnd.clear();
                vnj.clear();
                vnm.clear();
                vnw.clear();
                vnb.clear();
                String[] items = s.trim().split("\\s+");
                for (String item : items) {
                    int code = judge(item);
                    switch (code) {
                        case OTHER_CODE:
                            break;
                        case NB_CODE:
                            vnb.add(item.substring(0, item.length() - 3));
                            break;
                        case ND_CODE:
                            vnd.add(Normalize.getDisease(item.substring(0, item.length() - 3)));
                            break;
                        case NJ_CODE:
                            vnj.add(Normalize.getSymptom(item.substring(0, item.length() - 3)));
                            break;
                        case NM_CODE:
                            vnm.add(item.substring(0, item.length() - 3));
                            break;
                        case NW_CODE:
                            vnw.add(item.substring(0, item.length() - 3));
                            break;
                        default:
                            break;
                    }
                }
                bodydisease(vnb, vnd, vnj, sentence);
                caredisease(vnm, vnd, vnj, sentence);
                issymptom(vnj, vnd, sentence);
                riskfactor(vnw, vnd, vnj, sentence);
            }
        }
    }

    public static void pattern(String QDetail, String Answers) {
        pattern(QDetail);
        pattern(Answers);
    }

    public static void patternMining() {
        try {
            MySql sqlTool = new MySql();
            sqlTool.connect();
            System.out.println("Connect Successful");
//            String query = "select DiseaseName,title,QInfo,QDetail,Answers from qwebinfo";
            String query = "select QDetail,Answers from qwebinfo limit 24580,32000";
            PreparedStatement ps = sqlTool.con.prepareCall(query, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            ps.setFetchDirection(ResultSet.FETCH_REVERSE);
            ResultSet rs = ps.executeQuery(query);
            int count = 24580;
            sql.connect();
            while (rs.next()) {
                count++;
//                String DiseaseName = rs.getString(1);
//                String title = rs.getString(2);
//                String QInfo = rs.getString(3);
                String QDetail = rs.getString(1);
                String Answers = rs.getString(2);
                pattern(QDetail, Answers);
                if (count % 20 == 0)
                    System.out.println("Have processed : " + count + "/ 57038");
            }
            sql.closeConnection();
            rs.close();
            sqlTool.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("hello,world!");
            NlpirProcess.init();
            patternMining();
            NlpirProcess.exit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
