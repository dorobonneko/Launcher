package com.moe.icelauncher.widget.celllayout;
import com.moe.icelauncher.widget.CellLayout;
import android.graphics.Rect;
import android.view.View;
import com.moe.icelauncher.widget.IconView;
import com.moe.icelauncher.model.ItemInfo;
import com.moe.icelauncher.model.FavoriteInfo;
import java.util.Arrays;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.ViewConfiguration;
import android.content.ContentValues;
import com.moe.icelauncher.LauncherSettings;

public class FloatViewPointCheck implements Runnable,ValueAnimator.AnimatorUpdateListener
{
	private CellLayout mCellLayout;
	private float x,y;
	private int spanX,spanY;
	private Rect[][] rects;
	private Rect oldPoint;
	private View[] floatView;
	private float distance;//边缘距离
	private float oldValue;
	private Direction mDirection;
	private ValueAnimator anime;
	private boolean active;
	private Object locked=new Object();
	public FloatViewPointCheck(CellLayout cell)
	{
		mCellLayout = cell;
		distance = -1;
		rects = new Rect[mCellLayout.getRownums()][];
	}

	public void save()
	{
		synchronized(locked){
		if(floatView!=null&&oldValue!=0){
			switch(mDirection){
			case Left:
			case Right:
					int offset=(int)(oldValue/mCellLayout.getChildWidth());
				for(View view:floatView){
					if(view instanceof IconView){
						ItemInfo info=((IconView)view).getItemInfo();
						if(info instanceof FavoriteInfo){
							FavoriteInfo favoriteInfo=(FavoriteInfo) info;
							favoriteInfo.cellX+=offset;
							//favoriteInfo.cellY+=offset;
							//favoriteInfo.spanX+=offset;
							//favoriteInfo.spanY+=offset;
							ContentValues cv=new ContentValues();
							favoriteInfo.addToDatabase(cv,mCellLayout.getContext());
							mCellLayout.getContext().getContentResolver().update(LauncherSettings.Favorites.CONTENT_URI,cv,LauncherSettings.Favorites._ID+"=?",new String[]{favoriteInfo._id+""});
						}
					}
					view.setTranslationX(0);
					view.setTranslationY(0);
				}
				break;
			case Top:
			case Bottom:
					int offsetY=(int)(oldValue/mCellLayout.getChildHeight());
					for(View view:floatView){
						if(view instanceof IconView){
							ItemInfo info=((IconView)view).getItemInfo();
							if(info instanceof FavoriteInfo){
								FavoriteInfo favoriteInfo=(FavoriteInfo) info;
								//favoriteInfo.cellX+=offset;
								favoriteInfo.cellY+=offsetY;
								//favoriteInfo.spanX+=offset;
								//favoriteInfo.spanY+=offsetY;
								ContentValues cv=new ContentValues();
								favoriteInfo.addToDatabase(cv,mCellLayout.getContext());
								mCellLayout.getContext().getContentResolver().update(LauncherSettings.Favorites.CONTENT_URI,cv,LauncherSettings.Favorites._ID+"=?",new String[]{favoriteInfo._id+""});
							}
						}
						view.setTranslationX(0);
						view.setTranslationY(0);
					}
				break;
		}
		mCellLayout.invalidate();
		}
		floatView=null;
		}
	}
	public void setActive(boolean active){
		this.active=active;
	}
	public void setPoint(float x, float y, int spanX, int spanY)
	{
		this.x = x;
		this.y = y;
		this.spanX = spanX;
		this.spanY = spanY;
	}
	@Override
	public void run()
	{
		synchronized(locked){
		if(distance==-1)distance=mCellLayout.getChildWidth()/2f;
		//检查view的移动
		Arrays.fill(rects, null);
		
		/*for(int i=0;i<mCellLayout.getChildCount();i++){
		 View view=mCellLayout.getChildAt(i);
		 if(view instanceof IconView){
		 IconView iconView=(IconView)view;
		 ItemInfo itemInfo=iconView.getItemInfo();
		 if(itemInfo instanceof FavoriteInfo){
		 FavoriteInfo favoriteInfo=(FavoriteInfo) itemInfo;
		 Rect[] row=rects[favoriteInfo.cellX];
		 if(row==null)
		 row=rects[favoriteInfo.cellX]=new Rect[mCellLayout.getColnums()];
		 Rect rect=null;
		 row[favoriteInfo.cellY]=rect=new Rect();
		 rect.left=favoriteInfo.cellX*mCellLayout.getChildWidth();
		 rect.top=favoriteInfo.cellY*mCellLayout.getChildHeight();
		 rect.right=rect.left+favoriteInfo.spanX*mCellLayout.getChildWidth();
		 rect.bottom=rect.top+favoriteInfo.spanY*mCellLayout.getChildHeight();
		 }
		 }
		 }*/
		//初始化view位置数据
		//检查当前位置
		int[] cellXY=mCellLayout.pointToCell(x, y);
		Rect floatPoint=new Rect();
		floatPoint.left = cellXY[0];
		floatPoint.top = cellXY[1];
		floatPoint.right = floatPoint.left + spanX;
		floatPoint.bottom = floatPoint.top + spanY;
		if(!active){
			exit(null);
			return;
		}
		if (oldPoint != null&&!oldPoint.isEmpty())
		{
			if (oldPoint.contains(cellXY[0], cellXY[1]))
			{
				location();
			}
			else
			{
				exit(floatPoint);//退出旧点
				enter(floatPoint);//进入新点
			}
		}
		else
		{
			//旧点不存在，进入新点
			enter(floatPoint);
		}
		}
	}
	//在点内移动，判断是否在创建文件夹的区域
	private void location()
	{

	}
	//进入点
	private void enter(Rect cell)
	{
		//判断此处是否有视图，如果有并且偏移点在边缘，就移动此视图(如果可能的话)
		View[] view=mCellLayout.findViewsFromRect(cell);
		if (view.length == 0)return;
		floatView = view;
		Rect viewRect=new Rect(mCellLayout.getMeasuredWidth(), mCellLayout.getMeasuredHeight(), 0, 0);
		if (oldPoint == null)
			oldPoint = new Rect();
		oldPoint.left = mCellLayout.getColnums();
		oldPoint.top = mCellLayout.getRownums();
		oldPoint.right = 0;
		oldPoint.bottom = 0;
		for (View child:view)
		{
			if(child==null)
				return;
			viewRect.left = Math.min(child.getLeft(), viewRect.left);
			viewRect.top = Math.min(child.getTop(), viewRect.top);
			viewRect.right = Math.max(child.getRight(), viewRect.right);
			viewRect.bottom = Math.max(child.getBottom(), viewRect.bottom);
			if (child instanceof Cell)
			{
				Cell iconView=(Cell) child;
				oldPoint.left = Math.min(iconView.getCellX(), oldPoint.left);
				oldPoint.top = Math.min(iconView.getCellY(), oldPoint.top);
				oldPoint.right = Math.max(oldPoint.right, iconView.getCellX() + iconView.getSpanX());
				oldPoint.bottom = Math.max(oldPoint.bottom, iconView.getCellY() + iconView.getSpanY());
			}
		}
		//获得当前view的位置
		
		//判断触摸点是否在边缘
		if (x - viewRect.left < distance)
		{
			//处于左边缘
			if (oldPoint.right + cell.width() < mCellLayout.getColnums()&&!check(oldPoint.left+cell.width(),oldPoint.top,oldPoint.right+cell.width(),oldPoint.bottom))
			{
				right(cell);
				//向右移
			}
			else if (oldPoint.left - cell.width() >= 0&&!check(oldPoint.left-cell.width(),oldPoint.top,oldPoint.right-cell.width(),oldPoint.bottom))
			{
				left(cell);
				//向左移
			}
			else if (oldPoint.top - cell.height() >= 0&&!check(oldPoint.left,oldPoint.top-cell.height(),oldPoint.right,oldPoint.bottom-cell.height()))
			{
				top(cell);
				//向上移
			}
			else if (oldPoint.bottom + cell.height() < mCellLayout.getRownums()&&!check(oldPoint.left,oldPoint.top+cell.height(),oldPoint.right,oldPoint.bottom+cell.height()))
			{
				bottom(cell);
				//向下移
			}
		}
		else if (y - viewRect.top < distance)
		{
			//处于上边缘
			if (oldPoint.bottom + cell.height() < mCellLayout.getRownums()&&!check(oldPoint.left,oldPoint.top+cell.height(),oldPoint.right,oldPoint.bottom+cell.height()))
			{
				bottom(cell);
				//向下移
			}
			else if (oldPoint.top - cell.height() >= 0&&!check(oldPoint.left,oldPoint.top-cell.height(),oldPoint.right,oldPoint.bottom-cell.height()))
			{
				top(cell);
				//向上移
			}
			else if (oldPoint.right + cell.width() < mCellLayout.getColnums()&&!check(oldPoint.left+cell.width(),oldPoint.top,oldPoint.right+cell.width(),oldPoint.bottom))
			{
				right(cell);
				//向右移
			}
			else if (oldPoint.left - cell.width() >= 0&&!check(oldPoint.left-cell.width(),oldPoint.top,oldPoint.right-cell.width(),oldPoint.bottom))
			{
				left(cell);
				//向左移
			}
		}
		else if (viewRect.right - x < distance)
		{
			//右边缘
			if (oldPoint.left - cell.width() >= 0&&!check(oldPoint.left-cell.width(),oldPoint.top,oldPoint.right-cell.width(),oldPoint.bottom))
			{
				left(cell);
				//向左移
			}
			else if (oldPoint.right + cell.width() < mCellLayout.getColnums()&&!check(oldPoint.left+cell.width(),oldPoint.top,oldPoint.right+cell.width(),oldPoint.bottom))
			{
				//向右移
				right(cell);
			}
			else if (oldPoint.top - cell.height() >= 0&&!check(oldPoint.left,oldPoint.top-cell.height(),oldPoint.right,oldPoint.bottom-cell.height()))
			{
				//向上移
				top(cell);
			}
			else if (oldPoint.bottom + cell.height() < mCellLayout.getRownums()&&!check(oldPoint.left,oldPoint.top+cell.height(),oldPoint.right,oldPoint.bottom+cell.height()))
			{
				//向下移
				bottom(cell);
			}
		}
		else if (viewRect.bottom - y < distance)
		{
			//下边缘
			if (oldPoint.top - cell.height() >= 0&&!check(oldPoint.left,oldPoint.top-cell.height(),oldPoint.right,oldPoint.bottom-cell.height()))
			{
				//向上移
				top(cell);
			}
			else if (oldPoint.bottom + cell.height() < mCellLayout.getRownums()&&!check(oldPoint.left,oldPoint.top+cell.height(),oldPoint.right,oldPoint.bottom+cell.height()))
			{
				//向下移
				bottom(cell);
			}
			else if (oldPoint.right + cell.width() < mCellLayout.getColnums()&&!check(oldPoint.left+cell.width(),oldPoint.top,oldPoint.right+cell.width(),oldPoint.bottom))
			{
				//向右移
				right(cell);
			}
			else if (oldPoint.left - cell.width() >= 0&&!check(oldPoint.left-cell.width(),oldPoint.top,oldPoint.right-cell.width(),oldPoint.bottom))
			{
				//向左移
				left(cell);
			}
		}
		else
		{
			//处于中心
		}
	}
	//退出点
	private void exit(Rect rect)
	{
		//判断当前点的视图是否处于偏移状态，如果有，则移回
		if(floatView==null)return;
		if(anime!=null&&anime.isRunning())
			anime.cancel();
		if(oldValue!=0){
			anime=ObjectAnimator.ofFloat(oldValue, 0);
			anime.setDuration(150);
			anime.addUpdateListener(this);
			anime.start();
		}
		if(rect==null)oldPoint.setEmpty();
		
	}
	private void left(Rect rect)
	{
		if(floatView==null)return;
		mDirection = Direction.Left;
		if(anime!=null&&anime.isRunning())
			anime.cancel();
		anime=ObjectAnimator.ofFloat(oldValue, -rect.width() * mCellLayout.getChildWidth());
		anime.setDuration(150);
		anime.addUpdateListener(this);
		anime.start();
	}
	private void right(Rect rect)
	{
		if(floatView==null)return;
		mDirection = Direction.Right;
		if(anime!=null&&anime.isRunning())
			anime.cancel();
		anime=ObjectAnimator.ofFloat(oldValue, rect.width() * mCellLayout.getChildWidth());
		anime.setDuration(150);
		anime.addUpdateListener(this);
		anime.start();
	}
	private void top(Rect rect)
	{
		if(floatView==null)return;
		mDirection = Direction.Top;
		if(anime!=null&&anime.isRunning())
			anime.cancel();
		anime=ObjectAnimator.ofFloat(oldValue, -rect.height()* mCellLayout.getChildHeight());
		anime.setDuration(150);
		anime.addUpdateListener(this);
		anime.start();
	}
	private void bottom(Rect rect)
	{
		if(floatView==null)return;
		mDirection = Direction.Bottom;
		if(anime!=null&&anime.isRunning())
			anime.cancel();
		anime=ObjectAnimator.ofFloat(oldValue, rect.height() * mCellLayout.getChildHeight());
		anime.setDuration(150);
		anime.addUpdateListener(this);
		anime.start();
	}

	@Override
	public void onAnimationUpdate(ValueAnimator p1)
	{
		float offset=(float)p1.getAnimatedValue() - oldValue;
		oldValue = p1.getAnimatedValue();
		View[] views=floatView;
		for (View v:views)
		{
			switch (mDirection)
			{
				case Left:
				case Right:
					v.setTranslationX(v.getTranslationX() + offset);
					break;
				case Top:
				case Bottom:
					v.setTranslationY(v.getTranslationY()+offset);
					break;
			}
		}
	}
	private boolean check(int left,int top,int right,int bottom){
		for(;left<right;left++)
		for(;top<bottom;top++)
		if(mCellLayout.findViewFromPoint(left,top)!=null)
			return true;
		return false;
	}
	enum Direction
	{
		Left,Top,Right,Bottom;
	}
}
