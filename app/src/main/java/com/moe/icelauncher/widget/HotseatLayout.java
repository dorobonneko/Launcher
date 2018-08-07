package com.moe.icelauncher.widget;
import android.view.ViewGroup;
import android.util.AttributeSet;
import android.content.Context;
import android.view.DragEvent;
import android.graphics.Canvas;
import com.moe.icelauncher.R;
import android.view.View;

public class HotseatLayout extends CellLayout
{
	public HotseatLayout(Context context,AttributeSet attrs){
		super(context,attrs);
		setPrebiew(false);
		setAllowBlank(true);
		int padding=getResources().getDimensionPixelOffset(R.dimen.celllayout_shown_size)+getResources().getDimensionPixelOffset(R.dimen.workspace_padding);
		setPadding(padding,0,padding,0);
	}

	@Override
	public void addView(View child)
	{
		((IconView)child).setShowTitle(false);
		super.addView(child);
	}

	@Override
	public void setBorderLine(boolean border)
	{
		
	}

	@Override
	public void setBackground(int background)
	{
		
	}

	@Override
	public int getRownums()
	{
		return 1;
	}

	
	
	
	
}
