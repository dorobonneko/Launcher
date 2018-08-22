package com.moe.icelauncher.widget;
import android.util.AttributeSet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import com.moe.icelauncher.R;
import com.moe.icelauncher.adapter.IconsAdapter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Rect;

public class GridView extends android.widget.GridView
{
	private LinearGradient shader;
	private Paint paint;
	private float oldY;
	private boolean intercept;
	private View mMotionTarget;
	public GridView(Context context,AttributeSet attrs){
		super(context,attrs);
		setSelector(new BitmapDrawable());
		setSmoothScrollbarEnabled(true);
		//setFastScrollEnabled(true);
		//setFastScrollAlwaysVisible(true);
		//setFastScrollStyle(android.R.style.Widget_Material_FastScroll);
		setOverScrollMode(OVER_SCROLL_ALWAYS);
		setNumColumns(4);
		paint=new Paint(Paint.ANTI_ALIAS_FLAG);
		shader=new LinearGradient(0,0,0,10,0x30000000,0x00000000,LinearGradient.TileMode.CLAMP);
		paint.setShader(shader);
		paint.setColor(getResources().getColor(R.color.launcher_accent_color));
	}
	@Override
	public void draw(Canvas canvas)
	{
		super.draw(canvas);
		//绘制阴影
		if(getChildScrollY()==0){
			paint.setShader(null);
		canvas.drawRect(35,0,canvas.getWidth()-35,2,paint);
		}else{
			paint.setShader(shader);
		canvas.drawRect(0,0,canvas.getWidth(),10,paint);
	
		}}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		if(!isEnabled())return false;
		switch(ev.getAction()){
			case ev.ACTION_DOWN:
				super.dispatchTouchEvent(ev);
				oldY=ev.getRawY();
				break;
			case ev.ACTION_CANCEL:
			case ev.ACTION_UP:
				//onTouchEvent(ev);
				//if(mMotionTarget!=null)
				//	mMotionTarget.onTouchEvent(ev);
				//mMotionTarget=null;
				break;
		}
		if(intercept){
		switch(ev.getAction()){
			case ev.ACTION_MOVE:
				onTouchEvent(ev);
				return true;
			case ev.ACTION_CANCEL:
				//ev.setAction(ev.ACTION_UP);
			case ev.ACTION_UP:
				onTouchEvent(ev);
				intercept=false;
				break;
		}
		return true;
		}
		boolean flag=dispatchChild(ev);
		if(!flag)
		switch(ev.getAction()){
			case ev.ACTION_DOWN:
				//onTouchEvent(ev);
				//oldY=ev.getRawY();
				intercept=true;
				//return true;
				break;
			case ev.ACTION_MOVE:
				if(getChildScrollY()==0&&ev.getRawY()>oldY)
					intercept=false;
				else{
					intercept=true;
					onTouchEvent(ev);
				//scrollListBy((int)(-ev.getRawY()+oldY));
				}
				//oldY=ev.getRawY();
				break;
				case ev.ACTION_CANCEL:
				case ev.ACTION_UP:
					//onTouchEvent(ev);
					//return true;
		}
		return flag||intercept;
	}
	
	
private boolean dispatchChild(MotionEvent event){
	float ex=event.getX(),ey=event.getY();
	float scrolledXFloat=ex+getScrollX();
	float scrolledYFloat=ey+getScrollY();
	
	if(event.getAction()==event.ACTION_DOWN){
		mMotionTarget=null;
		Rect frame=new Rect();
		for (int i = getChildCount() - 1; i >= 0; i--) {
			final View child = getChildAt(i);
			if (child.getVisibility() == VISIBLE
				|| child.getAnimation() != null) {

				child.getHitRect(frame);
				/**
				 * 如果子View包含当前触摸的点
				 */
				if (frame.contains((int)scrolledXFloat, (int)scrolledYFloat)) {
					// offset the event to the view's coordinate system
					final float xc = scrolledXFloat - child.getLeft();
					final float yc = scrolledYFloat - child.getTop();
					event.setLocation(xc, yc);
					if (child.dispatchTouchEvent(event))  {
						// Event handled, we have a target now.
						/**
						 * 如果子View的dispatchTouchEvent方法的返回值为true,则表示子View已经消费了事件
						 * 此时将子View赋值给mMotionTarget
						 */
						mMotionTarget = child;
						/**
						 * 直接返回true,表示down事件被消费掉了
						 */
						return true;
					}
				}
			}
		}
		event.setLocation(event.getX(),event.getY());
	}
	if(mMotionTarget!=null){
		final float xc = scrolledXFloat - mMotionTarget.getLeft();
		final float yc = scrolledYFloat - mMotionTarget.getTop();
		MotionEvent me=MotionEvent.obtain(event);
		me.setLocation(xc,yc);
		return mMotionTarget.dispatchTouchEvent(me);
		
	}
	event.setLocation(ex,ey);
		return false;
}
	public int getChildScrollY()
	{
		if(getChildCount()>0){
			View view=getChildAt(0);
			return view.getTop();
			//return -view.getTop()+getFirstVisiblePosition()*view.getHeight();
		}
		return 0;
	}
}
