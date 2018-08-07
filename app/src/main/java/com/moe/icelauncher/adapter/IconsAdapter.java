package com.moe.icelauncher.adapter;
import android.widget.BaseAdapter;
import android.view.ViewGroup;
import android.view.View;
import com.moe.icelauncher.model.AppInfo;
import java.util.List;
import com.moe.icelauncher.widget.IconView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.database.DataSetObserver;
import java.util.ArrayList;
import android.text.Editable;
import com.moe.icelauncher.util.SubThread;
import java.util.Collections;
import android.os.Handler;
import android.os.Looper;

public class IconsAdapter implements ListAdapter
{
	private List<DataSetObserver> mDataSetOnserver=new ArrayList<>();
	private List<AppInfo> list;
	private GridView.OnItemClickListener oicl;
	private boolean filter;
	private List<AppInfo> filterList=new ArrayList<>();
	private Filter runnable=new Filter();
	public IconsAdapter(List<AppInfo> list){
		this.list=list;
	}
	public void filter(Editable p1)
	{
		filter=p1.length()>0&&!p1.toString().equals("搜索应用");
		if(filter){
			runnable.setFilter(p1.toString());
			remove(runnable);
			postDelay(runnable,200);
			}else
			notifyDataSetChanged();
	}
	public void postDelay(Runnable runnable,long time){
		SubThread.getInstance().postDelayed(runnable,time);
	}
	public void remove(Runnable runnable){
		SubThread.getInstance().removeCallback(runnable);
	}
	@Override
	public boolean areAllItemsEnabled()
	{
		// TODO: Implement this method
		return true;
	}
	public void notifyDataSetChanged(){
		for(DataSetObserver changed:mDataSetOnserver)
		changed.onChanged();
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
		mDataSetOnserver.add(p1);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver p1)
	{
		mDataSetOnserver.remove(p1);
	}

	@Override
	public boolean hasStableIds()
	{
		return true;
	}

	@Override
	public int getItemViewType(int p1)
	{
		return IGNORE_ITEM_VIEW_TYPE;
	}

	@Override
	public boolean isEmpty()
	{
		// TODO: Implement this method
		return filter?filterList.isEmpty():list.isEmpty();
	}
	
	
	@Override
	public int getCount()
	{
		// TODO: Implement this method
		return filter?filterList.size():list.size();
	}

	@Override
	public Object getItem(int p1)
	{
		// TODO: Implement this method
		return filter?filterList.get(p1):list.get(p1);
	}

	@Override
	public long getItemId(int p1)
	{
		// TODO: Implement this method
		return p1;
	}

	@Override
	public View getView(final int p1, View p2, final ViewGroup p3)
	{
		if(p2==null)
			p2=new IconView(p3.getContext());
			((IconView)p2).setItemInfo(filter?filterList.get(p1):list.get(p1));
		p2.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View view)
				{
					if(oicl!=null)
						oicl.onItemClick((GridView)p3,view,p1,getItemId(p1));
				}
			});
		return p2;
	}

	@Override
	public int getViewTypeCount()
	{
		return 1;
	}
	public void setOnItemClickListener(GridView.OnItemClickListener l){
		oicl=l;
	}
	class Filter implements Runnable
	{
		String text;
		public void setFilter(String text){
			this.text=text;
		}
		@Override
		public void run()
		{
			filterList.clear();
			for(AppInfo info:list){
				if(info.title.matches(".*"+text+".*"))
					filterList.add(info);
			}
			new Handler(Looper.getMainLooper()).post(new Runnable(){

					@Override
					public void run()
					{
						notifyDataSetChanged();
					}
				});
		}

		
	}
}
