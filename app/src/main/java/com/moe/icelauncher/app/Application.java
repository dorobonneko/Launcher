package com.moe.icelauncher.app;
import android.app.Application;
import android.content.pm.PackageManager;
import android.content.Intent;
import com.moe.icelauncher.CrashActivity;
import android.os.Build;

public class Application extends android.app.Application
implements Thread.UncaughtExceptionHandler
{

	@Override
	public void uncaughtException(Thread p1, final Throwable p2)
	{
		new Thread(){
			public void run(){
				if(p2==null)return;
				StringBuffer sb=new StringBuffer(p2.getMessage());
				try
				{
					sb.append("\n").append(getPackageManager().getPackageInfo(getPackageName(), 0).versionName).append("\n").append(Build.MODEL).append(" ").append(Build.VERSION.RELEASE).append("\n");
				}
				catch (PackageManager.NameNotFoundException e)
				{}
				for (StackTraceElement element:p2.getStackTrace())
					sb.append("\n").append(element.toString());
				Intent intent=new Intent(getApplicationContext(),CrashActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(Intent.EXTRA_TEXT,sb.toString());
				startActivity(intent);
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}.start();
	}
	

	@Override
	public void onCreate()
	{
		// TODO: Implement this method
		super.onCreate();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	
}
