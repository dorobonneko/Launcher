package com.moe.icelauncher.shortcuts;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.content.ComponentName;
import android.content.pm.LauncherApps;
import java.util.ArrayList;
import com.moe.icelauncher.Utilities;
import java.util.List;
import android.content.pm.PackageManager;
import java.util.Map;
import android.content.pm.LauncherActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import java.util.HashMap;
import android.content.res.XmlResourceParser;
import org.xmlpull.v1.XmlPullParser;
import android.content.pm.ResolveInfo;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

public class DeepShortcutManagerBackport {
    static Drawable getShortcutIconDrawable(ShortcutInfoCompat shortcutInfo, int density) {
        return ((ShortcutInfoCompatBackport) shortcutInfo).getIcon(density);
    }

    public static List<ShortcutInfoCompat> getForPackage(Context context, LauncherApps mLauncherApps, ComponentName activity, String packageName) {
        List<ShortcutInfoCompat> shortcutInfoCompats = new ArrayList<>();
        if (Utilities.ATLEAST_MARSHMALLOW) {
            List<LauncherActivityInfo> infoList = mLauncherApps.getActivityList(packageName, android.os.Process.myUserHandle());
            for (LauncherActivityInfo info : infoList) {
                if (activity == null || activity.equals(info.getComponentName())) {
                    parsePackageXml(context, info.getComponentName().getPackageName(), info.getComponentName(), shortcutInfoCompats);
                }
            }
        }
        return shortcutInfoCompats;
    }

    private static void parsePackageXml(Context context, String packageName, ComponentName activity, List<ShortcutInfoCompat> shortcutInfoCompats) {
        PackageManager pm = context.getPackageManager();

        String resource = null;
        String currActivity = "";
        String searchActivity = activity.getClassName();

        Map<String, String> parsedData = new HashMap<>();

        try {
            Resources resourcesForApplication = pm.getResourcesForApplication(packageName);
            AssetManager assets = resourcesForApplication.getAssets();
            XmlResourceParser parseXml = assets.openXmlResourceParser("AndroidManifest.xml");

            int eventType;
            while ((eventType = parseXml.nextToken()) != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String name = parseXml.getName();
                    if ("activity".equals(name) || "activity-alias".equals(name)) {
                        parsedData.clear();
                        for (int i = 0; i < parseXml.getAttributeCount(); i++) {
                            parsedData.put(parseXml.getAttributeName(i), parseXml.getAttributeValue(i));
                        }
                        if (parsedData.containsKey("name")) {
                            currActivity = parsedData.get("name");
                        }
                    } else if (name.equals("meta-data") && currActivity.equals(searchActivity)) {
                        parsedData.clear();
                        for (int i = 0; i < parseXml.getAttributeCount(); i++) {
                            parsedData.put(parseXml.getAttributeName(i), parseXml.getAttributeValue(i));
                        }
                        if (parsedData.containsKey("name") &&
							parsedData.get("name").equals("android.app.shortcuts") &&
							parsedData.containsKey("resource")) {
                            resource = parsedData.get("resource");
                        }
                    }
                }
            }

            if (resource != null) {
                parseXml = resourcesForApplication.getXml(Integer.parseInt(resource.substring(1)));
                while ((eventType = parseXml.nextToken()) != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (parseXml.getName().equals("shortcut")) {
                            ShortcutInfoCompat info = parseShortcut(context,
																	activity,
																	resourcesForApplication,
																	packageName,
																	parseXml);

                            if (info != null && info.getId() != null) {
                                for (ResolveInfo ri : pm.queryIntentActivities(ShortcutInfoCompatBackport.stripPackage(info.makeIntent()), 0)) {
                                    if (ri.isDefault || ri.activityInfo.exported) {
                                        shortcutInfoCompats.add(info);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException | XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    private static ShortcutInfoCompat parseShortcut(Context context, ComponentName activity, Resources resourcesForApplication, String packageName, XmlResourceParser parseXml) {
        try {
            return new ShortcutInfoCompatBackport(context, resourcesForApplication, packageName, activity, parseXml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
