package com.moe.icelauncher.adapter;
import com.moe.icelauncher.compat.*;
import java.util.*;
import android.widget.*;
import android.view.*;
import android.database.*;
import com.moe.icelauncher.*;
import android.content.pm.*;

public class ActivityAdapter implements ListAdapter
{
	private ArrayList<DataSetObserver> mDataSet=new ArrayList<>();
	private ActivityInfo[] list;
	public ActivityAdapter(ActivityInfo[] list){
		this.list=list;
	}
	@Override
	public int getCount()
	{
		// TODO: Implement this method
		return list.length;
	}

	@Override
	public Object getItem(int p1)
	{
		// TODO: Implement this method
		return list[p1];
	}

	@Override
	public long getItemId(int p1)
	{
		// TODO: Implement this method
		return p1;
	}

	@Override
	public View getView(int p1, View p2, ViewGroup p3)
	{
		if(p2==null)
			p2=LayoutInflater.from(p3.getContext()).inflate(R.layout.activity_item,null);
		TextView title=p2.findViewById(android.R.id.title);
		TextView summary=p2.findViewById(android.R.id.summary);
		ImageView icon=p2.findViewById(android.R.id.icon);
		ActivityInfo activityInfo=list[p1];
		PackageManager pm=p3.getContext().getPackageManager();
		title.setText(activityInfo.loadLabel(pm));
		summary.setText(activityInfo.name);
		icon.setImageDrawable(activityInfo.loadUnbadgedIcon(pm));
		return p2;
	}
	
	@Override
	public boolean areAllItemsEnabled()
	{
		// TODO: Implement this method
		return true;
	}

	@Override
	public boolean isEnabled(int p1)
	{
		// TODO: Implement this method
		return true;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver p1)
	{
		mDataSet.add(p1);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver p1)
	{
		mDataSet.remove(p1);
	}

	@Override
	public boolean hasStableIds()
	{
		// TODO: Implement this method
		return false;
	}

	@Override
	public int getItemViewType(int p1)
	{
		// TODO: Implement this method
		return 0;
	}

	@Override
	public int getViewTypeCount()
	{
		// TODO: Implement this method
		return 1;
	}

	@Override
	public boolean isEmpty()
	{
		// TODO: Implement this method
		return list.length==0;
	}
}
