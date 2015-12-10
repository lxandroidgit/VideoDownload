package com.example.sniffer.httpdownload.read;


import android.util.Log;


import com.example.sniffer.httpdownload.bean.VideoDownInfo;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 读取每个视频的信息
 */
public class VideoResourcesReadThread extends Thread {
    //video_url: 'http://www.99rr1.com/get_file/3/97d0b7bbf078676ea1558f019feedfed/37000/37314/37314.mp4/'
    //preview_url: 'http://www.99rr1.com/contents/videos_screenshots/37000/37314/preview.mp4.jpg
    private String url;
    //  private AutoIO Io;
    //static Pattern z = Pattern.compile("[\\u4e00-\\u9fa5]+");
    static Pattern h = Pattern.compile("http.*mp4\\/");
    //static Pattern i = Pattern.compile("http.*?\\.jpg");
    private addVideo add;

    public VideoResourcesReadThread(String url, addVideo add) {
        this.url = url;
        this.add = add;
    }


    public interface addVideo {
        void addvideo(VideoDownInfo videoDownInfo);

    }

    @Override
    public void run() {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("script");
            String s = links.html();
            String videourl = "";
            if (s.contains("video_url")) {
                Matcher mp4 = h.matcher(s);
                if (mp4.find()) {
                    videourl = mp4.group();
                    //System.out.println("网址：" + mp4.group());
                }
                //Log.i("解析", "网址：" + s);
            }
            Elements alt = doc.select("img.player-preview");
            String name = alt.attr("alt");
            String image = alt.attr("src");
            System.out.println("一行：" + name + videourl + image);
            videourl = videourl.substring(videourl.indexOf("/get_file"));
            image = image.substring(image.indexOf("/contents"));
            VideoDownInfo videoinfo = new VideoDownInfo(name, image, videourl, 1);
            add.addvideo(videoinfo);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("解析", "解析错误");
        }

    }
}

  /*          String s = null;
            Io = new AutoIO(context);
            InputStream in = Io.openIO(url);
            if (in != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String videoName = "";
            String videoMp4 = "";
            String videoImage = "";
            try {
            while ((s = br.readLine()) != null) {
            if (s.contains("desc-video")) {
            System.out.println("获取的名字"+s);
            Matcher name = z.matcher(s);
            if (name.find()) {
            //   System.out.println("名字：" + name.group());
            videoName = name.group();
            }
            }
            if (s.contains("video_url")) {
            Matcher mp4 = h.matcher(s);
            if (mp4.find()) {
            videoMp4 = mp4.group();
            // System.out.println("网址：" + mp4.group());
            }
            String[] srs = s.split("\\,");
            for (String sr : srs) {
            if (sr.contains("preview_url")) {
            Matcher images = i.matcher(sr);
            if (images.find()) {
            videoImage = images.group();
            // System.out.println("图片：" + images.group());
            }
            }
            }
            }
            }*/

