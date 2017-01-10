package com.thlight.wifireceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

public class ApManager {
	Context context = null;
	Handler mHandler = null;
	
	static WifiManager wifimanager = null;
	static WifiConfiguration netConfig = null;
	
	usbSerialPortManager usbSerialPortManager = null;
	
	boolean isFirstEnable = true;
	boolean isFirstScan = true;
	
	ScanResult highestAp = null;
	
	public ApManager(Context context)
	{
		this.context = context;
		wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
		netConfig = new WifiConfiguration();
		usbSerialPortManager = THLApp.usbSerialPortManager;
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		context.registerReceiver(WifiBroadcastReceiver, intentFilter);
	}
	/**************************************/
	private final BroadcastReceiver WifiBroadcastReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
	        {
	        	//int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0); 
	        	int wifiState = wifimanager.getWifiState();
	            if(wifiState == WifiManager.WIFI_STATE_DISABLED)  
	            	Log.d("apManager","WIFI_STATE_DISABLED");
	            else if(wifiState == WifiManager.WIFI_STATE_DISABLING)  
	            	Log.d("apManager","WIFI_STATE_DISABLING");
	            else if(wifiState == WifiManager.WIFI_STATE_ENABLED)  
	            {
	            	if(!isFirstEnable)
	            	{
	            		wifimanager.startScan();		      
        			}
	            	isFirstEnable = false;
	            	Log.d("apManager","WIFI_STATE_ENABLED");
	            }
	            else if(wifiState == WifiManager.WIFI_STATE_ENABLING)  
	            	Log.d("apManager","WIFI_STATE_ENABLING");
	            else if(wifiState == WifiManager.WIFI_STATE_UNKNOWN)  
	            	Log.d("apManager","WIFI_STATE_UNKNOWN");
	            else
	            	Log.d("apManager","WIFI_STATE:"+wifiState);

	        }
	        else if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            {              
	        	Log.d("apManager","SCAN_RESULTS_AVAILABLE_ACTION");
	        	if(!isNetworkConnected())
	        	{
	                if(!THLApp.ssid.equals("") && !THLApp.key.equals(""))
	    			{	                   
	                    mHandler.postDelayed(new Runnable() {
							public void run() {
								List<ScanResult> list = wifimanager.getScanResults();
								if(list != null)
								{
									for(ScanResult i : list)
									{
									    if(i.SSID != null && i.SSID.equals(THLApp.ssid)) 
									    {
//									    	if(highestAp == null)
//									    	{
//									    		highestAp = i;
//									    	}
//									    	else if(i.level > highestAp.level)
//									    	{
//									    		highestAp = i;
//									    	}	
									    	wifimanager = (WifiManager) ApManager.this.context.getSystemService(ApManager.this.context.WIFI_SERVICE);
											netConfig = new WifiConfiguration();
											netConfig.SSID = String.format("\"%s\"", THLApp.ssid);
						            		netConfig.preSharedKey = String.format("\"%s\"", THLApp.key);  
						            		//netConfig.BSSID = highestAp.BSSID;
									    	int networkId = wifimanager.addNetwork(netConfig);
									    	boolean isDisconnect = wifimanager.disconnect();
									    	Log.d("apManager","isDis:"+isDisconnect);
									    	boolean isEnable = wifimanager.enableNetwork(networkId, true);
									    	Log.d("apManager","isEnable:"+isEnable);
									    	boolean isReconnect = wifimanager.reconnect();   
									    	Log.d("apManager","isReconnect:"+isReconnect);
									    	Log.d("apManager","wifi connect:"+i.SSID+",netConfig:"+netConfig.preSharedKey);
									    	break;
									    }           
									}
//									if(highestAp != null)
//									{
//										wifimanager = (WifiManager) ApManager.this.context.getSystemService(ApManager.this.context.WIFI_SERVICE);
//										netConfig = new WifiConfiguration();
//										netConfig.SSID = String.format("\"%s\"", THLApp.ssid);
//					            		netConfig.preSharedKey = String.format("\"%s\"", THLApp.key);  
//					            		netConfig.BSSID = highestAp.BSSID;
//								    	int networkId = wifimanager.addNetwork(netConfig);
//								    	boolean isDisconnect = wifimanager.disconnect();
//								    	//Log.d("apManager","isDis:"+isDisconnect);
//								    	boolean isEnable = wifimanager.enableNetwork(networkId, true);
//								    	//Log.d("apManager","isEnable:"+isEnable);
//								    	boolean isReconnect = wifimanager.reconnect();   
//								    	//Log.d("apManager","isReconnect:"+isReconnect);
//								    	Log.d("apManager","wifi connect:"+highestAp.SSID+",netConfig:"+netConfig.preSharedKey);
//								    	highestAp = null;
//									}
							    	
								}
							}
						}, 1000);    
	    			}

	        	}
            }
	        else if(action.equals("android.net.conn.CONNECTIVITY_CHANGE"))
	        {
	        	ConnectivityManager connectivityManager = (ConnectivityManager) 
                        context.getSystemService(Context.CONNECTIVITY_SERVICE );
				NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				NetworkInfo activeEthernetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
				boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();  
				boolean isEthernetConnected = activeEthernetInfo != null && activeEthernetInfo.isConnectedOrConnecting();  
				if(!THLApp.isEthernetOn)
				{
					if (isConnected)    
					{
//						if(usbSerialPortManager.isOurDevice)
//							usbSerialPortManager.startScanAll(); 
						Log.d("apManager", "wifi connect:" +isConnected);   
					}
					else 
					{
//						if(usbSerialPortManager.isOurDevice)
//							usbSerialPortManager.stopScanAll();
						Log.d("apManager", "wifi not connect:" +isConnected);
					}
				}
				else
				{
					if(isEthernetConnected)
					{
//						if(usbSerialPortManager.isOurDevice)
//							usbSerialPortManager.startScanAll(); 
						Log.d("apManager", "ethernet connect:" +isEthernetConnected); 
					}
					else
					{
//						if(usbSerialPortManager.isOurDevice)
//							usbSerialPortManager.stopScanAll();
						Log.d("apManager", "ethernet not connect:" +isEthernetConnected);
					}
				}
	        }
	    }
	};
	/**************************************/
	//check whether wifi hotspot on or off
	public static boolean isApOn(Context context) {
	    WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);     
	    try {
	        Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
	        method.setAccessible(true);
	        return (Boolean) method.invoke(wifimanager);
	    }
	    catch (Throwable ignored) {}
	    return false;
	}

	/**************************************/
	// toggle wifi hotspot on or off
	public static boolean configApState(Context context)
	{
	
		netConfig.SSID = "THL_Receiver_"+THLApp.WifiMac;
		netConfig.preSharedKey = "12345678";
		/*********WPA2 Setting**********/
		netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
		netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
	//	netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		//netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		//netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		
	    try {  
	        // if WiFi is on, turn it off
	        if(isApOn(context)) {               
	            wifimanager.setWifiEnabled(false);
	        }               
	        Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);                   
	        method.invoke(wifimanager, netConfig, !isApOn(context));
	        return true;
	    } 
	    catch (Exception e) {
	        e.printStackTrace();
	    }       
	    return false;
	}
	/**********************************************/
	public void unregisterReceiver()
	{
		context.unregisterReceiver(WifiBroadcastReceiver);
	}
	/** ================================================ */
	public void setHandler(Handler handler)
	{
		mHandler = handler;
	}
	/****************************************/
	public boolean isNetworkConnected()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE );
		NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return activeNetInfo != null && activeNetInfo.isConnectedOrConnecting(); 
	}
	/***********************************/
	public static boolean isEthOn() 
	{
        try {

            String line;
            boolean r = false;

            Process p = Runtime.getRuntime().exec("netcfg");

            BufferedReader input = new BufferedReader (new InputStreamReader(p.getInputStream()));   
            while ((line = input.readLine()) != null) {   

                if(line.contains("eth0")){
                    if(line.contains("UP")){
                        r=true;
                    }
                    else{
                        r=false;
                    }
                }
            }   
            input.close();

            Log.e("apManager","isEthOn: "+r);
            return r; 

        } catch (IOException e) {
            Log.e("apManager","Runtime Error: "+e.getMessage());
            e.printStackTrace();
            return false;
        }

    }
	/*************************************/
	public static void turnEthOnOrOff() 
	{
        try {
        	Process p = null;
            if(isEthOn()){
            	p = Runtime.getRuntime().exec("ifconfig eth0 up");
            }
            else{
            	p = Runtime.getRuntime().exec("ifconfig eth0 up");
            }
            
            String line;
            BufferedReader input = new BufferedReader (new InputStreamReader(p.getInputStream()));   
            while ((line = input.readLine()) != null) {   

                if(line.contains("eth0")){
                    if(line.contains("UP")){
                        
                    }
                    else{
                       
                    }
                }
            }   
            input.close();

        } catch (IOException e) {
            Log.e("apManager","Runtime Error: "+e.getMessage());
            e.printStackTrace();
        }
    }
} 
