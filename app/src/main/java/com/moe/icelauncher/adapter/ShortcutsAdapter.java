package com.moe.icelauncher.adapter;
import android.widget.ListAdapter;
import android.view.ViewGroup;
import android.view.View;
import android.database.DataSetObserver;
import com.moe.icelauncher.shortcuts.ShortcutInfoCompat;
import java.util.List;
import java.util.ArrayList;
import com.moe.icelauncher.widget.ShortCutView;

public class ShortcutsAdapter implements ListAdapter
{
	private List<ShortcutInfoCompat> list;
	private List<DataSetObserver> mDataSet=new ArrayList<>();
	public ShortcutsAdapter(List<ShortcutInfoCompat> list){
		this.list=list;
	}

	public void notifyDataSetChanged()
	{
		for(DataSetObserver set:mDataSet)
		set.onChanged();
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
		return list.get(p1).isEnabled();
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
	public int getCount()
	{
		// TODO: Implement this method
		return list.size();
	}

	@Override
	public Object getItem(int p1)
	{
		// TODO: Implement this method
		return list.get(p1);
	}

	@Override
	public long getItemId(int p1)
	{
		// TODO: Implement this method
		return p1;
	}

	@Override
	public boolean hasStableIds()
	{
		// TODO: Implement this method
		return false;
	}

	@Override
	public View getView(int p1, View p2, ViewGroup p3)
	{
		if(p2==null)
			p2=new ShortCutView(p3.getContext());
		((ShortCutView)p2).setShortcutInfo(list.get(p1));
		return p2;
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
		return list.isEmpty();
	}
	
}
