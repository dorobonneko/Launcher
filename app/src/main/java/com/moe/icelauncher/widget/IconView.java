package com.moe.icelauncher.widget;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import com.moe.icelauncher.model.AppInfo;
import android.graphics.Bitmap;
import android.graphics.Paint;
import com.moe.icelauncher.R;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.os.Handler;
import android.os.Message;
import android.view.ViewConfiguration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuffColorFilter;
import android.content.ClipData;
import android.graphics.Point;
import android.view.DragEvent;
import android.view.View.OnClickListener;
import com.moe.icelauncher.model.IconInfo;
import android.graphics.drawable.Drawable;
import com.moe.icelauncher.util.DeviceManager;
import android.content.pm.PackageManager;
import com.moe.icelauncher.model.ItemInfo;
import android.view.View.OnDragListener;
import com.moe.icelauncher.model.FavoriteInfo;
import com.moe.icelauncher.LauncherSettings;
import android.view.ViewGroup;
import com.moe.icelauncher.widget.celllayout.Cell;
import android.content.Intent;

public class IconView extends View implements Cell
{

	
	
	private ItemInfo info;
	private Paint paint;
	private Matrix matrix;
	private static final int ON_PRESSED=0;
	private static final int ON_LONG_CLICK=1;
	private static final int ON_CLICK=2;
	private int state=-1;
	private ViewConfiguration configuration;
	private boolean intercept;
	private float oldX,oldY;
	private ColorFilter mColorFilter;
	private boolean draging;
	private int dragState=DragEvent.ACTION_DRAG_ENDED;
	private OnClickListener ocl;
	private Rect textBounds=new Rect();
	private float iconScale;
	private int iconX,iconY,iconWidth,iconHeight;
	private Drawable snow;
	private int padding;
	private boolean showTtitle=true;
	private OnDragListener mOnDragListener;
	private DragShadowBuilder build;
	public IconView(Context context){
		super(context);
		matrix=new Matrix();
		paint=new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(getResources().getDimension(R.dimen.icon_text_size));
		paint.setColor(0xffffffff);
		paint.setShadowLayer(5,0,0,0xaf000000);
		configuration=ViewConfiguration.get(context);
		mColorFilter=(new PorterDuffColorFilter(0x50ffffff,PorterDuff.Mode.SRC_ATOP));
		snow=getResources().getDrawable(R.drawable.eye_off_outline);
		padding=getResources().getDimensionPixelOffset(R.dimen.icon_padding);
	}
	public boolean isDraging(){
		return draging;
	}
	public void setShowTitle(boolean show){
		showTtitle=show;
	}
	public void remove()
	{
		((ViewGroup)getParent()).removeView(this);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// TODO: Implement this method
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width=MeasureSpec.getSize(widthMeasureSpec);
		int measureheight=MeasureSpec.getSize(heightMeasureSpec);
		if(info==null)return;
		String name=info.title;
		Drawable icon=info.icon;
		paint.getTextBounds(name,0,name.length(),textBounds);
		iconScale=width/2f/icon.getIntrinsicWidth();
		iconWidth=(int)(icon.getIntrinsicWidth()*iconScale);
		iconHeight=(int)(icon.getIntrinsicHeight()*iconScale);
		int height=width/3*4;
		iconX=width/4;
		if(measureheight!=0&&height>measureheight){
			height=measureheight;
			}
		iconY=(height-iconHeight)/2;
		if(showTtitle)
			iconY-=textBounds.height();
		if(snow!=null)
		snow.setBounds((int)(iconX+iconWidth/2),(int)(iconY+iconHeight/2),(int)(iconX+iconWidth),(int)(iconY+iconHeight));
		//图片y值
		setMeasuredDimension(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));
		
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		if(draging)return;
		//if(dragState!=DragEvent.ACTION_DRAG_ENDED)return;
		if(info==null)return;
		Drawable icon=info.icon;
		
