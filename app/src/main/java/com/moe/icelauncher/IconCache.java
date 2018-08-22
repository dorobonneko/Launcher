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
import com.moe.icelauncher.model.IconItem;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;

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
	public IconItem getInbadedIcon(ComponentName componentName){
		IconItem iconItem=new IconItem();
		int currentVersion=0;
		try
		{
			currentVersion=context.getPackageManager().getPackageInfo(componentName.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES).versionCode;
		}
		catch (PackageManager.NameNotFoundException e)
		{}
		Cursor cursor=context.getContentResolver().query(LauncherSettings.Icons.CONTENT_URI,null,LauncherSettings.Icons.COMPONENTNAME+"=?",new String[]{componentName.flattenToString()},null);
		if(cursor!=null){
			if(cursor.moveToNext()){
				int version=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Icons.VERSION));
				if(version==currentVersion){
					iconItem.title=cursor.getString(cursor.getColumnIndex(LauncherSettings.Icons.TITLE));
					iconItem.icon=new BitmapDrawable(Utilities.createIconBitmap(cursor,cursor.getColumnIndex(LauncherSettings.Icons.ICON),context));
					iconItem.packageName=componentName.getPackageName();
					iconItem.componentName=componentName.flattenToString();
					iconItem.version=version;
					iconItem._id=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Icons._ID));
				}else
					context.getContentResolver().delete(LauncherSettings.Icons.CONTENT_URI,LauncherSettings.Icons._ID+"=?",new String[]{cursor.getInt(cursor.getColumnIndex(LauncherSettings.Icons._ID))+""});
			}else
				cursor.close();
		}
		if(iconItem.icon==null){
			try
			{
				ActivityInfo activityInfo=context.getPackageManager().getActivityInfo(componentName, PackageManager.GET_UNINSTALLED_PACKAGES);
				iconItem.title=activityInfo.loadLabel(context.getPackageManager()).toString();
				iconItem.icon=activityInfo.loadUnbadgedIcon(context.getPackageManager());
				iconItem.componentName=componentName.flattenToString();
				iconItem.packageName=componentName.getPackageName();
				iconItem.version=currentVersion;
				Bitmap bitmap=Utilities.createIconBitmap(iconItem.icon,context);
				iconItem.icon=new BitmapDrawable(bitmap);
				ContentValues cv=new ContentValues();
				cv.put(LauncherSettings.Icons.COMPONENTNAME,iconItem.componentName);
				cv.put(LauncherSettings.Icons.ICON,Utilities.flattenBitmap(bitmap));
				cv.put(LauncherSettings.Icons.PACKAGENAME,iconItem.packageName);
				cv.put(LauncherSettings.Icons.TITLE,iconItem.title);
				cv.put(LauncherSettings.Icons.VERSION,currentVersion);
				context.getContentResolver().insert(LauncherSettings.Icons.CONTENT_URI,cv);
				
				
			}
			catch (PackageManager.NameNotFoundException e)
			{}
		}
		return iconItem;
	}
	
	/*public IconItem getInbadedIcon(ResolveInfo info){
		IconItem iconItem=new IconItem();
		Drawable bufferDrawable=null;
		int currentVersion=0;
		
		String activity=info.activityInfo.packageName + "/" + info.activityInfo.name;
		
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
	return iconItem;
	}*/
}
