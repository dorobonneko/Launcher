package com.moe.icelauncher.widget;
import android.view.ViewGroup;
import android.util.AttributeSet;
import android.content.Context;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.database.DataSetObserver;
import android.view.View;

public class RecyclerView extends AdapterView
{
	private int gridSize;
	private Adapter adapter;
	private DataSetChanged datasetChanged;
	public RecyclerView(Context context,AttributeSet attrs){
		super(context,attrs);
	}
	@Override
	public Adapter getAdapter()
	{
		// TODO: Implement this method
		return adapter;
	}

	@Override
	public View getSelectedView()
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public void setSelection(int p1)
	{
		// TODO: Implement this method
	}
	public void setGridSize(int size){
		this.gridSize=size;
		invalidate();
	}
	public void setAdapter(Adapter adapter){
		if(this.adapter!=null&&datasetChanged!=null)
			this.adapter.unregisterDataSetObserver(datasetChanged);
		this.adapter=adapter;
		if(adapter!=null)
			adapter.registerDataSetObserver(datasetChanged=new DataSetChanged());
	}
	class DataSetChanged extends DataSetObserver{
		
	}
}
