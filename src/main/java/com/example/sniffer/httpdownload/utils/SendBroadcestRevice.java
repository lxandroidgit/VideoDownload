package com.example.sniffer.httpdownload.utils;

import android.content.Context;
import android.content.Intent;

import com.example.sniffer.httpdownload.bean.VideoDownInfo;

/**
 * 发送广播
 */
public class SendBroadcestRevice {

    public static void sendRecice(Context context, VideoDownInfo info, int state, int progress) {
        Intent intent = new Intent();
        intent.setAction(Key.ACTION_DOWNLOAD_STATE);
        info.setState(state);
        info.setVideoProgress(progress);
        intent.putExtra("videoinfo", info);
        context.sendBroadcast(intent);
    }

    public static void sendServiceRecive(Context context, String action, VideoDownInfo info) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (info != null) {
            intent.putExtra("videoinfo", info);
        }
        context.sendBroadcast(intent);
    }

}
