package com.raymondqck.message;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ScrollingActivity extends AppCompatActivity {
    private String s;
    private final String TAG = "==Main==";

    private Messenger mServiceMessenger;
    private boolean isCon;
    private boolean isRun = false;

    private TextView mTvResult;
    private EditText mEdtName;
    private EditText mEdtPasswd;
    private Button mBtnSignIn;
    private Button btn_startRun;
    Message msgFromClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        init();
        bindService();
        //        new Thread(new Runnable() {
        //            @Override
        //            public void run() {
        //                while (true){
        //                    if (isCon){
        //                        Message message = Message.obtain();
        //                        message.arg1 = 33;
        //                        message.what = 33;
        //                        message.replyTo = mMessenger;
        //                        message.obj = mMessenger;
        //                        mHandler.sendMessage(message);
        //                        break;
        //                    }
        //                }
        //            }
        //        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger = new Messenger(service);
            if (mServiceMessenger != null) {
                isCon = true;
                mTvResult.setText("成功获取服务Messenger");
                msgFromClient = Message.obtain(null, MyService.MSG_REGISTER);
                msgFromClient.replyTo = mMessenger;
                try {
                    mServiceMessenger.send(msgFromClient);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                mTvResult.setText("获取服务Messenger失败！");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msgFromServer) {
            super.handleMessage(msgFromServer);
            switch (msgFromServer.what) {

                case MyService.MSG_TEST:
                    if (msgFromServer.arg1 == MyService.CLOUD_SUCCESS) {
                        Log.i(TAG, "Activity--->同步成功:接收到Service传来的Message");
                        mTvResult.setText("同步通信成功！");
                        Toast.makeText(ScrollingActivity.this, "同步通信成功！", Toast.LENGTH_SHORT).show();
                    } else if (msgFromServer.arg1 == MyService.CLOUD_FAILURE) {
                        mTvResult.setText("同步通信失败！");
                        Toast.makeText(ScrollingActivity.this, "同步通信失败！", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MyService.MSG_RUN:
                    Bundle data = msgFromServer.getData();
                    s = "===Activity--->Service-->onStartCommond: " + data.getString(MyService.DATA_TEST) + ":" + msgFromServer.arg1;
                    Log.i(TAG, s);
                    mTvResult.setText(s);


                    break;
                case 33:
                    if (msgFromServer.replyTo != null) {
                        mTvResult.setText("" + msgFromServer.arg1 + "-msgFromServer.replyTo!=null");
                    }

                    break;
            }

        }

    };

    public Messenger mMessenger = new Messenger(mHandler);

    public void init() {
        mTvResult = (TextView) findViewById(R.id.tv_result);
        mBtnSignIn = (Button) findViewById(R.id.btn_signIn);
        mEdtName = (EditText) findViewById(R.id.edt_username);
        mEdtPasswd = (EditText) findViewById(R.id.edt_passwd);

        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mEdtName.getText().toString();
                String passwd = mEdtPasswd.getText().toString();
                if (!name.equals("") && !passwd.equals("")) {
                    msgFromClient = Message.obtain(null, MyService.MSG_TEST);
                    Bundle data = new Bundle();
                    data.putString(MyService.CLOUD_NAME, name);
                    data.putString(MyService.CLOUD_PASSWD, passwd);
                    Log.i(TAG, "name:" + data.getString(MyService.CLOUD_NAME) + ":" + data.getString(MyService.CLOUD_PASSWD));
                    msgFromClient.setData(data);
                    msgFromClient.replyTo = mMessenger;
                    if (mServiceMessenger != null) {
                        try {
                            mServiceMessenger.send(msgFromClient);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ScrollingActivity.this, "服务未成功连接", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(ScrollingActivity.this, "不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_startRun = (Button) findViewById(R.id.btn_startRun);
        btn_startRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRun){
                    btn_startRun.setText("STOP RUN");
                    msgFromClient = Message.obtain(null, MyService.MSG_RUN);
                    msgFromClient.arg1 = 1;
                    try {
                        mServiceMessenger.send(msgFromClient);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    isRun = !isRun;
                    Log.i(TAG,"Button Start run");
                }else if (isRun){
                    btn_startRun.setText("START RUN");
                    msgFromClient = Message.obtain(null, MyService.MSG_RUN);
                    msgFromClient.arg1 = 2;
                    try {
                        mServiceMessenger.send(msgFromClient);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    isRun = !isRun;
                    Log.i(TAG,"Button STOP run");
                }
            }
        });
    }

    private void bindService() {
        Intent i = new Intent(ScrollingActivity.this, MyService.class);
        bindService(i, mServiceConnection, BIND_AUTO_CREATE);
        startService(i);
        Log.i(TAG, "bindService invoked !");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        stopService(new Intent(ScrollingActivity.this, MyService.class));
        System.exit(0);
    }


}
