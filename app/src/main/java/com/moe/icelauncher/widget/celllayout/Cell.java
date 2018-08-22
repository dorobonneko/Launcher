package com.moe.icelauncher.widget.celllayout;
import android.content.Intent;

public interface Cell
{
	public int getCellX();
	public int getCellY();
	public int getSpanX();
	public int getSpanY();
	public int getState();
	public Intent getIntent();
}
