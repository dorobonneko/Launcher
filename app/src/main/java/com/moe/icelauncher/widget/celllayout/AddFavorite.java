package com.moe.icelauncher.widget.celllayout;
import com.moe.icelauncher.widget.CellLayout;
import com.moe.icelauncher.model.ItemInfo;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewGroup;
import com.moe.icelauncher.widget.WorkSpaceLayout;
import android.content.ContentValues;
import com.moe.icelauncher.LauncherSettings;
import com.moe.icelauncher.widget.IconView;
import com.moe.icelauncher.model.FavoriteInfo;
import com.moe.icelauncher.model.AppInfo;
import android.content.Context;
import com.moe.icelauncher.widget.HotseatLayout;

public class AddFavorite implements Runnable
{
	private CellLayout mCellLayout;
	private int[] cellXY;
	private CellLayout.Info info;
	private WorkSpaceLayout mWorkSpaceLayout;
	public AddFavorite(CellLayout cell){
		this.mCellLayout=cell;
		}
	public void set(int[] cell,CellLayout.Info info){
		cellXY=cell;
		this.info=info;
	}
	private Context getContext(){
		return mCellLayout.getContext();
	}
	@Override
	public void run()
	{
		addFavourites(cellXY,info);
	}
	public void addFavourites(int[] cell,CellLayout.Info celllayout){
		ItemInfo info=celllayout.info;
		View view=celllayout.view;
		if(mWorkSpaceLayout==null)
			mWorkSpaceLayout=(WorkSpaceLayout) mCellLayout.getParent();
		if(mCellLayout.isPreview()){
			mCellLayout.setPrebiew(false);
			int index=mWorkSpaceLayout.indexOfChild(mCellLayout);
			if(index==0){
				mCellLayout.setRank(((CellLayout)mWorkSpaceLayout.getChildAt(index+1)).getRank()-1);
				mWorkSpaceLayout.addPreview();
			}else{
				mCellLayout.setRank(((CellLayout)mWorkSpaceLayout.getChildAt(index-1)).getRank()+1);
				mWorkSpaceLayout.addNextview();
			}
			//加入数据库
			ContentValues cv=new ContentValues();
			cv.put(LauncherSettings.WorkspaceScreens.SCREEN_RANK,mCellLayout.getRank());
			if(mCellLayout.getContext().getContentResolver().insert(LauncherSettings.WorkspaceScreens.CONTENT_URI,cv)==null)
				throw new IllegalStateException("workspace database insert failed");
		}
		//创建图标
		IconView iconView=new IconView(mCellLayout.getContext());
		if(info instanceof AppInfo){
			AppInfo appInfo=(AppInfo) info;
			FavoriteInfo favorite=new FavoriteInfo();
			favorite.cellX=cell[0];
			favorite.cellY=cell[1];
			favorite.container=mCellLayout instanceof HotseatLayout?LauncherSettings.Favorites.CONTAINER_HOTSEAT:LauncherSettings.Favorites.CONTAINER_DESKTOP;
			favorite.icon=appInfo.icon;
			favorite.iconSanifyScale=appInfo.iconSanifyScale;
			favorite.intent=appInfo.getIntent().toURI();
			favorite.itemType=appInfo.itemType;
			favorite.packageName=appInfo.packageName;
			favorite.profileId=appInfo.profileId;
			favorite.screen=mCellLayout.getRank();
			favorite.spanX=1;
			favorite.spanY=1;
			favorite.state=appInfo.state;
			favorite.title=appInfo.title;
			favorite.user=android.os.Process.myUserHandle();
			ContentValues cv=new ContentValues();
			favorite.addToDatabase(cv,getContext());
			getContext().getContentResolver().insert(LauncherSettings.Favorites.CONTENT_URI,cv);
			iconView.setItemInfo(favorite);
		}else if(info instanceof FavoriteInfo){
			FavoriteInfo favoriteInfo=(FavoriteInfo) info;
			favoriteInfo.cellX=cell[0];
			favoriteInfo.cellY=cell[1];
			favoriteInfo.screen=mCellLayout.getRank();
			favoriteInfo.container=mCellLayout instanceof HotseatLayout?LauncherSettings.Favorites.CONTAINER_HOTSEAT:LauncherSettings.Favorites.CONTAINER_DESKTOP;
			ContentValues cv=new ContentValues();
			favoriteInfo.addToDatabase(cv,getContext());
			iconView.setItemInfo(favoriteInfo);
			getContext().getContentResolver().update(LauncherSettings.Favorites.CONTENT_URI,cv,LauncherSettings.Favorites._ID+"=?",new String[]{favoriteInfo._id+""});
		}
		mCellLayout.addView(iconView);
		if(view instanceof IconView){
			if(info instanceof FavoriteInfo)
				((IconView)view).remove();
		}
	}
}
