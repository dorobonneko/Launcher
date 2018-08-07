package com.moe.icelauncher.util;
import android.os.HandlerThread;
import android.os.Handler;

public class SubThread extends HandlerThread
{
	private static SubThread mSubThread;
	private Handler handler;
	private SubThread(){
		super("SubThread");
		start();
	}
	public static SubThread getInstance(){
		synchronized(SubThread.class){
			if(mSubThread==null)
				mSubThread=new SubThread();
		}
		return mSubThread;
	}
	public void post(Runnable runnable){
		handler.post(runnable);
	}
	public void postDelayed(Runnable runnable,long time){
		handler.postDelayed(runnable,time);
	}
	public void removeCallback(Runnable runnable){
		handler.removeCallbacks(runnable);
	}

	@Override
	protected void onLooperPrepared()
	{
		handler=new Handler();
	}
	
}