		String name=info.title;
		if(icon!=null&&name!=null){
			matrix.setScale(iconScale,iconScale);
			//根据图片宽度和文字高度算出两者起始y值
			//图片y值
			matrix.postTranslate(iconX,iconY);
			switch(state){
				case -1:
				icon.setBounds((int)iconX,(int)iconY,(int)(iconX+iconWidth),(int)(iconY+iconHeight));
				icon.draw(canvas);
			break;
			case ON_PRESSED:
				icon.setColorFilter(mColorFilter);
				icon.setBounds((int)iconX,(int)iconY,(int)(iconX+iconWidth),(int)(iconY+iconHeight));
				icon.draw(canvas);
				icon.setColorFilter(null);
				break;
			case ON_LONG_CLICK:
				float size=iconWidth*0.1f;
				icon.setBounds((int)(iconX-size),(int)(iconY-size),(int)(iconX+iconWidth+size),(int)(iconY+iconHeight+size));
				icon.draw(canvas);
				//matrix.postScale(1.1f,1.1f,canvas.getWidth()/2f,canvas.getHeight()/2f);
				//canvas.drawBitmap(icon,matrix,null);
				break;
			}
			if(state!=ON_LONG_CLICK&&showTtitle){
			if(textBounds.width()>getMeasuredWidth())
				//超出视图长度，截断
			canvas.drawText(name,0,getMeasuredHeight()-iconX*1.5f,paint);
			else
			canvas.drawText(name,(getWidth()-textBounds.width())/2f,getMeasuredHeight()-iconX*1.5f,paint);
		}
		}
		if(info.state!=DeviceManager.PACKAGE_UNHIDE&&info.state!=PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
			drawSnow(canvas);
	}
	public void drawSnow(Canvas canvas){
		if(snow!=null)
		snow.draw(canvas);
	}
	public void setItemInfo(ItemInfo info){
		setWillNotDraw(false);
		this.info=info;
		if(info.icon!=null)
			info.icon.setCallback(this);
		invalidate();
	}
	public ItemInfo getItemInfo(){
		return info;
	}
	public void setColorFilter(ColorFilter filter){
		if(info.icon!=null)
			info.icon.setColorFilter(filter);
			/*View.DragShadowBuilder build=new View.DragShadowBuilder(){
					public void onDrawShadow(Canvas canvas){
						info.icon.draw(canvas);
					}
					public void onProvideShadowMetrics(Point size,Point touch){
						size.set(getWidth(),getHeight());
					}
				};*/
			updateDragShadow(build);
	}
	@Override
	public boolean dispatchTouchEvent(final MotionEvent event)
	{
		switch(event.getAction()){
			case event.ACTION_DOWN:
				oldX=event.getRawX();
				oldY=event.getRawY();
				state=-1;
				intercept=true;
				handler.sendEmptyMessageDelayed(ON_PRESSED,configuration.getPressedStateDuration());
				handler.sendMessageDelayed(handler.obtainMessage(ON_LONG_CLICK,(int)event.getX(),(int)event.getY(),null),configuration.getLongPressTimeout());
				break;
			case event.ACTION_MOVE:
				if(intercept&&(Math.abs(event.getRawY()-oldY)>configuration.getScaledTouchSlop()||Math.abs(event.getRawX()-oldX)>configuration.getScaledTouchSlop())){
					//偏移过多，视为滚动
					if(handler.hasMessages(ON_LONG_CLICK)){
						intercept=false;
						state=-1;
						invalidate();
					handler.removeMessages(ON_PRESSED);
					handler.removeMessages(ON_LONG_CLICK);
					//return true;
					}else if(Math.abs(event.getRawY()-oldY)>configuration.getScaledTouchSlop()||Math.abs(event.getRawX()-oldX)>configuration.getScaledTouchSlop()){
						intercept=false;
						/*View.DragShadowBuilder build=new View.DragShadowBuilder(IconView.this){
							public void onProvideShadowMetrics(Point out,Point outTouch){
								super.onProvideShadowMetrics(out,outTouch);
								outTouch.x+=(-getView().getWidth()/2+(int)event.getX());
								outTouch.y+=(-getView().getHeight()/2+(int)event.getY());
							}
						};*/
						View.DragShadowBuilder build=new View.DragShadowBuilder(){
							public void onDrawShadow(Canvas canvas){
								info.icon.draw(canvas);
							}
							public void onProvideShadowMetrics(Point size,Point touch){
								size.set(getMeasuredWidth(),getMeasuredHeight());
								touch.x=(int)event.getX()-(int)getTranslationX();
								touch.y=(int)event.getY()-(int)getTranslationY();
								
							}
						};
						draging=true;
						CellLayout.Info ci=new CellLayout.Info();
						ci.view=IconView.this;
						ci.info=info;
						ci.shadow=build;
						this.build=build;
						startDrag(ClipData.newPlainText("title",info.title),build,ci,DRAG_FLAG_OPAQUE);
						return true;
				}
				}
				if(draging)
					return true;
				break;
			case event.ACTION_CANCEL:
				state=-1;
				handler.removeMessages(ON_PRESSED);
				handler.removeMessages(ON_LONG_CLICK);
				if(!draging)
				invalidate();
				return true;
				//break;
			case event.ACTION_UP:
				state=-1;
				if(isClickable()&&handler.hasMessages(ON_LONG_CLICK)&&intercept)
					handler.sendEmptyMessage(ON_CLICK);
				handler.removeMessages(ON_PRESSED);
				handler.removeMessages(ON_LONG_CLICK);
					invalidate();
				break;
		}
		return intercept;
	}
	
