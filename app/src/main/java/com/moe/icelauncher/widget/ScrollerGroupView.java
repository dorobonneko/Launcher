package com.moe.icelauncher.widget;
import android.view.ViewGroup;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.moe.icelauncher.adapter.IconsAdapter;
import com.moe.icelauncher.R;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.DragEvent;

public class ScrollerGroupView extends ViewGroup
{
	private int SearchViewHeight;
	private int iconSize;
	private int background=0x5fffffff;
	private Paint paint;
	private boolean mEnabled=true;
	public ScrollerGroupView(Context context,AttributeSet attrs){
		super(context,attrs);
		iconSize=getResources().getDimensionPixelOffset(R.dimen.item_icon_size);
		paint=new Paint();
		paint.setColor(background);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int height=MeasureSpec.getSize(heightMeasureSpec);
		int width=MeasureSpec.getSize(widthMeasureSpec);
		
		if(getChildCount()>=3){
			View view0=getChildAt(0);
			view0.measure(MeasureSpec.makeMeasureSpec(iconSize,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(iconSize,MeasureSpec.EXACTLY));
			View view1=getChildAt(1);
			view1.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(SearchViewHeight,MeasureSpec.EXACTLY));
			View view2=getChildAt(2);
			view2.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height-SearchViewHeight,MeasureSpec.EXACTLY));
		}
		setMeasuredDimension(widthMeasureSpec,MeasureSpec.makeMeasureSpec(height+iconSize,MeasureSpec.EXACTLY));
	}
	public int getIconSize(){
		return iconSize;
	}
	
	@Override
	public void setAlpha(float alpha)
	{
		for(int i=1;i<getChildCount();i++)
		getChildAt(i).setAlpha(alpha);
	}
	
	
	@Override
	protected void onLayout(boolean p1, int p2, int p3, int p4, int p5)
	{if(p1){
		View view0=getChildAt(0);
		int left=(getMeasuredWidth()-view0.getMeasuredWidth())/2;
		view0.layout(left,0,left+view0.getMeasuredWidth(),view0.getMeasuredHeight());
		View view1=getChildAt(1);
		view1.layout(0,iconSize,view1.getMeasuredWidth(),iconSize+view1.getMeasuredHeight());
		View view2=getChildAt(2);
		view2.layout(0,SearchViewHeight+iconSize,view2.getMeasuredWidth(),SearchViewHeight+iconSize+view2.getMeasuredHeight());
		}else{
			int count=getChildCount();
			for(int i=0;i<count;i++){
				View child=getChildAt(i);
				child.layout(child.getLeft(),child.getTop(),child.getRight(),child.getBottom());
			}
		}
	}
public void setSearchViewHeight(int height){
	SearchViewHeight=height;
	if(isAttachedToWindow())
	invalidate();
}

@Override
protected void onFinishInflate()
{
	// TODO: Implement this method
	super.onFinishInflate();
	getChildAt(1).setPadding(0,getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android")),0,0);
}

@Override
public void setEnabled(boolean enabled)
{
	mEnabled=enabled;
}

@Override
public boolean dispatchTouchEvent(MotionEvent ev)
{
	if(!mEnabled)return false;
	return super.dispatchTouchEvent(ev);
}
/*@Override
protected void dispatchDraw(Canvas canvas)
{
	canvas.drawRect(0,iconSize,canvas.getWidth(),canvas.getHeight(),paint);
	super.dispatchDraw(canvas);
}*/

}
