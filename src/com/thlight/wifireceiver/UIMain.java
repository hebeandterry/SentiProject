package com.thlight.wifireceiver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

public class UIMain extends Activity implements View.OnClickListener , UncaughtExceptionHandler{
		
	ScrollView sv = null;
	TextView tv = null;
	TextView tv_time = null;
	TextView tv_bt_mac = null;
	TextView tv_wifi_mac = null;
	

	EditText et_upload_time = null;
	EditText et_scan_time 	= null;
	EditText et_url 		= null;
	EditText et_filter_uuid = null;
	EditText et_stop_scan_time = null;
	EditText et_ssid = null;
	EditText et_key = null;
	
	Button btn_save = null;
	Button btn_start_receiver = null;
	
	RadioGroup rg = null;
	RadioGroup rg_avg = null;
	RadioGroup rg_mode = null;
	
	RadioButton radio0 = null;
	RadioButton radio1 = null;
	RadioButton radio01 = null;
	RadioButton radio02 = null;
	RadioButton radio001 = null;
	RadioButton radio002 = null;
	
	TextClock dc = null;
	
	String MessageString = "";
	
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	int PlayerCount = 1;
	int count = 0;
	
	boolean isUpdated = true;
	
	ArrayList<BeaconInfo> beaconList = new ArrayList<BeaconInfo>();
	ArrayList<BeaconInfo> tempList = new ArrayList<BeaconInfo>();
	
	usbSerialPortManager usbSerialPortManager = null;
	ApManager apManager = null;
	MyServerSocket myServerSocket = null;
	TimeManager timeManager = null;
	
	BluetoothAdapter mBTAdapter		= BluetoothAdapter.getDefaultAdapter();
	
	WifiManager wifiManager = null;
	WifiConfiguration wifiConfig = null;
	
	//String deviceBTMac = "";
	//String WifiMac = "";
	
	String ServerType = "";
	String Algorithm = "";
	String Mode = "";
	
	String iScanUploadUrl = "http://iscan.atlasyun.com/tagger/multiPost";
	//String iScanUploadUrl = "http://52.69.77.170:3000/tagger/multiPost";	
	String[] strs = null;
	String appVersion;

    Boolean bUploadStart = false;

    ScheduledFuture<?> scheduledFuture ;   // For cancel the MyTask.
//	
//	int scan_time = 0;
//	int upload_time = 1000;

    class UploadDataToServer implements Runnable {

        @Override
        public void run() {
            try {

                if(!THLApp.BTMac.equals(""))
                {
                    isUpdated = false;
                    //mHandler.removeMessages(Constants.MSG_RECEIVE);
                    parseBeaconDataToList();
                    tempList = null;
                    tempList = cloneList(beaconList);
                    final ArrayList<BeaconInfo> tempListForUpload = cloneList(beaconList);
                    beaconList.clear();
                    Log.d("debug", "beacon size:"+tempListForUpload.size());

                    if(tempList.isEmpty())
                    {
                        BeaconInfo beaconInfo = new BeaconInfo();
                        Date curDate = new Date(System.currentTimeMillis());

                        beaconInfo.time = formatter.format(curDate);
                        beaconInfo.major = "0";
                        beaconInfo.minor = "0";
                        beaconInfo.uuid = "";
                        beaconInfo.scanned_mac = "FF:FF:FF:FF:FF:FF";
                        beaconInfo.rssi = "0";
                        beaconInfo.count = 1;
                        tempList.add(beaconInfo);
						curDate = null;
                        beaconInfo = null;
                    }

                    if(!tempList.isEmpty())
                    {
                        new Thread(new Runnable(){
                            @Override
                            public void run()
                            {
                                int count  = 0;
                                //Log.d("debug", "beacon size:"+tempList.size());
                                //Log.d("debug", "beacon size:"+tempList.get(300));
                                for(int i = 0 ; i < tempListForUpload.size(); i++)
                                {
                                    //Log.d("debug", "beacon("+ i +")count:"+tempList.get(i).count);
                                    count = count + tempListForUpload.get(i).count;
                                }
                                Log.d("debug", "beacon total count:"+ count);

                                if(THLApp.serverType.equals("Customize") && (URLUtil.isHttpUrl(THLApp.url) || URLUtil.isHttpsUrl(THLApp.url)))
                                    SendCustomizeDataToServer(tempListForUpload);
                                else if(THLApp.serverType.equals("iScan"))
                                    SendIScanDataToServer();

                                tempListForUpload.clear();
                                isUpdated = true;
                            }
                        }).start();
                    }
                    else
                    {
                        isUpdated = true;
                    }
                }
            }
            catch (Throwable e) {
                Log.d("debug", "MSG_UPLOAD fail.");
            }
        }
    }

