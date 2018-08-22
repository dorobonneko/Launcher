package com.moe.icelauncher.widget;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.view.animation.LinearInterpolator;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.view.GestureDetector;
import android.view.ViewConfiguration;
import android.view.View;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.VelocityTracker;
import com.moe.icelauncher.compat.StatusBarCompat;
import java.util.ArrayList;
import java.util.List;
import com.moe.icelauncher.R;
import com.moe.icelauncher.model.IconInfo;
import android.graphics.Rect;
import android.view.View.OnLongClickListener;
import android.view.DragEvent;
import com.moe.icelauncher.util.*;

public class ContainerView extends ViewGroup
{
	public static final int SCROLL_STATE_EXPAND=0;
	public static final int SCROLL_STATE_COLLAPSE=1;
	public static final int SCROLL_STATE_PROGRESS=2;
	private int scrollState=SCROLL_STATE_COLLAPSE;
	private static final int STATE_PROGRESS=1;//滚动中
	private static final int STATE_IDLE=2;//松手
	//private Scroller scroller;
	private int touchSlop,velotityY,longClick;
	private int bottomOffset,toolbarHeight,statusBarHeight;
	private float oldX,oldY;
	private int offsetY,childTop,scrollHeight,state=STATE_IDLE;
	private LinearGradient shader;
	private Paint paint;
	private VelocityTracker mVelocityTracker;
	private List<ScrollCallback> mCallbacks=new ArrayList<>();
	private mScrollCallback mScrollCallback=new mScrollCallback();
	private PopupView mPopupView;
	private OnLongClickListener mOnLongClickListener;
	private LongClick mLongClick=new LongClick();
	private Fling mFling;
	public ContainerView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		//scroller=new Scroller(context,new AccelerateInterpolator(0.5f));
		mFling = new Fling();
		ViewConfiguration viewConfiguration=ViewConfiguration.get(context);
		touchSlop = viewConfiguration.getScaledPagingTouchSlop();
		velotityY = viewConfiguration.getScaledMaximumFlingVelocity();
		longClick = viewConfiguration.getLongPressTimeout();
		bottomOffset = getResources().getDimensionPixelOffset(R.dimen.bottom_height);
		toolbarHeight = getResources().getDimensionPixelOffset(R.dimen.toolbar_height);
		statusBarHeight = getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"));
		shader = new LinearGradient(0, 0, 0, toolbarHeight + statusBarHeight, new int[]{0x50000000,0x00000000}, null, LinearGradient.TileMode.CLAMP);
		paint = new Paint();
		paint.setShader(shader);
		paint.setAntiAlias(true);
		registerOnScrollCallback(mScrollCallback);
	}

	public void showPopupView(IconInfo info)
	{
		mPopupView.show(info);
	}

	@Override
	public boolean onDragEvent(DragEvent event)
	{
		switch (event.getAction())
		{
			case event.ACTION_DRAG_STARTED:
				if (mPopupView.isShow())
					mPopupView.hide();
				if (getScrollState() == SCROLL_STATE_EXPAND)
					setScrollState(SCROLL_STATE_COLLAPSE);
				break;
		}
		return super.onDragEvent(event);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int width=MeasureSpec.getSize(widthMeasureSpec);
		int height=MeasureSpec.getSize(heightMeasureSpec);
		widthMeasureSpec = getSize(width);
		View view0=getChildAt(0);
		view0.measure(widthMeasureSpec, getSize(height - bottomOffset - statusBarHeight - toolbarHeight));
		View view1=getChildAt(1);
		view1.measure(widthMeasureSpec, getSize(toolbarHeight));
		View view2=getChildAt(3);
		view2.measure(getSize(width - (view2.getPaddingLeft() + view2.getPaddingRight())), getSize(bottomOffset));
		View view3=getChildAt(2);
		view3.measure(widthMeasureSpec, getSize(height));
		scrollHeight = height - bottomOffset;
		mPopupView.measure(widthMeasureSpec, heightMeasureSpec);
		//measureChildren(widthMeasureSpec,getSize(height));
		setMeasuredDimension(widthMeasureSpec, getSize(height));
	}


	private int getSize(int size)
	{
		return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		if(changed){//重新计算
		View view0=getChildAt(0);
		view0.layout(0, statusBarHeight + toolbarHeight, view0.getMeasuredWidth(), statusBarHeight + toolbarHeight + view0.getMeasuredHeight());
		View view1=getChildAt(1);
		view1.layout(0, statusBarHeight, view1.getMeasuredWidth(), statusBarHeight + view1.getMeasuredHeight());
		int offset=scrollHeight+offsetY ;
		View view2=getChildAt(3);
		view2.layout(view2.getPaddingLeft(), offset, view2.getPaddingLeft() + view2.getMeasuredWidth(), offset + view2.getMeasuredHeight());
		ScrollerGroupView view3=(ScrollerGroupView) getChildAt(2);
		view3.layout(0, offset - view3.getIconSize(), view3.getMeasuredWidth(), offset + view3.getMeasuredHeight());
		childTop = view2.getTop();
		}else{
			int count=getChildCount()-1;
			for(int i=0;i<count;i++){
				View child=getChildAt(i);
				child.layout(child.getLeft(),child.getTop(),child.getRight(),child.getBottom());
			}
			}
		if(mPopupView.getVisibility()!=GONE)
		mPopupView.layout(mPopupView.getLocationX() - mPopupView.getPaddingLeft(), mPopupView.getLocationY() - mPopupView.getPaddingTop(), mPopupView.getLocationX() + mPopupView.getMeasuredWidth() + mPopupView.getPaddingRight(), mPopupView.getLocationY() + mPopupView.getMeasuredHeight() + mPopupView.getPaddingBottom());
		}
	public void setBottomOffset(int offset)
	{
		bottomOffset = offset;
		invalidate();
	}
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (!isIntercept)return false;
		if (mVelocityTracker == null)
			mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(event);
		switch (event.getAction())
		{
			case event.ACTION_DOWN:

				break;
			case event.ACTION_MOVE:
				removeCallbacks(mLongClick);
				if (state == STATE_IDLE)
				{
					startScroll();
				}
				state = STATE_PROGRESS;
				scrollChildBy((int)(event.getRawY() - oldY));
				oldY = event.getRawY();
				mVelocityTracker.computeCurrentVelocity(1000);
				if (childTop == scrollHeight && mVelocityTracker.getYVelocity() > 3000)
					StatusBarCompat.expand(getContext());
				break;
			case event.ACTION_CANCEL:
			case event.ACTION_UP:
				removeCallbacks(mLongClick);
				state = STATE_IDLE;
				mVelocityTracker.computeCurrentVelocity(1000);
				float velotity=mVelocityTracker.getYVelocity(event.getPointerId(0));
				if (velotity > 3000)
				{
					smoothScroll(scrollHeight, 5000);
					//scroller.setFinalY(0);
					//scroller.fling(0,childTop,0,(int)mVelocityTracker.getYVelocity(event.getPointerId(0)),0,0,0,-childTop);

				}
				else if (velotity < -3000)
				{
					smoothScroll(0, 5000);
					//scroller.setFinalY(scrollHeight);
					//scroller.fling(0,childTop,0,-(int)mVelocityTracker.getYVelocity(event.getPointerId(0)),0,0,scrollHeight,scrollHeight);

				}
				else if (childTop < getHeight() / 2)
				{
					smoothScroll(0, 3000);//scrollChildBy(-childTop);
					//scroller.setFinalY(0);
					//scroller.fling(0,childTop,0,1,0,0,0,-childTop);

				}
				else
				{
					//scrollChildBy(getHeight()-bottomOffset-childTop);
					smoothScroll(scrollHeight, 3000);
					//scroller.setFinalY(scrollHeight);
					//scroller.fling(0,childTop,0,-1,0,0,scrollHeight,scrollHeight);

				}
				mVelocityTracker.clear();
				//invalidate();
				break;
		}
		return true;
	}
	boolean isIntercept,drop;
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		switch (ev.getAction())
		{
			case ev.ACTION_DOWN:
				if (ev.getAction() == ev.ACTION_DOWN && mPopupView.isShow())
				{
					if (dispatchChild(ev) != mPopupView)
					{
						mPopupView.hide();
						drop = true;
						return true;
					}
				}
				break;
			case ev.ACTION_MOVE:
				removeCallbacks(mLongClick);
				if (drop)return true;
				break;
			case ev.ACTION_CANCEL:
			case ev.ACTION_UP:
				removeCallbacks(mLongClick);
				if (drop)
				{
					drop = false;
					return true;
				}
				break;
		}

		if (isIntercept)
		{
			onTouchEvent(ev);
			switch (ev.getAction())
			{
				case ev.ACTION_CANCEL:
				case ev.ACTION_UP:
					state = STATE_IDLE;
					isIntercept = false;
					break;
			}
			return true;}
		boolean flag=dispatchChildTouchEvent(ev);
		switch (ev.getAction())
		{
			case ev.ACTION_CANCEL:
			case ev.ACTION_UP:
				state = STATE_IDLE;
				isIntercept = false;
				break;
		}
		if (!flag)
		{
			switch (ev.getAction())
			{
				case ev.ACTION_DOWN:
					removeCallbacks(mLongClick);
					postDelayed(mLongClick, longClick);
					oldX = ev.getRawX();
					oldY = ev.getRawY();
					if (!mFling.isFinished())
					{
						mFling.abortAnimation();
						return true;
					}
					//isIntercept=true;
					return true;
					//break;
				case ev.ACTION_MOVE:
					//if(Math.abs(ev.getRawY()-oldY)>touchSlop){
					isIntercept = true;
					//mViewDragHelper.shouldInterceptTouchEvent(ev);
					//}
					oldY = ev.getRawY();
					break;
				case ev.ACTION_CANCEL:
				case ev.ACTION_UP:
					if (childTop < getHeight() / 2)
					{
						smoothScroll(0);
						//scrollChildBy(-childTop);
					}
					else
					{
						//scrollChildBy(getHeight()-bottomOffset-childTop);
						smoothScroll(scrollHeight);
					}
					break;
			}
			return true;
			//return onInterceptTouchEvent(ev);
		}
		else
			return true;
	}
	private View mMotionTarget;
	public boolean dispatchChildTouchEvent(MotionEvent event)
	{
		float ex=event.getX(),ey=event.getY();
		float scrolledXFloat=ex + getScrollX() - getTranslationX();
		float scrolledYFloat=ey + getScrollY() - getTranslationY();
		if (event.getAction() == event.ACTION_DOWN)
			mMotionTarget = null;
		if (mMotionTarget == null)
		{
			Rect frame=new Rect();
			for (int i = getChildCount() - 1; i >= 0; i--)
			{
				final View child = getChildAt(i);
				if (child.getVisibility() == VISIBLE
					|| child.getAnimation() != null)
				{
					child.getHitRect(frame);
					if (frame.contains((int)scrolledXFloat, (int)scrolledYFloat))
					{
						final float xc = scrolledXFloat - child.getLeft();
						final float yc = scrolledYFloat - child.getTop();
						MotionEvent me=MotionEvent.obtain(event);
						me.setLocation(xc, yc);
						if (child.dispatchTouchEvent(me))
						{
							mMotionTarget = child;
							return true;
						}

					}
				}
			}
		}
		else
		{
			final float xc = scrolledXFloat - mMotionTarget.getLeft();
			final float yc = scrolledYFloat - mMotionTarget.getTop();
			MotionEvent me=MotionEvent.obtain(event);
			me.setLocation(xc, yc);
			return mMotionTarget.dispatchTouchEvent(me);

		}
		//event.setLocation(ex,ey);
		return false;
	}


	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{

		return false;
		//return super.onInterceptTouchEvent(ev);
	}


	@Override
	protected void onFinishInflate()
	{
		// TODO: Implement this method
		super.onFinishInflate();
		ScrollerGroupView scroll=(ScrollerGroupView)getChildAt(2);
		scroll.setSearchViewHeight(bottomOffset);
		scroll.setAlpha(0);
		scroll.setEnabled(false);
		//scroll.setVisibility(View.GONE);
		addView(mPopupView = new PopupView(getContext()));
		mPopupView.setVisibility(View.GONE);
	}

	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
		super.dispatchDraw(canvas);
	}

	public void childOffsetTopAndBottom(int offset)
	{
		getChildAt(2).offsetTopAndBottom(offset);
		getChildAt(3).offsetTopAndBottom(offset);
		//getChildAt(2).setTranslationY(getChildAt(2).getTranslationY()+offset);
		//getChildAt(3).setTranslationY(getChildAt(3).getTranslationY()+offset);
		offsetY += offset;
		childTop += offset;

	}
	private void scrollChildBy(int offset)
	{
		scrollState = SCROLL_STATE_PROGRESS;
		int coffset=0;
		if (childTop + offset <= 0)
		{
			childOffsetTopAndBottom(coffset = -childTop);
		}
		else if (childTop + offset >= scrollHeight)
		{
			childOffsetTopAndBottom(coffset = scrollHeight - childTop);
		}
		else
		{
			childOffsetTopAndBottom(coffset = offset);
		}
		for (ScrollCallback scroll:mCallbacks)
			scroll.onScroll(this, coffset, (float)childTop / scrollHeight);

	}
	private void scrollChildTo(int offset)
	{
		scrollState = SCROLL_STATE_PROGRESS;
		childOffsetTopAndBottom(offset - childTop);
		for (ScrollCallback scroll:mCallbacks)
			scroll.onScroll(this, offset - childTop, (float)childTop / scrollHeight);

	}
	/*private void smoothScroll(int positionY){
	 if(positionY==childTop)return;
	 scroller.startScroll(0,childTop,0,positionY-childTop,250);
	 invalidate();
	 }*/
	private void smoothScroll(int positionY)
	{
		mFling.fling(0, childTop, 0, positionY, 0, 5000);
		postInvalidateOnAnimation();
	}
	private void smoothScroll(int positionY, float velocity)
	{
		mFling.fling(0, childTop, 0, positionY, 0, velocity);
		postInvalidateOnAnimation();
	}
	@Override
	public void computeScroll()
	{
		if (mFling.computeScrollOffset())
		{
			scrollChildTo(mFling.getCurrY());
			postInvalidateOnAnimation();
		}
		else
		{
			scrollState = childTop == scrollHeight ?SCROLL_STATE_COLLAPSE: SCROLL_STATE_EXPAND;
			for (ScrollCallback scroll:mCallbacks)
				scroll.onStopScroll(this, (float)childTop / scrollHeight);
		}
	}
	public void registerOnScrollCallback(ScrollCallback call)
	{
		if (!mCallbacks.contains(call))
			mCallbacks.add(call);
	}
	public void unregistetOnScrollCallback(ScrollCallback call)
	{
		mCallbacks.remove(call);
	}
	public interface ScrollCallback
	{
		void onStartScroll(ContainerView view, float fractor);
		void onScroll(ContainerView view, int dy, float fractor);
		void onStopScroll(ContainerView view, float fractor);
	}
	class mScrollCallback implements ScrollCallback
	{

		@Override
		public void onStartScroll(ContainerView view, float fractor)
		{
			//getChildAt(3).setVisibility(View.VISIBLE);
			//getChildAt(3).setVisibility(View.VISIBLE);
		}

		@Override
		public void onScroll(ContainerView view, int dy, float fractor)
		{
			view.getChildAt(3).setAlpha(fractor);
			view.getChildAt(2).setAlpha((1 - fractor));
		}

		@Override
		public void onStopScroll(ContainerView view, float fractor)
		{
			if (fractor == 1)
			{
				getChildAt(2).setEnabled(false);
				getChildAt(3).setEnabled(true);
			}
			else if (fractor == 0)
			{
				getChildAt(3).setEnabled(false);
				getChildAt(2).setEnabled(true);
			}
		}


	}
	public int getScrollState()
	{
		return scrollState;
	}
	public void setScrollState(int state)
	{
		if (state == SCROLL_STATE_COLLAPSE)
		{
			startScroll();
			smoothScroll(scrollHeight);
		}
		else if (state == SCROLL_STATE_EXPAND)
		{
			startScroll();
			smoothScroll(0);
		}
	}
	private void startScroll()
	{
		for (ScrollCallback call:mCallbacks)
			call.onStartScroll(this, (float)childTop / scrollHeight);
	}
	public boolean onBack()
	{
		if (mPopupView.isShow())
		{
			mPopupView.hide();
			return true;
		}
		if (getScrollState() == SCROLL_STATE_EXPAND)
		{
			setScrollState(SCROLL_STATE_COLLAPSE);
			return true;
		}
		return false;
	}
	private View dispatchChild(MotionEvent event)
	{
		float ex=event.getX(),ey=event.getY();
		float scrolledXFloat=ex + getScrollX();
		float scrolledYFloat=ey + getScrollY();

		if (event.getAction() == event.ACTION_DOWN)
		{
			Rect frame=new Rect();
			for (int i = getChildCount() - 1; i >= 0; i--)
			{
				final View child = getChildAt(i);
				if (child.getVisibility() == VISIBLE
					|| child.getAnimation() != null)
				{

					child.getHitRect(frame);
					/**
					 * 如果子View包含当前触摸的点
					 */
					if (frame.contains((int)scrolledXFloat, (int)scrolledYFloat))
					{
						return child;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setOnLongClickListener(View.OnLongClickListener l)
	{
		mOnLongClickListener = l;
		setLongClickable(l != null);
	}
	class LongClick implements Runnable
	{

		@Override
		public void run()
		{
			if (mOnLongClickListener != null)mOnLongClickListener.onLongClick(ContainerView.this);
		}


	}
	class DragListener implements View.OnDragListener
	{

		@Override
		public boolean onDrag(View p1, DragEvent p2)
		{
			switch (p2.getAction())
			{
				case p2.ACTION_DRAG_ENTERED:
					break;
				case p2.ACTION_DRAG_ENDED:
					p1.setOnDragListener(null);
					break;
			}
			return false;
		}
	}

}
