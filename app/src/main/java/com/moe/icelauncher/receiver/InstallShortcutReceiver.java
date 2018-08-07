package com.moe.icelauncher.receiver;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

public class InstallShortcutReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context p1, Intent p2)
	{
		Object o=p2.getExtras().keySet();
	}

	
}
