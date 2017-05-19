package com.moe.view;
import android.view.SurfaceView;
import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback2;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.graphics.Rect;
import android.graphics.Bitmap;
import com.moe.HitFunny.R;
import com.moe.utils.ImageScale;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.BitmapFactory;
import java.util.Random;
import android.view.MotionEvent;

public class GroupView extends SurfaceView implements SurfaceHolder.Callback2,Runnable
{
	//默认难度
	private int level=3;
	//最大难度
	private int max=9;
	private SurfaceHolder sh;
	//生成数量
	private int count;
	private long fps=1;
	private long oldTime;
	Paint paint=new Paint();
	private Rect statusRect=new Rect();
	private Rect contentRect=new Rect();
	private long point;
	private int radius=0;
	private Bitmap funny;
	private int[][] object;
	private ObjectCreate oc;
	//等待用户点击时长
	private int waitTime=1000;
	//随机数生成类
	private Random random=new Random();
	//是否启动
	private boolean needWait=false;
	private Thread mainThread;
	//连击
	private int hit=0;
	//是否旋转
	private boolean roate;
	public GroupView(Context context, boolean roate)
	{
		super(context);
		mainThread = new Thread(this);
		paint.setAntiAlias(true);
		paint.setDither(true);
		this.roate = roate;
		getHolder().addCallback(this);
	}
	public GroupView(Context context)
	{
		this(context, false);
	}


	public void startWithLevel(int level)
	{
		needWait = true;
		this.level = level < 3 ?3: level > max ?max: level;
		count = this.level / 2;
		statusRect.set(0, 0, getWidth(), 60);
		if (getHeight() - statusRect.bottom > getWidth())
		{
			radius = getWidth() / this.level;
			contentRect.set(0, getHeight() - statusRect.bottom - getWidth() - (getHeight() - statusRect.bottom - getWidth()) / 2, getWidth(), getHeight() - statusRect.bottom - (getHeight() - statusRect.bottom - getWidth()) / 2);
		}
		else
		{
			radius = (getHeight() - statusRect.bottom) / this.level;
			contentRect.set((getWidth() - (getHeight() - statusRect.bottom)) / 2, statusRect.bottom, (getWidth() - (getHeight() - statusRect.bottom)) / 2 + (getHeight() - statusRect.bottom), getHeight());
		}
		if (funny != null)funny.recycle();
		funny = ImageScale.Scale(((BitmapDrawable)getResources().getDrawable(R.drawable.funny)).getBitmap(), radius / 3 * 2, radius / 3 * 2);
		
		
	}
	@Override
	public void surfaceCreated(SurfaceHolder p1)
	{

		this.sh = p1;
		statusRect.set(0, 0, getWidth(), 60);
		if (getHeight() - statusRect.bottom > getWidth())
		{
			radius = getWidth() / this.level;
			contentRect.set(0, getHeight() - statusRect.bottom - getWidth() - (getHeight() - statusRect.bottom - getWidth()) / 2, getWidth(), getHeight() - statusRect.bottom - (getHeight() - statusRect.bottom - getWidth()) / 2);
		}
		else
		{
			radius = (getHeight() - statusRect.bottom) / this.level;
			contentRect.set((getWidth() - (getHeight() - statusRect.bottom)) / 2, statusRect.bottom, (getWidth() - (getHeight() - statusRect.bottom)) / 2 + (getHeight() - statusRect.bottom), getHeight());
		}
		if (funny != null)funny.recycle();
		funny = ImageScale.Scale(((BitmapDrawable)getResources().getDrawable(R.drawable.funny)).getBitmap(), radius / 3 * 2, radius / 3 * 2);
		oc = new ObjectCreate();
		oc.start();
		mainThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder p1, int p2, int p3, int p4)
	{

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder p1)
	{
		sh = null;
		if (oc != null)oc.close();
		needWait = false;
	}

