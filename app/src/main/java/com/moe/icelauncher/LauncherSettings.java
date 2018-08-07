/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moe.icelauncher;

import android.provider.BaseColumns;

import com.moe.icelauncher.config.ProviderConfig;
import com.moe.icelauncher.model.AppInfo;
import android.net.Uri;
import android.content.ComponentName;

public class LauncherSettings
{
   static interface ChangeLogColumns extends BaseColumns
	{
        public static final String MODIFIED = "modified";
    }

    static interface BaseLauncherColumns extends ChangeLogColumns
	{
		public static final String _ID="_id";
		public static final String PACKAGENAME="packageName";
		public static final String TITLE="title";
		public static final String STATE="state";
		public static final String ICON="icon";
		public static final String ICONSANIFYSCALE="iconSanifyScale";
		public static final String ITEMTYPE="itemType";

    }
    public static final class WorkspaceScreens implements ChangeLogColumns
	{

        public static final String TABLE_NAME = "workspaceScreens";

        public static final Uri CONTENT_URI = Uri.parse("content://" +
														ProviderConfig.AUTHORITY + "/" + TABLE_NAME);
        public static final String SCREEN_RANK = "screenRank";
		public static final String ALLOWBLANK="allowBlank";
    }

	public static final class AllApps implements BaseLauncherColumns
	{
		public static final String TABLE_NAME = "allapps";
		public static final String COMPONENTNAME="componentName";
		public static final String LASTUPDATETIME="lastUpdateTime";
		public static final String FLAGS="flags";
		public static final Uri CONTENT_URI = Uri.parse("content://" +
														ProviderConfig.AUTHORITY + "/" + TABLE_NAME);

	}
    public static final class Favorites implements BaseLauncherColumns
	{

        public static final String TABLE_NAME = "favorites";
		public static final Uri CONTENT_URI = Uri.parse("content://" +
														ProviderConfig.AUTHORITY + "/" + TABLE_NAME);
        public static Uri getContentUri(long id)
		{
            return Uri.parse("content://" + ProviderConfig.AUTHORITY +
							 "/" + TABLE_NAME + "/" + id);
        }
		//区分dock栏和桌面
        public static final String CONTAINER = "container";
        public static final int CONTAINER_DESKTOP = -100;
        public static final int CONTAINER_HOTSEAT = -101;

        static final String containerToString(int container)
		{
            switch (container)
			{
                case CONTAINER_DESKTOP: return "desktop";
                case CONTAINER_HOTSEAT: return "hotseat";
                default: return String.valueOf(container);
            }
        }
		//属于第几块屏幕
        public static final String SCREEN = "screen";
		//所处x轴
        public static final String CELLX = "cellX";
		//所处y轴
        public static final String CELLY = "cellY";
		//所占区块大小
        public static final String SPANX = "spanX";
        public static final String SPANY = "spanY";
		
        public static final String PROFILE_ID = "profileId";
		
        public static final int ITEM_TYPE_FOLDER = 2;
        public static final int ITEM_TYPE_APPWIDGET = 4;
        public static final int ITEM_TYPE_CUSTOM_APPWIDGET = 5;
		public static final int ITEM_TYPE_APPLICATION=0;
		//如果是桌面控件，控件的ID和所有者
        public static final String APPWIDGET_ID = "appWidgetId";
        public static final String APPWIDGET_PROVIDER = "appWidgetProvider";
        
		public static final String RESTORED = "restored";
		
        public static final String RANK = "rank";
        public static final String OPTIONS = "options";
		public static final String INTENT="intent";
    }

    /**
     * Launcher settings
     */
    public static final class Settings
	{
		public static final String TABLE_NAME="settings";
        public static final Uri CONTENT_URI = Uri.parse("content://" +
														ProviderConfig.AUTHORITY + "/" + TABLE_NAME);

        public static final String METHOD_GET= "get_setting";
        public static final String METHOD_SET= "set_setting";

        public static final String EXTRA_VALUE = "value";
        public static final String EXTRA_DEFAULT_VALUE = "default_value";
    }
	public static final class Icons implements BaseColumns{
		public static final String TABLE_NAME="icons";
		public static final Uri CONTENT_URI = Uri.parse("content://" +
														ProviderConfig.AUTHORITY + "/" + TABLE_NAME);
		public static final String VERSION="version";
		public static final String _ID="_id";
		public static final String PACKAGENAME="packageName";
		public static final String TITLE="title";
		public static final String ICON="icon";
		public static final String COMPONENTNAME="componentName";
	}
}
