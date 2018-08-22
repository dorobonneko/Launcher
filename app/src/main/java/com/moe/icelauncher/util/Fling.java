package com.moe.icelauncher.util;
import android.widget.Scroller;
import android.animation.*;
import android.view.animation.*;

public class Fling
{
	private int startX,startY,finalX,finalY;
	private long duration,oldTime;
	private int offsetX,offsetY;
	private int currX,currY;
	private boolean mFinished;
	private float velocityX,velocityY;
	private TimeInterpolator mTimeInterpolator;
	public Fling(){
		this(new AccelerateInterpolator());
	}
	public Fling(TimeInterpolator mTimeInterpolator){
		this.mTimeInterpolator=mTimeInterpolator;
	}
	public void abortAnimation()
	{
		foreFinish(false);
	}
	public void fling(int startX, int startY, int finalX, int finalY, float velocityX, float velocityY)
	{
		this.startX = startX;
		this.startY = startY;
		this.finalX = finalX;
		this.finalY = finalY;
		this.velocityX = Math.abs(velocityX);
		this.velocityY = Math.abs(velocityY);
		//this.duration = duration;
		offsetY = 0;
		offsetX = 0;
		currX = startX;
		currY = startY;
		oldTime=System.nanoTime();
		mFinished=false;
	}
	public final boolean isFinished()
	{
		return mFinished;
	}
	public final boolean computeScrollOffset()
	{
		if(currX == finalX && currY == finalY)
			foreFinish(false);
		if (isFinished())
			return false;
			long time=System.nanoTime()-oldTime;
			double distance=time/1000000000d;
			if(startX!=finalX){
			int offsetX=(int)(distance*velocityX*mTimeInterpolator.getInterpolation(1-(currX-startX)/(finalX-startX)));
				int currX=this.currX+(finalX>startX?+offsetX:-offsetX);
				currX=finalX>startX?(currX>finalX?finalX:currX):(currX<finalX?finalX:currX);
				this.offsetX=currX-this.currX;
				this.currX=currX;
			}
			if(startY!=finalY){
			int offsetY=(int)(distance*velocityY*mTimeInterpolator.getInterpolation(1-(currY-startY)/(finalY-startY)));
			int currY=this.currY+(finalY>startY?+offsetY:-offsetY);
			currY=finalY>startY?(currY>finalY?finalY:currY):(currY<finalY?finalY:currY);
			this.offsetY=currY-this.currY;
			this.currY=currY;
			}
			oldTime=System.nanoTime();
		return true;
	}
	public final void foreFinish(boolean finished)
	{
		if (finished)
		{//直接到终点
			offsetX = finalX - currX;
			offsetY = finalY - currY;
			currX = finalX;
			currY = finalY;
		}
		else
		{//在当前点停止
			finalX = currX;
			finalY = currY;
			offsetX = 0;
			offsetY = 0;
		}
		mFinished = true;
	}
	public final int getCurrX()
	{
		return currX;
	}
	public final int getCurrY()
	{
		return currY;
	}
	public final int getOffsetX()
	{
		return offsetX;
	}
	public final int getOffsetY()
	{
		return offsetY;
	}
}
