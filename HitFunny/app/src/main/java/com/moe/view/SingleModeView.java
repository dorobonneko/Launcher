package com.moe.view;
import android.app.Fragment;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;

public class SingleModeView extends Fragment
{
	private GroupView gv;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO: Implement this method
		return new GroupView(getContext());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		//view.setBackgroundColor(0xffffffff);
		gv=(GroupView)view;
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onActivityCreated(savedInstanceState);
		gv.startWithLevel(3);
	}
	
}
