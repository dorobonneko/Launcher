package com.moe.icelauncher.widget;
import android.widget.FrameLayout;
import android.util.AttributeSet;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.moe.icelauncher.R;
import com.moe.icelauncher.LauncherSettings;
import android.database.Cursor;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.DragEvent;
import android.animation.ValueAnimator;
import android.animation.ObjectAnimator;
import android.animation.Animator;
import android.widget.Scroller;
import android.view.VelocityTracker;

public class WorkSpaceLayout extends MoeViewGroup implements ContainerView.ScrollCallback
{

	
	private int workspace_padding;
	private int celllayout_shown_size,mCurrentView;
	private ViewConfiguration mViewConfiguration;
	private float oldx,oldy;
	private Scroller mScroller;
	private ValueAnimator enter,exit;
	private int width;
	private boolean editAble;
	private View renderRemoveView;
	private VelocityTracker mVelocityTracker;
	private boolean pre,next,previewTable;
	public WorkSpaceLayout(Context context,AttributeSet attrs){
		super(context,attrs);
		mScroller=new Scroller(context);
		workspace_padding=getResources().getDimensionPixelOffset(R.dimen.workspace_padding);
		celllayout_shown_size=getResources().getDimensionPixelOffset(R.dimen.celllayout_shown_size);
		mViewConfiguration=ViewConfiguration.get(context);
		addPreview();
		addNextview();
		initAnimator();
	}
	public void setPreViewTable(boolean preview_table){
		this.previewTable=preview_table;
		for(int i=0;i<getChildCount();i++)
		((CellLayout)getChildAt(i)).setPreViewTable(preview_table);
	}
	private void initAnimator()
	{
		enter=ObjectAnimator.ofFloat(1,0.9f);
		enter.addListener(stateListener);
		enter.addUpdateListener(listener);
		enter.setDuration(200);
		exit=ObjectAnimator.ofFloat(0.9f,1);
		exit.addUpdateListener(listener);
		exit.setDuration(300);
		exit.addListener(stateListener);
	}
	@Override
	public void setTranslationX(float translationX)
	{
		if(translationX<-getChildAt(getChildCount()-1).getLeft()+workspace_padding+celllayout_shown_size)
			super.setTranslationX(-getChildAt(getChildCount()-1).getLeft()+workspace_padding+celllayout_shown_size);
		else if(translationX>workspace_padding+celllayout_shown_size)
			super.setTranslationX(workspace_padding+celllayout_shown_size);
		else
			super.setTranslationX(translationX);
	}
	public void addPreview(){
		mCurrentView++;
		CellLayout cell=new CellLayout(getContext());
		cell.setPreViewTable(previewTable);
		addView(cell,0);
		setTranslationX(getTranslationX()-width);
	}
	public void addNextview(){
		CellLayout cell=new CellLayout(getContext());
		cell.setPreViewTable(previewTable);
		addView(cell,getChildCount());
	}
	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		loadWorkSpcae();
		mCurrentView=findIndexView();
		((ContainerView)getParent()).registerOnScrollCallback(this);
		
	}
	public int findIndexView(){
		for(int i=1;i<getChildCount();i++){
			View child=getChildAt(i);
			if(((CellLayout)child).isAllowBlank())
				return i;
		}
		return 0;
	}
	private void loadWorkSpcae(){
		Cursor cursor=getContext().getContentResolver().query(LauncherSettings.WorkspaceScreens.CONTENT_URI,null,null,null,LauncherSettings.WorkspaceScreens.SCREEN_RANK+" desc");
		if(cursor==null)return;
		while(cursor.moveToNext()){
			CellLayout cell=new CellLayout(getContext());
			cell.setRank(cursor.getInt(cursor.getColumnIndex(LauncherSettings.WorkspaceScreens.SCREEN_RANK)));
			cell.setPrebiew(false);
			cell.setAllowBlank(cursor.getInt(cursor.getColumnIndex(LauncherSettings.WorkspaceScreens.ALLOWBLANK))==1);
			cell.setPreViewTable(previewTable);
			addView(cell,1);
		}
		cursor.close();
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int width=MeasureSpec.getSize(widthMeasureSpec);
		int height=MeasureSpec.getSize(heightMeasureSpec);
		measureChildren(width,height);
		int measuredWidth=workspace_padding;
		for(int i=0;i<getChildCount();i++){
			View child=getChildAt(i);
			measuredWidth+=child.getMeasuredWidth()+workspace_padding;
		}
		setMeasuredDimension(MeasureSpec.makeMeasureSpec(measuredWidth,MeasureSpec.EXACTLY),heightMeasureSpec);
	}

	@Override
	protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec)
	{
		for(int i=0;i<getChildCount();i++)
		measureChild(getChildAt(i),widthMeasureSpec,heightMeasureSpec);
	}

	@Override
	protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec)
	{
		width=parentWidthMeasureSpec;
		child.measure(parentWidthMeasureSpec-celllayout_shown_size*2-workspace_padding*2,parentHeightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean p1, int p2, int p3, int p4, int p5)
	{
		int offsetX=workspace_padding;
		for(int i=0;i<getChildCount();i++){
			View child=getChildAt(i);
			child.layout(offsetX,0,offsetX+=child.getMeasuredWidth(),child.getMeasuredHeight());
			offsetX+=workspace_padding;
		}
		toggleToView(getCurrentView());
	}
	private float getTransition(float value){
		return value*value*0.5f;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(mVelocityTracker==null)
			mVelocityTracker=VelocityTracker.obtain();
			mVelocityTracker.addMovement(event);
		switch(event.getAction()){
			case event.ACTION_MOVE:
				float offset=event.getRawX()-oldx;
				float position=getTranslationX()+offset;
				float currentPosition=getViewPosition(getCurrentView());
				if(!pre&&position>currentPosition){
					setTranslationX(getTranslationX()+getTransition(1-Math.abs(currentPosition-position)/getCurrentView().getMeasuredWidth())*offset);
				}else if(!next&&position<currentPosition){
					setTranslationX(getTranslationX()+getTransition(1-Math.abs(currentPosition-position)/getCurrentView().getMeasuredWidth())*offset);
				}else{
					setTranslationX(position);
				}
				oldx=event.getRawX();
				break;
			case event.ACTION_CANCEL:
			case event.ACTION_UP:
				mVelocityTracker.computeCurrentVelocity(1000);
				if(mVelocityTracker.getXVelocity()<-3000){
					//检查是否能滚动到下一个
					CellLayout cell=(CellLayout) getChildAt(mCurrentView+1);
					if(cell!=null&&!cell.isPreview())
						toggleToView(cell);
						else
						toggleToView(getCurrentView());
				}else if(mVelocityTracker.getXVelocity()>3000){
					CellLayout cell=(CellLayout) getChildAt(mCurrentView-1);
					if(cell!=null&&!cell.isPreview())
						toggleToView(cell);
					else
						toggleToView(getCurrentView());
				}else{
					float currentOffset=getTranslationX()-getViewPosition(getCurrentView());
					float size=getCurrentView().getMeasuredWidth()/2+celllayout_shown_size+workspace_padding;
					if(Math.abs(currentOffset)<size)
					toggleToView(getCurrentView());
					else if(currentOffset>size&&pre){
						toggleToView(getChildAt(mCurrentView-1));
					}else if(currentOffset<-size&&next){
						toggleToView(getChildAt(mCurrentView+1));
					}else{
						toggleToView(getCurrentView());
					}
				}
				isInterecpt=false;
				break;
		}
		return true;
	}
	private boolean isInterecpt;
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		switch(ev.getAction()){
			case ev.ACTION_DOWN:
				oldx=ev.getRawX();
				oldy=ev.getRawY();
				break;
		}
		if(dispatchChildTouchEvent(ev))return true;
		if(!isInterecpt)
			isInterecpt=onInterceptTouchEvent(ev);
		if(isInterecpt)return onTouchEvent(ev);
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		switch(ev.getAction()){
			case ev.ACTION_DOWN:
				oldx=ev.getRawX();
				oldy=ev.getRawY();
				break;
			case ev.ACTION_MOVE:
				if(Math.abs(ev.getRawX()-oldx)>mViewConfiguration.getTouchSlop()&&Math.abs(ev.getRawY()-oldy)<mViewConfiguration.getTouchSlop()*2)
					return true;
				break;
		}
		return false;
	}
	public View getCurrentView(){
		return getChildAt(mCurrentView);
	}
	public void toggleToView(View view){
		mCurrentView=indexOfChild(view);
		CellLayout cell=(CellLayout) getChildAt(mCurrentView+1);
		next=(cell!=null&&!cell.isPreview());
		cell=(CellLayout) getChildAt(mCurrentView-1);
		pre=(cell!=null&&!cell.isPreview());
		scrollToPosition(getViewPosition(view));
		}
	public void scrollToPosition(int position){
		mScroller.startScroll((int)(getTranslationX()),0,position-(int)getTranslationX(),0,400);
		postInvalidateOnAnimation();
	}
		public int getViewPosition(View view){
			return -(view.getLeft()-workspace_padding-celllayout_shown_size);
		}
	@Override
	public void computeScroll()
	{
		if(!mScroller.isFinished()&&mScroller.computeScrollOffset()){
			setTranslationX(mScroller.getCurrX());
			postInvalidateOnAnimation();
		}else if(renderRemoveView!=null){
			mCurrentView=renderRemoveView.getTag();
			super.removeView(renderRemoveView);
			renderRemoveView=null;
		}
	}
	@Override
	protected int computeHorizontalScrollRange()
	{
		return getMeasuredWidth();
	}
	@Override
	public boolean onDragEvent(DragEvent event)
	{
		switch(event.getAction()){
			case event.ACTION_DRAG_STARTED:
				//动画
				//enter.start();
				editAble=true;
				return true;
			case event.ACTION_DRAG_ENDED:
				//exit.start();
				editAble=false;
				return true;
		}
		return false;
	}

	@Override
	public void removeView(View view)
	{
		int index=indexOfChild(view);
		if(index==mCurrentView){
			renderRemoveView=view;
			CellLayout cell=(CellLayout) getChildAt(index-1);
			if(cell!=null&&!cell.isPreview()){
				toggleToView(cell);
				renderRemoveView.setTag(index-1);
			}else{
				renderRemoveView.setTag(index);
				cell=(CellLayout) getChildAt(index+1);
				if(cell!=null&&!cell.isPreview())
					toggleToView(cell);
			}
		}else{
			super.removeView(view);
		}
	}

	ValueAnimator.AnimatorUpdateListener listener=new ValueAnimator.AnimatorUpdateListener(){

		@Override
		public void onAnimationUpdate(ValueAnimator p1)
		{
			setScaleX(p1.getAnimatedValue());
			setScaleY(p1.getAnimatedValue());
		}
	};
	Animator.AnimatorListener stateListener=new Animator.AnimatorListener(){

		@Override
		public void onAnimationStart(Animator p1)
		{
			setTag(getTranslationX());
			View view=getCurrentView();
			setPivotX(view.getLeft()+view.getMeasuredWidth()/2);
			setPivotY(getMeasuredHeight()/4f*3);
		}

		@Override
		public void onAnimationEnd(Animator p1)
		{
			if(p1==exit)
				for(int i=0;i<getChildCount();i++){
					((CellLayout)getChildAt(i)).setBackground(0);
				}
			/*if(p1==exit){
			 float x=getTranslationX();
			 float cx=-(getChildAt(mCurrentView).getLeft()-celllayout_shown_size-workspace_padding)*getScaleX();
			 setTranslationX(-(getChildAt(mCurrentView).getLeft()-celllayout_shown_size-workspace_padding)*getScaleX());
			 }*/
		}

		@Override
		public void onAnimationCancel(Animator p1)
		{
			// TODO: Implement this method
		}

		@Override
		public void onAnimationRepeat(Animator p1)
		{
			// TODO: Implement this method
		}
	};

	
	
	@Override
	public void onStartScroll(ContainerView view, float fractor)
	{
		if(fractor==0)
			setVisibility(VISIBLE);
	}

	@Override
	public void onScroll(ContainerView view, int dy, float fractor)
	{
		setAlpha(fractor);
		}

	@Override
	public void onStopScroll(ContainerView view, float fractor)
	{
		if(fractor==0)
			setVisibility(INVISIBLE);
			
	}
	
}
