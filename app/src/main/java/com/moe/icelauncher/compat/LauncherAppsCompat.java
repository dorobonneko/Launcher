package com.moe.icelauncher.compat;
import android.content.pm.LauncherApps;
import android.content.Context;
import android.os.UserHandle;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ShortcutInfo;
import java.util.List;
import java.util.Collections;
import android.appwidget.AppWidgetProviderInfo;
import android.appwidget.AppWidgetManager;
import com.moe.icelauncher.Utilities;
import android.content.pm.ResolveInfo;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherActivityInfo;
import android.os.Bundle;
import android.appwidget.AppWidgetHost;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import com.moe.icelauncher.IconCache;

public abstract class LauncherAppsCompat
{
	private static LauncherAppsCompat compat;
	public interface OnAppsChangedCallbackCompat {
        void onPackageRemoved(String packageName, UserHandle user);
        void onPackageAdded(String packageName, UserHandle user);
        void onPackageChanged(String packageName, UserHandle user);
        void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing);
        void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing);
		//void onShortcutsChanged(String packageName, List<ShortcutInfo> shortcuts, UserHandle user);
    }
	public static LauncherAppsCompat getInstance(Context context){
		if(compat==null){
			synchronized(LauncherAppsCompat.class){
				if(compat==null)
				{
					if(Utilities.ATLEAST_LOLLIPOP)
						compat=new LauncherAppsCompatVL(context);
						else
						compat=new LauncherAppsCompatV16(context);
				}
			}
		}
		return compat;
	}
		//public abstract List<ShortcutInfo> getShortCuts(String activity);
		public abstract List<AppWidgetProviderInfo> getAppWidgetProviders(String packageName);
		public abstract List<LauncherActivityInfoCompat> getActivityList(String packageName,UserHandle user);
		public abstract void registerOnAppsChangedCallback(OnAppsChangedCallbackCompat call);
		public abstract void unRegisterOnAppsChangedCallback(OnAppsChangedCallbackCompat call);
	public abstract boolean bindAppWidgetIdIfAllowed(
		int appWidgetId, AppWidgetProviderInfo info, Bundle options);
	public abstract void startConfigActivity(AppWidgetProviderInfo info, int widgetId, Activity activity,
									AppWidgetHost host, int requestCode);
	public abstract Drawable loadPreview(AppWidgetProviderInfo info) ;
	public abstract Drawable loadIcon(AppWidgetProviderInfo info, IconCache cache);
	public abstract String loadLabel(AppWidgetProviderInfo info);
	public abstract List<AppWidgetProviderInfo> getAllProviders();
	public abstract boolean setPackageEnabled(String packageName,boolean enabled);
	public abstract boolean isPackageEnabled(String packageName);
	public abstract UserHandle getUser();
		
}
