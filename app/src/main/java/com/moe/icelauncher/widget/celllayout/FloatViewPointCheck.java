package com.moe.icelauncher.widget.celllayout;
import com.moe.icelauncher.widget.CellLayout;

public class FloatViewPointCheck implements Runnable
{
	private CellLayout mCellLayout;
	private float x,y;
	public FloatViewPointCheck(CellLayout cell){
		mCellLayout=cell;
	}
	public void setPoint(int cellX,int cellY,float x,float y){
		this.x=x;
		this.y=y;
	}
	@Override
	public void run()
	{
		//检查view的移动
	}
}
