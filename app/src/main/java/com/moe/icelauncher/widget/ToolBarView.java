package com.moe.icelauncher.widget;
import android.view.ViewGroup;
import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import com.moe.icelauncher.LauncherSettings;
import android.widget.ImageView;
import com.moe.icelauncher.R;
import android.view.View.OnDragListener;
import android.view.View;
import com.moe.icelauncher.model.ItemInfo;
import android.content.Intent;
import android.content.ComponentName;
import android.net.Uri;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff;
import android.animation.ValueAnimator;
import android.animation.ObjectAnimator;
import android.animation.Animator;
import com.moe.icelauncher.model.FavoriteInfo;
import com.moe.icelauncher.model.*;

public class ToolBarView extends ViewGroup implements View.OnDragListener
{
	private ImageView cancel,info,delete;
	private ValueAnimator enter,exit;
	private PorterDuffColorFilter normal,blue,red,green;
	public ToolBarView(Context context,AttributeSet attrs){
		super(context,attrs);
		cancel=new ImageView(context);
		info=new ImageView(context);
		delete=new ImageView(context);
		addView(cancel);
		addView(info);
		addView(delete);
		cancel.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		info.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		delete.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		cancel.setImageResource(R.drawable.close_circle_outline);
		info.setImageResource(R.drawable.alert_circle_outline);
		delete.setImageResource(R.drawable.delete_circle);
		cancel.setOnDragListener(this);
		info.setOnDragListener(this);
		delete.setOnDragListener(this);
		normal=new PorterDuffColorFilter(0xa0ffffff,PorterDuff.Mode.SRC_ATOP);
		info.setColorFilter(normal);
		delete.setColorFilter(normal);
		cancel.setColorFilter(normal);
		setAlpha(0);
		initAnime();
		blue=new PorterDuffColorFilter(0x9f0000ff,PorterDuff.Mode.SRC_ATOP);
		green=new PorterDuffColorFilter(0x9f00ff00,PorterDuff.Mode.SRC_ATOP);
		red=new PorterDuffColorFilter(0x9fff0000,PorterDuff.Mode.SRC_ATOP);
	}

	private void initAnime(){
		enter=ObjectAnimator.ofFloat(new float[]{0,1});
		enter.setRepeatCount(0);
		enter.setDuration(100);
		enter.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

				@Override
				public void onAnimationUpdate(ValueAnimator p1)
				{
					setAlpha(p1.getAnimatedFraction());
				}
			});
		enter.addListener(new Animator.AnimatorListener(){

				@Override
				public void onAnimationStart(Animator p1)
				{
					setAlpha(0);
				}

				@Override
				public void onAnimationEnd(Animator p1)
				{
					setAlpha(1f);
				}

				@Override
				public void onAnimationCancel(Animator p1)
				{
					setAlpha(1f);
				}

				@Override
				public void onAnimationRepeat(Animator p1)
				{
					// TODO: Implement this method
				}
			});
		exit=ObjectAnimator.ofFloat(new float[]{1,0});
		exit.setDuration(300);
		exit.setRepeatCount(0);
		exit.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

				@Override
				public void onAnimationUpdate(ValueAnimator p1)
				{
					setAlpha(1-p1.getAnimatedFraction());
				}
			});
		exit.addListener(new Animator.AnimatorListener(){

				@Override
				public void onAnimationStart(Animator p1)
				{
					setAlpha(1);
				}

				@Override
				public void onAnimationEnd(Animator p1)
				{
					setAlpha(0);
					
				}

				@Override
				public void onAnimationCancel(Animator p1)
				{
					setAlpha(0f);
					
				}

				@Override
				public void onAnimationRepeat(Animator p1)
				{
					// TODO: Implement this method
				}
			});
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int width=MeasureSpec.getSize(widthMeasureSpec);
		int size=delete.getVisibility()==VISIBLE?3:2;
		int childWidth=width/size;
		cancel.measure(MeasureSpec.makeMeasureSpec(childWidth,MeasureSpec.EXACTLY),heightMeasureSpec);
		info.measure(MeasureSpec.makeMeasureSpec(childWidth,MeasureSpec.EXACTLY),heightMeasureSpec);
		delete.measure(MeasureSpec.makeMeasureSpec(childWidth,MeasureSpec.EXACTLY),heightMeasureSpec);
		setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean p1, int p2, int p3, int p4, int p5)
	{
		cancel.layout(0,0,cancel.getMeasuredWidth(),cancel.getMeasuredHeight());
		info.layout(cancel.getMeasuredWidth(),0,cancel.getMeasuredWidth()+info.getMeasuredWidth(),info.getMeasuredHeight());
		delete.layout(cancel.getMeasuredWidth()+info.getMeasuredWidth(),0,cancel.getMeasuredWidth()+info.getMeasuredWidth()+delete.getMeasuredWidth(),delete.getMeasuredHeight());
	}

	@Override
	public boolean onDragEvent(DragEvent event)
	{
		switch(event.getAction()){
			case event.ACTION_DRAG_STARTED:
				CellLayout.Info info=(CellLayout.Info)event.getLocalState();
				if(info.info instanceof AppInfo){
					//显示3个条目
					delete.setVisibility(VISIBLE);
				}else{
					delete.setVisibility(GONE);
				}
				enter.start();
				return true;
			case event.ACTION_DRAG_ENDED:
				exit.start();
				return true;
		}
		return false;
	}

	@Override
	public boolean onDrag(View p1, DragEvent p2)
	{
		switch(p2.getAction()){
			case p2.ACTION_DRAG_ENTERED:
				IconView view=(IconView) ((CellLayout.Info)p2.getLocalState()).view;
				if(p1==cancel){
					cancel.setColorFilter(blue);
					view.setColorFilter(blue);
				}else if(p1==info){
					info.setColorFilter(green);
					view.setColorFilter(green);
				}else{
					delete.setColorFilter(red);
					view.setColorFilter(red);
					}
				break;
			
			case p2.ACTION_DROP:
				if(p1==info){
					//打开应用详情
					ItemInfo appinfo=((CellLayout.Info)p2.getLocalState()).info;
					Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
					//String pkg = "com.android.settings";
					//String cls = "com.android.settings.applications.InstalledAppDetails";
					//i.setComponent(new ComponentName(pkg, cls));
					i.setData(Uri.parse("package:" + appinfo.packageName));
					getContext().startActivity(i);
				}else if(p1==delete){
					ItemInfo appinfo=((CellLayout.Info)p2.getLocalState()).info;
					Intent i = new Intent(Intent.ACTION_DELETE,Uri.fromParts("package",appinfo.packageName,null));
					getContext().startActivity(i);
				}else if(p1==cancel){
					ItemInfo appinfo=((CellLayout.Info)p2.getLocalState()).info;
					if(appinfo instanceof FavoriteInfo){
						View infoView=((CellLayout.Info)p2.getLocalState()).view;
						if(infoView instanceof IconView){
							((IconView)infoView).remove();
							getContext().getContentResolver().delete(LauncherSettings.Favorites.CONTENT_URI,LauncherSettings.Favorites._ID+"=?",new String[]{appinfo._id+""});
						}
					}
				}
			case p2.ACTION_DRAG_EXITED:
				((ImageView)p1).setColorFilter(normal);
				((IconView) ((CellLayout.Info)p2.getLocalState()).view).setColorFilter(null);
				
				break;
		}
		return true;
	}


	
}
