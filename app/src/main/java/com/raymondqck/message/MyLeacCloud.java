package com.raymondqck.message;

import android.app.Application;
import android.util.Log;

import com.avos.avoscloud.AVOSCloud;

/**
 * Created by 陈其康 raymondchan on 2016/6/12 0012.
 */
public class MyLeacCloud extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this,"3QIRJGg73Q05BKykUtvDiKYX-gzGzoHsz","i8fXIWwpCVcurMRvVLkupQVf");
        Log.d("MyLeacCloud","Application----->onCreate() ");
    }
}
