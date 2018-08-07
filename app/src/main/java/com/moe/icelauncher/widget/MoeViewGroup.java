package com.moe.icelauncher.widget;
import android.view.ViewGroup;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.MotionEvent;
import android.graphics.Rect;

public abstract class MoeViewGroup extends ViewGroup
{
	private View mMotionTarget;
	public MoeViewGroup(Context context){
		super(context);
	}
	public MoeViewGroup(Context context,AttributeSet attrs){
		super(context,attrs);
	}
	public boolean dispatchChildTouchEvent(MotionEvent event){
		float ex=event.getX(),ey=event.getY();
		float scrolledXFloat=ex+getScrollX()-getTranslationX();
		float scrolledYFloat=ey+getScrollY()-getTranslationY();

		if(event.getAction()==event.ACTION_DOWN){
			mMotionTarget=findViewInPoint(scrolledXFloat,scrolledYFloat);
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
	public View findViewInPoint(float x,float y){
		Rect frame=new Rect();
		for (int i = getChildCount() - 1; i >= 0; i--) {
			final View child = getChildAt(i);
			if (child.getVisibility() == VISIBLE
				|| child.getAnimation() != null) {

				child.getHitRect(frame);
				if (frame.contains((int)x, (int)y)) {
					return child;

				}
			}
		}
		return null;
	}
}
