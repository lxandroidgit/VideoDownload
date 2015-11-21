package com.example.sniffer.httpdownload.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

import java.util.List;

public class ServiceStatusUtils {
    /**
     * 检查服务是否正在运行
     *
     * @return
     */
    public static boolean isAddressService(Context context, String serviceName) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> runningServices = am.getRunningServices(100);
        for (RunningServiceInfo runningServiceInfo : runningServices) {
            String className = runningServiceInfo.service.getClassName();
            //System.out.println(className);
            if (className.equals(serviceName)) {
                return true;
            }
        }
        return false;
    }
}
