package com.moe.icelauncher.model;
import android.content.Context;
import android.os.HandlerThread;
import android.os.Handler;
import android.os.Message;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import com.moe.icelauncher.LauncherSettings;
import android.database.Cursor;
import com.moe.icelauncher.LauncherProvider;
import java.util.List;
import java.util.ArrayList;
import com.moe.icelauncher.Utilities;
import android.content.ContentValues;
import android.os.Looper;
import com.moe.icelauncher.compat.LauncherAppsCompat;
import android.content.pm.ShortcutInfo;
import android.os.UserHandle;
import java.util.Collections;
import java.util.Comparator;
import android.content.ContentResolver;
import com.moe.icelauncher.IconCache;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager.NameNotFoundException;
import com.moe.icelauncher.util.DeviceManager;
import android.icu.text.Collator;
import com.moe.icelauncher.LauncherProviderChangeListener;
import java.util.Iterator;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.content.ComponentName;

public class LauncherModel implements LauncherAppsCompat.OnAppsChangedCallbackCompat
{
	private static LauncherModel launcherModel;
	private Context context;
	private LoadThread loadThread;
	private static final int CHECK_PACKAGES=1;
	private static final int GET_ALLPACKAGES=2;
	private List<AppInfo> appsList;
	private Callback callback;
	private LauncherAppsCompat  mLauncherAppsCompat;
	private ContentResolver mContentResolver;
	private DeviceManager mDeviceManager;
	private LauncherProvider mLauncherProvider;
	private LauncherModel(Context context){
		this.context=context.getApplicationContext();
		mContentResolver=context.getContentResolver();
		mLauncherAppsCompat=LauncherAppsCompat.getInstance(context);
		mLauncherAppsCompat.registerOnAppsChangedCallback(this);
		loadThread=new LoadThread();
		loadThread.start();
		mDeviceManager=DeviceManager.getInstance(context);
	}

	public void setLauncherProvider(LauncherProvider p0)
	{
		if(mLauncherProvider!=null)mLauncherProvider.setProviderChangeListener(null);
		mLauncherProvider=p0;
		if(mLauncherProvider!=null)mLauncherProvider.setProviderChangeListener(callback);
	}

	public static Looper getWorkerLooper()
	{
		return Looper.getMainLooper();
	}
	public static LauncherModel getInstance(Context context){
		if(launcherModel==null){
			synchronized(LauncherModel.class){
				if(launcherModel==null)
					launcherModel=new LauncherModel(context);
			}
		}
		return launcherModel;
	}
	public void onCreate(){
		if(loadThread.getHandler()!=null)
			loadThread.getHandler().sendEmptyMessage(GET_ALLPACKAGES);
	}
	public void loadModel(){
		if(loadThread.getHandler()!=null)
			loadThread.getHandler().sendEmptyMessage(GET_ALLPACKAGES);
	}
	class LoadThread extends HandlerThread implements Handler.Callback{
		private Handler handler;
		public LoadThread(){
			super("Package_load");
		}

