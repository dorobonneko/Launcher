package com.moe.icelauncher.compat;
import android.content.pm.LauncherApps;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import java.util.List;
import android.os.UserHandle;
import com.moe.icelauncher.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat;
import android.content.pm.ResolveInfo;
import android.appwidget.AppWidgetProviderInfo;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherActivityInfo;
import java.util.ArrayList;
import android.appwidget.AppWidgetManager;
import android.app.Activity;
import com.moe.icelauncher.IconCache;
import android.appwidget.AppWidgetHost;
import android.os.Bundle;
import android.graphics.drawable.Drawable;
import android.os.UserManager;
import android.content.ActivityNotFoundException;
import android.widget.Toast;
import com.moe.icelauncher.R;
import java.util.Collections;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import com.moe.icelauncher.receiver.Admin;
import android.content.pm.ShortcutManager;
import android.app.LauncherActivity;

public class LauncherAppsCompatVL extends LauncherAppsCompat
{

	@Override
	public UserHandle getUser()
	{
		// TODO: Implement this method
		return UserHandle.getUserHandleForUid(android.os.Process.myUid());
	}


	@Override
	public boolean isPackageEnabled(String packageName)
	{
		return launcherApps.isPackageEnabled(packageName,UserHandle.getUserHandleForUid(0));
	}

	private LauncherApps launcherApps;
	private Context context;
	private Callback mLauncherCallback=new Callback();
	private List<OnAppsChangedCallbackCompat> mCallback=new ArrayList<>();
	private AppWidgetManager mAppWidgetManager;
	private UserManager mUserManager;
	private DevicePolicyManager mDevicePolicyManager;
	private ComponentName mDeviceComponentName;
	public LauncherAppsCompatVL(Context context){
		this.context=context;
		launcherApps=context.getSystemService(LauncherApps.class);
		mAppWidgetManager=AppWidgetManager.getInstance(context);
		mUserManager=context.getSystemService(UserManager.class);
		mDevicePolicyManager=context.getSystemService(DevicePolicyManager.class);
		mDeviceComponentName=new ComponentName(context,Admin.class);
	}
	@Override
	public boolean setPackageEnabled(String packageName, boolean enabled)
	{
		return mDevicePolicyManager.setApplicationHidden(mDeviceComponentName,packageName,!enabled);
	}


	@Override
	public boolean bindAppWidgetIdIfAllowed(int appWidgetId, AppWidgetProviderInfo info, Bundle options)
	{
		// TODO: Implement this method
		return mAppWidgetManager.bindAppWidgetIdIfAllowed(
			appWidgetId, info.getProfile(), info.provider, options);
	}

	@Override
	public void startConfigActivity(AppWidgetProviderInfo info, int widgetId, Activity activity, AppWidgetHost host, int requestCode)
	{
		try {
            host.startAppWidgetConfigureActivityForResult(activity, widgetId, 0, requestCode, null);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        }
	}

	@Override
	public Drawable loadPreview(AppWidgetProviderInfo info)
	{
		// TODO: Implement this method
		return info.loadPreviewImage(context,0);
	}

	@Override
	public Drawable loadIcon(AppWidgetProviderInfo info, IconCache cache)
	{
		// TODO: Implement this method
		return cache.loadIcon(info.provider.getPackageName(),info.icon);
	}

	@Override
	public String loadLabel(AppWidgetProviderInfo info)
	{
		// TODO: Implement this method
		return info.label;
	}

	@Override
	public List<AppWidgetProviderInfo> getAllProviders()
	{
		ArrayList<AppWidgetProviderInfo> providers = new ArrayList<AppWidgetProviderInfo>();
        for (UserHandle user : mUserManager.getUserProfiles()) {
            providers.addAll(mAppWidgetManager.getInstalledProvidersForProfile(user));
        }
        return providers;
	}

	
	@Override
	public List<AppWidgetProviderInfo> getAppWidgetProviders(String packageName)
	{
		return mAppWidgetManager.getInstalledProvidersForPackage(packageName,getUser());
	}

	@Override
	public List<LauncherActivityInfoCompat> getActivityList(String packageName ,UserHandle user)
	{
		List<LauncherActivityInfo> list = launcherApps.getActivityList(packageName,
																		user);
        if (list.size() == 0) {
            return Collections.emptyList();
        }
        ArrayList<LauncherActivityInfoCompat> compatList =
			new ArrayList<LauncherActivityInfoCompat>(list.size());
        for (LauncherActivityInfo info : list) {
            compatList.add(new LauncherActivityInfoCompatVL(info));
        }
        return compatList;
	}

	@Override
	public void registerOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat call)
	{
		if(!mCallback.contains(call))
			mCallback.add(call);
			if(mCallback.size()==1)
				launcherApps.registerCallback(mLauncherCallback);
	}

	@Override
	public void unRegisterOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat call)
	{
		mCallback.remove(call);
		if(mCallback.size()==0)
			launcherApps.unregisterCallback(mLauncherCallback);
	}
	
	/*@Override
	public List<ShortcutInfo> getShortCuts(String activity)
	{
		if(launcherApps.hasShortcutHostPermission())
		try{
			
			ComponentName cn=ComponentName.unflattenFromString(activity);
		LauncherApps.ShortcutQuery query=new LauncherApps.ShortcutQuery();
		query.setPackage(cn.getPackageName()).setActivity(cn).setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED);
		return launcherApps.getShortcuts(query,getUser());
		}catch(Exception e){}
		return Collections.emptyList();
	}*/

	
	synchronized List<OnAppsChangedCallbackCompat> getCallbacks()
	{
		return new ArrayList<OnAppsChangedCallbackCompat>(mCallback);
	}
	class Callback extends LauncherApps.Callback
	{

		@Override
		public void onPackageRemoved(String p1, UserHandle p2)
		{
			for (OnAppsChangedCallbackCompat callback : getCallbacks())
			{
				callback.onPackageRemoved(p1, p2);
			}
		}

		@Override
		public void onPackageAdded(String p1, UserHandle p2)
		{
			for (OnAppsChangedCallbackCompat callback : getCallbacks())
			{
				callback.onPackageAdded(p1, p2);
			}
		}

		@Override
		public void onPackageChanged(String p1, UserHandle p2)
		{
			for (OnAppsChangedCallbackCompat callback : getCallbacks())
			{
				callback.onPackageChanged(p1, p2);
			}
		}

		@Override
		public void onPackagesAvailable(String[] p1, UserHandle p2, boolean p3)
		{
			for (OnAppsChangedCallbackCompat callback : getCallbacks())
			{
				callback.onPackagesAvailable(p1, p2,p3);
			}
		}

		@Override
		public void onPackagesUnavailable(String[] p1, UserHandle p2, boolean p3)
		{
			for (OnAppsChangedCallbackCompat callback : getCallbacks())
			{
				callback.onPackagesUnavailable(p1, p2,p3);
			}
		}

		/*@Override
		public void onShortcutsChanged(String packageName, List<ShortcutInfo> shortcuts, UserHandle user)
		{
			for (OnAppsChangedCallbackCompat callback : getCallbacks())
			{
				callback.onShortcutsChanged(packageName,shortcuts, user);
			}
		}*/

	}
}
