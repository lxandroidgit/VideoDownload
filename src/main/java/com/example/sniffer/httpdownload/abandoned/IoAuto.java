package com.example.sniffer.httpdownload.abandoned;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;


/**
 * 接口
 */
public interface IoAuto {

    public void downloadHttpUrl(String url);

    public void closeAll() throws IOException;

    public BufferedReader getbufferedReader();
}