	Handler mHandler= new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case Constants.MSG_RECEIVE:		
					strs = new String[usbSerialPortManager.deviceSize];
					for(int i = 0 ; i < usbSerialPortManager.deviceSize ; i++)
					{
						byte[] buf = new byte[2000]; 
						int re = usbSerialPortManager.getSerialPortData(buf,i);
						if(MessageString.length() < 25000)
						{
							if(re != -1)
							{
								for(int j = 0 ; j< re ;j++)
								{
	//								strs[i] +=(char)buf[j];
									MessageString +=(char)buf[j];
								}
	
								buf = null;
								//Log.d("debug", "re:"+re);
								//tv.setText(MessageString);
							}
						}
						else
						{
							parseBeaconDataToList();
                            buf = null;
						}
					}
					mHandler.sendEmptyMessageDelayed(Constants.MSG_RECEIVE,10);
					break;
				case Constants.MSG_UPLOAD:
					//if(isUpdated && !THLApp.BTMac.equals(""))
					if(!THLApp.BTMac.equals(""))
					{
						isUpdated = false;
						//mHandler.removeMessages(Constants.MSG_RECEIVE);
						parseBeaconDataToList();
						tempList = null;
						tempList = cloneList(beaconList);
						beaconList.clear();
						Log.d("debug", "beacon size:"+tempList.size());
						
						if(tempList.isEmpty())
						{
							BeaconInfo beaconInfo = new BeaconInfo();
							Date curDate = new Date(System.currentTimeMillis());
						
							beaconInfo.time = formatter.format(curDate);
							beaconInfo.major = "0";
							beaconInfo.minor = "0";
							beaconInfo.uuid = "";
							beaconInfo.scanned_mac = "FF:FF:FF:FF:FF:FF";
							beaconInfo.rssi = "0";
							beaconInfo.count = 1;
							tempList.add(beaconInfo);
						}
						
						if(!tempList.isEmpty())
						{
							new Thread(new Runnable(){	
			                    @Override
			                    public void run() 
			                    {
			                    	int count  = 0;
			                    	//Log.d("debug", "beacon size:"+tempList.size());
			                    	//Log.d("debug", "beacon size:"+tempList.get(300));
			                    	for(int i = 0 ; i < tempList.size(); i++)
			                    	{
			                    		//Log.d("debug", "beacon("+ i +")count:"+tempList.get(i).count);
			                    		count = count + tempList.get(i).count;
			                    	}
			                    	Log.d("debug", "beacon total count:"+ count);
			                    	
			                    	if(THLApp.serverType.equals("Customize") && (URLUtil.isHttpUrl(THLApp.url) || URLUtil.isHttpsUrl(THLApp.url)))
				                    	//SendCustomizeDataToServer();
			                    	//else if(THLApp.serverType.equals("iScan"))
			                    		SendIScanDataToServer();
			                    				                    	
			                    	tempList.clear();
			                    	isUpdated = true;
			                    }
			            	}).start();	
						}
						else
						{
							isUpdated = true;
						}
					}

					break;
				case Constants.MSG_SHOW_MAC:
					THLApp.loadCountSettings();
					
					Log.d("debug", THLApp.count+"");
					
