package org.zjuwangg.crawl.seeds;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zjuwangg.crawl.Constant;
import org.zjuwangg.tools.FileOperator;

import java.util.List;

/**
 * Created by wanggang on 2015/5/24.
 */
public class GetQwebSeeds {
    /**
     * 获得疾病分类信息
     */
    public static void getCatUrl() {
        String url = "http://ask.39.net";
        String filename = "D:\\code\\Design\\resources\\seed\\" +
                "dSubSeed.txt";
        //格式：大类--子类--url
        try {
            Document doc = Jsoup.connect(url).userAgent(Constant.UserAgent)
                    .timeout(5000).get();
//            System.out.println(doc.body());
            Elements elements = doc.select("body div.nav_box");
            FileOperator.createFile(filename);
            for (Element element : elements) {
                String mainCategory;
                String subCategory;
                String targetUrl;
                mainCategory = element.select("div.nav_title h2").first().text();
                Elements elements1 = element.select("div.nav_more dl");
                for (Element dl : elements1) {
                    Elements dt = dl.select("dt");
                    if (!dt.isEmpty()) {
                        Element dt1 = dt.first().select("a").first();
                        targetUrl = dt1.attr("href");
                        subCategory = dt1.attr("title");
                        FileOperator.appendFileByNextLine(filename, mainCategory +
                                Constant.Separator + subCategory + Constant.Separator + targetUrl);
                    } else {
                        Elements dds = dl.select("dd");
                        for (Element dd : dds) {
                            Element inner = dd.select("a").first();
                            targetUrl = inner.attr("href");
                            subCategory = inner.attr("title");
                            FileOperator.appendFileByNextLine(filename, mainCategory +
                                    Constant.Separator + subCategory + Constant.Separator + targetUrl);
                        }
                    }
                }

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void getSubCatDiseaseUrl() {
        String filename = "D:\\code\\Design\\resources\\seed\\" +
                "dSubSeed.txt";
        String outputfile = "D:\\code\\Design\\resources\\seed\\" +
                "dQwebSeed.txt";
        FileOperator.createFile(outputfile);
        List<String> urls = FileOperator.readFileByLines(filename);
        for (String url : urls) {
            System.out.println(url);
            String [] urlInfo = url.split(Constant.Separator);
//            System.out.println(urlInfo.length);
            int count = 0;
            while(count < Constant.Retry){
                count ++;
                 try {
                     Document doc = Jsoup.connect("http://ask.39.net"+urlInfo[2]).userAgent(Constant.UserAgent).timeout(Constant.TimeOut)
                        .get();
                     Elements lis = doc.select("body div.lab-list li");
                     for(Element li:lis){
                         Element tmp = li.select("a").first();
                         String diseaseName = tmp.attr("title");
                         String qwebUrl = tmp.attr("href");
                         FileOperator.appendFileByNextLine(outputfile,urlInfo[0]+Constant.Separator+
                                 urlInfo[1]+Constant.Separator+diseaseName+Constant.Separator+qwebUrl);
                     }
                     break;
                 } catch (Exception e) {
                System.out.println(e);
                 }
            }
        }

    }


    public static void getDetailQwebUrl(){
        String infile = "D:\\code\\Design\\resources\\seed\\" +
                "dQwebSeed.txt";
        String outfile = "D:\\code\\Design\\resources\\seed\\" +
                "dDetailWebSeed.txt";
        FileOperator.createFile(outfile);
        List<String> items = FileOperator.readFileByLines(infile);
        for(String item:items) {
            int count = 0;
            String[] info = item.split(Constant.Separator);
            String[] partUrl = info[3].split("-");
            for (int i = 1; i < 3; i++) {
                String targetUrl = partUrl[0] + "-2-" + i;
                System.out.println(targetUrl);
                while (count < Constant.Retry) {
                    count++;
                    try {
                        Document doc = Jsoup.connect("http://ask.39.net" + targetUrl).timeout(Constant.TimeOut)
                                .userAgent(Constant.UserAgent).get();
                        Elements asklist = doc.select("body ul.list_ask#list_ask li");
                        for(Element askItem:asklist){
                            String seedUrl = askItem.select("div.cap a").first().attr("href");
                            String content = info[0]+Constant.Separator+
                                    info[1]+Constant.Separator+info[2]+Constant.Separator+seedUrl;
                            FileOperator.appendFileByNextLine(outfile,content);
                        }
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public static void main(String[] args) {
        //getCatUrl();
        //getSubCatDiseaseUrl();
        getDetailQwebUrl();
    }
}
