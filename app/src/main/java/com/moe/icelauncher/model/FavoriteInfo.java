package com.moe.icelauncher.model;
import android.content.Intent;
import android.content.ContentValues;
import com.moe.icelauncher.LauncherSettings;
import com.moe.icelauncher.IconCache;
import com.moe.icelauncher.Utilities;
import java.net.URISyntaxException;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.content.Context;

public class FavoriteInfo extends ItemInfo
{
	public int container;
	public int screen,cellX,cellY,spanX,spanY,profileId,appWidgetId,restored,options;
	public String appWidgetProvider,intent;
	public int rank;
	public float iconSanifyScale;
	@Override
	public Intent getIntent()
	{
		try
		{
			return Intent.parseUri(intent, 0);
		}
		catch (URISyntaxException e)
		{}
		return null;
	}
	public void addToDatabase(ContentValues cv,Context context){
	cv.put(LauncherSettings.Favorites.ITEMTYPE,itemType);
	cv.put(LauncherSettings.Favorites.ICON,Utilities.flattenDrawable(icon,context));
	cv.put(LauncherSettings.Favorites.TITLE,title);
	cv.put(LauncherSettings.Favorites.PACKAGENAME,packageName);
	cv.put(LauncherSettings.Favorites.CONTAINER,container);
	cv.put(LauncherSettings.Favorites.SCREEN,screen);
	cv.put(LauncherSettings.Favorites.CELLX,cellX);
	cv.put(LauncherSettings.Favorites.CELLY,cellY);
	cv.put(LauncherSettings.Favorites.SPANX,spanX);
	cv.put(LauncherSettings.Favorites.SPANY,spanY);
	cv.put(LauncherSettings.Favorites.PROFILE_ID,profileId);
	cv.put(LauncherSettings.Favorites.APPWIDGET_ID,appWidgetId);
	cv.put(LauncherSettings.Favorites.RESTORED,restored);
	cv.put(LauncherSettings.Favorites.OPTIONS,options);
	cv.put(LauncherSettings.Favorites.APPWIDGET_PROVIDER,appWidgetProvider);
	cv.put(LauncherSettings.Favorites.INTENT,intent);
	cv.put(LauncherSettings.Favorites.RANK,rank);
	cv.put(LauncherSettings.Favorites.ICONSANIFYSCALE,iconSanifyScale);
	cv.put(LauncherSettings.Favorites.STATE,state);
	
	}
	public FavoriteInfo(){}
	public FavoriteInfo(Cursor cursor){
		itemType=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.ITEMTYPE));
		_id=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites._ID));
		byte[] iconData=cursor.getBlob(cursor.getColumnIndex(LauncherSettings.Favorites.ICON));
		icon=new BitmapDrawable(BitmapFactory.decodeByteArray(iconData,0,iconData.length));
		title=cursor.getString(cursor.getColumnIndex(LauncherSettings.Favorites.TITLE));
		modified=cursor.getLong(cursor.getColumnIndex(LauncherSettings.Favorites.MODIFIED));
		container=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.CONTAINER));
		cellX=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.CELLX));
		cellY=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.CELLY));
		spanX=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.SPANX));
		spanY=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.SPANY));
		profileId=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.PROFILE_ID));
		appWidgetId=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.APPWIDGET_ID));
		restored=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.RESTORED));
		options=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.OPTIONS));
		appWidgetProvider=cursor.getString(cursor.getColumnIndex(LauncherSettings.Favorites.APPWIDGET_PROVIDER));
		intent=cursor.getString(cursor.getColumnIndex(LauncherSettings.Favorites.INTENT));
		rank=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.ITEMTYPE));
		iconSanifyScale=cursor.getFloat(cursor.getColumnIndex(LauncherSettings.Favorites.ICONSANIFYSCALE));
		state=cursor.getInt(cursor.getColumnIndex(LauncherSettings.Favorites.STATE));
		packageName=cursor.getString(cursor.getColumnIndex(LauncherSettings.Favorites.PACKAGENAME));
	}
}
