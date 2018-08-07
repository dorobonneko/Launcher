package com.moe.icelauncher.util;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.ComponentName;
import com.moe.icelauncher.receiver.Admin;

public class DeviceManager
{
	public static final int PACKAGE_HIDE=-2;
	public static final int PACKAGE_UNHIDE=-1;
	private static DeviceManager dm;
	private DevicePolicyManager dpm;
	private String packageName;
	private ComponentName device;
	private DeviceManager(Context context){
		dpm=(DevicePolicyManager) context.getSystemService(context.DEVICE_POLICY_SERVICE);
		packageName=context.getPackageName().intern();
		device=new ComponentName(context,Admin.class);
	}
	public static DeviceManager getInstance(Context context){
		if(dm==null){
			synchronized(DeviceManager.class){
				if(dm==null)
					dm=new DeviceManager(context);
			}
		}
		return dm;
	}
	//sdk21
	public boolean enable(String packageName){
		if(dpm.isDeviceOwnerApp(this.packageName))
			return dpm.setApplicationHidden(device,packageName,false);
			return false;
	}
	public boolean disable(String packageName){
		if(dpm.isDeviceOwnerApp(this.packageName))
			return dpm.setApplicationHidden(device,packageName,true);
		return false;
	}
	public void removeDeviceOwner(){
		if(dpm.isDeviceOwnerApp(packageName))
		dpm.clearDeviceOwnerApp(packageName);
	}
	public boolean isApplicationHidden(String packageName){
		if(dpm.isDeviceOwnerApp(this.packageName))
			return dpm.isApplicationHidden(device,packageName);
			return false;
	}
}
