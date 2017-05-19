package com.moe.view;
import android.app.Fragment;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.view.Gravity;
import com.moe.HitFunny.R;
import android.widget.FrameLayout;
import android.view.View.OnClickListener;
import android.app.AlertDialog;

public class MainView extends Fragment implements View.OnClickListener
{

	private Button singleMode,doubleMode,about;
	private Fragment singleModeView,doubleModeView;
	private AlertDialog ad;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.main_view,container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		ViewGroup vg=(ViewGroup)((ViewGroup)view).getChildAt(0);
		singleMode=(Button)vg.getChildAt(0);
		doubleMode=(Button)vg.getChildAt(1);
		about=(Button)vg.getChildAt(2);
		singleMode.setId(0);
		doubleMode.setId(1);
		about.setId(2);
		singleMode.setOnClickListener(this);
		doubleMode.setOnClickListener(this);
		about.setOnClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onActivityCreated(savedInstanceState);
		ad=new AlertDialog.Builder(getActivity()).setTitle("关于").setMessage("").create();
	}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case 0:
				if(singleModeView==null)
					singleModeView=new SingleModeView();
					if(singleModeView.isAdded())
						getFragmentManager().beginTransaction().show(singleModeView).addToBackStack("single").commit();
						else
						getFragmentManager().beginTransaction().add(R.id.main_viewLinearLayout,singleModeView).addToBackStack("single").commit();
				break;
			case 1:
				if(doubleModeView==null)
					doubleModeView=new DoubleModeView();
				if(doubleModeView.isAdded())
					getFragmentManager().beginTransaction().show(doubleModeView).addToBackStack("double").commit();
				else
					getFragmentManager().beginTransaction().add(R.id.main_viewLinearLayout,doubleModeView).addToBackStack("double").commit();
				break;
			case 2:
				ad.show();
				break;
		}
	}

	
}
