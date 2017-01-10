package com.thlight.wifireceiver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

public class TimeManager {
	
	Context context = null;
	
	public TimeManager(Context context)
	{
		this.context = context;
	}

	/***********************************************************************/
	public void changeSystemTime(String year,String month,String day,String hour,String minute,String second){
	    try {
	        Process process = Runtime.getRuntime().exec("su");
	        DataOutputStream os = new DataOutputStream(process.getOutputStream());
	        String command = "date -s "+year+month+day+"."+hour+minute+second+"\n";
	        Log.e("command",command);
	        os.writeBytes(command);
	        os.flush();
	        os.writeBytes("exit\n");
	        os.flush();
	        process.waitFor();
	    } catch (InterruptedException e) {
	    	Log.d("debug", "InterruptedException:"+e.toString());
	        e.printStackTrace();
	    } catch (IOException e) {
	    	Log.d("debug", "IOException:"+e.toString());
	        e.printStackTrace();
	    }
//		Calendar c = Calendar.getInstance();
//		c.set(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day), 
//				Integer.valueOf(hour), Integer.valueOf(minute), Integer.valueOf(second));
//		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//		am.setTime(c.getTimeInMillis());
//		if (ShellInterface.isSuAvailable()) {
//			Log.d("TimeManager", "su is available");
//			ShellInterface.runCommand("chmod 666 /dev/alarm");
//			boolean isSuccess = SystemClock.setCurrentTimeMillis(c.getTimeInMillis());
//			Log.d("TimeManager", "set Time:"+isSuccess);
//		    ShellInterface.runCommand("chmod 664 /dev/alarm");
//		}
//		else
//		{
//			Log.d("TimeManager", "su is not available");
//		}
	}
}
