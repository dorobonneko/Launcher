package com.moe.HitFunny;

import android.app.*;
import android.os.*;
import android.view.WindowManager;
import com.moe.view.MainView;
import android.widget.FrameLayout;
import android.view.ViewGroup;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
		FrameLayout fl=new FrameLayout(this);
		fl.setId(233);
		setContentView(fl,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
		getFragmentManager().beginTransaction().add(233,new MainView(),"main").commit();
    }

	@Override
	public void onBackPressed()
	{
		if(!getFragmentManager().popBackStackImmediate())
		super.onBackPressed();
	}
	
}
