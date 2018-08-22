package com.moe.icelauncher.widget;

import android.animation.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.moe.icelauncher.*;
import com.moe.icelauncher.model.*;
import com.moe.icelauncher.shortcuts.*;
import java.util.*;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.net.Uri;
import com.moe.icelauncher.adapter.ShortcutsAdapter;
import com.moe.icelauncher.util.DeviceManager;
import java.net.URISyntaxException;

public class PopupView extends ViewGroup implements OnClickListener,ListView.OnItemClickListener,ListView.OnItemLongClickListener
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
	private ListView listView;
	private ShortcutsAdapter mShortcutsAdapter;
	private List<ShortcutInfoCompat> list;
	public PopupView(Context context)
	{
		super(context);
		path = new Path();
		setLayerType(LAYER_TYPE_SOFTWARE, null);
		itemHeight = getResources().getDimensionPixelOffset(R.dimen.item_height);
		initAnime();
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(0xffffffff);
		cursorSize = getResources().getDimension(R.dimen.cursor_size);
		int padding=getResources().getDimensionPixelOffset(R.dimen.item_cellspacing);
		paint.setShadowLayer(padding / 2, 0, 0, 0x50000000);
		setPadding(padding, padding, padding, padding);
		addView(mShortCutTitleView = new ShortCutTitleView(context));
		mShortCutTitleView.inflateMenu(R.menu.shortcut_menu);
		mShortCutTitleView.setOnClickListener(this);
		listView=new ListView(context);
		listView.setAdapter(mShortcutsAdapter=new ShortcutsAdapter(list=new ArrayList<>()));
		addView(listView);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
	}
	private void initAnime()
	{
		enter = ObjectAnimator.ofFloat(new float[]{0,1});
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
		exit = ObjectAnimator.ofFloat(new float[]{1,0});
		exit.setDuration(100);
		exit.setRepeatCount(0);
		exit.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

				@Override
				public void onAnimationUpdate(ValueAnimator p1)
				{
					setAlpha(1 - p1.getAnimatedFraction());
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
		parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		int width=parentWidth / 5 * 2;
		//int height=getChildCount() * itemHeight;
		mShortCutTitleView.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(itemHeight,MeasureSpec.EXACTLY));
		getChildAt(1).measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(itemHeight*6,MeasureSpec.AT_MOST));
		measureWidth = width;
		measureHeight = getChildAt(1).getMeasuredHeight()+itemHeight;
		setMeasuredDimension(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(itemHeight+getChildAt(1).getMeasuredHeight(), MeasureSpec.EXACTLY));
		toDoXY();
	}
	@Override
	protected void onLayout(boolean p1, int p2, int p3, int p4, int p5)
	{
		int offsetY=0;
		for (int i=0;i < getChildCount();i++)
		{
			View child=getChildAt(i);
			child.layout(getPaddingLeft(), offsetY + getPaddingTop(), child.getMeasuredWidth() + getPaddingRight(), (offsetY += child.getMeasuredHeight()) + getPaddingBottom());
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		//canvas.drawRect(getPaddingLeft(),getPaddingTop(),canvas.getWidth()-getPaddingRight(),canvas.getHeight()-getPaddingBottom(),paint);
		canvas.drawPath(path, paint);
		super.dispatchDraw(canvas);
	}

	public void hide()
	{
		mShown = false;
		if (exit.isRunning())exit.cancel();
		else
			exit.start();
	}
	public boolean isShow()
	{
		return mShown;
	}
	public void show(IconInfo info)
	{
		if (info.obj instanceof AppInfo)
		{
			AppInfo appinfo=(AppInfo)info.obj;
			((ImageView)mShortCutTitleView.findMenu(R.id.hideorunhide)).setImageResource(appinfo.state == DeviceManager.PACKAGE_UNHIDE || appinfo.state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ?R.drawable.eye_outline: R.drawable.eye_off_outline);
			loadShortcuts(appinfo.componentName());
		}
		else if (info.obj instanceof FavoriteInfo)
		{
			FavoriteInfo appinfo=(FavoriteInfo)info.obj;
			((ImageView)mShortCutTitleView.findMenu(R.id.hideorunhide)).setImageResource(appinfo.state == DeviceManager.PACKAGE_UNHIDE || appinfo.state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ?R.drawable.eye_outline: R.drawable.eye_off_outline);
			try
			{
				loadShortcuts(Intent.parseUri(appinfo.intent, 0).getComponent().flattenToString());
			}
			catch (URISyntaxException e)
			{}
		}
		this.info = info;
		//toDoXY();
		mShown = true;
		if (enter.isRunning())enter.cancel();
		enter.start();
	}
	private void loadShortcuts(String activity)
	{
		ComponentName mComponentName=ComponentName.unflattenFromString(activity);
		List<ShortcutInfoCompat> list=DeepShortcutManager.getInstance(getContext()).queryForShortcutsContainer(mComponentName, null, android.os.Process.myUserHandle());
		if(list==null)list=Collections.emptyList();
		//parseList(list,false);
		//list.clear();
		Cursor cursor=getContext().getContentResolver().query(LauncherSettings.Shortcuts.CONTENT_URI,null,LauncherSettings.Shortcuts.PACKAGENAME+"=?",new String[]{mComponentName.getPackageName()},null);
		while(cursor!=null&&cursor.moveToNext()){
			ShortcutInfo.Builder build=new ShortcutInfo.Builder(getContext(),cursor.getInt(cursor.getColumnIndex(LauncherSettings.Shortcuts._ID))+"");
			Intent intent=null;
			try
			{
				build.setIntent(intent=Intent.parseUri(cursor.getString(cursor.getColumnIndex(LauncherSettings.Shortcuts.INTENT)), 0));
			}
			catch (URISyntaxException e)
			{}
			if(intent!=null)
			build.setIcon(Icon.createWithBitmap(Utilities.drawable2Bitmap(IconCache.getInstance(getContext()).getInbadedIcon(intent.getComponent()).icon)));
			build.setShortLabel(cursor.getString(cursor.getColumnIndex(LauncherSettings.Shortcuts.TITLE)));
			list.add(new ShortcutInfoCompat(build.build(),true));
		}
		if(cursor!=null)cursor.close();
		this.list.clear();
		this.list.addAll(list);
		mShortcutsAdapter.notifyDataSetChanged();
		//parseList(list,true);
	}
	/*private void parseList(List<ShortcutInfoCompat> list,boolean longClick){
		for (ShortcutInfoCompat info:list)
		{
			ShortCutView scv=new ShortCutView(getContext());
			scv.setShortcutInfo(info);
			TypedArray ta=getContext().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackgroundBorderless});
			Drawable ripple=ta.getDrawable(0);
			scv.setForeground(ripple);
			ta.recycle();
			scv.setOnClickListener(this);
			if(longClick)
			scv.setOnLongClickListener(this);
			listView.addView(scv);
		}
	}*/
	private void toDoXY()
	{
		path.reset();
		path.moveTo(getPaddingLeft(), getPaddingTop());
		path.rLineTo(getMeasuredWidth(), 0);
		path.rLineTo(0, getMeasuredHeight());
		if (info != null)
		{
			int x=info.viewX + info.viewWidth / 4;
			if (x + getMeasuredWidth() > parentWidth)
			{
				x = x - getMeasuredWidth() + info.iconWidth;
				//x在右边
				path.rLineTo(-info.iconWidth / 2 + cursorSize, 0);
				setPivotX(getMeasuredWidth() - info.iconWidth / 2 - cursorSize);
			}
			else
			{
				setPivotX(info.iconWidth / 2 + cursorSize);
				path.rLineTo(-(getMeasuredWidth() - info.iconWidth / 2 - cursorSize), 0);
			}
			point.x = x;
			path.rLineTo(-cursorSize, cursorSize);
			path.rLineTo(-cursorSize, -cursorSize);
			path.lineTo(getPaddingLeft(), measureHeight + getPaddingBottom());
			path.close();
			int y=info.viewY + info.iconY / 2 - measureHeight;
			if (y < 0)
			{
				y = info.viewY + info.viewHeight - info.iconY;
				//下方显示
				setPivotY(0);
				Camera camera=new Camera();
				camera.rotateX(180);
				camera.getMatrix(pathMatrix);
				pathMatrix.preTranslate(0, -measureHeight / 2f - getPaddingLeft());
				pathMatrix.postTranslate(0, measureHeight / 2f + getPaddingLeft());
			}
			else
			{
				setPivotY(measureHeight);
				pathMatrix.reset();
			}
			path.transform(pathMatrix);
			point.y = y;
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
		switch (p1.getId())
		{
			case R.id.applicationinfo:
				if (info.obj instanceof ItemInfo)
				{
					ItemInfo appinfo=(ItemInfo)info.obj;
					Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
					//String pkg = "com.android.settings";
					//String cls = "com.android.settings.applications.InstalledAppDetails";
					//i.setComponent(new ComponentName(pkg, cls));
					i.setData(Uri.parse("package:" + appinfo.packageName));
					getContext().startActivity(i);
				}
				break;
			case R.id.hideorunhide:
				if (info.obj instanceof ItemInfo)
				{
					ItemInfo appinfo=(ItemInfo) info.obj;
					switch (appinfo.state)
					{
						case DeviceManager.PACKAGE_UNHIDE:
						case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
							//隐藏
							if (DeviceManager.getInstance(getContext()).disable(appinfo.packageName))
								appinfo.state = DeviceManager.PACKAGE_HIDE;
							break;
						case DeviceManager.PACKAGE_HIDE:
							//显示
							if (DeviceManager.getInstance(getContext()).enable(appinfo.packageName))
								appinfo.state = DeviceManager.PACKAGE_UNHIDE;
							break;
						default:
							//用root激活app
							break;
					}
					p1.invalidate();
					//((IconsAdapter)((GridView)getRootView().findViewById(R.id.gridview)).getAdapter()).notifyDataSetChanged();
				}
				break;
			case R.id.plus:
				if (info.obj instanceof ItemInfo)
				{
					ItemInfo itemInfo=(ItemInfo) info.obj;
					getContext().startActivity(new Intent(getContext(), DetailsActivity.class).setPackage(itemInfo.packageName));
				}break;
			default:
				break;
		}
		hide();
	}

	@Override
	public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		ShortcutInfoCompat info=list.get(p3);
		Intent intent=info.getIntent();
		if(intent==null){
			intent=new Intent();
			intent.setComponent(info.getActivity());
			intent.setPackage(info.getPackage());
		}
		DeepShortcutManager.getInstance(getContext()).startShortcut(info.getPackage(), info.getId(), intent, null, info.getUserHandle());
		hide();
	}


	@Override
	public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
	{
		final ShortcutInfoCompat shortcutInfo=list.get(p3);
		if(!shortcutInfo.isCustom())return true;
		new AlertDialog.Builder(getContext()).setTitle(R.string.ornotdelete).setMessage(shortcutInfo.getShortLabel()).setPositiveButton(android.R.string.cancel, null).setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					list.remove(shortcutInfo);
					mShortcutsAdapter.notifyDataSetChanged();
					//toDoXY();
					getContext().getContentResolver().delete(LauncherSettings.Shortcuts.CONTENT_URI,LauncherSettings.Shortcuts._ID+"=?",new String[]{shortcutInfo.getId()});
				}
			}).show();
		return true;
	}




}
