package com.example.sniffer.httpdownload.activity;

import android.app.Activity;
import android.os.Bundle;

import com.example.sniffer.httpdownload.R;

/**
 * 无网络连接
 */
public class NoNetworkActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_network);
    }
}