					if(THLApp.count%2 == 0)
					{
						THLApp.count++;
						THLApp.saveCountSettings();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						ActivityManager activityManager			= (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
					    List<RunningAppProcessInfo> procInfos	= activityManager.getRunningAppProcesses();

					    if(null != procInfos)
					    {
				    	    for(RunningAppProcessInfo procInfo : procInfos)
				    	    {
				    	        if(procInfo.processName.equals("com.thlight.wifireceiver") && (procInfo.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND
				    	           || procInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND))
				    	        {
									usbSerialPortManager.stopScanAll();
				    	        	android.os.Process.killProcess(procInfo.pid);
				    	        	Log.d("debug", "killProcess");
				    	        	break;
				    	        }
				    	    }
					    }
					}
					else
					{
						THLApp.count++;
						THLApp.saveCountSettings();
					}
					wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					if(wifiManager != null)
					{
						WifiInfo wifiInf = wifiManager.getConnectionInfo();
						if(wifiInf == null)
						{
							Log.d("debug","wifi inf is null");
							mHandler.sendEmptyMessageDelayed(Constants.MSG_SHOW_MAC, 3000);
							return;
						}
			        	THLApp.WifiMac = wifiInf.getMacAddress();
			        	if(THLApp.WifiMac == null)
			        	{
			        		Log.d("debug","WifiMac is null");
							mHandler.sendEmptyMessageDelayed(Constants.MSG_SHOW_MAC, 3000);
							return;
			        	}
			        	else
			        	{
			        		THLApp.WifiMac = THLApp.WifiMac.toUpperCase();
			        		THLApp.BTMac = THLApp.WifiMac;
			        	}
			        	tv_bt_mac.setText(THLApp.BTMac);
			        	tv_wifi_mac.setText(THLApp.WifiMac);
			        	tv.setText(usbSerialPortManager.DeviceInfoString+"\n"+"BT MAC:"+THLApp.BTMac+",WIFI MAC:"+THLApp.WifiMac);
			        	changeUITime();
			        	
			        	if(!ApManager.isApOn(UIMain.this))
			            {
			            	wifiManager.setWifiEnabled(false);
			            	ApManager.configApState(UIMain.this); // change Ap state :boolean
			            }
			            mHandler.postDelayed(new Runnable() {
			    			public void run() {
			    				changeToApMode();
			    			}
			    		}, 300000);
			        	
			        	Log.d("debug", "BT MAC:"+THLApp.BTMac+",Wifi MAC:"+THLApp.WifiMac);
					}
					else
					{
						mHandler.sendEmptyMessageDelayed(Constants.MSG_SHOW_MAC, 3000);
					}
					usbSerialPortManager.startScanAll();
					break;
				case Constants.MSG_CONNECT_SUCCESS:
					tv.setText("connect success");
					break;
				case Constants.MSG_SHOW_TCPIP_DATA:
					String outString = (String)msg.obj;
					Toast.makeText(UIMain.this, outString, Toast.LENGTH_LONG).show();
					try {
						parseTcpipData(outString);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.d("debug",outString);
				break;
				case Constants.MSG_SHOW_TIME:
					changeUITime();
					break;
				case Constants.MSG_CHANGE_AP_WIFI:
					if(ApManager.isApOn(UIMain.this))
			        {
			        	boolean isChangeApSuccess = ApManager.configApState(UIMain.this);
			        	boolean isWifiEnableSuccess = wifiManager.setWifiEnabled(true);
			        	Log.d("debug", "AP is on,isChangeApSuccess:"+isChangeApSuccess+",isWifiEnableSuccess:"+isWifiEnableSuccess);
			        }   
			        else
			        {
			        	Log.d("debug", "AP is off");
			        	if(!wifiManager.isWifiEnabled())
			            {
			        		boolean isWifiEnableSuccess = wifiManager.setWifiEnabled(true);
			            	Log.d("debug", "AP is off,isWifiEnableSuccess:"+isWifiEnableSuccess);
			            }
			        }
				break;
				case Constants.MSG_DELETE_AW_FILE:
					File file = new File(Environment.getExternalStorageDirectory().toString());
					File[] files = file.listFiles();
					for(int i = 0; i<files.length;i++)
					{
						Log.d("debug",files[i].getName());
						if(files[i].getName().startsWith("aw_"))
						{
							files[i].delete();
						}
					}
					mHandler.sendEmptyMessageDelayed(Constants.MSG_DELETE_AW_FILE, 3600000);
					break;
                case Constants.MSG_STATR_UPLOAD_TASK:

                    // Avoid running two same tasks.
                    if (bUploadStart == false) {
                        // Start upload task repeatedly  every THLApp.upload_time.
                        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                        long period = Integer.valueOf(THLApp.upload_time);//1000; // the period between successive executions
                        scheduledFuture = exec.scheduleAtFixedRate(new UploadDataToServer(), period, period, TimeUnit.MILLISECONDS);
                        bUploadStart = true;
                    }
                    else
                    {
                        // do nothing;
                    }
                    break;
			}
		}
	};
	/*************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_main);
   
        PackageManager manager = this.getPackageManager();
        try { PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
        appVersion = info.versionName; //版本名
        } catch (NameNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }
        System.out.println("version:" + appVersion);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        
        Thread.setDefaultUncaughtExceptionHandler(this);

        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        long period = Integer.valueOf(THLApp.upload_time);//1000; // the period between successive executions
        //scheduledFuture = exec.scheduleAtFixedRate(new MyTask(), period, period, TimeUnit.MILLISECONDS);


        usbSerialPortManager = new usbSerialPortManager(this);
        usbSerialPortManager.setHandler(mHandler);    
        
        THLApp.usbSerialPortManager = usbSerialPortManager;
        
        apManager = new ApManager(this);
        apManager.setHandler(mHandler);
        
        THLApp.apManager = apManager;
        
        myServerSocket = new MyServerSocket(this, mHandler);
        
        timeManager = new TimeManager(this);
        
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiConfig = new WifiConfiguration();       
       
        tv = (TextView)findViewById(R.id.tv);
        tv_time = (TextView)findViewById(R.id.tv_time);
        tv_bt_mac = (TextView)findViewById(R.id.tv_bt_mac);
        tv_wifi_mac = (TextView)findViewById(R.id.tv_wifi_mac);
        
        dc = (TextClock)findViewById(R.id.dc);

        sv = (ScrollView)findViewById(R.id.sv);
        
        btn_save = (Button)findViewById(R.id.btn_save);
        btn_save.setOnClickListener(this);
        btn_start_receiver = (Button)findViewById(R.id.btn_start_receiver);
        btn_start_receiver.setOnClickListener(this);
        
        et_upload_time = (EditText)findViewById(R.id.et_upload_time);
        et_scan_time = (EditText)findViewById(R.id.et_scan_time);
        et_url = (EditText)findViewById(R.id.et_url);
        et_filter_uuid = (EditText)findViewById(R.id.et_filter_uuid);
        et_stop_scan_time = (EditText)findViewById(R.id.et_stop_scan_time);
        et_ssid = (EditText)findViewById(R.id.et_ssid);
        et_key = (EditText)findViewById(R.id.et_key);
        
        rg = (RadioGroup)findViewById(R.id.rg);
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener() 
        {		
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				RadioButton rb = (RadioButton)findViewById(checkedId);
				Log.d("debug", "rg:"+checkedId+","+rb.getText().toString());
				if(rb.getText().toString().equals("iScan"))
				{
					et_url.setEnabled(false);
					THLApp.serverType = "iScan";
				}
				else
				{
					et_url.setEnabled(true);
					THLApp.serverType = "Customize";
				}
			}
		});
        for(int i = 0 ; i < rg.getChildCount() ; i++)
        {
        	RadioButton rb = (RadioButton) rg.getChildAt(i);
        	if(rb.getText().toString().equals(THLApp.serverType))
        	{
        		ServerType = THLApp.serverType;
        		rb.setChecked(true);
        		break;
        	}
        }
        
        rg_avg = (RadioGroup)findViewById(R.id.rg_avg);
        rg_avg.setOnCheckedChangeListener(new OnCheckedChangeListener() 
        {		
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				RadioButton rb = (RadioButton)findViewById(checkedId);
				Log.d("debug", "rg_avg:"+checkedId+","+rb.getText().toString());
				if(rb.getText().toString().equals("None"))
				{
					THLApp.Algorithm = "None";
				}
				else
				{
					//et_url.setEnabled(true);
					THLApp.Algorithm = "AVG";
				}
			}
		});
        for(int i = 0 ; i < rg_avg.getChildCount() ; i++)
        {
        	RadioButton rb = (RadioButton) rg_avg.getChildAt(i);
        	if(rb.getText().toString().equals(THLApp.Algorithm))
        	{
        		Algorithm = THLApp.Algorithm;
        		rb.setChecked(true);
        		break;
        	}
        }
        
        rg_mode = (RadioGroup)findViewById(R.id.rg_mode);
        rg_mode.setOnCheckedChangeListener(new OnCheckedChangeListener() 
        {		
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				RadioButton rb = (RadioButton)findViewById(checkedId);
				Log.d("debug", "rg_mode:"+checkedId+","+rb.getText().toString());
				if(rb.getText().toString().equals("Wifi"))
				{
					THLApp.isEthernetOn = false;
					THLApp.Mode = "Wifi";
				}
				else
				{
					//et_url.setEnabled(true);
					THLApp.isEthernetOn = true;
					THLApp.Mode = "Ethernet";
				}
			}
		});
        for(int i = 0 ; i < rg_mode.getChildCount() ; i++)
        {
        	RadioButton rb = (RadioButton) rg_mode.getChildAt(i);
        	if(rb.getText().toString().equals(THLApp.Mode))
        	{
        		Mode = THLApp.Mode;
        		rb.setChecked(true);
        		break;
        	}
        }
//        String serial = "";
//        if(VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
//            serial = Build.SERIAL;
//        }
//        
//        //serial name not mac 
//        THLApp.BTMac = serial;
	            
        //THLApp.BTMac = mBTAdapter.getAddress().toUpperCase();         
       
        et_upload_time.setText(THLApp.upload_time);
        et_scan_time.setText(THLApp.scan_time);
        et_url.setText(THLApp.url);
        et_filter_uuid.setText(THLApp.uuid);
        et_stop_scan_time.setText(THLApp.stop_scan_time);
        et_ssid.setText(THLApp.ssid);
        et_key.setText(THLApp.key);   
        
        radio0 = (RadioButton) findViewById(R.id.radio0);
        radio1 = (RadioButton) findViewById(R.id.radio1);
        radio01 = (RadioButton) findViewById(R.id.radio01);
        radio02 = (RadioButton) findViewById(R.id.radio02);
        radio001 = (RadioButton) findViewById(R.id.radio001);
        radio002 = (RadioButton) findViewById(R.id.radio002);
        
        if (dm.widthPixels <= 800)
        {
            radio0.setScaleX((float)0.5);
            radio0.setScaleY((float)0.5);
            radio1.setScaleX((float)0.5);
            radio1.setScaleY((float)0.5);
            radio01.setScaleX((float)0.5);
            radio01.setScaleY((float)0.5);
            radio02.setScaleX((float)0.5);
            radio02.setScaleY((float)0.5);
            radio001.setScaleX((float)0.5);
            radio001.setScaleY((float)0.5);
            radio002.setScaleX((float)0.5);
            radio002.setScaleY((float)0.5);
        }
        else
        {
            radio0.setScaleX((float)0.9);
            radio0.setScaleY((float)0.9);
            radio1.setScaleX((float)0.9);
            radio1.setScaleY((float)0.9);
            radio01.setScaleX((float)0.9);
            radio01.setScaleY((float)0.9);
            radio02.setScaleX((float)0.9);
            radio02.setScaleY((float)0.9);
            radio001.setScaleX((float)0.9);
            radio001.setScaleY((float)0.9);
            radio002.setScaleX((float)0.9);
            radio002.setScaleY((float)0.9);
        }

          
        ApManager.turnEthOnOrOff();
        
        mHandler.sendEmptyMessageDelayed(Constants.MSG_DELETE_AW_FILE, 1);
        
        mHandler.sendEmptyMessageDelayed(Constants.MSG_CHANGE_AP_WIFI, 5000);
        
        mHandler.sendEmptyMessageDelayed(Constants.MSG_SHOW_MAC, 10000);    
        
        
//        TestLED testLED = new TestLED();
//        testLED.start();
    }
    /** ================================================ */
   	@Override
   	public void onResume()
	{
		super.onResume();
		usbSerialPortManager.USBintial();
		Log.d("debug", "onResume");
	}
    /** ================================================ */
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		usbSerialPortManager.stopScanAll();
		usbSerialPortManager.isOurDevice = false;
		usbSerialPortManager.unregisterReceiver();
		apManager.unregisterReceiver();
		
		mHandler.removeMessages(Constants.MSG_RECEIVE);
		mHandler.removeMessages(Constants.MSG_UPLOAD);
		mHandler.removeMessages(Constants.MSG_SHOW_TIME);
		mHandler.removeMessages(Constants.MSG_DELETE_AW_FILE);
		myServerSocket.closeReceive();
        bUploadStart = false;
        scheduledFuture.cancel(false);        //Cancel upload data to server.
		System.exit(0);
	}
	
    /** ================================================ */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id= v.getId();
    	
    	switch(id)
    	{
    		case R.id.btn_save:
    			if(!et_upload_time.getText().toString().trim().equals("") 
    			|| !et_scan_time.getText().toString().trim().equals("")
    			|| !et_stop_scan_time.getText().toString().trim().equals(""))
    			{
    				THLApp.upload_time = et_upload_time.getText().toString().trim();
    				THLApp.scan_time = et_scan_time.getText().toString().trim();
    				THLApp.url = et_url.getText().toString().trim();
    				THLApp.uuid = et_filter_uuid.getText().toString().trim();
    				THLApp.stop_scan_time = et_stop_scan_time.getText().toString().trim();
//    				THLApp.serverType = ServerType;
//    				THLApp.Algorithm = Algorithm;
//    				THLApp.Mode = Mode;
    				THLApp.ssid = et_ssid.getText().toString();
    				THLApp.key = et_key.getText().toString();
    				
    				THLApp.saveSettings();
    				
    				beaconList.clear();
    				tempList.clear();
    				
//    				if(apManager.isNetworkConnected())
//    				{  				
//	    				usbSerialPortManager.stopScanAll();	    				
//	    				usbSerialPortManager.startScanAll();
//    				}
    				
    				tv.setText("Save success");
    			}
    			else
    			{
    				runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(UIMain.this, "欄位不得為空", Toast.LENGTH_LONG).show();
						}
					});
    			}
    			break;
    		case R.id.btn_start_receiver:
    			changeToApMode();
    			break;
    	}
	}
	
	
	/***************************************************************/
	public static ArrayList<BeaconInfo> cloneList(ArrayList<BeaconInfo> list) {
		ArrayList<BeaconInfo> clone = new ArrayList<BeaconInfo>(list.size());
	    for(BeaconInfo item: list) clone.add(item.clone());
	    return clone;
	}
	/*****************************************************/
	@SuppressWarnings("deprecation")
	public void httpUpdateDevice(String url,List<NameValuePair> params)
	{
		//Log.d("Terry", "params : " + params.toString());
				
		String strResult 	= "";
		
		String body;
		
        HttpPost post = new HttpPost(url);
    
        try {
        	
        	post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        	//post.addHeader("content-type", "application/json");
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
            HttpConnectionParams.setSoTimeout(httpParameters, 10000);
            
            HttpResponse httpResponse = new DefaultHttpClient(httpParameters).execute(post);

            //Log.d("Terry", "HTTP Status: " + httpResponse.getStatusLine().getStatusCode());
            if(httpResponse.getStatusLine().getStatusCode()==200){

                strResult = EntityUtils.toString(httpResponse.getEntity());
                Log.d("debug", ""+httpResponse.getStatusLine().getStatusCode()+",result="+strResult);
              //  mHandler.sendEmptyMessageDelayed(Constants.MSG_UPLOAD, 900);//Integer.valueOf(THLApp.upload_time));
				//mHandler.sendEmptyMessageDelayed(Constants.MSG_RECEIVE,10);
            }
            else
            {
            	strResult = EntityUtils.toString(httpResponse.getEntity());
            	Log.d("debug", ""+httpResponse.getStatusLine().getStatusCode()+",result="+strResult);
            	//mHandler.sendEmptyMessageDelayed(Constants.MSG_UPLOAD, Integer.valueOf(THLApp.upload_time));
				//mHandler.sendEmptyMessageDelayed(Constants.MSG_RECEIVE,10);
            }
        } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("debug", ""+e.toString());
			//mHandler.sendEmptyMessageDelayed(Constants.MSG_UPLOAD, Integer.valueOf(THLApp.upload_time));
		//	mHandler.sendEmptyMessageDelayed(Constants.MSG_RECEIVE,10);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("debug", ""+e.toString());
			//mHandler.sendEmptyMessageDelayed(Constants.MSG_UPLOAD, Integer.valueOf(THLApp.upload_time));
		//	mHandler.sendEmptyMessageDelayed(Constants.MSG_RECEIVE,10);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("debug", ""+e.toString());
			//mHandler.sendEmptyMessageDelayed(Constants.MSG_UPLOAD, Integer.valueOf(THLApp.upload_time));
			//mHandler.sendEmptyMessageDelayed(Constants.MSG_RECEIVE,10);
		}

	}
	
	/** ===================================================================================== */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub		
		
		saveErrorLog(ex);
		
		usbSerialPortManager.stopScanAll();
		
