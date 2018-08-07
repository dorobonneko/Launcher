package com.moe.icelauncher.widget;
import android.view.ViewGroup;
import android.content.Context;
import com.moe.icelauncher.R;
import android.graphics.Canvas;
import android.content.res.XmlResourceParser;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;
import android.widget.ImageView;
import android.content.res.TypedArray;
import android.view.View;
import android.util.TypedValue;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class ShortCutTitleView extends ViewGroup implements View.OnClickListener,View.OnLongClickListener
{
	private int background;
	private int cell_spacing,icon_size;
	private OnClickListener ocl;
	public ShortCutTitleView(Context context){
		super(context);
		cell_spacing=getResources().getDimensionPixelOffset(R.dimen.item_cellspacing);
		icon_size=getResources().getDimensionPixelOffset(R.dimen.item_icon_size);
		background=getResources().getColor(R.color.launcher_accent_color);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// TODO: Implement this method
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size=MeasureSpec.makeMeasureSpec(icon_size,MeasureSpec.EXACTLY);
		for(int i=0;i<getChildCount();i++){
			View child=getChildAt(i);
			child.measure(size,size);
		}
		setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean p1, int p2, int p3, int p4, int p5)
	{
		int x=getMeasuredWidth()-getChildCount()*(icon_size+cell_spacing);
		int y=(getMeasuredHeight()-icon_size)/2;
		for(int i=0;i<getChildCount();i++){
			View child=getChildAt(i);
			child.layout(x,y,x+=icon_size,y+icon_size);
			x+=cell_spacing;
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		canvas.drawColor(background);
		super.dispatchDraw(canvas);
	}

	@Override
	public void onClick(View p1)
	{
		if(ocl!=null)
			ocl.onClick(p1);
	}
	public View findMenu(int id){
		return findViewById(id);
	}
	public void inflateMenu(int res){
		inflateMenu(getResources().getXml(res));
	}
	private void inflateMenu(XmlResourceParser xml){
		try
		{
			int event;
			while((event=xml.nextToken())!=xml.END_DOCUMENT){
				switch(event){
				case xml.START_TAG:
					if(!xml.getName().equals("item"))break;
					ImageView view=new ImageView(getContext());
					TypedArray ta=getContext().obtainStyledAttributes(xml,new int[]{android.R.attr.id,android.R.attr.src,android.R.attr.title,android.R.attr.selectableItemBackgroundBorderless});
					view.setId(ta.getResourceId(0,-1));
					view.setImageResource(ta.getResourceId(1,-1));
					view.setTag(ta.getString(2));
					view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					view.setBackgroundResource(ta.getResourceId(3,-1));
					ta.recycle();
					addView(view);
					view.setOnClickListener(this);
					view.setOnLongClickListener(this);
					break;
					}
			}
		}
		catch (IOException e)
		{}
		catch (XmlPullParserException e)
		{}
	}

	@Override
	public void setOnClickListener(View.OnClickListener l)
	{
		ocl=l;
	}

	@Override
	public boolean onLongClick(View p1)
	{
		if(p1.getTag()!=null)
		Toast.makeText(p1.getContext(),p1.getTag().toString(),Toast.LENGTH_SHORT).show();
		return true;
	}

	
}
