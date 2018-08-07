package com.moe.icelauncher.shortcuts;
import android.content.ComponentName;
import android.os.UserHandle;
import android.content.Intent;
import com.moe.icelauncher.model.ItemInfo;
import android.content.Context;
import java.util.Arrays;
import com.moe.icelauncher.util.Preconditions;
import com.moe.icelauncher.compat.UserManagerCompat;

public class ShortcutKey
{public final ComponentName componentName;
    public final UserHandle user;

    private final int mHashCode;

    public ShortcutKey(ComponentName componentName, UserHandle user) {
        Preconditions.assertNotNull(componentName);
        Preconditions.assertNotNull(user);
        this.componentName = componentName;
        this.user = user;
        mHashCode = Arrays.hashCode(new Object[] {componentName, user});

    }

    /**
     * Creates a new component key from an encoded component key string in the form of
     * [flattenedComponentString#userId].  If the userId is not present, then it defaults
     * to the current user.
     */
    public ShortcutKey(Context context, String componentKeyStr) {
        int userDelimiterIndex = componentKeyStr.indexOf("#");
        if (userDelimiterIndex != -1) {
            String componentStr = componentKeyStr.substring(0, userDelimiterIndex);
            Long componentUser = Long.valueOf(componentKeyStr.substring(userDelimiterIndex + 1));
            componentName = ComponentName.unflattenFromString(componentStr);
            user = UserManagerCompat.getInstance(context)
				.getUserForSerialNumber(componentUser.longValue());
        } else {
            // No user provided, default to the current user
            componentName = ComponentName.unflattenFromString(componentKeyStr);
            user = android.os.Process.myUserHandle();
        }
        Preconditions.assertNotNull(componentName);
        Preconditions.assertNotNull(user);
        mHashCode = Arrays.hashCode(new Object[] {componentName, user});
    }

    @Override
    public int hashCode() {
        return mHashCode;
    }

    @Override
    public boolean equals(Object o) {
        ShortcutKey other = (ShortcutKey) o;
        return other.componentName.equals(componentName) && other.user.equals(user);
    }

    /**
     * Encodes a component key as a string of the form [flattenedComponentString#userId].
     */
    @Override
    public String toString() {
        return componentName.flattenToString() + "#" + user.toString().replaceAll("\\D+","");
    }
	public ShortcutKey(String packageName, UserHandle user, String id) {
        // Use the id as the class name.
        this(new ComponentName(packageName, id), user);
    }

    public String getId() {
        return componentName.getClassName();
    }

    public static ShortcutKey fromInfo(ShortcutInfoCompat shortcutInfo) {
        return new ShortcutKey(shortcutInfo.getPackage(), shortcutInfo.getUserHandle(),
							   shortcutInfo.getId());
    }

    public static ShortcutKey fromIntent(Intent intent, UserHandle user) {
        String shortcutId = intent.getStringExtra(
			ShortcutInfoCompat.EXTRA_SHORTCUT_ID);
        return new ShortcutKey(intent.getPackage(), user, shortcutId);
    }

    public static ShortcutKey fromItemInfo(ItemInfo info) {
        return fromIntent(info.getIntent(), info.user);
    }
}
