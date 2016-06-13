package com.imooc.messenger_server;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
/*
Message API部分摘录
字段
public int arg1
如果只需要存储几个整型数据，arg1 和 arg2是setData()的低成本替代品。

public int arg2
如果只需要存储几个整型数据，arg1 和 arg2是setData()的低成本替代品。

public Messenger replyTo
指明此message发送到何处的可选Messenger对象。具体的使用方法由发送者和接受者决定。

public int what
用户自定义的消息代号，这样接受者可以了解这个消息的信息。每个handler各自包含自己的消息代码，所以不用担心自定义的消息跟其他handlers有冲突。

public Object obj
发送给接收器的任意对象。当使用Message对象在线程间传递消息时，如果它包含一个Parcelable的结构类（不是由应用程序实现的类），此字段必须为非空（non-null）。其他的数据传输则使用setData(Bundle)方法。

方法：
public void setData (Bundle data)
public Bundle getData ()
*/
public class MessengerService extends Service
{

	//统一本消息回路的标志MSG_SUM = 0x110
    private static final int MSG_SUM = 0x110;
	
	/**
	* 1- 创建 服务端 Messenger信使 ，以 Handler为参数构造
	* （Handler用于处理Message）
	*/
    //最好换成HandlerThread的形式
    private Messenger mMessenger = new Messenger(new Handler()
    {
	/**
	* 2-在 Handler中处理客户端传来的消息Message
	*/
        @Override
        public void handleMessage(Message msgfromClient)
        {
			/*
			3-从Message池中，通过客户端传来的Message--msgfromClient来构建一个
			“返回给客户端的Message”--msgToClient
			（构成一个消息环路）
			*/
            Message msgToClient = Message.obtain(msgfromClient);//返回给客户端的消息
			//分析客户端传来的消息what字段
            switch (msgfromClient.what)
            {
                //msg 客户端传来的消息
                case MSG_SUM:
                    msgToClient.what = MSG_SUM;
                    try
                    {
                        //模拟耗时
                        Thread.sleep(2000);
                        msgToClient.arg2 = msgfromClient.arg1 + msgfromClient.arg2;
						//如果只需要存储几个整型数据，arg1 和 arg2是setData()的低成本替代品。
						/**
						通过msgFromClient的replyTo将服务端消息发回给客户端
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
		//!!!服务绑定时，以Binder的形式
		//返回上述Messenger---服务端信使Messenger返回上述Messenger---服务端信使Messenger
	
        return mMessenger.getBinder();
    }
}