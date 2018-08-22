package com.moe.icelauncher.compat;
import android.content.*;
import java.util.*;
import android.content.res.*;
import org.xmlpull.v1.*;

public class ActivityCompat
{
	private static ActivityCompat mActivityCompat;
	public static ActivityCompat getInstance(){
		synchronized(ActivityCompat.class){
			if(mActivityCompat==null)
				mActivityCompat=new ActivityCompat();
			return mActivityCompat;
		}
	}
	public static List<LauncherActivityInfoCompat> getActivitysFormPackage(Context context,String packageName){
		ArrayList<LauncherActivityInfoCompat> list=new ArrayList<>();
		try{
			Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(packageName);
			AssetManager assets = resourcesForApplication.getAssets();
			XmlResourceParser parseXml = assets.openXmlResourceParser("AndroidManifest.xml");
			Map<String,String> parseData=new HashMap<>();
			int eventType;
            while ((eventType = parseXml.nextToken()) != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String name = parseXml.getName();
                    if ("activity".equals(name) || "activity-alias".equals(name)) {
						parseData.clear();
						for(int i=0;i<parseXml.getAttributeCount();i++)
						parseData.put(parseXml.getAttributeName(i),parseXml.getAttributeValue(i));
						
                      }
                }
            }
		}catch(Exception e){}
		return list;
	}
}
