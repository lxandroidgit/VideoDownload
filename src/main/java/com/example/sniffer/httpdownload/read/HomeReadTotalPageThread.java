package com.example.sniffer.httpdownload.read;



import com.example.sniffer.httpdownload.utils.StringToInt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 *  获取视频总页数
 */
public class HomeReadTotalPageThread extends Thread {
    //http://www.99rr1.com/videos/37567/1ec1988b8f163b99ed6411be46f961e1/
    //http://([w-]+.)+[w-]+(/[w- ./?%&=]*)?  ([\w-]+\.)

    @Override
    public void run() {
        try {
            //String url = "http://www.99rr1.com/latest-updates/1/";
            String url = "http://www.99rr1.com/categories/451bf6aed63bf5d8f04a13fb2448d52d/1/";
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String s = link.attr("class");
                if (s.equals("btn")) {
                    String str = link.text();
                    str = StringToInt.toIntString(str);
                    if (str != null && !str.equals("")) {
                        int number = Integer.parseInt(str);
                      /*  if (number > Key.totalPage) {
                            Key.totalPage = number;
                        }*/
                    }
                }
            }
            // System.out.println("href"+links);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }

}

