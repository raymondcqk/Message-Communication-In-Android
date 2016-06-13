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
	
	//ͳһ����Ϣ��·�ı�־MSG_SUM = 0x110---��Ϣ����
    private static final int MSG_SUM = 0x110;

    private Button mBtnAdd;
    private LinearLayout mLyContainer;
    //��ʾ����״̬
    private TextView mTvState;

	//�������� �����ʱ���ӷ���˴�����Messenger��Ҳ�ʹ����ŷ�������Ϊ����ͨ����Messenger�ٿ�Service������״̬
    private Messenger mService;
	
    private boolean isConn;

	
	/**
	* 1- �����ͻ�����ʹMessenger ���� HandlerΪ��������
	* ��Handler���ڴ���Message��
	*/
	
    private Messenger mMessenger = new Messenger(new Handler()
    {
        /**
		* 2-�� Handler�д������˴�������ϢMessage
		*/
		@Override
        public void handleMessage(Message msgFromServer)
        {
            switch (msgFromServer.what)//�����ͻ��˴�������Ϣwhat�ֶ�
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
            //ͨ��Messenger�Ĺ��캯�����������˷��ص�IBinder����ԭ�����Messenger��ʹ
			mService = new Messenger(service);
            
			//�ɹ�ȡ�÷����Messenger���������󶨳ɹ�
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

        //��ʼ�󶨷���
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

                    //����һ��tv,��ӵ�LinearLayout��
                    TextView tv = new TextView(MainActivity.this);
                    tv.setText(a + " + " + b + " = caculating ...");
                    tv.setId(a);
                    mLyContainer.addView(tv);
					
					//�����ͻ�����Ϣ�����͸�����˵ģ���ͬʱ����Ϣ���š�����a��b��װ
					//��������--Handler h�����մ���Ϣ��Handler����, int what, int arg1, int arg2
                    Message msgFromClient = Message.obtain(null, MSG_SUM, a, b);
                    //ָ����message���͵��δ��Ŀ�ѡMessenger�����ٷ���˻�ø�Message��ͨ����relyTo���������ݣ�Message�ͻ᷵�ص��ͻ��˵�Messenger����
					msgFromClient.replyTo = mMessenger;
					//!!!���жϷ����Ƿ�ɹ���������--������ͨ����������
                    if (isConn)
                    {
                        //������˷�����Ϣ
                        mService.send(msgFromClient);
						//ͨ���������ʹMessenger���󣬷���Message���ͻ�����Ϣ��
						//!!!!ϣ���Ǹ�Messenger������գ������ĸ�Messenger����
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