		@Override
		protected void onLooperPrepared()
		{
			handler=new Handler(this);
			if(callback!=null)callback.onInited(LauncherModel.this);
		}
		public Handler getHandler(){
			return handler;
		}
		@Override
		public boolean handleMessage(Message p1)
		{
			switch(p1.what){
				case CHECK_PACKAGES:
					//校验数据库数据
					Cursor packages=mContentResolver.query(LauncherSettings.AllApps.CONTENT_URI,new String[]{LauncherSettings.AllApps.PACKAGENAME},null,null,null);
					if(packages!=null){
						while(packages.moveToNext()){
							try{context.getPackageManager().getInstallerPackageName(packages.getString(0));}
							catch(Exception e){
								onPackageRemoved(packages.getString(0),null);
							}
						}
						packages.close();
					}
					Intent intent=new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					List<ResolveInfo> listResolve=context.getPackageManager().queryIntentActivities(intent,PackageManager.GET_UNINSTALLED_PACKAGES);
					for(ResolveInfo info:listResolve){
						Cursor cursor=mContentResolver.query(LauncherSettings.AllApps.CONTENT_URI,null,LauncherSettings.AllApps.COMPONENTNAME+"=?",new String[]{new ComponentName(info.activityInfo.packageName,info.activityInfo.name).flattenToString()},null);
						if(cursor!=null&&!cursor.moveToNext()){
							//插入一条数据
							onPackageAdded(info.activityInfo.packageName,null);
						//AppInfo item=loadResolveInfo(info);
						//ContentValues cv=new ContentValues();
						//item.addToDatabase(cv,context);
						//mContentResolver.insert(LauncherSettings.AllApps.CONTENT_URI,cv);
						}
						if(cursor!=null)cursor.close();
						}
					break;
				case GET_ALLPACKAGES:
					if(appsList==null)
						appsList=new ArrayList<AppInfo>();
						appsList.clear();
						Cursor cursor=mContentResolver.query(LauncherSettings.AllApps.CONTENT_URI,null,null,null);
						if(cursor!=null){
							while(cursor.moveToNext()){
							//存在该数据，解析
							AppInfo item=new AppInfo();
							item._id=cursor.getInt(cursor.getColumnIndex(LauncherSettings.AllApps._ID));
							if(!appsList.contains(item)){
								//列表没有该项，继续解析并加入该
								//item.title=cursor.getString(cursor.getColumnIndex(LauncherSettings.AllApps.TITLE));
								//item.icon=new BitmapDrawable(Utilities.createIconBitmap(cursor,cursor.getColumnIndex(LauncherSettings.AllApps.ICON),context));
								item.activity=ComponentName.unflattenFromString( cursor.getString(cursor.getColumnIndex(LauncherSettings.AllApps.COMPONENTNAME))).getClassName();
								item.state=cursor.getInt(cursor.getColumnIndex(LauncherSettings.AllApps.STATE));
								item.lastUpdateTime=cursor.getLong(cursor.getColumnIndex(LauncherSettings.AllApps.LASTUPDATETIME));
								item.modified=cursor.getLong(cursor.getColumnIndex(LauncherSettings.AllApps.MODIFIED));
								item.flags=cursor.getInt(cursor.getColumnIndex(LauncherSettings.AllApps.FLAGS));
								item.iconSanifyScale=cursor.getFloat(cursor.getColumnIndex(LauncherSettings.AllApps.ICONSANIFYSCALE));
								//item.profileId=cursor.getInt(cursor.getColumnIndex(LauncherSettings.AllApps.PROFILEID));
								item.packageName=cursor.getString(cursor.getColumnIndex(LauncherSettings.AllApps.PACKAGENAME));
								IconItem iconItem=IconCache.getInstance(context).getInbadedIcon(ComponentName.unflattenFromString(item.componentName()));
								item.title=iconItem.title;
								item.icon=iconItem.icon;
									
								if(item.title==null)
									onPackageRemoved(item.packageName,null);
								else
								appsList.add(item);
							}
							}
							cursor.close();
						}
						if(callback!=null){
							sort();
							new Handler(Looper.getMainLooper()).post(new Runnable(){

									@Override
									public void run()
									{
										callback.onSuccess(appsList);
										
									}
								});
						}
						handler.obtainMessage(CHECK_PACKAGES).sendToTarget();
					break;
			}
			return true;
		}

		@Override
		public void destroy()
		{
			quit();
		}
		
	}
	private void sort(){
		Collections.sort(appsList, new Comparator<AppInfo>(){

				@Override
				public int compare(AppInfo p1, AppInfo p2)
				{
					Collator myCollator = Collator.getInstance(java.util.Locale.CHINA);

					if (myCollator.compare(p1.title, p2.title) < 0)

						return -1;

					else if (myCollator.compare(p1.title, p2.title) > 0)

						return 1;

					else

						return 0;

					
					//return p1.title.compareTo(p2.title);
				}
			});
	}
	public void setCallback(Callback call){
		this.callback=call;
		if(mLauncherProvider!=null)mLauncherProvider.setProviderChangeListener(call);
		if(loadThread.getHandler()!=null)
			call.onInited(this);
	}
	public interface Callback extends LauncherProviderChangeListener{
		void onSuccess(List<AppInfo> list);
		void onInited(LauncherModel model);
		void notifyDataSetChanged();
		}
	@Override
	public void onPackageRemoved(String packageName, UserHandle user)
	{
		try{
		context.getPackageManager().getInstallerPackageName(packageName);
		updateState(packageName,DeviceManager.PACKAGE_HIDE);
			ContentValues cv=new ContentValues();
			cv.put(LauncherSettings.Favorites.STATE,DeviceManager.PACKAGE_HIDE);
			mContentResolver.update(LauncherSettings.Favorites.CONTENT_URI,cv,LauncherSettings.Favorites.PACKAGENAME+"=?",new String[]{packageName});
			mContentResolver.update(LauncherSettings.AllApps.CONTENT_URI,cv,LauncherSettings.AllApps.PACKAGENAME+"=?",new String[]{packageName});
		}catch(Exception e){
			//软件已卸载
			mContentResolver.delete(LauncherSettings.Icons.CONTENT_URI,LauncherSettings.Icons.PACKAGENAME+"=?",new String[]{packageName});
			mContentResolver.delete(LauncherSettings.Favorites.CONTENT_URI,LauncherSettings.Favorites.PACKAGENAME+"=?",new String[]{packageName});
			mContentResolver.delete(LauncherSettings.AllApps.CONTENT_URI,LauncherSettings.AllApps.PACKAGENAME+"=?",new String[]{packageName});
			delete(packageName);
		}
		if(callback!=null){
			callback.notifyDataSetChanged();
			callback.onLauncherProviderChange(packageName);
			}
	}

