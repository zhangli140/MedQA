package org.zjuwangg.crawl.webpage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

/**
 * Created by wanggang on 2015/4/23.
 */
public class QWebPage {

    public List<Answers> ans;
    public Question qus;
    public String url;
    public String mainCategory;
    public String subCategory;
    public String diseaseDetail;
    public String title;

    public QWebPage(List<Answers> ans, Question qus, String url, String mainCategory, String subCategory, String diseaseDetail, String title) {
        this.ans = ans;
        this.qus = qus;
        this.url = url;
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.diseaseDetail = diseaseDetail;
        this.title = title;
    }

    @Override
    public String toString() {
        return "QWebPage{" +
                "ans=" + ans +
                ", qus=" + qus +
                ", url='" + url + '\'' +
                ", mainCategory='" + mainCategory + '\'' +
                ", subCategory='" + subCategory + '\'' +
                ", diseaseDetail='" + diseaseDetail + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Answers> getAns() {
        return ans;
    }

    public void setAns(List<Answers> ans) {
        this.ans = ans;
    }

    public Question getQus() {
        return qus;
    }

    public void setQus(Question qus) {
        this.qus = qus;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getDiseaseDetail() {
        return diseaseDetail;
    }

    public void setDiseaseDetail(String diseaseDetail) {
        this.diseaseDetail = diseaseDetail;
    }

    public static void Test(String url) throws Exception {
        Document doc = Jsoup.connect(url).timeout(5000)
                .userAgent(" Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36")
                .get();
        System.out.println(doc);
    }

    public static void main(String[] args) {
        try{
            Test("Test");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}