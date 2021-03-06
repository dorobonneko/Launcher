package com.moe.icelauncher;
import android.content.ContentProvider;
import android.database.Cursor;
import android.content.ContentValues;
import android.net.Uri;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.content.UriMatcher;
import com.moe.icelauncher.config.ProviderConfig;
import android.text.TextUtils;
import android.content.ContentUris;
import android.content.SharedPreferences;
import android.os.Binder;
import android.content.Context;
import com.moe.icelauncher.model.LauncherModel;
import android.content.*;
import android.provider.*;
import android.content.pm.*;
import com.moe.icelauncher.model.*;
import java.util.*;

public class LauncherProvider extends ContentProvider
{
	private final static String TAG="launcher";
	private SQLiteDatabase sql;
	private UriMatcher mUriMatcher;
	private static final int URI_MATCH_ALLAPPS=0;
	private static final int URI_MATCH_FAVORITES=1;
	private static final int URI_MATCH_WORKSPACESCREENS=2;
	private SharedPreferences settings;
	private LauncherProviderChangeListener mListener;
	@Override
	public boolean onCreate()
	{
		mUriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(ProviderConfig.AUTHORITY,LauncherSettings.AllApps.TABLE_NAME,URI_MATCH_ALLAPPS);
		mUriMatcher.addURI(ProviderConfig.AUTHORITY,LauncherSettings.Favorites.TABLE_NAME,URI_MATCH_FAVORITES);
		mUriMatcher.addURI(ProviderConfig.AUTHORITY,LauncherSettings.WorkspaceScreens.TABLE_NAME,URI_MATCH_WORKSPACESCREENS);
		sql=getContext().openOrCreateDatabase(TAG,Context.MODE_PRIVATE,null);
		if(sql.getVersion()!=1){
			sql.execSQL("DROP TABLE IF EXISTS "+LauncherSettings.AllApps.TABLE_NAME);
			sql.execSQL("CREATE TABLE allapps (_id INTEGER PRIMARY KEY, componentName TEXT NOT NULL UNIQUE, profileId LONG DEFAULT -1, title TEXT, icon BLOB, iconSanifyScale FLOAT, lastUpdateTime LONG NOT NULL DEFAULT 0,flags INTEGER NOT NULL DEFAULT 0,modified INTEGER NOT NULL DEFAULT 0,state INTEGER NOT NULL DEFAULT 0,packageName TEXT NOT NULL)");
			sql.execSQL("DROP TABLE IF EXISTS favorites");
			sql.execSQL("CREATE TABLE favorites (_id INTEGER PRIMARY KEY,title TEXT,intent TEXT,container INTEGER,screen INTEGER,rank INTEGER,cellX REAL,cellY REAL,spanX REAL,spanY REAL,itemType INTEGER,appWidgetId INTEGER NOT NULL DEFAULT -1,icon BLOB,iconSanifyScale FLOAT,appWidgetProvider TEXT,flags INTEGER NOT NULL DEFAULT 0, modified INTEGER NOT NULL DEFAULT 0,restored INTEGER NOT NULL DEFAULT 0,profileId LONG DEFAULT -1,packageName TEXT NOT NULL,state INTEGER NOT NULL DEFAULT -1,options INTEGER)");
			sql.execSQL("DROP TABLE IF EXISTS workspaceScreens");
			sql.execSQL("CREATE TABLE workspaceScreens (_id INTEGER PRIMARY KEY,screenRank INTEGER,allowBlank BOOLEAN NOT NULL DEFAULT false,modified INTEGER NOT NULL DEFAULT 0)");
			sql.execSQL("INSERT INTO workspaceScreens(screenRank,allowBlank,modified) values(0,1,"+System.currentTimeMillis()+")");
			sql.execSQL("DROP TABLE IF EXISTS "+LauncherSettings.Icons.TABLE_NAME);
			sql.execSQL("CREATE TABLE icons (_id INTEGER PRIMARY KEY, componentName TEXT NOT NULL UNIQUE, title TEXT, icon BLOB,modified INTEGER NOT NULL DEFAULT 0,packageName TEXT NOT NULL,version INTEGER)");
			sql.execSQL("DROP TABLE IF EXISTS "+LauncherSettings.Shortcuts.TABLE_NAME);
			sql.execSQL("CREATE TABLE shortcuts (_id INTEGER PRIMARY KEY, intent TEXT NOT NULL UNIQUE, title TEXT,modified INTEGER NOT NULL DEFAULT 0,packageName TEXT NOT NULL)");
			initShortcuts();
			sql.setVersion(1);
			
		}
		/*settings=getContext().openOrCreateDatabase("Settings",0,null);
		if(settings.getSyncedTables().size()==0){
			sql.execSQL("DROP TABLE IF EXISTS ?",new Object[]{LauncherSettings.Settings.TABLE_NAME});
			sql.execSQL("CREATE TABLE ? (? TEXT PRIMARY KEY,? INTEGER DEFAULT 0)",new Object[]{LauncherSettings.Settings.TABLE_NAME,"key","value"});
		}*/
		settings=getContext().getSharedPreferences(LauncherSettings.Settings.TABLE_NAME,getContext().MODE_PRIVATE);
		LauncherModel.getInstance(getContext()).setLauncherProvider(this);
		return true;
	}
	public void setProviderChangeListener(LauncherProviderChangeListener l){
		mListener=l;
	}
	@Override
	public Cursor query(Uri p1, String[] p2, String p3, String[] p4, String p5)
	{
		SqlArguments args=new SqlArguments(p1,p3,p4);
		return sql.query(args.table,p2,args.where,args.args,null,null,p5);
	}

