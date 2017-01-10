/** ======================================================================== */
package com.thlight.wifireceiver;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


//import com.THLight.BLE.USBeacon.Writer.Simple.AccountInfo;

/** ======================================================================== */
public class THLApp extends Application
{
    public static String upload_time = "";
    public static String scan_time = "";
    public static String stop_scan_time = "";  
    public static String url = "";
    public static String uuid = "";
    public static String serverType = "";
    public static String Algorithm = "";
    public static String Mode = "";
    
    public static String filter = "";
    
    public static String deviceName = "";
    
    public static String BTMac = "";
    public static String WifiMac = "";
    
    public static String ssid = "";
    public static String key = ""; 
    
    public static boolean isEthernetOn = true; 
    
    public static THLApp App = null;     
    
    public static String STORE_PATH	 = Environment.getExternalStorageDirectory().toString()+ "/WifiReceiver/";
    
    public static usbSerialPortManager usbSerialPortManager = null;
    public static ApManager apManager = null;
    
    public static int count = 0;
    
     
	/** ========================================================== */
	public THLApp()
	{
		super();
		App = this;
	}
	
	/** ========================================================== */
	@Override
	public void onCreate()
	{
		super.onCreate();
		File file= new File(STORE_PATH);
		if(!file.exists())
		{
			if(!file.mkdirs())
			{
				Toast.makeText(this, "Create folder("+ STORE_PATH+ ") failed.", Toast.LENGTH_SHORT).show();
			}
		}
		loadSettings();
		startService(new Intent(this, CheckService.class));
		
	}

	/** ========================================================== */
	@Override
	public void onTerminate()
	{		
		super.onTerminate();
	}
	
	 /** ========================================================== */
	public static void  loadCountSettings()
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App);
		count  = sp.getInt("count", 0);
	}
	/** ========================================================== */
	public static void  saveCountSettings()
	{
		SharedPreferences sp			= PreferenceManager.getDefaultSharedPreferences(App);
    	SharedPreferences.Editor edit	= sp.edit();

    	edit.putInt("count", count);
    	edit.commit();

	}

	 /** ========================================================== */
	public static void loadSettings()
	{
		SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(App);
		
		/** */		
		upload_time  = sp.getString("upload_time", "1000");
		scan_time  	 = sp.getString("scan_time", "100");
		//url   		 = sp.getString("url", "http://52.192.101.248/api/scanBeacon.php");
		url   		 = sp.getString("url", "http://52.192.101.248/api/scanBeacon.php");
		uuid		 = sp.getString("uuid", "A20D79DB-364F-4038-A637-AAEC2086382B");
		serverType   = sp.getString("serverType", "Customize");
		stop_scan_time = sp.getString("stop_scan_time", "10");
		Algorithm 	= sp.getString("algorithm", "None");
		Mode 		= sp.getString("mode", "Wifi");
		ssid 		= sp.getString("ssid", "THLight-Asus");
		key 		= sp.getString("key", "thlight2670");
		
		if(Mode.equals("Ethernet"))
			isEthernetOn = true;
		else
			isEthernetOn = false;
		//IP   		 = sp.getString("IP", "61.216.93.208");

	}
		
	/** ========================================================== */
	public static void saveSettings()
	{
		SharedPreferences sp			= PreferenceManager.getDefaultSharedPreferences(App);
    	SharedPreferences.Editor edit	= sp.edit();

    	edit.putString("upload_time", upload_time);
    	edit.putString("scan_time", scan_time);
    	edit.putString("url", url);
    	edit.putString("uuid", uuid);
    	edit.putString("serverType", serverType);
    	edit.putString("stop_scan_time", stop_scan_time);
    	edit.putString("algorithm", Algorithm);
    	edit.putString("mode", Mode);
    	edit.putString("ssid", ssid);
    	edit.putString("key", key);

    	edit.commit();

	}

	/** ========================================================== */
	public static boolean isNumeric(String str) {
	    Pattern pattern = Pattern.compile("[0-9.-]+");
	    return pattern.matcher(str).matches();
	}
	/** ========================================================== */
	public static boolean isNumerAndEnglish(String str) {
	    Pattern pattern = Pattern.compile("[a-eA-e0-9]+");
	    return pattern.matcher(str).matches();
	}
	
}

/** ======================================================================== */

