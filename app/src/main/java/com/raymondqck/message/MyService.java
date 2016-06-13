package com.raymondqck.message;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;

/**
 * Created by 陈其康 raymondchan on 2016/6/12 0012.
 */
public class MyService extends Service {

    private boolean isServiceRun = false;

    private final String TAG = "MyService";

    public final static String DATA_TEST = "test";
    private int count;

    public final static int CLOUD_SUCCESS = 0x10;
    public final static int CLOUD_FAILURE = 0x11;

    public final static int MSG_TEST = 0x12;
    public final static int MSG_TEST_1 = 0x13;
    public final static int MSG_REGISTER = 0x14;
    public final static int MSG_RUN= 0x15;




    public final static String CLOUD_NAME = "name";
    public final static String CLOUD_PASSWD = "passwd";
    private boolean isSaveCloud = false;


    public MyService() {
        isServiceRun = false;
    }

    private Messenger messengerClient;
    private Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(final Message msgFromClient) {

            Message msgToClient = Message.obtain(msgFromClient);

            msgToClient.what = MSG_TEST;
            //messengerClient = msgFromClient.replyTo;
            switch (msgFromClient.what) {
                case MSG_TEST:

                    Log.i(TAG, "Service-->message.what = " + msgFromClient.what);
                    Bundle data = msgFromClient.getData();
                    String name = data.getString(CLOUD_NAME);
                    String passwd = data.getString(CLOUD_PASSWD);
                    Log.i(TAG, "Service-->message.data--name:" + name + ":" + passwd);
                    saveToCloud(name, passwd, msgToClient);

                    break;
                case MSG_REGISTER:
                    messengerClient = msgFromClient.replyTo;
                    if (msgFromClient != null) {
                        Log.i(TAG, "Service===> MSG_REGISTER，服务端成功获取客户端Messenger");
                    }

                    break;
                case MSG_RUN:
                    if (msgFromClient.arg1 == 1){
                        Log.i(TAG, "Service===> MSG_RUN");
                        isServiceRun = true;

                    }else if (msgFromClient.arg1 == 2){
                        isServiceRun = false;
                    }

            }
        }
    });

    public void sendMessage(Message msgToClient) {
        try {
            messengerClient.send(msgToClient);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void saveToCloud(String name, String passwd, final Message msgToClient) {
        AVObject user = new AVObject("user");
        user.put(CLOUD_NAME, name);
        user.put(CLOUD_PASSWD, passwd);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {

                    msgToClient.arg1 = CLOUD_SUCCESS;
                    sendMessage(msgToClient);
                } else {

                    msgToClient.arg1 = CLOUD_FAILURE;
                    sendMessage(msgToClient);
                }
            }

        });
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service----> onBind()");

        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service----> onCreate()");
        //        //获得Activity的Messenger，用于发送Message---->因为msgFromClinet.replyto null object reference问题没解决
        //        mainActivity = new ScrollingActivity();
        //        //mainActivity.init();
        //        mMessenger_Activity = mainActivity.mMessenger;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service----> onStartCommand()");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    while (isServiceRun) {

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        count++;


                        Bundle data = new Bundle();
                        data.putString(DATA_TEST, "test");
                        Message m = Message.obtain();
                        m.what = MSG_RUN;
                        m.setData(data);
                        m.arg1 = count;
                        try {
                            messengerClient.send(m);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                    }
                }

                //stopSelf();
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRun = false;
        Log.i(TAG, "Service----> onDestroy()");

    }
}

