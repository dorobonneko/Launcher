package com.moe.icelauncher.compat;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import java.util.List;
import android.content.IntentFilter;
import android.content.Intent;
import java.util.ArrayList;
import android.content.BroadcastReceiver;
import android.os.UserHandle;
import com.moe.icelauncher.Utilities;
import com.moe.icelauncher.compat.LauncherAppsCompat.OnAppsChangedCallbackCompat;
import android.content.pm.ResolveInfo;
import android.appwidget.AppWidgetProviderInfo;
import android.content.pm.PackageManager;
import android.content.pm.ActivityInfo;
import java.util.Arrays;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.LauncherActivityInfo;
import android.app.Activity;
import com.moe.icelauncher.IconCache;
import android.appwidget.AppWidgetHost;
import android.os.Bundle;
import android.graphics.drawable.Drawable;
import android.appwidget.AppWidgetManager;

public class LauncherAppsCompatV16 extends LauncherAppsCompat
{

	@Override
	public UserHandle getUser()
	{
		// TODO: Implement this method
		return null;
	}


	@Override
	public boolean isPackageEnabled(String packageName)
	{
		// TODO: Implement this method
		return false;
	}


	@Override
	public boolean setPackageEnabled(String packageName, boolean enabled)
	{
		return false;
	}

	@Override
	public boolean bindAppWidgetIdIfAllowed(int appWidgetId, AppWidgetProviderInfo info, Bundle bundle)
	{
		if (Utilities.ATLEAST_JB_MR1) {
            return mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider, bundle);
        } else {
            return mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider);
        }
	}

	@Override
	public void startConfigActivity(AppWidgetProviderInfo info, int widgetId, Activity activity, AppWidgetHost host, int requestCode)
	{
		Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        intent.setComponent(info.configure);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        activity.startActivityForResult(intent, requestCode);
	}

	@Override
	public Drawable loadPreview(AppWidgetProviderInfo info)
	{
		// TODO: Implement this method
		return mContext.getPackageManager().getDrawable(info.provider.getPackageName(),info.previewImage,null);
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
		// TODO: Implement this method
		return mAppWidgetManager.getInstalledProviders();
	}
	private AppWidgetManager mAppWidgetManager;
	private Context mContext;
	private List<OnAppsChangedCallbackCompat> mCallbacks=new ArrayList<>();
	private PackageMonitor mPackageMonitor=new PackageMonitor();
	public LauncherAppsCompatV16(Context context)
	{
		this.mContext = context;
		mAppWidgetManager=AppWidgetManager.getInstance(context);
	}
	@Override
	public List<AppWidgetProviderInfo> getAppWidgetProviders(String packageName)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public List<LauncherActivityInfoCompat> getActivityList(String packageName,UserHandle user)
	{
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);
        List<ResolveInfo> infos = mContext.getPackageManager().queryIntentActivities(mainIntent, 0);
        List<LauncherActivityInfoCompat> list =
			new ArrayList<LauncherActivityInfoCompat>(infos.size());
        for (ResolveInfo info : infos) {
            list.add(new LauncherActivityInfoCompatV16(mContext, info));
        }
        return list;
	}

	@Override
	public void registerOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat call)
	{
		if(!mCallbacks.contains(call))
			mCallbacks.add(call);
			if(mCallbacks.size()==1)
				registerForPackageIntents();
	}

	@Override
	public void unRegisterOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat call)
	{
		mCallbacks.remove(call);
		if(mCallbacks.size()==0)
			unregisterForPackageIntents();
	}

	/*	@Override
	public List<ShortcutInfo> getShortCuts(String activity)
	{
		// TODO: Implement this method
		return null;
	}*/

	
	private void unregisterForPackageIntents()
	{
		mContext.unregisterReceiver(mPackageMonitor);
	}

	private void registerForPackageIntents()
	{
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		mContext.registerReceiver(mPackageMonitor, filter);
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
		mContext.registerReceiver(mPackageMonitor, filter);
	}

	synchronized List<OnAppsChangedCallbackCompat> getCallbacks()
	{
		return new ArrayList<OnAppsChangedCallbackCompat>(mCallbacks);
	}

	class PackageMonitor extends BroadcastReceiver
	{
		public void onReceive(Context context, Intent intent)
		{
			final String action = intent.getAction();
			final UserHandle user = UserHandle.getUserHandleForUid(android.os.Process.myUid());

			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
				|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
				|| Intent.ACTION_PACKAGE_ADDED.equals(action))
			{
				final String packageName = intent.getData().getSchemeSpecificPart();
				final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

				if (packageName == null || packageName.length() == 0)
				{
					// they sent us a bad intent
					return;
				}
				if (Intent.ACTION_PACKAGE_CHANGED.equals(action))
				{
					for (OnAppsChangedCallbackCompat callback : getCallbacks())
					{
						callback.onPackageChanged(packageName, user);
					}
				}
				else if (Intent.ACTION_PACKAGE_REMOVED.equals(action))
				{
					if (!replacing)
					{
						for (OnAppsChangedCallbackCompat callback : getCallbacks())
						{
							callback.onPackageRemoved(packageName, user);
						}
					}
					// else, we are replacing the package, so a PACKAGE_ADDED will be sent
					// later, we will update the package at this time
				}
				else if (Intent.ACTION_PACKAGE_ADDED.equals(action))
				{
					if (!replacing)
					{
						for (OnAppsChangedCallbackCompat callback : getCallbacks())
						{
							callback.onPackageAdded(packageName, user);
						}
					}
					else
					{
						for (OnAppsChangedCallbackCompat callback : getCallbacks())
						{
							callback.onPackageChanged(packageName, user);
						}
					}
				}
			}
			else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action))
			{
				// EXTRA_REPLACING is available Kitkat onwards. For lower devices, it is broadcasted
				// when moving a package or mounting/un-mounting external storage. Assume that
				// it is a replacing operation.
				final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING,
																 !Utilities.ATLEAST_KITKAT);
				String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
				for (OnAppsChangedCallbackCompat callback : getCallbacks())
				{
					callback.onPackagesAvailable(packages, user, replacing);
				}
			}
			else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action))
			{
				// This intent is broadcasted when moving a package or mounting/un-mounting
				// external storage.
				// However on Kitkat this is also sent when a package is being updated, and
				// contains an extra Intent.EXTRA_REPLACING=true for that case.
				// Using false as default for Intent.EXTRA_REPLACING gives correct value on
				// lower devices as the intent is not sent when the app is updating/replacing.
				final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
				String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
				for (OnAppsChangedCallbackCompat callback : getCallbacks())
				{
					callback.onPackagesUnavailable(packages, user, replacing);
				}
			}
		}
	}
}
