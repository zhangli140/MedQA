package org.zjuwangg.crawl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zjuwangg.crawl.webpage.Answers;
import org.zjuwangg.crawl.webpage.QWebPage;
import org.zjuwangg.crawl.webpage.Question;
import org.zjuwangg.mysql.MySql;
import org.zjuwangg.tools.FileOperator;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by wanggang on 2015/5/25.
 */

class crawlThread implements Runnable{
    public List<String> urls = null;
    public int id = 0;
    public static MySql sqlTool = new MySql();
    public crawlThread(List<String> urls,int id){
        this.urls = urls;
        this.id = id;
    }
    @Override
    public void run() {
        System.out.println("Thread: " + id + "  start!");
        List<QWebPage> res = new ArrayList<QWebPage>();
        for(String url:urls) {
            int count = 0;
            String[] info = url.split(Constant.Separator);
            String targetUrl = "http://ask.39.net"+info[3];
//            System.out.println(id + "---"+targetUrl);
            int timeout = Constant.TimeOut;
            while (count < Constant.Retry) {
                count++;
                try {
                    Document doc = Jsoup.connect(targetUrl).userAgent(Constant.UserAgent)
                            .timeout(timeout).get();
                    Element body = doc.body();
                    String title = doc.title();
                    String[] titles = title.split("_");
                    if(titles.length > 0)
                        title = titles[0];
                    Element qelement = body.select("div.tbox.tbox_nobg").first();
                    Elements aelements = body.select("div#doctor_reply");

                    if(aelements.isEmpty())
                        break;
                    Elements qinfos = qelement.select("ul.user_msg");
                    String questionInfo = "";
                    String questionDetail = "";
                    if(!qinfos.isEmpty())
                        questionInfo = qinfos.first().text();
                    Elements qinfods = qelement.select("p.user_p");
                    for(Element e:qinfods)
                        questionDetail += e.text()+Constant.Separator;

                    questionDetail = questionDetail.substring(0,questionDetail.length() - 3);

                    List<Answers> answers = new ArrayList<Answers>();
                    Element atmp = aelements.first();
                    Elements ans = atmp.select("div.t_con div.t_right");
                    for(Element as:ans){
                        Answers atmp1 = new Answers("","");
                        Elements t = as.select("p.user_p");
                        String st = "";
                        for(Element e1:t){
                            st += e1.text();
                        }
                        atmp1.setAnswers(st);
                        answers.add(atmp1);
                    }
                    Question question = new Question();
                    question.setQuestionDetail(questionDetail);
                    question.setQuestionInfo(questionInfo);
                    QWebPage webPage = new QWebPage(answers,question,targetUrl,info[0],info[1],info[2],title);
                    res.add(webPage);
//                    System.out.println(webPage);
                    break;
                } catch (Exception e) {
                    timeout = timeout*2 + 1;
                    System.out.println("Thread:" + this.id + " exception");
//                    e.printStackTrace();
                }
            }
        }

        Vector<Map<String,String>>  insertItems = new Vector<Map<String,String>>();
        for(QWebPage q:res) {
            Map<String, String> item = new HashMap<String, String>();
            item.put("MainCategory",q.getMainCategory());
            item.put("SubCategory",q.getSubCategory());
            item.put("DiseaseName",q.getDiseaseDetail());
            item.put("Url",q.getUrl());
            item.put("title",q.getTitle());
            item.put("QInfo",q.getQus().getQuestionInfo());
            item.put("QDetail",q.getQus().getQuestionDetail());
            String ans = "";
            List<Answers> answersList = q.getAns();
            int len = answersList.size();
            for(int i = 0;i < len;i++){
                ans = ans + answersList.get(i).getAnswers();
                if(i != len-1)
                    ans += Constant.Separator;
            }
            item.put("Answers",ans);
            insertItems.add(item);
        }
        sqlTool.insertManyItmes("qwebinfo", insertItems);
        System.out.println("Thread: "+ id + " end.");
    }

}
public class SpiderUtil{
    public static void CrawlQwebPage() {
        String filename = "D:\\code\\Design\\resources\\seed\\" +
                "dDetailWebSeed.txt";
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(20,30,10,
                TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>());
        List<String>  urlSeeds = FileOperator.readFileByLines(filename);
        urlSeeds = urlSeeds.subList(11000,79999);
        for(int i = 0;i <= urlSeeds.size()/10;i++){
            poolExecutor.submit(new crawlThread(urlSeeds.subList(i*10,i*10+9),i));
        }
        try {
            poolExecutor.awaitTermination(7, TimeUnit.HOURS);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        crawlThread.sqlTool.connect();
        CrawlQwebPage();
        crawlThread.sqlTool.closeConnection();
    }
}
