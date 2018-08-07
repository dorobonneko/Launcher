package com.moe.icelauncher.model;
import android.graphics.Bitmap;
import android.content.ContentValues;
import android.content.pm.LauncherApps;
import com.moe.icelauncher.LauncherSettings;
import com.moe.icelauncher.Utilities;
import android.content.Intent;
import android.content.ComponentName;
import android.graphics.drawable.Drawable;

public class AppInfo extends ItemInfo
{
	public int profileId;
	public float iconSanifyScale;
	public long lastUpdateTime;
	public int flags;
	public String activity;
	public String componentName(){
		return packageName+"/"+activity;
		}
	
	/*void addToDatabase(ContentValues cv){
		//cv.put(LauncherSettings.AllApps._ID,_id);
		cv.put(LauncherSettings.AllApps.COMPONENTNAME,componentName);
		cv.put(LauncherSettings.AllApps.ICON,Utilities.flattenBitmap(icon));
		cv.put(LauncherSettings.AllApps.TITLE,title);
		cv.put(LauncherSettings.AllApps.PROFILEID,profileId);
		cv.put(LauncherSettings.AllApps.ICONSANIFYSCALE,iconSanifyScale);
		cv.put(LauncherSettings.AllApps.STATE,state);
		cv.put(LauncherSettings.AllApps.LASTUPDATETIME,lastUpdateTime);
		cv.put(LauncherSettings.AllApps.FLAGS,flags);
		cv.put(LauncherSettings.AllApps.PACKAGENAME,packageName);
		
	}*/

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof AppInfo)
			return ((AppInfo)obj).activity.equals(activity)&&((AppInfo)obj).packageName.equals(packageName);
		return false;
	}

	@Override
	public Intent getIntent()
	{
		Intent intent=new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(new ComponentName(packageName,activity));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}

	
}
