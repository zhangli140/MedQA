package org.zjuwangg.crawl.seeds;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zjuwangg.mysql.MySql;
import org.zjuwangg.tools.FileOperator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by wanggang on 2015/4/23.
 */
public class GetUrlSeeds {
//    public final static String SeedsFolder = "D:\\code\\Design\\seeds\\";

    /**
     * 将爬取结果存储到数据库medical.jbk39disease表中
     * Table Structure:
     * DiseaseId :primary key auto-increment
     * Disease : name of disease
     * Aliases: other names of disease
     * Url : the url which point to introduction html
     * RelatedSymptoms: as name describe
     * BriefIntroduction: brief introduction
     */
    public Vector<Map<String, String>> get39DIterms(String url) throws Exception {
        Vector<Map<String, String>> res = new Vector<Map<String, String>>();
        Document doc = Jsoup.connect(url).timeout(5000)
                .userAgent(" Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36")
                .get();
        Elements el = doc.body().select("div.res_list");
        for (Element e : el) {
            Map<String, String> m = new HashMap<String, String>();
            String Url = e.select("dl dt h3 a").first().attr("href");
            String Disease = e.select("dl dt h3 a").first().text();
            String Aliases = "";
            String RelatedSymptom = "";
            String BriefIntroduction = "";
            Elements tmp = e.select("dl dt cite");
            if (!tmp.isEmpty()) {
                Aliases = tmp.attr("title");
            }
            BriefIntroduction = e.select("dl dd ").text().replace("[详细]", "");
            Elements rs = e.select("div.other.clearfix");
            if (!rs.isEmpty()) {
                RelatedSymptom = rs.first().text().trim().replace("|", ",").replace("相关症状", "");
            }
            m.put("Disease", Disease);
            m.put("Url", Url);
            m.put("Aliases", Aliases);
            m.put("RelatedSymptoms", RelatedSymptom);
            m.put("BriefIntroduction", BriefIntroduction);
            res.add(m);
        }
        return res;
    }

    public void getNetJbk39Disease() {
        String targetUrl = "http://jbk.39.net/bw_t1_p";
        MySql mysqlTool = new MySql();
        mysqlTool.connect();
        Vector<String> unUrls = new Vector<String>();
        for (int j = 0; j < 784; j++) {
            String url = targetUrl + j;
            System.out.println(url);
            Vector<Map<String, String>> res;
            try {
                res = get39DIterms(url);
                mysqlTool.insertManyItmes("jbk39disease", res);
            } catch (Exception e) {
                e.printStackTrace();
                unUrls.add(targetUrl + j);
            }
        }
        mysqlTool.closeConnection();
        System.out.println("Failure Crawled: " + unUrls.size());
        if (unUrls.size() > 0) {
            String fileName = "D:\\code\\Design\\resources\\seed\\unsuccessful\\jbk39netdisease.txt";
            FileOperator.createFile(fileName);
            for (String s : unUrls) {
                try {
                    String ss = FileOperator.readFileByBytes(fileName);
                    FileOperator.writeFileByFileWriter(fileName, ss + s + "\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(s);
            }
        }
    }

    public void crawlFailureSeed(String filePath) {
        List<String> urls = FileOperator.readFileByLines(filePath);
        MySql sqlTool = new MySql();
        sqlTool.connect();
        Vector<Map<String, String>> res;
        for (String url : urls) {
            System.out.println(url);
            try {
                res = get39DIterms(url);
                sqlTool.insertManyItmes("jbk39disease", res);
            } catch (Exception e) {
                System.out.println("Failure:" + url);
                e.printStackTrace();
            }
        }
        sqlTool.closeConnection();
    }

    /**
     * 将爬取结果存储到数据库medical.jbk39symptom表中
     * Table Structure:
     * SymptomId: primary key,auto-increment
     * Symptom:name of symptom
     * Aliases: other names of symptom
     * RelatedDisease: as name indicates
     * BriefIntroduction :brief introduction of symptom
     * Url: the related html which about the symptom
     */
    public Vector<Map<String, String>> get39SIterms(String url) throws Exception {
        Vector<Map<String, String>> res = new Vector<Map<String, String>>();
        Document doc = Jsoup.connect(url).timeout(5000)
                .userAgent(" Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36")
                .get();
        Elements el = doc.body().select("div.res_list");
        for (Element e : el) {
            Map<String, String> m = new HashMap<String, String>();
            String Url = e.select("dl dt h3 a").first().attr("href");
            String Symptom = e.select("dl dt h3 a").first().text();
            String Aliases = "";
            String RelatedDisease = "";
            String BriefIntroduction = "";
            Elements tmp = e.select("dl dt cite");
            if (!tmp.isEmpty()) {
                Aliases = tmp.attr("title");
            }
            BriefIntroduction = e.select("dl dd ").text().replace("[详细]", "");
            Elements rs = e.select("div.other.clearfix");
            if (!rs.isEmpty()) {
                RelatedDisease = rs.first().text().trim().replace("|", ",").replace("相关疾病", "");
            }
            m.put("Symptom", Symptom);
            m.put("Url", Url);
            m.put("Aliases", Aliases);
            m.put("RelatedDisease", RelatedDisease);
            m.put("BriefIntroduction", BriefIntroduction);
            res.add(m);
        }
        return res;
    }

    public void getNetJbk39Symptom() {
        String prefixUrl = "http://jbk.39.net/bw_t2_p";
        Vector<String> unUrls = new Vector<String>();
        MySql mysql = new MySql();
        mysql.connect();
        for (int i = 0; i < 667; i++) {
            String url = prefixUrl + i;
            System.out.println(url);
            try {
                mysql.insertManyItmes("jbk39symptom", get39SIterms(url));
            } catch (Exception e) {
                e.printStackTrace();
                unUrls.add(url);
            }
        }
        mysql.closeConnection();
        System.out.println("Failure Crawled: " + unUrls.size());
        if (unUrls.size() > 0) {
            String fileName = "D:\\code\\Design\\resources\\seed\\unsuccessful\\jbk39netsymptom.txt";
            FileOperator.createFile(fileName);
            for (String s : unUrls) {
                try {
                    String ss = FileOperator.readFileByChars(fileName);
                    FileOperator.writeFileByFileWriter(fileName, ss + s + "\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(s);
            }
        }

    }

    public void crawlFailureSeed1(String filePath) {
        List<String> urls = FileOperator.readFileByLines(filePath);
        MySql sqlTool = new MySql();
        sqlTool.connect();
        Vector<Map<String, String>> res;
        for (String url : urls) {
            try {
                System.out.println(url);
                res = get39SIterms(url);
                sqlTool.insertManyItmes("jbk39symptom", res);
            } catch (Exception e) {
                System.out.println("Failure:" + url);
                e.printStackTrace();
            }
        }
        sqlTool.closeConnection();
    }

    public static void main(String[] args) throws Exception {
        new GetUrlSeeds().getNetJbk39Disease();
        new GetUrlSeeds().crawlFailureSeed("D:\\code\\Design\\resources\\seed\\unsuccessful\\jbk39netdisease.txt");
        new GetUrlSeeds().getNetJbk39Symptom();
        new GetUrlSeeds().crawlFailureSeed1("D:\\code\\Design\\resources\\seed\\unsuccessful\\jbk39netsymptom.txt");
//        String url = "http://jbk.39.net/bw_t2_p166";
//        MySql mysql = new MySql();
//        mysql.connect();
//        mysql.insertManyItmes("jbk39symptom",new GetUrlSeeds().get39SIterms(url));
//        mysql.closeConnection();
    }
}
