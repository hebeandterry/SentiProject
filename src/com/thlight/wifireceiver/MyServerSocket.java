package com.thlight.wifireceiver;

import java.io.BufferedReader;//緩衝字符輸入流
import java.io.IOException;//IO相關錯誤
import java.io.InputStream;//輸入資料流
import java.io.InputStreamReader;//字符輸入流，將讀入的字節轉成字符
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;//伺服端Socket
import java.net.Socket;//建立網絡連接時使用的
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;//程序處理
import android.util.Log;

/********************************************
 * 此區的實作主要為TCP/IP,
 * 連線為一對一阻塞式,
 * 所以最好在新的thread內執行
********************************************/
public class MyServerSocket
{
	final int MSG_LOG = 1000;
	final int MSG_M_LOG = 2000;
	final int MSG_CLEAR_BD = 3000;
	//Server Port
	private static final int SERVER_HOST_PORT = 9527;
	public ServerSocket msSocket;
	
	public Handler mHandler;
	
	private Context context = null;
	//必須優雅的關閉不能讓使用者看到錯誤
	public boolean isClose;
	
	PrintWriter mpw = null;
	
	public MyServerSocket(Context context,Handler INHandle)
	{
		this.context = context;
		mHandler = INHandle;
		isClose = false; 
		
		openReceive(SERVER_HOST_PORT);
	}
	
	/********************************************
	 * function name: openReceive
	 * Input: NULL
	 * Output: NULL
	 * Remark: 建立ServerSocket
	********************************************/
	public void openReceive(int PORT)
	{
		try {
			msSocket = new ServerSocket(PORT);
			msSocket.setSoTimeout(10000);
			
			startReceive();
			Log.d("debug", "openReceive success:");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("debug", "openReceive fail:"+e.toString());
		}
	}
	
	/********************************************
	 * function name: startReceive
	 * Input: NULL
	 * Output: NULL
	 * Remark: 開始進入阻塞並接收訊息
	********************************************/
	public void startReceive()
	{
		new Thread(new Runnable() {
            public void run() 
            {
            	while(!isClose) {
            		Socket mSocket = null;
            		try {
            			
            			mSocket = msSocket.accept();//接收
            			
            			InputStream misIN = mSocket.getInputStream();//取得輸入資料流
            			BufferedReader mbr = new BufferedReader(new InputStreamReader(misIN));
            			
            			String strBuff = "";
            			String str = null;
            			while((str = mbr.readLine()) != null) //readLine()讀一行字串
            			{
            				//傳送訊息至主thread
            				strBuff = strBuff + str;
            				mHandler.obtainMessage(Constants.MSG_SHOW_TCPIP_DATA,0,0,str).sendToTarget(); 
            				
            				try {								
	            				JSONObject cmdObject = new JSONObject(str);
	            				int cmd = 0;
	            				if(!cmdObject.isNull("cmd"))
	            					cmd = cmdObject.getInt("cmd");
	            				
	            				mpw = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()), true);
	            			
	            				if(cmd == 3000)
	            				{
	            					ackAllData();
	            				}
	            				else
	            					mpw.println("ok");
            				} catch (Exception e) {
								// TODO: handle exception
							}
    
        				}//End while           			   			
           
            		} catch(IOException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            			Log.d("debug", "accept:"+e.toString());
            		} finally {
            			try {
            				if(mSocket != null) {
            					//mSocket.shutdownOutput();//關閉輸出
            					mSocket.close();
            					mSocket=null;
            				}
            			} catch(IOException e) {
            				// TODO Auto-generated catch block
            				//e.printStackTrace();
            			}
            		}//End try
            	}//End while
            	close();
            }
		}).start();
	}
	
	/********************************************
	 * function name: closeReceive
	 * Input: NULL
	 * Output: NULL
	 * Remark: 通知外部Thread主Thread已關閉
	********************************************/
	public void closeReceive()
	{
		isClose = true;
	}
	
	/********************************************
	 * function name: close
	 * Input: NULL
	 * Output: NULL
	 * Remark: 關閉ServerSocket
	********************************************/
	public void close()
	{
		closeReceive();
		if(msSocket != null)
			try {
				msSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		msSocket = null;
		
	}
	/********************************************/
	public void ackAllData()
	{
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("ack", Constants.TCPIP_ACK_ALL_DATA);
			jsonObject.put("upload_time", THLApp.upload_time);
			jsonObject.put("start_scan_time", THLApp.scan_time);
			jsonObject.put("stop_scan_time", THLApp.stop_scan_time);
			jsonObject.put("server_type", THLApp.serverType);
			jsonObject.put("url", THLApp.url);
			jsonObject.put("uuid_filter", THLApp.uuid);
			jsonObject.put("algorithm", THLApp.Algorithm);
			jsonObject.put("mode", THLApp.Mode);
			jsonObject.put("ssid", THLApp.ssid);
			jsonObject.put("key", THLApp.key);
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			Date curDate = new Date(System.currentTimeMillis());
			jsonObject.put("time", formatter.format(curDate));
			
			jsonObject.put("bt_mac", THLApp.BTMac);
			jsonObject.put("wifi_mac", THLApp.WifiMac);
			
			if(mpw != null)
				mpw.println(jsonObject.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}