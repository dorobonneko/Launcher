package com.moe.icelauncher;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import com.moe.icelauncher.compat.*;
import com.moe.icelauncher.adapter.*;
import android.content.*;
import android.content.pm.*;
import java.util.*;
import android.graphics.drawable.*;

public class DetailsActivity extends ListActivity implements ListView.OnItemLongClickListener
{
	private ActivityAdapter mActivityAdapter;
	private AlertDialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		try{
			PackageInfo packageInfo=getPackageManager().getPackageInfo(getIntent().getPackage(),PackageManager.GET_UNINSTALLED_PACKAGES|PackageManager.GET_ACTIVITIES);
			setTitle(packageInfo.applicationInfo.loadLabel(getPackageManager()));
			getActionBar().setSubtitle(packageInfo.packageName);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			//getActionBar().setLogo(packageInfo.applicationInfo.loadUnbadgedIcon(getPackageManager()));
			//getActionBar().setDisplayUseLogoEnabled(true);
		setListAdapter(mActivityAdapter=new ActivityAdapter(packageInfo.activities));
		getListView().setOnItemLongClickListener(this);
		}catch(Exception e){
			finish();
			Toast.makeText(this,R.string.package_not_found,Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		try{
			ActivityInfo activity=(ActivityInfo) getListAdapter().getItem(position);
			startActivity(new Intent().setComponent(new ComponentName(activity.packageName,activity.name)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}catch(Exception e){
			Toast.makeText(this,R.string.activity_not_opened,Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> p1, View p2, int position, long p4)
	{
		final ActivityInfo activity=(ActivityInfo) getListAdapter().getItem(position);
		if(dialog==null)
			dialog = new AlertDialog.Builder(this).setTitle(R.string.shortcut).setView(R.layout.activity_dialog).setPositiveButton(android.R.string.cancel, null).setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						
						runOnUiThread(new Shortcut(((TextView)dialog.findViewById(R.id.name)).getText().toString(),ComponentName.unflattenFromString(((TextView)dialog.findViewById(R.id.class_name)).getText().toString())));
					}
				}).create();
			dialog.show();
			((TextView)dialog.findViewById(R.id.name)).setText(activity.loadLabel(getPackageManager()));
			((TextView)dialog.findViewById(R.id.class_name)).setText(new ComponentName(activity.packageName,activity.name).flattenToString());
			//dialog.show();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()){
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class Shortcut implements Runnable
	{
		private String title;
		private ComponentName mComponentName;
		public Shortcut(String title,ComponentName componentName){
			this.title=title;
			this.mComponentName=componentName;
		}
		@Override
		public void run()
		{
			Intent intent=new Intent();
			intent.setComponent(mComponentName);
			ContentValues cv=new ContentValues();
			cv.put(LauncherSettings.Shortcuts.INTENT,intent.toURI());
			cv.put(LauncherSettings.Shortcuts.PACKAGENAME,mComponentName.getPackageName());
			cv.put(LauncherSettings.Shortcuts.TITLE,title);
			getContentResolver().insert(LauncherSettings.Shortcuts.CONTENT_URI,cv);
		}

		
	}
}
