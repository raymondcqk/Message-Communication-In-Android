package com.imooc.messenger_client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
	
	//统一本消息回路的标志MSG_SUM = 0x110---消息代号
    private static final int MSG_SUM = 0x110;

    private Button mBtnAdd;
    private LinearLayout mLyContainer;
    //显示连接状态
    private TextView mTvState;

	//用来引用 服务绑定时，从服务端传来的Messenger，也就代表着服务本身，因为可以通过该Messenger操控Service方法与状态
    private Messenger mService;
	
    private boolean isConn;

	
	/**
	* 1- 创建客户端信使Messenger ，以 Handler为参数构造
	* （Handler用于处理Message）
	*/
	
    private Messenger mMessenger = new Messenger(new Handler()
    {
        /**
		* 2-在 Handler中处理服务端传来的消息Message
		*/
		@Override
        public void handleMessage(Message msgFromServer)
        {
            switch (msgFromServer.what)//分析客户端传来的消息what字段
            {
                case MSG_SUM:
                    TextView tv = (TextView) mLyContainer.findViewById(msgFromServer.arg1);
                    tv.setText(tv.getText() + "=>" + msgFromServer.arg2);
                    break;
            }
            super.handleMessage(msgFromServer);
        }
    });


    private ServiceConnection mConn = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            //通过Messenger的构造函数，传入服务端返回的IBinder，还原服务端Messenger信使
			mService = new Messenger(service);
            
			//成功取得服务端Messenger，代表服务绑定成功
			isConn = true;
            mTvState.setText("connected!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mService = null;
            isConn = false;
            mTvState.setText("disconnected!");
        }
    };

    private int mA;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //开始绑定服务
        bindServiceInvoked();

        mTvState = (TextView) findViewById(R.id.id_tv_callback);
        mBtnAdd = (Button) findViewById(R.id.id_btn_add);
        mLyContainer = (LinearLayout) findViewById(R.id.id_ll_container);

        mBtnAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    int a = mA++;
                    int b = (int) (Math.random() * 100);

                    //创建一个tv,添加到LinearLayout中
                    TextView tv = new TextView(MainActivity.this);
                    tv.setText(a + " + " + b + " = caculating ...");
                    tv.setId(a);
                    mLyContainer.addView(tv);
					
					//创建客户端消息（发送给服务端的），同时将消息代号、数据a、b封装
					//参数解释--Handler h（接收此消息的Handler对象）, int what, int arg1, int arg2
                    Message msgFromClient = Message.obtain(null, MSG_SUM, a, b);
                    //指明此message发送到何处的可选Messenger对象，再服务端获得该Message后，通过改relyTo来发送数据，Message就会返回到客户端的Messenger处理
					msgFromClient.replyTo = mMessenger;
					//!!!先判断服务是否成功启动并绑定--活动与服务通信连接起来
                    if (isConn)
                    {
                        //往服务端发送消息
                        mService.send(msgFromClient);
						//通过服务端信使Messenger对象，发送Message（客户端消息）
						//!!!!希望那个Messenger对象接收，就用哪个Messenger发送
                    }
                } catch (RemoteException e)
                {
                    e.printStackTrace();
                }
            }
        });

    }

    private void bindServiceInvoked()
    {
        Intent intent = new Intent();
        intent.setAction("com.zhy.aidl.calc");
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        Log.e(TAG, "bindService invoked !");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unbindService(mConn);
    }


}