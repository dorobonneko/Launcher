package com.moe.icelauncher.widget;
import android.view.ViewGroup;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.moe.icelauncher.adapter.IconsAdapter;

public class ScrollerGroupView extends ViewGroup
{
	private int SearchViewHeight;
	public ScrollerGroupView(Context context,AttributeSet attrs){
		super(context,attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// TODO: Implement this method
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if(getChildCount()==2){
			int height=MeasureSpec.getSize(heightMeasureSpec);
			int width=MeasureSpec.getSize(widthMeasureSpec);
			View view1=getChildAt(0);
			view1.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(SearchViewHeight,MeasureSpec.EXACTLY));
			View view2=getChildAt(1);
			view2.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height-SearchViewHeight,MeasureSpec.EXACTLY));
		}
		setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
	}
	
	
	@Override
	protected void onLayout(boolean p1, int p2, int p3, int p4, int p5)
	{
		View view1=getChildAt(0);
			view1.layout(0,0,p4-p2,SearchViewHeight);
			View view2=getChildAt(1);
			view2.layout(0,SearchViewHeight,view2.getMeasuredWidth(),SearchViewHeight+view2.getMeasuredHeight());
		
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
	getChildAt(0).setPadding(0,getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android")),0,0);
}
}