	@Override
	public void surfaceRedrawNeeded(SurfaceHolder p1)
	{

	}
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		try{
		switch (event.getAction() & event.ACTION_MASK)
		{
			case event.ACTION_DOWN:
			case event.ACTION_POINTER_DOWN:
				for (int pointer=0;pointer < event.getPointerCount();pointer++)
				{
					int x=(int)event.getX(pointer);
					int y=(int)event.getY(pointer);
					if (roate)
					{
						x=getWidth()-x;
						y=getHeight()-y;
					}
					
						if (contentRect.contains(x, y))
						{
							for (int i=0;i < level;i++)
							{
								int start_y=i * radius + contentRect.top;
								int end_y=(i + 1) * radius + contentRect.top;
								for (int n=0;n < level;n++)
								{
									int start_x=n * radius + contentRect.left;
									int end_x=(n + 1) * radius + contentRect.left;
									if (x > start_x && x < end_x && y > start_y && y < end_y && object[i][n] == 1)
									{
										hit++;
										point += 5;
										object[i][n] = -1;
										if(point>level*30<<2){
											startWithLevel(level+1);
											}
									}

								}
							}
						
					}
				}
				break;
		}
		}catch(Exception e){}
		return true;
	}
	private void drawHit(Canvas c)
	{
		if (hit > 0)
		{
			c.save();
			c.clipRect(statusRect);
			paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, getResources().getDisplayMetrics()));
			paint.setStrokeWidth(3);
			paint.setTextAlign(Paint.Align.CENTER);
			c.drawText("连击:" + hit, statusRect.right / 2, statusRect.bottom / 2 - (paint.ascent() + paint.descent()) / 2, paint);
			c.restore();
		}
	}
	private void drawPoint(Canvas c)
	{
		c.save();
		c.clipRect(statusRect);
		paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, getResources().getDisplayMetrics()));
		paint.setStrokeWidth(3);
		paint.setTextAlign(Paint.Align.LEFT);
		c.drawText("分数:" + point, statusRect.left, statusRect.bottom / 2 - (paint.ascent() + paint.descent()) / 2, paint);
		c.restore();
	}
	private void drawFps(Canvas c)
	{
		c.save();
		c.clipRect(statusRect);
		paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, getResources().getDisplayMetrics()));
		paint.setStrokeWidth(3);
		paint.setTextAlign(Paint.Align.RIGHT);
		c.drawText("FPS:" + fps, statusRect.right, statusRect.bottom / 2 - (paint.ascent() + paint.descent()) / 2, paint);
		c.restore();
	}
	private void drawBackground(Canvas c)
	{
		c.save();
		c.clipRect(contentRect);
		//外循环行，内循环列
		for (int i=0;i < level;i++)
		{
			//通过行算出y坐标
			int y=i * radius + radius / 2 + contentRect.top;
			for (int n=0;n < level;n++)
			{
				//通过列算出x坐标
				int x=n * radius + radius / 2 + contentRect.left;
				//绘制图形
				c.drawCircle(x, y, radius / 3, paint);
			}
		}
		c.restore();
	}
	private void drawItem(Canvas c)
	{
		c.save();
		c.clipRect(contentRect);
		//外循环行，内循环列
		for (int i=0;i < level;i++)
		{
			//通过行算出y坐标
			int y=i * radius + radius / 2 + contentRect.top;
			for (int n=0;n < level;n++)
			{
				//通过列算出x坐标
				int x=n * radius + radius / 2 + contentRect.left;
				//绘制图形
				switch (object[i][n])
				{//如果当前位置有可点击元素则绘制元素
					case 1:c.drawBitmap(funny, x - radius / 3, y - radius / 3, paint);break;
					case -1:paint.setColor(0xffffffff); c.drawCircle(x, y, radius / 3, paint); break;
				}
			}
		}
		c.restore();
	}
	@Override
	public void run()
	{
		try
		{
			if (needWait)
				for (int i=3;i > 0;i--)
				{
					Canvas c=sh.lockCanvas();
					if (roate)c.rotate(180, c.getWidth() / 2, c.getHeight() / 2);
					c.drawColor(0xffffffff);
					paint.setColor(0xff000000);
					paint.setTextAlign(Paint.Align.CENTER);
					paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 48, getResources().getDisplayMetrics()));
					paint.setStrokeWidth(6);
					c.drawText(i + "", getWidth() / 2, getHeight() / 2 - (paint.ascent() + paint.descent()) / 2, paint);
					sh.unlockCanvasAndPost(c);
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{}
				}
			while (sh != null)
			{
				long time=System.currentTimeMillis();
				Canvas c=sh.lockCanvas();
				if (roate)c.rotate(180, c.getWidth() / 2, c.getHeight() / 2);
				c.drawColor(0xffffffff);
				paint.setColor(0xff000000);
				try{drawFps(c);
				drawPoint(c);
				drawHit(c);
				drawBackground(c);
				drawItem(c);
				}catch(Exception e){}
				sh.unlockCanvasAndPost(c);
				try
				{
					Thread.sleep(System.currentTimeMillis() - time < 30 ?30: 0);
				}
				catch (InterruptedException e)
				{}
				if (time - oldTime >= 1000)
				{
					fps = 1000 / (System.currentTimeMillis() - time);
					oldTime = time;
				}
			}
		}
		catch (Exception e)
		{}
	}

	class ObjectCreate extends Thread
	{
		private boolean running=false;
		public ObjectCreate()
		{
			running = true;
		}
		public void close()
		{
			running = false;
		}
		@Override
		public void run()
		{
			
			while (sh != null && running == true)
			{
				//生成指定个数的可点击对象
				if(object==null||object.length!=level)
				object = new int[level][level];
				for (int i=0;i < count;i++)
				{
					//行产生
					try{
					object[random.nextInt(level)][random.nextInt(level)] = 1;
					}catch(Exception e){}
				}
				//等待点击
				try
				{
					sleep(waitTime);
				}
				catch (InterruptedException e)
				{}
				//清空所有点击对象
				for (int n=0;n < object.length;n++)
					for (int t=0;t < object[n].length;t++)
					{
						if (object[n][t] == 1)
						{
							//有未点到的，连击失败
							hit = 0;
						}
						object[n][t] = 0;
					}
				//等待300ms进入下一轮
				try
				{
					sleep(300);
				}
				catch (InterruptedException e)
				{}
			}
		}

	}
}