	@Override
	public void onPackageAdded(String packageName, UserHandle user)
	{
		ContentValues cv=new ContentValues();
		cv.put(LauncherSettings.Favorites.STATE,DeviceManager.PACKAGE_UNHIDE);
		mContentResolver.update(LauncherSettings.Favorites.CONTENT_URI,cv,LauncherSettings.Favorites.PACKAGENAME+"=?",new String[]{packageName});
		Intent intent=new Intent(Intent.ACTION_MAIN);
		intent.addCategory(intent.CATEGORY_LAUNCHER);
		intent.setPackage(packageName);
		List<ResolveInfo> list=context.getPackageManager().queryIntentActivities(intent,PackageManager.GET_UNINSTALLED_PACKAGES);
		for(ResolveInfo info:list){
			AppInfo item=loadResolveInfo(info);
			int index=appsList.indexOf(item);
			if(index!=-1)
				appsList.set(index,item);
				else
				appsList.add(item);
			Cursor cursor=mContentResolver.query(LauncherSettings.AllApps.CONTENT_URI,null,LauncherSettings.AllApps.COMPONENTNAME+"=?",new String[]{item.activity},null);
			if(cursor==null||!cursor.moveToNext()){
				item.addToDatabase(cv,context);
				mContentResolver.insert(LauncherSettings.AllApps.CONTENT_URI,cv);
			}
			if(cursor!=null)cursor.close();
			
		}
		mContentResolver.update(LauncherSettings.AllApps.CONTENT_URI,cv,LauncherSettings.AllApps.PACKAGENAME+"=?",new String[]{packageName});
		sort();
		if(callback!=null){
			callback.notifyDataSetChanged();
			callback.onLauncherProviderChange(packageName);
			}
	}
	@Override
	public void onPackageChanged(String packageName, UserHandle user)
	{
		onPackageAdded(packageName,user);
		
	}

	@Override
	public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing)
	{
		return;
	}

	@Override
	public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing)
	{
		return;
	}

	/*@Override
	public void onShortcutsChanged(String packageName, List<ShortcutInfo> shortcuts, UserHandle user)
	{
		// TODO: Implement this method
	}*/
	public List<AppInfo> getAppsList(){
		return appsList;
	}
	/*private void insert(AppInfo item){
		ContentValues cv=new ContentValues();
		item.addToDatabase(cv);
		if(mContentResolver.insert(LauncherSettings.AllApps.CONTENT_URI,cv)!=null)
			appsList.add(item);
	}
	private void update(AppInfo item){
		ContentValues cv=new ContentValues();
		item.addToDatabase(cv);
		if(mContentResolver.update(LauncherSettings.AllApps.CONTENT_URI,cv,LauncherSettings.AllApps.COMPONENTNAME+"=?",new String[]{item.componentName})>0)
		{
			int index=appsList.indexOf(item);
			if(index==-1)
			appsList.add(item);
			else
			appsList.set(index,item);
		}
	}*/
	private AppInfo loadResolveInfo(ResolveInfo info){
		final AppInfo item=new AppInfo();
		ActivityInfo activity=info.activityInfo;
		IconItem iconItem=IconCache.getInstance(context).getInbadedIcon(new ComponentName(info.activityInfo.packageName,info.activityInfo.name));
		item.title=iconItem.title;
		item.icon=iconItem.icon;
		item.packageName=activity.packageName;
		item.activity=activity.name;
		int state=context.getPackageManager().getApplicationEnabledSetting(info.activityInfo.packageName);
		item.state=state==PackageManager.COMPONENT_ENABLED_STATE_DEFAULT?(mDeviceManager.isApplicationHidden(item.packageName)?DeviceManager.PACKAGE_HIDE:DeviceManager.PACKAGE_UNHIDE):state;
		item.itemType=LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
		return item;
	}
	private void delete(String packageName){
		if(appsList!=null){
			Iterator<AppInfo> list=appsList.iterator();
			while(list.hasNext()){
				if(list.next().packageName.equals(packageName))
				list.remove();
			}
		}
	}
	private void updateState(String packageName,int state){
		if(appsList!=null){
			Iterator<AppInfo> list=appsList.iterator();
			while(list.hasNext()){
				AppInfo info=list.next();
				if(info.packageName.equals(packageName))
					info.state=state;
			}
		}
	}
}