	@Override
	public String getType(Uri uri)
	{
		SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
	}

	@Override
	public Uri insert(Uri p1, ContentValues p2)
	{
		addModifiedTime(p2);
		if(sql.insert(new SqlArguments(p1).table,null,p2)>0){
			if(mListener!=null){
				if(mUriMatcher.match(p1)==URI_MATCH_WORKSPACESCREENS)
					mListener.onAppWidgetHostReset();
				//	else
				//mListener.onLauncherProviderChange();
				}
			return p1;
			}
			return null;
	}

	@Override
	public int delete(Uri p1, String p2, String[] p3)
	{
		SqlArguments args=new SqlArguments(p1,p2,p3);
		
		int size=sql.delete(args.table,args.where,args.args);
		if(size>0&&mListener!=null){
			if(mUriMatcher.match(p1)==URI_MATCH_WORKSPACESCREENS)
				mListener.onAppWidgetHostReset();
			//	else
			//mListener.onLauncherProviderChange();
			}
			return size;
	}

	@Override
	public int update(Uri p1, ContentValues p2, String p3, String[] p4)
	{
		addModifiedTime(p2);
		SqlArguments args=new SqlArguments(p1,p3,p4);
		int size=sql.update(args.table,p2,args.where,args.args);
		//if(size>0&&mListener!=null)
		//	mListener.onLauncherProviderChange();
		return size;
	}

	@Override
	public Bundle call(String method, String arg, Bundle extras)
	{
		if (Binder.getCallingUid() != android.os.Process.myUid()) {
            return null;
        }

        switch (method) {
            case LauncherSettings.Settings.METHOD_GET: {
					Bundle result = new Bundle();
					result.putBoolean(LauncherSettings.Settings.EXTRA_VALUE,
									  settings
									  .getBoolean(arg, extras.getBoolean(
													  LauncherSettings.Settings.EXTRA_DEFAULT_VALUE)));
					return result;
				}
            case LauncherSettings.Settings.METHOD_SET: {
					boolean value = extras.getBoolean(LauncherSettings.Settings.EXTRA_VALUE);
					settings.edit().putBoolean(arg, value).apply();
					if (mListener != null) {
						mListener.onSettingsChanged(arg, value);
					}
					Bundle result = new Bundle();
					result.putBoolean(LauncherSettings.Settings.EXTRA_VALUE, value);
					return result;
				}
        }
        return null;
	}
	static void addModifiedTime(ContentValues values) {
        values.put(LauncherSettings.ChangeLogColumns.MODIFIED, System.currentTimeMillis());
    }
	/*private String getTable(Uri uri){
		switch(mUriMatcher.match(uri)){
			case URI_MATCH_ALLAPPS:
				return LauncherSettings.AllApps.TABLE_NAME;
			case URI_MATCH_FAVORITES:
				return LauncherSettings.Favorites.TABLE_NAME;
			case URI_MATCH_WORKSPACESCREENS:
				return LauncherSettings.WorkspaceScreens.TABLE_NAME;
		}
		return null;
	}*/
	static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
	private void initShortcuts(){
		//通话
		Intent call=new Intent(Intent.ACTION_DIAL);
		//短信
		Intent sms=new Intent(Intent.ACTION_MAIN);
		sms.addCategory(Intent.CATEGORY_APP_MESSAGING);
		//浏览器
		Intent browser=new Intent(Intent.ACTION_MAIN);
		browser.addCategory(Intent.CATEGORY_APP_BROWSER);
		//拍照
		Intent capture=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		insert(call,0);
		insert(sms,1);
		insert(browser,2);
		insert(capture,3);
	}
	private void insert(Intent intent,int cellX){
		PackageManager pm=getContext().getPackageManager();
		List<ResolveInfo> list=pm.queryIntentActivities(intent,pm.GET_UNINSTALLED_PACKAGES|pm.GET_INTENT_FILTERS);
		if(list.size()==0)return;
		ResolveInfo info=list.get(0);
			Intent launcher=pm.getLaunchIntentForPackage(info.activityInfo.packageName);
			if(launcher!=null){
				FavoriteInfo favoriteInfo=new FavoriteInfo();
				favoriteInfo.cellX=cellX;
				favoriteInfo.container=LauncherSettings.Favorites.CONTAINER_HOTSEAT;
				favoriteInfo.intent=launcher.toURI();
				favoriteInfo.itemType=LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
				favoriteInfo.spanX=1;
				favoriteInfo.spanY=1;
				favoriteInfo.state=-1;
				favoriteInfo.title=info.loadLabel(pm).toString();
				favoriteInfo.icon=info.loadIcon(pm);
				favoriteInfo.packageName=info.activityInfo.packageName;
				ContentValues cv=new ContentValues();
				favoriteInfo.addToDatabase(cv,getContext());
				insert(LauncherSettings.Favorites.CONTENT_URI,cv);
			}
			
		
	}
}
