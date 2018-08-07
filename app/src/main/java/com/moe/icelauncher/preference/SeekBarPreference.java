package com.moe.icelauncher.preference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.content.Context;
import android.view.ViewGroup;
import android.view.View;
import android.content.res.TypedArray;
import android.widget.TextView;
import com.moe.icelauncher.R;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener
{
	private TextView tip;
	private SeekBar mSeekBar;
	private int min,max,progress;
	public SeekBarPreference(Context context,AttributeSet attrs){
		super(context,attrs);
		setWidgetLayoutResource(R.layout.textview);
		TypedArray ta=context.obtainStyledAttributes(attrs,new int[]{android.R.attr.min,R.attr.max});
		min=ta.getInt(0,0);
		max=ta.getInt(1,0);
		ta.recycle();
	}

	@Override
	protected View onCreateView(ViewGroup parent)
	{
		View view=super.onCreateView(parent);
		tip=view.findViewById(R.id.tip);
		RelativeLayout group=(RelativeLayout) ((ViewGroup)view).getChildAt(1);
		RelativeLayout.LayoutParams param=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		param.addRule(RelativeLayout.BELOW,group.getChildAt(1).getId());
		param.addRule(RelativeLayout.ALIGN_BOTTOM,group.getChildAt(1).getId());
		group.addView(mSeekBar=new SeekBar(getContext()),param);
		return view;
	}

	@Override
	protected void onBindView(View view)
	{
		// TODO: Implement this method
		super.onBindView(view);
		mSeekBar.setMax(max-min);
		mSeekBar.setProgress(progress-min);
		tip.setText(progress+"");
		mSeekBar.setOnSeekBarChangeListener(this);
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return progress=a.getInt(index,0);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
	{
		if(restorePersistedValue)
		progress=getPersistedInt(progress);
		else{
			progress=(Integer)defaultValue;
			persistInt(progress);
			}
			
	}

	@Override
	public void onStartTrackingTouch(SeekBar p1)
	{
		// TODO: Implement this method
	}

	@Override
	public void onStopTrackingTouch(SeekBar p1)
	{
		progress=p1.getProgress()+min;
		tip.setText(progress+"");
		if(shouldPersist()&&callChangeListener(progress))
			persistInt(progress);
			notifyChanged();
	}

	@Override
	public void onProgressChanged(SeekBar p1, int p2, boolean p3)
	{
		tip.setText(p2+min+"");
		
	}



	
}
