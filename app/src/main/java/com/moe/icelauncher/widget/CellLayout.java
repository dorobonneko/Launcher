package com.moe.icelauncher.widget;
import android.view.ViewGroup;
import android.content.Context;
import android.util.AttributeSet;
import com.moe.icelauncher.view.Drag;
import com.moe.icelauncher.widget.CellLayout.Info;
import android.view.View;
import android.view.MotionEvent;
import android.view.DragEvent;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.moe.icelauncher.model.ItemInfo;
import android.graphics.drawable.Drawable;
import com.moe.icelauncher.LauncherSettings;
import android.content.ContentValues;
import com.moe.icelauncher.model.FavoriteInfo;
import com.moe.icelauncher.model.AppInfo;
import android.os.UserHandle;
import android.database.Cursor;
import android.animation.ValueAnimator;
import android.animation.ObjectAnimator;
import android.animation.Animator;
import com.moe.icelauncher.util.DeviceManager;
import android.content.pm.PackageManager;
import com.moe.icelauncher.compat.LauncherAppsCompat;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.moe.icelauncher.R;
import com.moe.icelauncher.model.IconInfo;
import com.moe.icelauncher.widget.celllayout.AddFavorite;
import com.moe.icelauncher.widget.celllayout.FloatViewPointCheck;
import android.graphics.Rect;

public class CellLayout extends MoeViewGroup implements View.OnClickListener,View.OnDragListener
{
	private int rank;
	private boolean preview=true;
	private boolean border,allowBlank;
	private Paint paint;
	private int background;
	private Runnable hover=new hover();
	private FloatViewPointCheck mPointCheck=new FloatViewPointCheck(this);
	private boolean preview_table,editAble;
	private AddFavorite mAddFavorite=new AddFavorite(this);
	//单格宽高
	private int childWidth,childHeight;
	public CellLayout(Context context){
		this(context,null);
	}
	public CellLayout(Context context,AttributeSet attrs){
		super(context,attrs);
		paint=new Paint();
		paint.setColor(0xffffffff);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		
	}

