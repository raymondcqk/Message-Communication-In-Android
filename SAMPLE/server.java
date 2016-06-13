package com.imooc.messenger_server;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
/*
Message API����ժ¼
�ֶ�
public int arg1
���ֻ��Ҫ�洢�����������ݣ�arg1 �� arg2��setData()�ĵͳɱ����Ʒ��

public int arg2
���ֻ��Ҫ�洢�����������ݣ�arg1 �� arg2��setData()�ĵͳɱ����Ʒ��

public Messenger replyTo
ָ����message���͵��δ��Ŀ�ѡMessenger���󡣾����ʹ�÷����ɷ����ߺͽ����߾�����

public int what
�û��Զ������Ϣ���ţ����������߿����˽������Ϣ����Ϣ��ÿ��handler���԰����Լ�����Ϣ���룬���Բ��õ����Զ������Ϣ������handlers�г�ͻ��

public Object obj
���͸���������������󡣵�ʹ��Message�������̼߳䴫����Ϣʱ�����������һ��Parcelable�Ľṹ�ࣨ������Ӧ�ó���ʵ�ֵ��ࣩ�����ֶα���Ϊ�ǿգ�non-null�������������ݴ�����ʹ��setData(Bundle)������

������
public void setData (Bundle data)
public Bundle getData ()
*/
public class MessengerService extends Service
{

	//ͳһ����Ϣ��·�ı�־MSG_SUM = 0x110
    private static final int MSG_SUM = 0x110;
	
	/**
	* 1- ���� ����� Messenger��ʹ ���� HandlerΪ��������
	* ��Handler���ڴ���Message��
	*/
    //��û���HandlerThread����ʽ
    private Messenger mMessenger = new Messenger(new Handler()
    {
	/**
	* 2-�� Handler�д���ͻ��˴�������ϢMessage
	*/
        @Override
        public void handleMessage(Message msgfromClient)
        {
			/*
			3-��Message���У�ͨ���ͻ��˴�����Message--msgfromClient������һ��
			�����ظ��ͻ��˵�Message��--msgToClient
			������һ����Ϣ��·��
			*/
            Message msgToClient = Message.obtain(msgfromClient);//���ظ��ͻ��˵���Ϣ
			//�����ͻ��˴�������Ϣwhat�ֶ�
            switch (msgfromClient.what)
            {
                //msg �ͻ��˴�������Ϣ
                case MSG_SUM:
                    msgToClient.what = MSG_SUM;
                    try
                    {
                        //ģ���ʱ
                        Thread.sleep(2000);
                        msgToClient.arg2 = msgfromClient.arg1 + msgfromClient.arg2;
						//���ֻ��Ҫ�洢�����������ݣ�arg1 �� arg2��setData()�ĵͳɱ����Ʒ��
						/**
						ͨ��msgFromClient��replyTo���������Ϣ���ظ��ͻ���
						*/
                        msgfromClient.replyTo.send(msgToClient);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    } catch (RemoteException e)
                    {
                        e.printStackTrace();
                    }
                    break;
            }

            super.handleMessage(msgfromClient);
        }
    });

    @Override
    public IBinder onBind(Intent intent)
    {
		//!!!�����ʱ����Binder����ʽ
		//��������Messenger---�������ʹMessenger��������Messenger---�������ʹMessenger
	
        return mMessenger.getBinder();
    }
}