package com.moe.icelauncher.widget;
import android.view.ViewGroup;
import android.content.Context;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import com.moe.icelauncher.R;
import android.graphics.Canvas;
import android.graphics.Path;
import android.view.View;
import android.graphics.Paint;
import com.moe.icelauncher.model.IconInfo;
import com.moe.icelauncher.model.AppInfo;
import android.widget.ImageView;
import com.moe.icelauncher.util.DeviceManager;
import android.content.pm.PackageManager;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.content.ComponentName;
import android.graphics.Point;
import android.graphics.Matrix;
import android.graphics.Camera;
import com.moe.icelauncher.compat.LauncherAppsCompat;
import android.content.pm.ShortcutInfo;
import com.moe.icelauncher.shortcuts.DeepShortcutManager;
import com.moe.icelauncher.shortcuts.ShortcutInfoCompat;
import android.graphics.drawable.RippleDrawable;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import com.moe.icelauncher.adapter.IconsAdapter;
import android.animation.ValueAnimator;
import android.animation.TypeEvaluator;
import com.moe.icelauncher.model.FavoriteInfo;
import java.net.URISyntaxException;
import com.moe.icelauncher.model.ItemInfo;

public class PopupView extends ViewGroup implements OnClickListener
{
	private boolean mShown;
	private ValueAnimator enter,exit;
	private int itemHeight;
	private Paint paint;
	private ShortCutTitleView mShortCutTitleView;
	private int parentWidth,parentHeight;
	private IconInfo info;
	private Path path;
	private Point point=new Point();
	private float cursorSize;
	private Matrix pathMatrix=new Matrix();
	private int measureWidth,measureHeight;
	public PopupView(Context context){
		super(context);
		path=new Path();
		setLayerType(LAYER_TYPE_SOFTWARE,null);
		itemHeight=getResources().getDimensionPixelOffset(R.dimen.item_height);
		initAnime();
		paint=new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(0xffffffff);
		cursorSize=getResources().getDimension(R.dimen.cursor_size);
		int padding=getResources().getDimensionPixelOffset(R.dimen.item_cellspacing);
		paint.setShadowLayer(padding/2,0,0,0x50000000);
		setPadding(padding,padding,padding,padding);
		addView(mShortCutTitleView=new ShortCutTitleView(context));
		mShortCutTitleView.inflateMenu(R.menu.shortcut_menu);
		mShortCutTitleView.setOnClickListener(this);
	}
	private void initAnime(){
		enter=ObjectAnimator.ofFloat(new float[]{0,1});
		enter.setRepeatCount(0);
		enter.setDuration(200);
		enter.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

				@Override
				public void onAnimationUpdate(ValueAnimator p1)
				{
					setScaleX(p1.getAnimatedFraction());
					setScaleY(p1.getAnimatedFraction());
					setAlpha(p1.getAnimatedFraction());
				}
			});
		enter.addListener(new Animator.AnimatorListener(){

				@Override
				public void onAnimationStart(Animator p1)
				{
					setAlpha(0);
					setScaleX(0);
					setScrollY(0);
					setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationEnd(Animator p1)
				{
					setScaleX(1);
					setScaleY(1);
					setAlpha(1f);
				}

				@Override
				public void onAnimationCancel(Animator p1)
				{
					setScrollY(1);
					setScaleX(1);
					setAlpha(1f);
				}

				@Override
				public void onAnimationRepeat(Animator p1)
				{
					// TODO: Implement this method
				}
			});
		exit=ObjectAnimator.ofFloat(new float[]{1,0});
		exit.setDuration(100);
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
					setVisibility(GONE);
				}

				@Override
				public void onAnimationCancel(Animator p1)
				{
					setAlpha(0f);
					setVisibility(GONE);
				}

				@Override
				public void onAnimationRepeat(Animator p1)
				{
					// TODO: Implement this method
				}
			});
	}
	public int getLocationX()
	{
		
		return point.x;
	}

	public int getLocationY()
	{
		return point.y;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		parentWidth=MeasureSpec.getSize(widthMeasureSpec);
		parentHeight=MeasureSpec.getSize(heightMeasureSpec);
		int width=parentWidth/5*2;
		int height=getChildCount()*itemHeight;
		for(int i=0;i<getChildCount();i++){
			getChildAt(i).measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(itemHeight,MeasureSpec.EXACTLY));
		}
		measureWidth=width;
		setMeasuredDimension(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));
	}
	private void measure(){
		measureHeight=getChildCount()*itemHeight;
		setMeasuredDimension(measureWidth,measureHeight);
	}
	
	
	@Override
	protected void onLayout(boolean p1, int p2, int p3, int p4, int p5)
	{
		int offsetY=0;
		for(int i=0;i<getChildCount();i++){
			View child=getChildAt(i);
			child.layout(getPaddingLeft(),offsetY+getPaddingTop(),child.getMeasuredWidth()+getPaddingRight(),(offsetY+=child.getMeasuredHeight())+getPaddingBottom());
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		//canvas.drawRect(getPaddingLeft(),getPaddingTop(),canvas.getWidth()-getPaddingRight(),canvas.getHeight()-getPaddingBottom(),paint);
		canvas.drawPath(path,paint);
		super.dispatchDraw(canvas);
	}
	
	public void hide(){
		mShown=false;
		if(exit.isRunning())exit.cancel();
		else
			exit.start();
	}
	public boolean isShow(){
		return mShown;
	}
	public void show(IconInfo info){
		if(info.obj instanceof AppInfo){
			AppInfo appinfo=(AppInfo)info.obj;
			((ImageView)mShortCutTitleView.findMenu(R.id.hideorunhide)).setImageResource(appinfo.state==DeviceManager.PACKAGE_UNHIDE||appinfo.state==PackageManager.COMPONENT_ENABLED_STATE_ENABLED?R.drawable.eye_outline:R.drawable.eye_off_outline);
			loadShortcuts(appinfo.componentName());
		}else if(info.obj instanceof FavoriteInfo){
			FavoriteInfo appinfo=(FavoriteInfo)info.obj;
			((ImageView)mShortCutTitleView.findMenu(R.id.hideorunhide)).setImageResource(appinfo.state==DeviceManager.PACKAGE_UNHIDE||appinfo.state==PackageManager.COMPONENT_ENABLED_STATE_ENABLED?R.drawable.eye_outline:R.drawable.eye_off_outline);
			try
			{
				loadShortcuts(Intent.parseUri(appinfo.intent, 0).getComponent().flattenToString());
			}
			catch (URISyntaxException e)
			{}
		}
		this.info=info;
		measure();
		toDoXY();
		mShown=true;
		if(enter.isRunning())enter.cancel();
		enter.start();
	}
	private void loadShortcuts(String activity){
		if(getChildCount()>1)
			removeViews(1,getChildCount()-1);
		for(ShortcutInfoCompat info:DeepShortcutManager.getInstance(getContext()).queryForShortcutsContainer(ComponentName.unflattenFromString(activity),null,android.os.Process.myUserHandle())){
			ShortCutView scv=new ShortCutView(getContext());
			scv.setShortcutInfo(info);
			TypedArray ta=getContext().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackgroundBorderless});
			Drawable ripple=ta.getDrawable(0);
			scv.setForeground(ripple);
			ta.recycle();
			scv.setOnClickListener(this);
			addView(scv);
		}
	}
	private void toDoXY(){
		path.reset();
		path.moveTo(getPaddingLeft(),getPaddingTop());
		path.rLineTo(getMeasuredWidth(),0);
		path.rLineTo(0,getMeasuredHeight());
		if(info!=null){
			int x=info.viewX+info.viewWidth/4;
			if(x+getMeasuredWidth()>parentWidth){
				x=x-getMeasuredWidth()+info.iconWidth;
				//x在右边
				path.rLineTo(-info.iconWidth/2+cursorSize,0);
				setPivotX(getMeasuredWidth()-info.iconWidth/2-cursorSize);
				}else{
					setPivotX(info.iconWidth/2+cursorSize);
					path.rLineTo(-(getMeasuredWidth()-info.iconWidth/2-cursorSize),0);
				}
				point.x=x;
				path.rLineTo(-cursorSize,cursorSize);
				path.rLineTo(-cursorSize,-cursorSize);
				path.lineTo(getPaddingLeft(),measureHeight+getPaddingBottom());
				path.close();
			int y=info.viewY+info.iconY/2-measureHeight;
			if(y<0){
				y=info.viewY+info.viewHeight-info.iconY;
				//下方显示
				setPivotY(0);
				Camera camera=new Camera();
				camera.rotateX(180);
				camera.getMatrix(pathMatrix);
				pathMatrix.preTranslate(0,-measureHeight/2f-getPaddingLeft());
				pathMatrix.postTranslate(0,measureHeight/2f+getPaddingLeft());
				}else{
					setPivotY(measureHeight);
					pathMatrix.reset();
				}
				path.transform(pathMatrix);
				point.y=y;
		}
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		// TODO: Implement this method
		 super.dispatchTouchEvent(ev);
		 return true;
	}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.applicationinfo:
				if(info.obj instanceof ItemInfo){
					ItemInfo appinfo=(ItemInfo)info.obj;
					Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
					String pkg = "com.android.settings";
					String cls = "com.android.settings.applications.InstalledAppDetails";
					i.setComponent(new ComponentName(pkg, cls));
					i.setData(Uri.parse("package:" + appinfo.packageName));
					getContext().startActivity(i);
				}
				break;
			case R.id.hideorunhide:
				if(info.obj instanceof ItemInfo){
					ItemInfo appinfo=(ItemInfo) info.obj;
					switch(appinfo.state){
						case DeviceManager.PACKAGE_UNHIDE:
						case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
							//隐藏
							if(DeviceManager.getInstance(getContext()).disable(appinfo.packageName))
								appinfo.state=DeviceManager.PACKAGE_HIDE;
							break;
						case DeviceManager.PACKAGE_HIDE:
						//显示
						if(DeviceManager.getInstance(getContext()).enable(appinfo.packageName))
							appinfo.state=DeviceManager.PACKAGE_UNHIDE;
							break;
							default:
							//用root激活app
							break;
					}
					p1.invalidate();
					//((IconsAdapter)((GridView)getRootView().findViewById(R.id.gridview)).getAdapter()).notifyDataSetChanged();
				}
				break;
			default:
			ShortcutInfoCompat info=((ShortCutView)p1).getShortcutInfo();
			Intent intent=new Intent();
			intent.setComponent(info.getActivity());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			DeepShortcutManager.getInstance(getContext()).startShortcut(info.getPackage(),info.getId(),intent,null,info.getUserHandle());
			break;
		}
		hide();
	}
	
}
