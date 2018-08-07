package com.moe.icelauncher.widget;
import android.view.View;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import com.moe.icelauncher.R;
import android.content.pm.ShortcutInfo;
import android.os.PersistableBundle;
import com.moe.icelauncher.shortcuts.ShortcutInfoCompat;
import com.moe.icelauncher.shortcuts.DeepShortcutManager;
import android.view.Gravity;
import android.util.TypedValue;

public class ShortCutView extends ViewGroup
{
	private TextView mTitle;
	private ImageView mIcon;
	//private float item_title_size;
	private int item_icon_size,item_cellspacing;
	private ShortcutInfoCompat info;
	public ShortCutView(Context context){
		super(context);
		item_cellspacing=getResources().getDimensionPixelOffset(R.dimen.item_cellspacing);
		item_icon_size=getResources().getDimensionPixelOffset(R.dimen.item_icon_size);
		//item_title_size=getResources().getDimension(R.dimen.item_title_size);
		addView(mTitle=new TextView(context));
		addView(mIcon=new ImageView(context));
		mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
		mTitle.setPadding(item_cellspacing*2+item_icon_size,0,0,0);
		mTitle.setGravity(Gravity.CENTER_VERTICAL);
	}

	public void setShortcutInfo(ShortcutInfoCompat info)
	{
		this.info=info;
		setEnabled(info.isEnabled());
		setTitle(info.getShortLabel());
		setIcon(DeepShortcutManager.getInstance(getContext()).getShortcutIconDrawable(info,getResources().getDisplayMetrics().densityDpi));
	}
	public ShortcutInfoCompat getShortcutInfo(){
		return info;
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mTitle.measure(widthMeasureSpec,heightMeasureSpec);
		mIcon.measure(MeasureSpec.makeMeasureSpec(item_icon_size,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(item_icon_size,MeasureSpec.EXACTLY));
		setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean p1, int p2, int p3, int p4, int p5)
	{
		mTitle.layout(0,0,mTitle.getMeasuredWidth(),mTitle.getMeasuredHeight());
		int iconTop=(getMeasuredHeight()-mIcon.getMeasuredHeight())/2;
		mIcon.layout(item_cellspacing,iconTop,item_cellspacing+mIcon.getMeasuredWidth(),iconTop+mIcon.getMeasuredHeight());
	}

	
	public void setIcon(Bitmap bitmap){
		mIcon.setImageBitmap(bitmap);
	}
	public void setIcon(Drawable icon){
		mIcon.setImageDrawable(icon);
	}
	public void setTitle(CharSequence title){
		mTitle.setText(title);
	}
}