//		PendingIntent intent = PendingIntent.getActivity(UIMain.this, 0,
//	            new Intent(getIntent()), getIntent().getFlags());
//		AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, intent);
		System.exit(2);
	}
	
	/**======================================================================================**/
	public boolean saveErrorLog(Throwable ex)
	{
		Date curDate = new Date(System.currentTimeMillis());
			
		
		String SavePath = THLApp.STORE_PATH + "error_log.txt";
		
		Writer writer = new StringWriter();  
        PrintWriter printWriter = new PrintWriter(writer);  
        ex.printStackTrace(printWriter);  
        Throwable cause = ex.getCause();  
        while (cause != null) {  
            cause.printStackTrace(printWriter);  
            cause = cause.getCause();  
        }  
        printWriter.close();  
        String result = writer.toString();  
        
        Log.d("debug","crash:"+result);
		
		try {
			FileWriter fw = new FileWriter(SavePath,true);
		   	BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
  	        bw.write(formatter.format(curDate)+":"+result);
  	        bw.newLine();
  	        bw.close();  
  	        return true;
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		   e.printStackTrace();
	    }
		return false;		
	}
	/**======================================================================================
	 * @throws JSONException */
	public void parseTcpipData(String tcpipString) throws JSONException
	{
		JSONObject jsonObject = new JSONObject(tcpipString);
		
		int cmd = 0;
		if(!jsonObject.isNull("cmd"))
			cmd = jsonObject.getInt("cmd");
		
		switch (cmd)
		{
//			case Constants.TCPIP_SET_UPLOAD_TIME:
//				String upload_time = "";
//				if(!jsonObject.isNull("upload_time"))
//				{
//					upload_time = jsonObject.getString("upload_time");
//					et_upload_time.setText(upload_time);
//					btn_save.performClick();
//				}
//				break;
//			case Constants.TCPIP_SET_SCAN_TIME:
//				String start_scan_time = "";
//				String stop_scan_time = "";
//				if(!jsonObject.isNull("start_scan_time"))
//				{
//					start_scan_time = jsonObject.getString("start_scan_time");
//					et_scan_time.setText(start_scan_time);
//				}
//				if(!jsonObject.isNull("stop_scan_time"))
//				{
//					stop_scan_time = jsonObject.getString("stop_scan_time");
//					et_stop_scan_time.setText(stop_scan_time);
//				}
//				if(!start_scan_time.equals("") && !stop_scan_time.equals(""))
//				{
//					btn_save.performClick();
//				}
//				break;
//			case Constants.TCPIP_SET_SERVER_TYPE_AND_URL:
//				String server_type = "";
//				String url = "";
//				if(!jsonObject.isNull("server_type"))
//				{
//					server_type = jsonObject.getString("server_type");
//					for(int i = 0 ; i < rg.getChildCount() ; i++)
//			        {
//			        	RadioButton rb = (RadioButton) rg.getChildAt(i);
//			        	if(rb.getText().toString().equals(server_type))
//			        	{
//			        		rb.setChecked(true);
//			        		break;
//			        	}
//			        }
//				}
//				if(!jsonObject.isNull("url"))
//				{
//					url = jsonObject.getString("url");
//					et_url.setText(url);
//				}
//				if(!server_type.equals(""))
//				{
//					btn_save.performClick();
//				}
//				break;
//			case Constants.TCPIP_SET_ALGORITHM:
//				String algorithm = "";
//				if(!jsonObject.isNull("algorithm"))
//				{
//					algorithm = jsonObject.getString("algorithm");
//					for(int i = 0 ; i < rg_avg.getChildCount() ; i++)
//			        {
//			        	RadioButton rb = (RadioButton) rg_avg.getChildAt(i);
//			        	if(rb.getText().toString().equals(algorithm))
//			        	{
//			        		rb.setChecked(true);
//			        		break;
//			        	}
//			        }
//				}
//				if(!algorithm.equals(""))
//				{
//					btn_save.performClick();
//				}
//				break;
//			case Constants.TCPIP_SET_UUID_FILTER:
//				String uuid_filter = "";
//				if(!jsonObject.isNull("uuid_filter"))
//				{
//					uuid_filter = jsonObject.getString("uuid_filter");
//					et_filter_uuid.setText(uuid_filter);
//					btn_save.performClick();
//				}
//				break;
//			case Constants.TCPIP_SET_SSID_AND_KEY:
//				if(!jsonObject.isNull("ssid"))
//				{
//					THLApp.ssid = jsonObject.getString("ssid");
//					et_ssid.setText(THLApp.ssid);
//				}
//				if(!jsonObject.isNull("key"))
//				{
//					THLApp.key = jsonObject.getString("key");
//					et_key.setText(THLApp.key);
//				}
//				
//				myServerSocket.ackAllData();
//				
//				if(!THLApp.ssid.equals("") && !THLApp.key.equals(""))
//				{
//					if(!ApManager.isApOn(UIMain.this))
//					{
//						wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//				        wifiConfig = new WifiConfiguration();
//				        
//						wifiConfig.SSID = String.format("\"%s\"", THLApp.ssid);
//		                wifiConfig.preSharedKey = String.format("\"%s\"", THLApp.key);
//	
//		                int netId = wifiManager.addNetwork(wifiConfig);
//	
//		                wifiManager.disconnect();
//	
//		                wifiManager.enableNetwork(netId, true);
//		                wifiManager.reconnect();
//		                
////		                myServerSocket.close();
////		    			myServerSocket = null;
////		    			myServerSocket = new MyServerSocket(UIMain.this, mHandler);
//					}
//	                
//					btn_save.performClick();
//				}
//				break;
//			case Constants.TCPIP_SET_TIME:
//				String time = "";
//				if(!jsonObject.isNull("time"))
//				{
//					time = jsonObject.getString("time");
//					String[] times = time.split("-");
//					timeManager.changeSystemTime(times[0],times[1],times[2],times[3],times[4],times[5]);
//					changeUITime();
//					//tv_time.setText(time);
//					//btn_save.performClick();
//				}
//				break;
			case Constants.TCPIP_START_WIFI_MODE:
				myServerSocket.ackAllData();
				changeToApMode();
//				if(THLApp.isEthernetOn)
//					usbSerialPortManager.startScan();
				break;
			case Constants.TCPIP_SET_ALL_DATA:
				String upload_time = "";
				if(!jsonObject.isNull("upload_time"))
				{
					upload_time = jsonObject.getString("upload_time");
					et_upload_time.setText(upload_time);
				}
				String start_scan_time = "";
				String stop_scan_time = "";
				if(!jsonObject.isNull("start_scan_time"))
				{
					start_scan_time = jsonObject.getString("start_scan_time");
					et_scan_time.setText(start_scan_time);
				}
				if(!jsonObject.isNull("stop_scan_time"))
				{
					stop_scan_time = jsonObject.getString("stop_scan_time");
					et_stop_scan_time.setText(stop_scan_time);
				}
				String server_type = "";
				String url = "";
				if(!jsonObject.isNull("server_type"))
				{
					server_type = jsonObject.getString("server_type");
					for(int i = 0 ; i < rg.getChildCount() ; i++)
			        {
			        	RadioButton rb = (RadioButton) rg.getChildAt(i);
			        	if(rb.getText().toString().equals(server_type))
			        	{
			        		rb.setChecked(true);
			        		if(rb.getText().toString().equals("iScan"))
							{
								et_url.setEnabled(false);
								THLApp.serverType = "iScan";
							}
							else
							{
								et_url.setEnabled(true);
								THLApp.serverType = "Customize";
							}
			        		break;
			        	}
			        }
				}
				if(!jsonObject.isNull("url"))
				{
					url = jsonObject.getString("url");
					et_url.setText(url);
				}
				String algorithm = "";
				if(!jsonObject.isNull("algorithm"))
				{
					algorithm = jsonObject.getString("algorithm");
					for(int i = 0 ; i < rg_avg.getChildCount() ; i++)
			        {
			        	RadioButton rb = (RadioButton) rg_avg.getChildAt(i);
			        	if(rb.getText().toString().equals(algorithm))
			        	{
			        		rb.setChecked(true);
			        		if(rb.getText().toString().equals("None"))
							{
								THLApp.Algorithm = "None";
							}
							else
							{
								THLApp.Algorithm = "AVG";
							}
			        		break;
			        	}
			        }
				}
				String mode = "";
				if(!jsonObject.isNull("mode"))
				{
					mode = jsonObject.getString("mode");
					for(int i = 0 ; i < rg_mode.getChildCount() ; i++)
			        {
			        	RadioButton rb = (RadioButton) rg_mode.getChildAt(i);
			        	if(rb.getText().toString().equals(mode))
			        	{
			        		rb.setChecked(true);
			        		if(rb.getText().toString().equals("Wifi"))
							{
								THLApp.isEthernetOn = false;
								THLApp.Mode = "Wifi";
							}
							else
							{
								//et_url.setEnabled(true);
								THLApp.isEthernetOn = true;
								THLApp.Mode = "Ethernet";
							}
			        		break;
			        	}
			        }
				}
				String uuid_filter = "";
				if(!jsonObject.isNull("uuid_filter"))
				{
					uuid_filter = jsonObject.getString("uuid_filter");
					et_filter_uuid.setText(uuid_filter);	
				}
				if(!jsonObject.isNull("ssid"))
				{
					THLApp.ssid = jsonObject.getString("ssid");
					et_ssid.setText(THLApp.ssid);
				}
				if(!jsonObject.isNull("key"))
				{
					THLApp.key = jsonObject.getString("key");
					et_key.setText(THLApp.key);
				}							
				String time = "";
				if(!jsonObject.isNull("time"))
				{
					time = jsonObject.getString("time");
					String[] times = time.split("-");
					timeManager.changeSystemTime(times[0],times[1],times[2],times[3],times[4],times[5]);
					changeUITime();
				}
				THLApp.saveSettings();
				if(!THLApp.isEthernetOn)
				{
					if(!THLApp.ssid.equals("") && !THLApp.key.equals(""))
					{
						if(!ApManager.isApOn(UIMain.this))
						{
							myServerSocket.ackAllData();
							wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					        wifiConfig = new WifiConfiguration();
					        
							wifiConfig.SSID = String.format("\"%s\"", THLApp.ssid);
			                wifiConfig.preSharedKey = String.format("\"%s\"", THLApp.key);
		
			                int netId = wifiManager.addNetwork(wifiConfig);
		
			                wifiManager.disconnect();
		
			                wifiManager.enableNetwork(netId, true);
			                wifiManager.reconnect();
	
						}
					}
				}
				btn_save.performClick();
				break;
		}
		if(cmd != Constants.TCPIP_START_WIFI_MODE && cmd != Constants.TCPIP_SET_SSID_AND_KEY
		&& cmd != Constants.TCPIP_ACK_ALL_DATA)
		{
			myServerSocket.ackAllData();
		}
	}
	
	/**======================================================================================*/
	public void parseBeaconDataToList()
	{
//		for(int i = 0 ; i < strs.length ; i++)
//		{
//			MessageString = MessageString +"\n"+ strs[i];
//		}
		String[] receivers = MessageString.split("\n");
//		Log.d("usbInput", "==========================");
//		Log.d("usbInput", MessageString);
//		Log.d("usbInput", "==========================");
		for(int i = 0 ; i < receivers.length ; i++)
		{
			String data = receivers[i].replace("\r", "");
			if(data.length() == 72 || data.length() == 84 || data.length() == 71)
			{
				BeaconInfo beaconInfo = new BeaconInfo();
				Date curDate = new Date(System.currentTimeMillis());
				
				beaconInfo.time = formatter.format(curDate);
				beaconInfo.scanned_mac = data.substring(0, 17);
				try {
					if(data.length() == 72)
					{
						beaconInfo.major = String.valueOf(Integer.parseInt(data.substring(55, 59),16));
						beaconInfo.minor = String.valueOf(Integer.parseInt(data.substring(60, 64),16));
						beaconInfo.rssi = data.substring(69, 72);	
						beaconInfo.uuid = data.substring(18, 54);	
					}
					else if(data.length() == 71)
					{
						beaconInfo.major = String.valueOf(Integer.parseInt(data.substring(55, 59),16));
						beaconInfo.minor = String.valueOf(Integer.parseInt(data.substring(60, 64),16));
						beaconInfo.rssi = data.substring(68, 71);	
						beaconInfo.uuid = data.substring(18, 54);	
					}
					else if(data.length() == 84){
						beaconInfo.rssi = data.substring(81, 84);
						beaconInfo.uuid = data.substring(19, 78);
					}			
				} catch (Exception e) {
					// TODO: handle exception
					Log.d("debug", "major or minor not integer");
					continue;
				}
				
//				if(!THLApp.isNumeric(beaconInfo.major))
//					return;
//				if(!THLApp.isNumeric(beaconInfo.minor))
//					return;
				if(!THLApp.isNumeric(beaconInfo.rssi))
				{
					Log.d("debug", "rssi not integer");
//					receivers = null;
//					MessageString = "";
					continue;
				}
				if(!THLApp.uuid.trim().equals(""))
				{
					if(!THLApp.uuid.equals(beaconInfo.uuid) 
					&& !beaconInfo.uuid.equals("00112233-4455-6677-8899-AABBCCDDEEFF"))
					{
						//Log.d("debug", THLApp.uuid + "," + beaconInfo.uuid);
//						receivers = null;
//						MessageString = "";
						continue;
					}
				}
				//Log.d("debug", THLApp.uuid + "," + beaconInfo.uuid + ",pass");
				/********************************************************************************/
				if(!beaconInfo.rssi.equals(""))
				{
					if(beaconList.isEmpty())
					{
						try {
							beaconInfo.rssiList.add(Double.valueOf(beaconInfo.rssi));
							beaconInfo.count++;
							beaconList.add(beaconInfo);
						} catch (Exception e) {
							// TODO: handle exception
							Log.d("debug", e.toString());
						}	
					}
					else 
					{
						if(THLApp.Algorithm.equals("AVG"))
						{
							count = 0;
							for(int j = 0; j<beaconList.size(); j++)
							{
								if(beaconList.get(j).scanned_mac.equals(beaconInfo.scanned_mac)
								 && !beaconList.get(j).uuid.equals("00112233-4455-6677-8899-AABBCCDDEEFF"))
								{		
									try {
										beaconList.get(j).add(Double.valueOf(beaconInfo.rssi));
										beaconList.get(j).time = beaconInfo.time;
										beaconList.get(j).rssi = beaconInfo.rssi;
										beaconList.get(j).count++;
		
										beaconList.get(j).avgRssi(1);
										beaconList.set(j, beaconList.get(j));
									} catch (Exception e) {
										// TODO: handle exception
										Log.d("debug", e.toString());
									}	
									break;
								}
								count++;
							}					
						
							if(count == beaconList.size())
							{
								try {
									beaconInfo.rssiList.add(Double.valueOf(beaconInfo.rssi));
									beaconInfo.count++;
									beaconList.add(beaconInfo);
								} catch (Exception e) {
									// TODO: handle exception
									Log.d("debug", e.toString());
								}	
							}
						}
						else
						{
							try {
								beaconInfo.rssiList.add(Double.valueOf(beaconInfo.rssi));
								beaconInfo.count++;
								beaconList.add(beaconInfo);
							} catch (Exception e) {
								// TODO: handle exception
								Log.d("debug", e.toString());
							}	
						}
					}
				}
                beaconInfo = null;
                curDate = null;
			}
			
		}
		receivers = null;
		MessageString = "";
	}
	/***********************************************************************************************/
	public void SendCustomizeDataToServer(ArrayList<BeaconInfo> BeaconDataList)
	{
        long abc = System.currentTimeMillis();
        double bbb = (double) abc/1000;
        double ccc = (Math.round(bbb)) * 1000;
		Date curDate = new Date((long) ccc);//System.currentTimeMillis());
		
    	//JSONArray jsArray = new JSONArray();
    	JSONArray jsArray1 = new JSONArray();
    
    	JSONObject object = new JSONObject();
    	try {
    		object.put("project", "THLight");
    		object.put("version", appVersion);
    		object.put("intervals", THLApp.upload_time);
    		object.put("name", "");
    		object.put("mac", THLApp.BTMac);
    		object.put("sentTime", formatter.format(curDate));
            Log.d("Terry : ", "sentTime: " + formatter2.format(curDate));
    		object.put("scanned", jsArray1);
            curDate = null;
    	} catch (JSONException e) {
			e.printStackTrace();
			Log.d("debug", "SendCustomizeDataToServer JSONException:"+e.toString());
		}		                    	
    	//jsArray.put(object);
    	
    	for(int i = 0 ; i < BeaconDataList.size(); i++)
    	{
    				                
    		JSONObject object1 = new JSONObject();

            if (BeaconDataList.get(i) != null) {
                try {
                    object1.put("mac", BeaconDataList.get(i).scanned_mac);
                    object1.put("uuid", BeaconDataList.get(i).uuid);
                    object1.put("major", BeaconDataList.get(i).major);
                    object1.put("minor", BeaconDataList.get(i).minor);
                    object1.put("rssi", BeaconDataList.get(i).rssi);
                    object1.put("scannedTime", BeaconDataList.get(i).time);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Log.d("debug", "SendCustomizeDataToServer JSONException:" + e.toString());
                    e.printStackTrace();
                }
                jsArray1.put(object1);
            }
            else
            {
                Log.d("terry", "BeaconDataList.get( " + i + " ) is NULL.");
            }
    	}
    	
    	
    	List<NameValuePair> params = new ArrayList<NameValuePair>();				        
    	params.add(new BasicNameValuePair("act", "receiver"));
    	params.add(new BasicNameValuePair("data", object.toString()));
    	System.out.println("jsArray:" + object.toString());
    	////////for mackay
		/*List<NameValuePair> params = new ArrayList<NameV aluePair>();
        params.add(new BasicNameValuePair("id", THLApp.BTMac + "_" + formatter.format(curDate)));
        params.add(new BasicNameValuePair("deviceId", ""));
        params.add(new BasicNameValuePair("major", ""));
        params.add(new BasicNameValuePair("minor", ""));
        params.add(new BasicNameValuePair("uuid", ""));
        params.add(new BasicNameValuePair("rssi", ""));
        params.add(new BasicNameValuePair("time", jsArray.toString()));
*/
    	httpUpdateDevice(THLApp.url,params);    
	}
	/***********************************************************************/
	public void SendIScanDataToServer()
	{
		Date curDate = new Date(System.currentTimeMillis());
		
		JSONArray jsArray = new JSONArray();
		
		for(int i = 0 ; i < tempList.size(); i++)
		{
			JSONObject object = new JSONObject();		                
	
			try {
				object.put("lon", "-1");
				object.put("lat", "-1");
	    		object.put("userId", "");
	    		object.put("enterTime", tempList.get(i).time);
	    		object.put("wifiId", THLApp.WifiMac);
	    		object.put("beaconId", "");
	    		object.put("deviceId", tempList.get(i).scanned_mac);
	    		object.put("rssi", tempList.get(i).rssi);
	    		object.put("sentTime", formatter.format(curDate));
                //Log.d("Terry : ", "sentTime: " + formatter2.format(curDate));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		                    	
			jsArray.put(object);
		}
		List<NameValuePair> params = new ArrayList<NameValuePair>();				        
		params.add(new BasicNameValuePair("data", jsArray.toString()));
		httpUpdateDevice(iScanUploadUrl,params);    
	}
	
	/******************************* 改變時間 **********************************/
    private void changeUITime()
    {
    	dc.setFormat24Hour("yyyy-MM-dd hh:mm, EEEE");
        tv_time.setText(dc.getText().toString());
        mHandler.sendEmptyMessageDelayed(Constants.MSG_SHOW_TIME,5000);
    }
    /******************************* 改變AP mode **********************************/
    private void changeToApMode() {
    	if(ApManager.isApOn(UIMain.this))
		{
			ApManager.configApState(UIMain.this);
			if(!THLApp.isEthernetOn)
				wifiManager.setWifiEnabled(true);
			else
			{
				wifiManager.setWifiEnabled(false);
				//usbSerialPortManager.startScan();
			}
			myServerSocket.close();
			myServerSocket = null;
			myServerSocket = new MyServerSocket(UIMain.this, mHandler);

		}
    	else
    	{
    		if(!THLApp.isEthernetOn)
				wifiManager.setWifiEnabled(true);
    		else
			{
				wifiManager.setWifiEnabled(false);
				//usbSerialPortManager.startScan();
			}
    		myServerSocket.close();
			myServerSocket = null;
			myServerSocket = new MyServerSocket(UIMain.this, mHandler);
    	}
	}
    
}
