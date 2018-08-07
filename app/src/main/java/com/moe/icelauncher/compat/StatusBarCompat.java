package com.moe.icelauncher.compat;
import android.content.Context;
import java.lang.reflect.Method;

public class StatusBarCompat
{
	public static void expand(Context context){
		Object service = context.getSystemService("statusbar");

		if (null == service)

			return;

		try {

			Class<?> clazz = Class.forName("android.app.StatusBarManager");

			int sdkVersion = android.os.Build.VERSION.SDK_INT;

			Method expand = null;

			if (sdkVersion <= 16) {

				expand = clazz.getMethod("expand");

			} else {

				/*

				 * Android SDK 16之后的版本展开通知栏有两个接口可以处理

				 * expandNotificationsPanel()

				 * expandSettingsPanel()

				 */

				//expand =clazz.getMethod("expandNotificationsPanel");

				expand = clazz.getMethod("expandSettingsPanel");

			}



			expand.setAccessible(true);

			expand.invoke(service);

		} catch (Exception e) {

			e.printStackTrace();

		}
	}
	public static void collapse(Context context){
		Object service = context.getSystemService("statusbar");

		if (null == service)

			return;

		try {

			Class<?> clazz = Class.forName("android.app.StatusBarManager");

			int sdkVersion = android.os.Build.VERSION.SDK_INT;

			Method collapse = null;

			if (sdkVersion <= 16) {

				collapse = clazz.getMethod("collapse");

			} else {

				collapse = clazz.getMethod("collapsePanels");

			}



			collapse.setAccessible(true);

			collapse.invoke(service);

		} catch (Exception e) {

			e.printStackTrace();

		}
	}
}
