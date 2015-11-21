package com.example.sniffer.httpdownload.download;


import android.content.Context;
import android.os.Environment;
import android.os.Handler;

import com.example.sniffer.httpdownload.utils.AutoIO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by sniffer on 15-10-15.
 */
public class DownloadMp4Thread extends Thread {
    private String url;

    private Handler handler;

    private String fileName;

    private DownloadSize callback;

    private Context context;


    public interface DownloadSize {
        public void fileress(int progress);

        public void isComplete(boolean mflag);
    }


    public DownloadMp4Thread(String url, Handler handler, Context context, String fileName, DownloadSize callback) {
        this.url = url;
        this.handler = handler;
        this.callback = callback;
        this.context = context;
        this.fileName=fileName;
    }

    @Override
    public void run() {


        FileOutputStream out = null;

        super.run();
        try {
            System.out.println(url);
            AutoIO mAutoIo=new AutoIO();
            InputStream in = mAutoIo.openIO(url);
            File file = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                file = new File(Environment.getExternalStorageDirectory()
                        + "/Download/" + fileName +".mp4");
                out = new FileOutputStream(file);
            }
            int len = -1;
            int progress = 0;
            byte[] buffer = new byte[4*1024];
            //写输入流
            if (out != null) {
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    progress+=len;
                    callback.fileress(progress);
                }
            }
            // final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //        iv_down.setImageBitmap(bitmap);
                  //  System.out.println(size);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            callback.isComplete(true);
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