	private Handler handler=new Handler(){

		@Override
		public void handleMessage(final Message msg)
		{
			switch(msg.what){
				case ON_PRESSED:
					state=ON_PRESSED;
					invalidate();
					break;
				case ON_LONG_CLICK:
					state=ON_LONG_CLICK;
					int[] location=new int[2];
					getLocationInWindow(location);
					IconInfo info=new IconInfo();
					info.viewX=location[0];
					info.viewY=location[1];
					info.viewWidth=getWidth();
					info.viewHeight=getHeight();
					info.iconX=(int)iconX;
					info.iconY=(int)iconY;
					info.iconWidth=(int)iconWidth;
					info.iconHeight=(int)iconHeight;
					info.obj=IconView.this.info;
					((ContainerView)getRootView().findViewById(R.id.containerview)).showPopupView(info);
					invalidate();
					break;
				case ON_CLICK:
					if(ocl!=null)
						ocl.onClick(IconView.this);
					break;
			}
		}
		
	};

	@Override
	public void setOnDragListener(View.OnDragListener l)
	{
		mOnDragListener=l;
	}
	
	@Override
	public boolean onDragEvent(DragEvent event)
	{
		if(mOnDragListener!=null)mOnDragListener.onDrag(this,event);
		if(((CellLayout.Info)event.getLocalState()).view!=this)return false;
		dragState=event.getAction();
		
		switch(event.getAction()){
			case event.ACTION_DRAG_STARTED:
				break;
			case event.ACTION_DRAG_ENTERED:
			case event.ACTION_DRAG_EXITED:
				return false;
			case event.ACTION_DRAG_ENDED:
				draging=false;
				break;
		}
		invalidate();
		return true;
	}

	@Override
	public void setOnClickListener(View.OnClickListener l)
	{
		ocl=l;
		setClickable(l!=null);
	}
	/*private static Bitmap reSetIcon(Bitmap icon,int width){
		//检查图标状态，重新处理
	}*/
	@Override
	public int getCellX()
	{
		if(info instanceof FavoriteInfo)
		return ((FavoriteInfo)info).cellX;
		return 0;
	}

	@Override
	public int getCellY()
	{
		if(info instanceof FavoriteInfo)
			return ((FavoriteInfo)info).cellY;
		return 0;
	}

	@Override
	public int getSpanX()
	{
		if(info instanceof FavoriteInfo)
			return ((FavoriteInfo)info).spanX;
		return 1;
	}

	@Override
	public int getSpanY()
	{
		if(info instanceof FavoriteInfo)
			return ((FavoriteInfo)info).spanY;
		return 1;
	}

	@Override
	public int getState()
	{
		return info.state;
	}

	@Override
	public Intent getIntent()
	{
		return info.getIntent();
	}
}
