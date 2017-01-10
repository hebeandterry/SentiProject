package com.thlight.wifireceiver;

import java.util.List;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		{
			Log.d("debug", "ACTION_BOOT_COMPLETED");
//			ActivityManager activityManager			= (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
//		    List<RunningAppProcessInfo> procInfos	= activityManager.getRunningAppProcesses();
//
//		    if(null != procInfos)
//		    {
//	    	    for(RunningAppProcessInfo procInfo : procInfos)
//	    	    {
//	    	        if(procInfo.processName.equals("com.thlight.wifireceiver") && (procInfo.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND
//	    	           || procInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND))
//	    	        {
//	    	        	android.os.Process.killProcess(procInfo.pid);
//	    	        	Log.d("debug", "killProcess");
//	    	        	break;
//	    	        }
//	    	    }
//		    }

			Intent sintent = context.getPackageManager().getLaunchIntentForPackage("com.thlight.wifireceiver");
			context.startActivity(sintent);
		}
	}
	

}
