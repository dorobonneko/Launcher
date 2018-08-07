package com.moe.icelauncher;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.content.ContentValues;
import android.graphics.Bitmap;

public class IconCache
{
	private int fullIconDpi;
	private Context context;
	private static IconCache mIconCache;
	

	public Drawable loadIcon(String packageName, int icon)
	{
		try
		{
			return context.getPackageManager().getResourcesForApplication(packageName).getDrawableForDensity(icon, context.getResources().getDisplayMetrics().DENSITY_DEFAULT);
		}
		catch (PackageManager.NameNotFoundException e)
		{}
		return null;
	}
	public static IconCache getInstance(Context context){
		if(mIconCache==null){
			synchronized(IconCache.class){
				if(mIconCache==null)
					mIconCache=new IconCache(context);
			}
		}
		return mIconCache;
	}
	private IconCache(Context context){
		this.context=context;
		fullIconDpi=context.getResources().getDisplayMetrics().densityDpi;
	}
	public Drawable getInbadedIcon(ResolveInfo info){
		Drawable bufferDrawable=null;
		int currentVersion=0;
		try
		{
			currentVersion=context.getPackageManager().getPackageInfo(info.activityInfo.packageName, PackageManager.GET_UNINSTALLED_PACKAGES).versionCode;
		}
		catch (PackageManager.NameNotFoundException e)
		{}
		String activity=info.activityInfo.packageName + "/" + info.activityInfo.name;
		Cursor cursor=context.getContentResolver().query(LauncherSettings.Icons.CONTENT_URI,null,LauncherSettings.Icons.COMPONENTNAME+"=?",new String[]{activity},null);
		if(cursor!=null){
			if(cursor.moveToNext()){
				int version=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Icons.VERSION));
				if(version==currentVersion)
				bufferDrawable=new BitmapDrawable(Utilities.createIconBitmap(cursor,cursor.getColumnIndex(LauncherSettings.Icons.ICON),context));
				else
				context.getContentResolver().delete(LauncherSettings.Icons.CONTENT_URI,LauncherSettings.Icons._ID+"=?",new String[]{cursor.getInt(cursor.getColumnIndex(LauncherSettings.Icons._ID))+""});
			}else
			cursor.close();
		}
		if(bufferDrawable==null){
			bufferDrawable=info.activityInfo.loadUnbadgedIcon(context.getPackageManager());
			Bitmap bitmap= Utilities.createIconBitmap(bufferDrawable,context);
			bufferDrawable=new BitmapDrawable(bitmap);
			ContentValues cv=new ContentValues();
			cv.put(LauncherSettings.Icons.COMPONENTNAME,activity);
			cv.put(LauncherSettings.Icons.ICON,Utilities.flattenBitmap(bitmap));
			cv.put(LauncherSettings.Icons.PACKAGENAME,info.activityInfo.packageName);
			cv.put(LauncherSettings.Icons.TITLE,info.loadLabel(context.getPackageManager()).toString());
			cv.put(LauncherSettings.Icons.VERSION,currentVersion);
			context.getContentResolver().insert(LauncherSettings.Icons.CONTENT_URI,cv);
			}
	return bufferDrawable;
	}
}
