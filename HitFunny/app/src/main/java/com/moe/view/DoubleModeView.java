package com.moe.view;
import android.app.Fragment;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.content.res.TypedArray;
import android.graphics.Color;

public class DoubleModeView extends Fragment
{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		LinearLayout ll= new LinearLayout(getContext());
		ll.setOrientation(ll.VERTICAL);
		ll.addView(new GroupView(getContext(),true),new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1));
		ll.addView(new View(getContext()),new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1));
		ll.addView(new GroupView(getContext()),new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1));
		return ll;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		/**TypedArray ta=getContext().obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
		view.setBackground(ta.getDrawable(0));
		ta.recycle();*/
		((ViewGroup)getView()).getChildAt(1).setBackgroundColor(Color.GRAY);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onActivityCreated(savedInstanceState);
		((GroupView)((ViewGroup)getView()).getChildAt(0)).startWithLevel(3);
		((GroupView)((ViewGroup)getView()).getChildAt(2)).startWithLevel(3);
	}
	
	
}
