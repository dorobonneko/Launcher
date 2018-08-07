package com.moe.icelauncher.model;
import android.content.Intent;
import android.os.UserHandle;
import android.graphics.drawable.Drawable;

public abstract class ItemInfo
{
	public int itemType;
	public UserHandle user;
	public int _id;
	public Drawable icon;
	public String title;
	public long modified;
	public String packageName;
	public int state;
	public abstract Intent getIntent();

	public boolean isDisabled()
	{
		return false;
	}
}
