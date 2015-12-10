package com.example.sniffer.httpdownload.read;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

/**
 * 下载页面的每个视频连接
 */
public class HomeReadUrlThread extends Thread {
    private List<String> strlist;
    private String url;
    //http://www.99rr1.com/videos/37567/1ec1988b8f163b99ed6411be46f961e1/
    //http://([w-]+.)+[w-]+(/[w- ./?%&=]*)?  ([\w-]+\.)


    public HomeReadUrlThread(String url, List<String> strlist) {
        this.url = url;
        this.strlist = strlist;
    }


    @Override
    public void run() {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");
            analysisJiuJiuReVideoHtml(links);
            // System.out.println("href"+links);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void analysisJiuJiuReVideoHtml(Elements elements) {
        for (Element link : elements) {
            String s = link.attr("class");
            if (s.equals("kt_imgrc")) {
                String str = link.attr("abs:href");
                 //Log.i("解析", "网址：" + str);
                strlist.add(str);
            }
        }
    }

}