	public void setPreViewTable(boolean preview_table)
	{
		this.preview_table=preview_table;
	}
	public void reloadFavorites(String packageName){
		if(!isPreview()){
			for(int i=0;i<getChildCount();i++){
				IconView child=(IconView)getChildAt(i);
				ItemInfo info=child.getItemInfo();
				if(info.packageName.equals(packageName))
					removeView(child);
			}
			Cursor cursor=getContext().getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI,null,LauncherSettings.Favorites.CONTAINER+"=? and "+LauncherSettings.Favorites.SCREEN+"=? and "+LauncherSettings.Favorites.PACKAGENAME+"=?",new String[]{(this instanceof HotseatLayout?LauncherSettings.Favorites.CONTAINER_HOTSEAT:LauncherSettings.Favorites.CONTAINER_DESKTOP)+"",rank+"",packageName},null);
			if(cursor!=null){
				while(cursor.moveToNext()){
					FavoriteInfo info=new FavoriteInfo(cursor);
					if(info.appWidgetId==0){
						IconView iconView=new IconView(getContext());
						iconView.setItemInfo(info);
						addView(iconView);
					}
				}
				cursor.close();
			}
		}
	}
	@Override
	public void addView(View child)
	{
		child.setOnClickListener(this);
		child.setOnDragListener(this);
		super.addView(child);
	}

	@Override
	public void onClick(final View view)
	{
		final ItemInfo info=((IconView)view).getItemInfo();
		if(info!=null){
			try{
				switch(info.state){
					case DeviceManager.PACKAGE_UNHIDE:
					case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
						break;
					case DeviceManager.PACKAGE_HIDE:
						LauncherAppsCompat.getInstance(getContext()).setPackageEnabled(info.packageName,true);
						break;
					default:
						//用root权限操作
						break;
				}
				getContext().startActivity(info.getIntent());
			}catch(Exception e){
				try{getContext().getPackageManager().getInstallerPackageName(info.packageName);}
				catch(Exception e1){
					new AlertDialog.Builder(getContext()).setTitle(info.title).setMessage(R.string.package_not_found).setPositiveButton(android.R.string.cancel, null).setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								if(getContext().getContentResolver().delete(LauncherSettings.Favorites.CONTENT_URI,LauncherSettings.Favorites._ID+"=?",new String[]{info._id+""})>0)
								removeView(view);
							}
						}).show();
						}
					}
			}
	}


	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		return dispatchChildTouchEvent(ev);
	}
	public int getChildWidth(){
		return childWidth;
	}
	public int getChildHeight(){
		return childHeight;
	}
	
	public void setAllowBlank(boolean allowBlank){
		this.allowBlank=allowBlank;
	}
	
	public boolean isAllowBlank(){
		return this.allowBlank;
	}
	public int getColnums(){
		return 4;
	}
	public int getRownums(){
		return 4;
	}
	public void setPrebiew(boolean preview){
		this.preview=preview;
	}
	public boolean isPreview(){
		return preview;
	}
	public void setRank(int rank)
	{
		this.rank=rank;
	}
	public int getRank(){
		return rank;
	}
	public void setBorderLine(boolean border){
		this.border=border;
		invalidate();
	}

	
	public void setBackground(int background)
	{
		this.background=background;
		invalidate();
	}

	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		if(border){
			paint.setStyle(Paint.Style.STROKE);
			canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(),paint);
		}
		if(background!=0)
		canvas.drawColor(background);
		if(preview_table&&editAble){
			paint.setStyle(Paint.Style.FILL);
			//绘制表格
			float[] line=new float[4];
			line[1]=0;
			line[3]=canvas.getHeight();
			for(int i=1;i<getColnums();i++){
				line[0]=getChildWidth()*i;
				line[2]=line[0];
				canvas.drawLines(line,paint);
			}
			line[0]=0;
			line[2]=canvas.getWidth();
			for(int i=1;i<getRownums();i++){
				line[1]=getChildHeight()*i;
				line[3]=line[1];
				canvas.drawLines(line,paint);
			}
			
		}
		super.dispatchDraw(canvas);
	}
	
	@Override
	protected void onAttachedToWindow()
	{
		if(!preview)
			loadFavourites();
		super.onAttachedToWindow();
	}
	
	public void loadFavourites(){
		Cursor cursor=getContext().getContentResolver().query(LauncherSettings.Favorites.CONTENT_URI,null,LauncherSettings.Favorites.CONTAINER+"=? and "+LauncherSettings.Favorites.SCREEN+"=?",new String[]{(this instanceof HotseatLayout?LauncherSettings.Favorites.CONTAINER_HOTSEAT:LauncherSettings.Favorites.CONTAINER_DESKTOP)+"",rank+""},null);
		if(cursor!=null){
			while(cursor.moveToNext()){
				FavoriteInfo info=new FavoriteInfo(cursor);
				if(info.appWidgetId==0){
					IconView iconView=new IconView(getContext());
					iconView.setItemInfo(info);
					addView(iconView);
				}
			}
			cursor.close();
		}
		}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int width=MeasureSpec.getSize(widthMeasureSpec);
		int height=MeasureSpec.getSize(heightMeasureSpec);
		childWidth=width/getColnums();
		childHeight=height/getRownums();
		for(int i=0;i<getChildCount();i++){
			View child=getChildAt(i);
			child.measure(getSize(childWidth),getSize(childHeight));
		}
		setMeasuredDimension(getSize(width),getSize(height));
	}
	public int getSize(int size){
		return MeasureSpec.makeMeasureSpec(size,MeasureSpec.EXACTLY);
	}
	@Override
	protected void onLayout(boolean p1, int p2, int p3, int p4, int p5)
	{
		int width=getMeasuredWidth()/getColnums();
		int height=getMeasuredHeight()/getRownums();
		for(int i=0;i<getChildCount();i++){
			View child=getChildAt(i);
			if(child instanceof IconView){
				IconView iconView=(IconView) child;
				ItemInfo info=iconView.getItemInfo();
				if(info instanceof FavoriteInfo){
					FavoriteInfo favoriteInfo=(FavoriteInfo) info;
					child.layout(favoriteInfo.cellX*width,favoriteInfo.cellY*height,favoriteInfo.cellX*width+width,favoriteInfo.cellY*height+height);
				}
			}
		}
	}
	@Override
	public boolean onDragEvent(DragEvent event)
	{
		switch(event.getAction()){
			case event.ACTION_DRAG_STARTED:
				editAble=true;
				setBackground(0x50ffffff);
				break;
			case event.ACTION_DRAG_ENTERED:
				setBorderLine(true);
				if(!(this instanceof HotseatLayout))
				postDelayed(hover,500);
				break;
			case event.ACTION_DRAG_EXITED:
				removeCallbacks(hover);
				setBorderLine(false);
				break;
			case event.ACTION_DRAG_ENDED:
				editAble=false;
				setBorderLine(false);
				setBackground(0);
				checkItems();
				break;
			case event.ACTION_DROP:
				int[] cellXY=pointToSpan(event.getX(),event.getY());
				if(getViewFromPoint(cellXY[0],cellXY[1])== ((CellLayout.Info)event.getLocalState()).view)break;
				mAddFavorite.set(cellXY,(CellLayout.Info)event.getLocalState());
				post(mAddFavorite);
				break;
		}
		invalidate();
		return true;
	}
	private int[] pointToSpan(float x,float y){
		int[] cellXY=new int[2];
		cellXY[0]=(int)(x/(getMeasuredWidth()/getColnums()));
		cellXY[1]=(int)(y/(getMeasuredHeight()/getRownums()));
		return cellXY;
	}
	
	private void checkItems(){
		if(!isPreview()&&!isAllowBlank()&&getChildCount()==0)
			if(getContext().getContentResolver().delete(LauncherSettings.WorkspaceScreens.CONTENT_URI,LauncherSettings.WorkspaceScreens.SCREEN_RANK+"=?",new String[]{getRank()+""})>0)
				((ViewGroup)getParent()).removeView(this);
	}
	public View getViewFromPoint(int cellx,int celly){
		return findViewInPoint(cellx*childWidth,celly*childHeight);
	}
	public static class Info{
		public View view;
		public ItemInfo info;
	}
	private class hover implements Runnable
	{

		@Override
		public void run()
		{
			((WorkSpaceLayout)getParent()).toggleToView(CellLayout.this);
		}
	}
	
	@Override
	public boolean onDrag(View p1, DragEvent p2)
	{
		switch(p2.getAction()){
			case p2.ACTION_DRAG_LOCATION:
				//悬浮检查
			//	mPointCheck.setPoint(cell[0],cell[1],p2.getX(),p2.getY());
			//	removeCallbacks(mPointCheck);
			//	postDelayed(mPointCheck,200);
				break;
		}
		return true;
	}
	
}
