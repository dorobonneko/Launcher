package com.moe.icelauncher;

import android.app.*;
import android.os.*;
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.app.admin.DevicePolicyManager;
import com.moe.icelauncher.util.DeviceManager;
import com.moe.icelauncher.model.LauncherModel;
import com.moe.icelauncher.widget.GridView;
import java.util.List;
import com.moe.icelauncher.model.AppInfo;
import java.util.ArrayList;
import com.moe.icelauncher.adapter.IconsAdapter;
import java.util.Collections;
import java.util.Comparator;
import android.widget.AdapterView;
import android.widget.Adapter;
import android.view.View;
import android.content.pm.PackageManager;
import com.moe.icelauncher.compat.LauncherAppsCompat;
import android.content.ComponentName;
import com.moe.icelauncher.widget.ContainerView;
import com.moe.icelauncher.widget.WorkSpaceLayout;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import com.moe.icelauncher.widget.HotseatLayout;
import com.moe.icelauncher.widget.CellLayout;
import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;
import com.moe.icelauncher.util.SubThread;
import com.moe.icelauncher.widget.IconView;
import com.moe.icelauncher.model.ItemInfo;

public class LauncherActivity extends Activity implements LauncherModel.Callback,GridView.OnItemClickListener,ContainerView.ScrollCallback,SharedPreferences.OnSharedPreferenceChangeListener,View.OnClickListener,View.OnLongClickListener,TextWatcher
{

	
	private LauncherAppsCompat mLauncherAppsCompat;
	private List<AppInfo> applist=new ArrayList<>();
	private GridView grid;
	private ContainerView mContainerView;
	private WorkSpaceLayout mWorkSpaceLayout;
	private LauncherModel mLauncherModel;
	private SharedPreferences mSharedPreferences;
	private HotseatLayout hotseat;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		mSharedPreferences=getSharedPreferences(LauncherSettings.Settings.TABLE_NAME,MODE_PRIVATE);
		SubThread.getInstance();//初始化
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		//跟随系统
		////startActivity(new Intent(this,SettingsActivity.class));
		grid=findViewById(R.id.gridview);
		grid.setAdapter(new IconsAdapter(applist));
		((IconsAdapter)grid.getAdapter()).setOnItemClickListener(this);
		mContainerView=findViewById(R.id.containerview);
		((TextView)mContainerView.findViewById(R.id.searchview)).addTextChangedListener(this);
		mWorkSpaceLayout=(WorkSpaceLayout) mContainerView.getChildAt(0);
		mWorkSpaceLayout.setPreViewTable(mSharedPreferences.getBoolean(Utilities.SharedPreferences.PREVIEWTABLE,false));
		hotseat=(HotseatLayout) mContainerView.getChildAt(2);
		mContainerView.setOnLongClickListener(this);
		//mContainerView.registerOnScrollCallback(this);
		mLauncherAppsCompat=LauncherAppsCompat.getInstance(this);
		mLauncherModel=LauncherModel.getInstance(this);
		mLauncherModel.setCallback(this);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		grid.setNumColumns(mSharedPreferences.getInt(Utilities.SharedPreferences.COLNUMS,4));
		if(mSharedPreferences.getBoolean(Utilities.SharedPreferences.ALLOW_ROTATION_PREFERENCE_KEY,false))
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
    }

	@Override
	public void onInited(LauncherModel launcher)
	{
		launcher.onCreate();
	}

	
	@Override
	public void onSuccess(List<AppInfo> list)
	{
		notifyDataSetChanged();
	}
	
	@Override
	protected void onNewIntent(Intent intent)
	{
		//多次点击home键
		super.onNewIntent(intent);
		boolean home=intent.getBooleanExtra("android.intent.extra.FROM_HOME_KEY",false);
		if(home&&hasWindowFocus())
			onBackPressed();
	}

	@Override
	protected void onRestart()
	{
		// TODO: Implement this method
		super.onRestart();
		
	}

	@Override
	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		final ItemInfo info=((IconView)p2).getItemInfo();
		if(info!=null){
			try{
			switch(info.state){
				case DeviceManager.PACKAGE_UNHIDE:
				case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
					break;
				case DeviceManager.PACKAGE_HIDE:
					mLauncherAppsCompat.setPackageEnabled(info.packageName,true);
					break;
				default:
				//用root权限操作
				break;
			}
			startActivity(info.getIntent());
		}catch(Exception e){
			try{getPackageManager().getInstallerPackageName(info.packageName);}
			catch(Exception e1){
				new AlertDialog.Builder(this).setTitle(info.title).setMessage(R.string.package_not_found).setPositiveButton(android.R.string.cancel, null).setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							if(getContentResolver().delete(LauncherSettings.AllApps.CONTENT_URI,LauncherSettings.AllApps.PACKAGENAME+"=?",new String[]{info.packageName})>0){
							mLauncherModel.getAppsList().remove(info);
							applist.remove(info);
							((IconsAdapter)grid.getAdapter()).notifyDataSetChanged();
							}
							}
					}).show();
			}
		}
		}
	}

	@Override
	public void onBackPressed()
	{
		mContainerView.onBack();
		
	}

	@Override
	public void onStartScroll(ContainerView view, float fractor)
	{
		// TODO: Implement this method
	}

	@Override
	public void onStopScroll(ContainerView view, float fractor)
	{
		
	}

	@Override
	public void onScroll(ContainerView view, int dy, float fractor)
	{
		// TODO: Implement this method
	}


	@Override
	public void onLauncherProviderChange(String packageName)
	{
		hotseat.reloadFavorites(packageName);
		for(int i=0;i<mWorkSpaceLayout.getChildCount();i++)
		((CellLayout)mWorkSpaceLayout.getChildAt(i)).reloadFavorites(packageName);
	}

	@Override
	public void onSettingsChanged(String settings, boolean value)
	{
		
	}

	@Override
	public void onAppWidgetHostReset()
	{
		// TODO: Implement this method
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences p1, String p2)
	{
		switch(p2){
			case Utilities.SharedPreferences.COLNUMS:
				grid.setNumColumns(p1.getInt(p2,4));
				break;
			case Utilities.SharedPreferences.ALLOW_ROTATION_PREFERENCE_KEY:
				if(p1.getBoolean(p2,false))
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
				else
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
			case Utilities.SharedPreferences.PREVIEWTABLE:
				mWorkSpaceLayout.setPreViewTable(p1.getBoolean(p2,false));
				break;
		}
	}

	@Override
	public void onClick(View p1)
	{
		
	}

	@Override
	public boolean onLongClick(View p1)
	{
		switch(p1.getId()){
			case R.id.containerview:
				startActivity(new Intent(this,SettingsActivity.class));
				return true;
		}
		return false;
	}

	@Override
	public void notifyDataSetChanged()
	{
		applist.clear();
		applist.addAll(mLauncherModel.getAppsList());
		((IconsAdapter)grid.getAdapter()).notifyDataSetChanged();
	}




	@Override
	public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4)
	{
		
	}

	@Override
	public void onTextChanged(CharSequence p1, int p2, int p3, int p4)
	{
		
	}

	@Override
	public void afterTextChanged(Editable p1)
	{
		IconsAdapter iconsAdapter=(IconsAdapter) grid.getAdapter();
		if(iconsAdapter!=null)
			iconsAdapter.filter(p1);
	}

	
}
